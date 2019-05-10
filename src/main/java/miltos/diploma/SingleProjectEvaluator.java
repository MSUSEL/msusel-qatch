package miltos.diploma;

import miltos.diploma.calibration.BenchmarkAnalyzer;
import miltos.diploma.evaluation.EvaluationResultsExporter;
import miltos.diploma.evaluation.Project;
import miltos.diploma.evaluation.ProjectCharacteristicsEvaluator;
import miltos.diploma.evaluation.ProjectEvaluator;
import miltos.diploma.qualitymodel.*;
import miltos.diploma.toolkit.*;
import miltos.diploma.utility.ProjectInfo;
import miltos.diploma.utility.ProjectLanguage;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Driver class for single project evaluation
 */
public class SingleProjectEvaluator {

    private static File root = new File(FileSystems.getDefault().getPath(".").toAbsolutePath().toString());

    private static final boolean INSPECTION_RESULTS = false;
    private static final boolean RERUN = true;
    private static final String QM_LOC = "resources/Models/csharp/qualityModel_csharp.xml";

    private static Properties properties = new Properties();

    /**
     * @param args configuration array in following order:
     *             0: path to project root folder
     *             1: path to folder to place results
     *    These arg paths can be relative or full path
     */
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, CloneNotSupportedException, InterruptedException, URISyntaxException {

        clean("resources", "config.properties");

        System.out.println("******************************  Project Evaluator *******************************");
        System.out.println();

        // Initialize configurations
        try {
            String projectLoc = args[0];
            String resultsLoc = args[1];
            setConfig(projectLoc, resultsLoc);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Application was not provided project location and results location command line arguments");
        }
        properties.load(new FileInputStream("config.properties"));
        ProjectLanguage projectLanguage = ProjectInfo.getProjectLanguage(properties.getProperty("project.location"));

        // temp
        if (projectLanguage == ProjectLanguage.Java) throw new RuntimeException("[temp] temporarily only supporting csharp");

        extractResources();


        /*
         * Step 0 : Load the desired Quality Model
         */
        System.out.println("**************** STEP 0: Quality Model Loader ************************");
        System.out.println("*");
        System.out.println("* Loading the desired Quality Model...");
        System.out.println("* Please wait...");
        System.out.println("*");

        //Instantiate the Quality Model importer
        QualityModelLoader qmImporter = new QualityModelLoader(properties.getProperty("qm.location"));

        //Load the desired quality model
        QualityModel qualityModel = qmImporter.importQualityModel();

        System.out.println("* Quality Model successfully loaded..!");


        /*
         * Step 1: Create the Project object that simulates the desired project
         */
        System.out.println("\n**************** STEP 1: Project Loader ******************************");
        System.out.println("*");
        System.out.println("* Loading the desired project...");
        System.out.println("* Please wait...");
        System.out.println("*");

        //Get the directory of the project
        File projectDir = new File(properties.getProperty("project.location"));

        //Create a Project object to store the results of the static analysis and the evaluation of this project...
        Project project = new Project();

        //Set the absolute path and the name of the project
        project.setPath(properties.getProperty("project.location"));
        project.setName(projectDir.getName());

        System.out.println("* Project Name : " + project.getName());
        System.out.println("* Project Path : " + project.getPath());
        System.out.println("*");
        System.out.println("* Project successfully loaded..!");


        /*
         * Step 2: Analyze the desired project against the selected properties
         */
        if(Boolean.parseBoolean(properties.getProperty("analysis.rerun"))) {

            //Check if the results directory exists and if not create it. Clear it's contents as well.
            checkCreateClearDirectory(BenchmarkAnalyzer.SINGLE_PROJ_RESULT_PATH + File.separator + project.getName());

            //Print some messages...
            System.out.println("\n**************** STEP 2: Project Analyzer ****************************");
            System.out.println("*");
            System.out.println("* Analyzing the desired project");
            System.out.println("* Please wait...");
            System.out.println("*");

            //Instantiate the available single project analyzers of the system ...
            //(TODO): Refactor into Builder or Template design pattern and move into framework classes
            Analyzer metricsAnalyzer;
            Analyzer findingsAnalyzer;

            if (projectLanguage == ProjectLanguage.Java) {
                metricsAnalyzer = new CKJMAnalyzer();
                findingsAnalyzer = new PMDAnalyzer();
            }
            else if (projectLanguage == ProjectLanguage.CSharp) {
                metricsAnalyzer = new LOCMetricsAnalyzer();
                findingsAnalyzer = new FxcopAnalyzer();
            }
            else throw new RuntimeException("projectLanguage did not match to a support language enumeration");

            metricsAnalyzer.analyze(
                    properties.getProperty("project.location"),
                    BenchmarkAnalyzer.SINGLE_PROJ_RESULT_PATH + File.separator + project.getName(),
                    qualityModel.getProperties()
            );
            findingsAnalyzer.analyze(
                    properties.getProperty("project.location"),
                    BenchmarkAnalyzer.SINGLE_PROJ_RESULT_PATH + File.separator + project.getName(),
                    qualityModel.getProperties()
            );

            //Print some messages to the user
            System.out.println("* The analysis is finished");
            System.out.println("* You can find the results at : " + BenchmarkAnalyzer.SINGLE_PROJ_RESULT_PATH);
            System.out.println();
        }

        System.out.println("\n**************** STEP 3: Results Importer ****************************");
        System.out.println("*");
        System.out.println("* Importing the results of the analysis...");
        System.out.println("* Please wait...");
        System.out.println("*");

        //(TODO): Refactor into Builder or Template design pattern and move into framework classes
        //Create a simple PMD and CKJM Result Importers
        MetricsResultsImporter metricsImporter;
        FindingsResultsImporter findingsImporter;

        if (projectLanguage == ProjectLanguage.Java) {
            metricsImporter = new CKJMResultsImporter();
            findingsImporter = new PMDResultsImporter();
        }
        else if (projectLanguage == ProjectLanguage.CSharp) {
            metricsImporter = new LOCMetricsResultsImporter();
            findingsImporter = new FxcopResultsImporter();
        }
        else throw new RuntimeException("projectLanguage did not match to a support language enumeration");

        //Get the directory with the results of the analysis
        File resultsDir = new File(BenchmarkAnalyzer.SINGLE_PROJ_RESULT_PATH + File.separator + project.getName());
        File[] results = resultsDir.listFiles();

        //For each result file found in the directory do...
        if (results == null) throw new RuntimeException("Scanner results directory " + resultsDir.toString() + " has no files");
        for(File resultFile : results){

            //(TODO): Refactor into Builder or Template design pattern and move into framework classes
            //Check if it is a ckjm result file
            if(!resultFile.getName().contains("ckjm") && !resultFile.getName().contains("Loc")){
                //Parse the issues and add them to the IssueSet Vector of the Project object
                project.addIssueSet(findingsImporter.parse(resultFile.getAbsolutePath()));
            }else{
                //Parse the metrics of the project and add them to the MetricSet field of the Project object
                project.setMetrics(metricsImporter.parse(resultFile.getAbsolutePath()));
            }
        }

        // Print some informative messages to the console
        System.out.println("*");
        System.out.println("* The results of the static analysis are successfully imported ");


        /*
         * Step 4 : Aggregate the static analysis results of the desired project
         */
        System.out.println("\n**************** STEP 4: Aggregation Process *************************");

        //Print some messages
        System.out.println("*");
        System.out.println("* Aggregating the results of the project...");
        System.out.println("* I.e. Calculating the normalized values of its properties...");
        System.out.println("* Please wait...");
        System.out.println("*");

        //Clone the properties of the quality model to the properties of the certain project
        for(int i = 0; i < qualityModel.getProperties().size(); i++){
            //Clone the property and add it to the PropertySet of the current project
            Property p = (Property) qualityModel.getProperties().get(i).clone();
            project.addProperty(p);
        }

        //(TODO): Refactor into design pattern
        Aggregator metricsAggregator;
        Aggregator findingsAggregator;
        if (projectLanguage == ProjectLanguage.Java) {
            metricsAggregator = new CKJMAggregator();
            findingsAggregator = new PMDAggregator();
        }
        else if (projectLanguage == ProjectLanguage.CSharp) {
            metricsAggregator = new LOCMetricsAggregator();
            findingsAggregator = new FxcopAggregator();
        }
        else throw new RuntimeException("projectLanguage did not match to a support language enumeration");

        //Aggregate all the analysis results
        metricsAggregator.aggregate(project);
        findingsAggregator.aggregate(project);

        //Normalize their values
        for(int i = 0; i < project.getProperties().size(); i++){
            Property property =  project.getProperties().get(i);
            property.getMeasure().calculateNormValue();
        }

        System.out.println("*");
        System.out.println("* Aggregation process finished..!");


        /*
         * STEP 5 : Evaluate all the benchmark projects against their thresholds.
         */

        System.out.println("\n**************** STEP 5: Properties Evaluation ***********************");
        System.out.println("*");
        System.out.println("* Evaluating the project's properties against the calculated thresholds...");
        System.out.println("* This will take a while...");
        System.out.println("*");

        //Create a single project property evaluator
        ProjectEvaluator evaluator = new ProjectEvaluator();

        //Evaluate all its properties
        evaluator.evaluateProjectProperties(project);

        System.out.println("*");
        System.out.println("* The project's properties successfully evaluated..!");

        /*
         * Step 6 : Evaluate the project's characteristics
         */

        System.out.println("\n**************** STEP 6: Characteristics Evaluation ******************");
        System.out.println("*");
        System.out.println("* Evaluating the project's characteristics based on the eval values of its properties...");
        System.out.println("* This will take a while...");
        System.out.println("*");

        //Clone the quality model characteristics inside the project
        //For each quality model's characteristic do...
        for(int i = 0; i < qualityModel.getCharacteristics().size(); i++){
            //Clone the characteristic and add it to the CharacteristicSet of the current project
            Characteristic c = (Characteristic) qualityModel.getCharacteristics().get(i).clone();
            project.getCharacteristics().addCharacteristic(c);
        }

        //Create a single project property evaluator
        ProjectCharacteristicsEvaluator charEvaluator = new ProjectCharacteristicsEvaluator();

        //Evaluate the project's characteristics
        charEvaluator.evaluateProjectCharacteristics(project);

        System.out.println("*");
        System.out.println("* The project's characteristics successfully evaluated..!");

        /*
         * Step 7 : Calculate the TQI of the project
         */

        System.out.println("\n**************** STEP 7: TQI Calculation *****************************");
        System.out.println("*");
        System.out.println("* Calgculating the TQI of the project ...");
        System.out.println("* This will take a while...");
        System.out.println("*");

        //Copy the TQI object of the QM to the tqi field of this project
        project.setTqi((Tqi)qualityModel.getTqi().clone());

        //Calculate the project's TQI
        project.calculateTQI();

        System.out.println("*");
        System.out.println("* The TQI of the project successfully evaluated..!");

        /*
         * Step 8 : Export the project's data and properties in a json file
         */
        System.out.println("\n**************** STEP 8: Exporting Evaluation Results ****************");
        System.out.println("*");
        System.out.println("* Exporting the results of the project evaluation...");
        System.out.println("* This will take a while...");
        System.out.println("*");

        //Clear Issues and metrics for more lightweight solution
        //TODO: Remove this ... For debugging purposes only
        if(!Boolean.parseBoolean(properties.getProperty("output.inspectionresults"))){
            project.clearIssuesAndMetrics();
        }

        EvaluationResultsExporter.exportProjectToJson(
                project,
                new File(properties.getProperty("results.location") + File.separator + project.getName() + "_evalResults.json")
                        .getAbsolutePath()
        );

        System.out.println("* Results successfully exported..!");
        System.out.println("* You can find the results at : " + new File(properties.getProperty("results.location")).getAbsolutePath());
    }


