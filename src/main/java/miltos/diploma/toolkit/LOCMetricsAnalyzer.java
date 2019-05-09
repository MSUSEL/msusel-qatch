package miltos.diploma.toolkit;

import miltos.diploma.qualitymodel.PropertySet;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class gathers metrics on a single project by invoking the
 * LOCMetrics.exe tool.
 *
 * Due to being only runnable on Windows, these methods should only
 * be used on C# projects
 */
public class LOCMetricsAnalyzer implements Analyzer {

    public static final String TOOL_NAME = "LOCMetrics";
    public static final String RESULT_FILE_NAME = "LocMetricsFolders.csv";

    @Override
    public void analyze(String src, String dest) {

        // temp directory needed for clean handling or extra files produced by tool
        Path tempDest;
        ProcessBuilder builder;
        String rootDirectory = System.getProperty("user.dir");

        try {
            tempDest = Files.createTempDirectory(Paths.get(rootDirectory), "LOCMetrics");
            tempDest.toFile().deleteOnExit();

            if(System.getProperty("os.name").contains("Windows")){

                builder = new ProcessBuilder(
                    "cmd.exe",
                    "/c",
                    "ant -Dbasedir=" + rootDirectory +
                    " -f resources/Ant/locmetrics_build.xml" +
                    " -Dsrc.dir=" + src +
                    " -Ddest.dir=" + tempDest.toString()
                );
            }
            else throw new RuntimeException("LOCMetrics tool only supported on Windows operating systems.");

            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

            //Print the console output for debugging purposes
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) { break; }
            }

            // move desired results file out of temporary folder
            File results = new File(tempDest.toFile(), LOCMetricsAnalyzer.RESULT_FILE_NAME);
            FileUtils.copyFileToDirectory(results, new File(dest));
            FileUtils.cleanDirectory(tempDest.toFile());

            if (!new File(new File(dest), results.getName()).isFile()) {
                throw new RuntimeException("No LOCMetrice results found in " + dest);
            }

        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void analyze(String src, String dest, String ruleset, String filename) {
        throw new RuntimeException("Ruleset analyze not supported in CKJM");
    }

    @Override
    public void analyze(String src, String dest, PropertySet properties) {
        // temp solution: just run LOCMetrics in order to get LOC metric
        analyze(src, dest);
    }
}
