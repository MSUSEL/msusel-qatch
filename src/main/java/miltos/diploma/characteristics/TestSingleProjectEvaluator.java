package miltos.diploma.characteristics;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import miltos.diploma.BenchmarkAnalyzer;
import miltos.diploma.CKJMAggregator;
import miltos.diploma.CKJMAnalyzer;
import miltos.diploma.CKJMResultsImporter;
import miltos.diploma.EvaluationResultsExporter;
import miltos.diploma.PMDAggregator;
import miltos.diploma.PMDAnalyzer;
import miltos.diploma.PMDResultsImporter;
import miltos.diploma.Project;
import miltos.diploma.ProjectEvaluator;
import miltos.diploma.Property;

/**
 * TODO: Bring the cloning processes together at the beginning of the class
 * TODO: Add a parallel analysis option...
 * TODO: Extract a jar
 * TODO: Document the code
 * @author Miltos
 *
 */
public class TestSingleProjectEvaluator {

    //User defined fields
    public static String projectPath;
    public static String qmPath;
    public static String resPath;
    public static boolean includeInspectRes = false;
    public static boolean staticAnalysis = false;
    public static boolean keepResults = false;


    public static void main(String[] args) throws Exception {

        System.out.println("******************************  Project Evaluator *******************************");
        System.out.println();

        //Extract necessary tools if not already extracted
        extractResources();
        //Get the configuration
        getConfig();
        //Receive the appropriate configuration from the user through terminal
        getUserInputs(args);

        /*
         * Step 0 : Load the desired Quality Model
         */
        System.out.println("**************** STEP 0: Quality Model Loader ************************");
        System.out.println("*");
        System.out.println("* Loading the desired Quality Model...");
        System.out.println("* Please wait...");
        System.out.println("*");

        //Instantiate a new QualityModel object
        QualityModel qualityModel = new QualityModel();
        //Instantiate the Quality Model importer
        QualityModelLoader qmImporter = new QualityModelLoader(qmPath);

        //Load the desired quality model
        qualityModel = qmImporter.importQualityModel();

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
        File projectDir = new File(projectPath);

        //Create a Project object to store the results of the static analysis and the evaluation of this project...
        Project project = new Project();

        //Set the absolute path and the name of the project
        project.setPath(projectPath);
        project.setName(projectDir.getName());

        System.out.println("* Project Name : " + project.getName());
        System.out.println("* Project Path : " + project.getPath());
        System.out.println("*");
        System.out.println("* Project successfully loaded..!");

        /*
         * Step 2: Analyze the desired project against the selected properties
         */

        if(staticAnalysis){

            //Check if the results directory exists and if not create it. Clear it's contents as well.
            checkCreateClearDirectory(BenchmarkAnalyzer.SINGLE_PROJ_RESULT_PATH);

            //Print some messages...
            System.out.println("\n**************** STEP 2: Project Analyzer ****************************");
            System.out.println("*");
            System.out.println("* Analyzing the desired project");
            System.out.println("* Please wait...");
            System.out.println("*");

            //Instantiate the available single project analyzers of the system ...
            PMDAnalyzer pmd = new PMDAnalyzer();
            CKJMAnalyzer ckjm = new CKJMAnalyzer();

            //Analyze the project against the desired properties of each tool supported by the system...
            pmd.analyze(projectPath, BenchmarkAnalyzer.SINGLE_PROJ_RESULT_PATH+"/"+project.getName(), qualityModel.getProperties());
            ckjm.analyze(projectPath, BenchmarkAnalyzer.SINGLE_PROJ_RESULT_PATH+"/"+project.getName(), qualityModel.getProperties());

            //Print some messages to the user
            System.out.println("* The analysis is finished");
            System.out.println("* You can find the results at : " + BenchmarkAnalyzer.SINGLE_PROJ_RESULT_PATH);
            System.out.println();
        }

        /*
         * Step 3: Import the results of the static analysis tools
         */

        System.out.println("\n**************** STEP 3: Results Importer ****************************");
        System.out.println("*");
        System.out.println("* Importing the results of the analysis...");
        System.out.println("* Please wait...");
        System.out.println("*");

        //Create a simple PMD and CKJM Result Importers
        PMDResultsImporter pmdImporter = new PMDResultsImporter();
        CKJMResultsImporter ckjmImporter = new CKJMResultsImporter();

        //Get the directory with the results of the analysis
        File resultsDir = new File(BenchmarkAnalyzer.SINGLE_PROJ_RESULT_PATH+"/"+project.getName());
        File[] results = resultsDir.listFiles();

        //For each result file found in the directory do...
        for(File resultFile : results){

            //Check if it is a ckjm result file
            if(!resultFile.getName().contains("ckjm")){

                //Parse the issues and add them to the IssueSet Vector of the Project object
                project.addIssueSet(pmdImporter.parseIssues(resultFile.getAbsolutePath()));

            }else{

                //Parse the metrics of the project and add them to the MetricSet field of the Project object
                project.setMetrics(ckjmImporter.parseMetrics(resultFile.getAbsolutePath()));
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

        //Create an empty PMDAggregator and CKJMAggregator
        PMDAggregator pmd = new PMDAggregator();
        CKJMAggregator ckjm = new CKJMAggregator();

        //Aggregate all the analysis results
        pmd.aggregate(project);
        ckjm.aggregate(project);

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
        if(!includeInspectRes){
            project.clearIssuesAndMetrics();
        }

        EvaluationResultsExporter.exportProjectToJson(project, new File(resPath + "/" + project.getName() + "_evalResults.json").getAbsolutePath());

        System.out.println("* Results successfully exported..!");
        System.out.println("* You can find the results at : " + new File(resPath).getAbsolutePath());

        /*
         * Step 9 : Export the results to the predefined path as well
         */

        checkCreateClearDirectory(EvaluationResultsExporter.SINGLE_PROJ_RESULT_PATH);

        //Export the results
        EvaluationResultsExporter.exportProjectToJson(project, new File(EvaluationResultsExporter.SINGLE_PROJ_RESULT_PATH + "/" + project.getName() + "_evalResults.json").getAbsolutePath());
        System.out.println("* You can find the results at : " + new File(EvaluationResultsExporter.SINGLE_PROJ_RESULT_PATH).getAbsolutePath() + " as well..!");
    }

    /**
     * A method that parses the configuration xml file in order to set up
     * some fixed user defined parameters.
     */
    public static void getConfig(){
        try {
            // Import the desired xml configuration file
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(new File(System.getProperty("user.dir") + "/config.xml").getAbsoluteFile());
            Element root = (Element) doc.getRootElement();

            // Create a list of all the its elements
            List<Element> elements = root.getChildren();

            //Iterate through the elements of the file and parse their values
            for(Element el : elements){
                if("keepResults".equalsIgnoreCase(el.getName())){
                    keepResults =Boolean.parseBoolean(el.getText());
                }
            }
        }  catch (JDOMException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * A method that checks the predefined directory structure, creates the
     * directory tree if it doesn't exists and clears it's contents for the
     * new analysis (optional).
     */

    public static void checkCreateClearDirectory(String path){

        File dir = new File(path);

        //Check if the directory exists
        if(!dir.isDirectory() || !dir.exists()){
            dir.mkdirs();
        }

        //Clear previous results
        if(!keepResults){
            try {
                FileUtils.cleanDirectory(dir);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * A method that implements the CMD User Interface of the script.
     * TODO: Add more checks - e.g. Check the validity of the xml file
     */
    private static void getUserInputs(String... args) {
        //Handle input from either console or input param config file
        ArrayList<String> configsLocation = new ArrayList<>();

        if (args.length > 0) {
            String configLocation = args[0];
            try (Stream<String> configLines = Files.lines(new File(configLocation).toPath())) {
                Iterator itr = configLines.iterator();
                while (itr.hasNext()) {
                    configsLocation.add(itr.next().toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else {
            //Create a Scanner object in order to read data from the command line
            Scanner console = new Scanner(System.in);

            System.out.println("\nPlease provide the path of the desired project that you would like to assess its quality : ");
            configsLocation.add(console.nextLine());

            System.out.println("\nPlease provide the path of the XML file that contains the desired Quality Model : ");
            configsLocation.add(console.nextLine());

            System.out.println("\nPlease provide the path where you would like the results of the analysis to be placed : ");
            configsLocation.add(console.nextLine());

            System.out.println("\nWould you like to include the inspection results as well? (yes/no): ");
            configsLocation.add(console.nextLine());

            System.out.println("\nWould you like to run a new static analysis ? (yes/no): ");
            System.out.println("(If no then the results of the previous analysis will be used for the project's evaluation) ");
            configsLocation.add(console.nextLine());
        }

        File dir = new File(configsLocation.get(0));
        boolean exists = false;
        while(!exists) {
            if(dir.exists() && dir.isDirectory()){

                DirectoryScanner scanner = new DirectoryScanner();
                scanner.setIncludes(new String[]{"**/*.java"});
                scanner.setBasedir(dir.getAbsolutePath());
                scanner.setCaseSensitive(false);
                scanner.scan();
                String[] javaFiles = scanner.getIncludedFiles();

                scanner.setIncludes(new String[]{"**/*.class"});
                scanner.setBasedir(dir.getAbsolutePath());
                scanner.setCaseSensitive(false);
                scanner.scan();
                String[] classFiles = scanner.getIncludedFiles();

                scanner.setIncludes(new String[]{"**/*.jar"});
                scanner.setBasedir(dir.getAbsolutePath());
                scanner.setCaseSensitive(false);
                scanner.scan();
                String[] jarFiles = scanner.getIncludedFiles();

                if(javaFiles.length == 0 && classFiles.length == 0 && jarFiles.length == 0){
                    System.out.println("There are no java, class, or jar files inside the desired directory!");
                }else{
                    exists = true;
                    projectPath = dir.getAbsolutePath();
                }
            }else{
                System.out.println("The desired directory doesn't exist..!");
            }
        }

        File qmXMLFile = new File(configsLocation.get(1));
        exists = false;
        while(!exists){
            if(!qmXMLFile.exists() || !qmXMLFile.isFile()){
                System.out.println("The desired file doesn't exist..!");
            }else if(!qmXMLFile.getName().contains(".xml")){
                System.out.println("The desired file is not an XML file..!");
            }else{
                qmPath = qmXMLFile.getAbsolutePath();
                exists = true;
            }
        }

        File resDirPath = new File(configsLocation.get(2));
        if(resDirPath.exists() && resDirPath.isDirectory()){
            resPath = resDirPath.getAbsolutePath();
        }else {
            throw new RuntimeException("The destination folder doesn't exist..!");
        }

        String inspectionAnswer = configsLocation.get(3);
        if("yes".equalsIgnoreCase(inspectionAnswer)){
            includeInspectRes = true;
        }else if("no".equalsIgnoreCase(inspectionAnswer)){
            includeInspectRes = false;
        }else{
            throw new RuntimeException("include inspection results input was not of form 'yes' or 'no'");
        }

        String newAnalysisAnswer = configsLocation.get(4);
        if("yes".equalsIgnoreCase(newAnalysisAnswer)){
            staticAnalysis = true;
        }else if("no".equalsIgnoreCase(newAnalysisAnswer)){
            staticAnalysis = false;
        }else{
            throw new RuntimeException("\nnew static analysis input was not of form 'yes' or 'no'");
        }

        //If the user doesn't want a new analysis check if there are results for the desired project
        if("no".equalsIgnoreCase(newAnalysisAnswer)){
            File resDir = new File(BenchmarkAnalyzer.SINGLE_PROJ_RESULT_PATH + "/" + dir.getName());
            if(!resDir.isDirectory() || !resDir.exists() ){
                throw new RuntimeException("\nThe aren't any previous results for this project..! ");
            }
        }
    }

    /**
     * Automatically extract necessary scripts and tools for analaysis run.
     * Works whether run as a JAR or within an IDE.
     */
    private static void extractResources() {
        //Set filepath for resources depending if run from jar or in IDE
        String buildLoc, configLoc, pmd_buildLoc, rulesetsLoc, toolsLoc;
        File rootDirectory = new File(FileSystems.getDefault().getPath(".").toAbsolutePath().toString());

        String protocol = TestSingleProjectEvaluator.class.getResource("").getProtocol();

        if (Objects.equals(protocol, "jar")) {
            try {
                extractResourcesToTempFolder(rootDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if (Objects.equals(protocol, "file")) {

            String resourcesLoc = "src/main/resources/";
            buildLoc = resourcesLoc + "build.xml";
            configLoc = resourcesLoc + "config.xml";
            pmd_buildLoc = resourcesLoc + "pmd_build.xml";
            rulesetsLoc = resourcesLoc + "Rulesets";
            toolsLoc = resourcesLoc + "tools";

            File buildXml = new File(buildLoc);
            File configXml = new File(configLoc);
            File pmd_buildXml = new File(pmd_buildLoc);
            File rulesetsFolder = new File(rulesetsLoc);
            File toolsFolder = new File(toolsLoc);

            try {
                FileUtils.copyFileToDirectory(buildXml, rootDirectory);
                FileUtils.copyFileToDirectory(configXml, rootDirectory);
                FileUtils.copyFileToDirectory(pmd_buildXml, rootDirectory);
                FileUtils.copyDirectoryToDirectory(rulesetsFolder, rootDirectory);
                FileUtils.copyDirectoryToDirectory(toolsFolder, rootDirectory);
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
    private static void extractResourcesToTempFolder(File root) throws IOException {
        File resources = new File(root, "resources");
        String rootPath = TestSingleProjectEvaluator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String destPath = root.getCanonicalPath().concat(File.separator);

        //If folder exist, delete it.
        if (Files.exists(resources.toPath())) {
            FileUtils.forceDelete(new File(root, "resources"));
            System.out.println("Deleted resources folder");
        }

        //Recursively build resources folder from JAR sibling to JAR file
        JarFile jarFile = new JarFile(rootPath);
        Enumeration<JarEntry> enums = jarFile.entries();
        while (enums.hasMoreElements()) {
            JarEntry entry = enums.nextElement();
            if (entry.getName().startsWith("resources")) {
                File toWrite = new File(destPath + entry.getName());
                if (entry.isDirectory()) {
                    toWrite.mkdirs();
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

        //Move necessary files to root directory
        FileUtils.copyFileToDirectory(new File(resources, "build.xml"), root);
        FileUtils.copyFileToDirectory(new File(resources, "config.xml"), root);
        FileUtils.copyFileToDirectory(new File(resources, "pmd_build.xml"), root);
        FileUtils.copyDirectoryToDirectory(new File(resources, "Rulesets"), root);
        FileUtils.copyDirectoryToDirectory(new File(resources, "tools"), root);
    }
}
