package miltos.diploma;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Driver class for single project evaluation
 */
public class SingleProjectEvaluator {

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
    public static void main(String[] args) {
        try {
            String projectLoc = args[0];
            String resultsLoc = args[1];
            setConfig(projectLoc, resultsLoc);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Application was not provided project location and results location command line arguments");
        }
    }

    /**
     * Update config.properties with desired user input
     */
    private static void setConfig(String projectLoc, String resultsLocation) {
        try {
//            String propertiesLocation = "src/main/resources/config.properties";
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
