package miltos.diploma.toolkit;

import miltos.diploma.qualitymodel.Measure;
import miltos.diploma.qualitymodel.Property;
import miltos.diploma.qualitymodel.PropertySet;
import miltos.diploma.utility.FileUtility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class is responsible for analyzing a single C# project
 * against:
 * 	1. a certain ruleset (i.e. property) or
 *  2. a set of rulesets (i.e. properties)
 * by invoking the FxCopExe tool.
 */
public class FxcopAnalyzer implements Analyzer {
    private static final String TOOL_NAME = "FxCop";

    @Override
    public void analyze(String src, String dest) {
        throw new RuntimeException("FxCop needs a ruleset as parameter in order to analyze");
    }

    /**
     * This method is used in order to analyze a single project against a certain ruleset (property)
     * by calling the FxCop tool through the command line with the appropriate configuration.
     *
     * ATTENTION:
     *  - The appropriate fxcop_build.xml ant file should be placed inside the resources directory.
     *
     * @param src      : The path of the folder that contains the sources of the project.
     *                   The folder must contain at least one .dll or .exe item.
     * @param dest     : The path where the XML files with the results will be placed.
     * @param ruleset  : The  rules against which the project will be analyzed.
     * @param filename : The name of the XML file containing scan results.
     */
    public void analyze(String src, String dest, String ruleset, String filename) {

        ProcessBuilder builder;
        String destFile = dest + "/" + filename;

        Set<String> assemblyDirs = FileUtility.findAssemblyDirectories(src, ".exe", ".dll");
        StringBuilder sb = new StringBuilder("\"");
        assemblyDirs.forEach(dir -> sb.append("/f:").append(dir).append(" "));
        sb.append("\"");

        // Attach FxCopExe option flags
        String srcExt = sb.toString();
        String destExt = "/out:" + destFile;
        String rulesetExt = "/r:" + ruleset;


        if(System.getProperty("os.name").contains("Windows")){
            String rootDirectory = System.getProperty("user.dir");
            builder = new ProcessBuilder(
     "cmd.exe",
                "/c",
                "ant -Dbasedir=" + rootDirectory +
                " -f src/main/resources/fxcop_build.xml" +
                " -Dsrc.dir=" + srcExt +
                " -Ddest.dir=" + destExt +
                " -Druleset.file=" + rulesetExt);
        } else {
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

        Set<Path> findings = new HashSet<>();
        try {
            Files.list(Paths.get(dest))
                .filter(f -> f.toString().endsWith(".xml"))
                .forEach(findings::add);
        } catch (IOException e) { e.printStackTrace(); }

        if (findings.isEmpty()) throw new RuntimeException("No findings XML files were generated by FxCop analysis");
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
        Property p;

        //For each property found in the PropertySet do...
        while(iterator.hasNext()){

            //Get the current property
            p = iterator.next();

            //Check if it is an FxCop Property
            //TODO: Check this outside this function
            if(p.getMeasure().getTool().equals(FxcopAnalyzer.TOOL_NAME) && p.getMeasure().getType() == Measure.FINDING) {
                //Analyze the project against this property
                analyze(src, dest, p.getMeasure().getRulesetPath(), p.getName()+".xml");
            }
        }
    }
}
