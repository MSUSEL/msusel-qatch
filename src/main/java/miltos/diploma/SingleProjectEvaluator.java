package miltos.diploma;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Driver class for single project evaluation
 */
public class SingleProjectEvaluator {

    /**
     * @param args configuration array in following order:
     *             0: path to project root folder, can be relative or full path
     *             1: path to folder to place results, can re relative or full path
     */
    public static void main(String[] args) {

    }

    /**
     * Update config.properties with desired user input
     */
    private void setConfig(String projectLoc, String resultsLocation) {
        try {
//            String propertiesLocation = "src/main/resources/config.properties";
            Properties properties = new Properties();
            properties.load(SingleProjectEvaluator.class.getClassLoader().getResourceAsStream("config.properties"));

            properties.setProperty("project.location", projectLoc);
            properties.setProperty("qm.location", qmLoc);
            properties.setProperty("analysis.rerun", Boolean.toString(analysisRerun));
            properties.setProperty("output.inspectionresults", Boolean.toString(inspectionResults));
            properties.setProperty("results.location", resultsLocation);

            properties.store(new FileOutputStream(propertiesLocation), null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
