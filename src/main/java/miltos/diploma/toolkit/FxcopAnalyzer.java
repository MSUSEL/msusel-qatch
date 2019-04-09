package miltos.diploma.toolkit;

import miltos.diploma.qualitymodel.Measure;
import miltos.diploma.qualitymodel.Property;
import miltos.diploma.qualitymodel.PropertySet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * This class is responsible for analyzing a single C# project
 * against:
 *
 * 	1. a certain ruleset (i.e. property) or
 *  2. a set of rulesets (i.e. properties)
 * by invoking the FxCopExe tool.
 */
public class FxcopAnalyzer {
    public static final String TOOL_NAME = "FxCop";

    /**
     * This method is used in order to analyze a single project against a certain ruleset (property)
     * by calling the FxCop tool through the command line with the appropriate configuration.
     *
     * ATTENTION:
     *  - The appropriate build.xml ant file should be placed inside the base directory.
     */
    public void analyze(String src, String dest, String ruleset, String filename) {
        //Set the path delimiter based on the OS that is used
        ProcessBuilder builder;
        String output = dest + "/" + filename;

        if(System.getProperty("os.name").contains("Windows")){
            String rootDirectory = System.getProperty("user.dir");
            builder = new ProcessBuilder(
     "cmd.exe",
                "/c",
                "ant -Dbasedir=" + rootDirectory +
                    " -f src/main/resources/fxcop_build.xml" +
                    " -Dsrc.dir=" + src +
                    " -Ddest.dir=" + output +
                    " -Druleset.dir=" + ruleset);
        }
        else {
            throw new RuntimeException("FxCop C# analysis not supported on non-Windows machines. FxCopCmd.exe tool only supported on Windows.");
        }

        builder.redirectErrorStream(true);

        //Execute the command
        try{
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            //Print the messages to the console for debugging purposes
            do {
                line = r.readLine();
            } while (line != null);
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method is responsible for analyzing a single project against a set of
     * properties (i.e. FxCop rulesets) by using the FxCop Tool.
     *
     * @param src        : The path of the folder that contains the sources of the project.
     * @param dest       : The path where the XML files with the results will be placed.
     * @param properties : The set of properties against which the project will be analyzed.
     *
     * Typically this method does the following:
     *
     * 		1. Iterates through the PropertySet
     * 		2. For each Property object the method calls the analyze() method in order to
     * 	       analyze the project against this single property.
     */
    public void analyze(String src, String dest, PropertySet properties) {
        //Create an Iterator in order to iterate through the properties of the desired PropertySet object
        Iterator<Property> iterator = properties.iterator();
        Property p = null;

        //For each property found in the PropertySet do...
        while(iterator.hasNext()){

            //Get the current property
            p = iterator.next();

            //Check if it is an FxCop Property
            //TODO: Check this outside this function
            if(p.getMeasure().getTool().equals(FxcopAnalyzer.TOOL_NAME) && p.getMeasure().getType() == Measure.FINDING) {
                //Analyze the project against this property
                analyze(src, dest, p.getMeasure().getRulesetPath(), p.getName());
            }
        }
    }
}