    /**
     * A method that checks the predefined directory structure, creates the
     * directory tree if it doesn't exists and clears it's contents for the
     * new analysis (optional).
     */
    private static void checkCreateClearDirectory(String path){

        File dir = new File(path);

        //Check if the directory exists
        if(!dir.isDirectory() || !dir.exists()) dir.mkdirs();

        //Clear previous results
        try {
            FileUtils.cleanDirectory(dir);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void clean(String... filePaths) throws IOException {
        for (String f : filePaths) {
            File toDelete = new File(f);
            if (Files.exists(toDelete.toPath())) {
                FileUtils.forceDelete(toDelete);
                System.out.println("Deleted File " + f);
            }
        }
    }

    private static void extractResources() throws URISyntaxException {

        String protocol = SingleProjectEvaluator.class.getResource("").getProtocol();

        if (Objects.equals(protocol, "jar")) {
            try {
                extractResourcesToTempFolder(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if (Objects.equals(protocol, "file")) {

            String resourcesLoc = "src/main/resources";
            File resourcesFolder = new File(resourcesLoc);
            try {
                FileUtils.copyDirectoryToDirectory(resourcesFolder , root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else {
            throw new RuntimeException("Unable to determine if project is running from IDE or JAR");
        }
    }

    /**
     * Code from https://stackoverflow.com/questions/1529611/how-to-write-a-java-program-which-can-extract-a-jar-file-and-store-its-data-in-s/1529707#1529707
     * by user Jamesst20
     *
     * Used when running program as a JAR.
     *
     * Takes resources in the resources folder within the JAR and copies them to a
     * resources folder in the same directory as the JAR. Also moves the ant build.xml
     * file to root directory.
     */
    private static void extractResourcesToTempFolder(File root) throws IOException, URISyntaxException {

        String rootPath = new File(SingleProjectEvaluator.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath())
            .getName();
        String destPath = root.getCanonicalPath().concat(File.separator);

        //Recursively build resources folder from JAR sibling to JAR file
        JarFile jarFile = new JarFile(rootPath);
        Enumeration<JarEntry> enums = jarFile.entries();
        while (enums.hasMoreElements()) {
            JarEntry entry = enums.nextElement();
            if (entry.getName().startsWith("resources")) {
                File toWrite = new File(destPath + entry.getName());
                if (entry.isDirectory()) {
                    boolean mkdirsResult = toWrite.mkdirs();
                    continue;
                }
                InputStream in = new BufferedInputStream(jarFile.getInputStream(entry));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(toWrite));
                byte[] buffer = new byte[2048];
                for (;;) {
                    int nBytes = in.read(buffer);
                    if (nBytes <= 0) {
                        break;
                    }
                    out.write(buffer, 0, nBytes);
                }
                out.flush();
                out.close();
                in.close();
            }
        }
    }

    /**
     * Update config.properties with desired user input
     */
    private static void setConfig(String projectLoc, String resultsLocation) {
        try {
            properties.load(SingleProjectEvaluator.class.getClassLoader().getResourceAsStream("resources/config.properties"));

            properties.setProperty("project.location", projectLoc);
            properties.setProperty("qm.location", QM_LOC);
            properties.setProperty("analysis.rerun", Boolean.toString(RERUN));
            properties.setProperty("output.inspectionresults", Boolean.toString(INSPECTION_RESULTS));
            properties.setProperty("results.location", resultsLocation);

            properties.store(new FileOutputStream(new File(System.getProperty("user.dir") + File.separator + "config.properties")), null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
