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
import java.util.concurrent.TimeUnit;

/**
 * This class gathers metrics on a single project by invoking the
 * LOCMetrics.exe tool.
 *
 * Due to being only runnable on Windows, these methods should only
 * be used on C# projects
 */
public class LOCMetricsAnalyzer implements Analyzer {

    public static final String RESULT_FILE_NAME = "LocMetricsFolders.csv";

    @Override
    public void analyze(String src, String dest) throws InterruptedException {

        ProcessBuilder pb;
        try {
            if(System.getProperty("os.name").contains("Windows")){
                String sep = File.separator;
                pb = new ProcessBuilder(
                    "cmd.exe", "/c",
                    "tools"+sep+"LocMetrics.exe", "-i", src, "-o", dest
                );
            }
            else throw new RuntimeException("LOCMetrics tool only supported on Windows operating systems.");

            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor();

        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void analyze(String src, String dest, String ruleset, String filename) {
        throw new RuntimeException("Ruleset analyze not supported in CKJM");
    }

    @Override
    public void analyze(String src, String dest, PropertySet properties) throws InterruptedException {
        // temp solution: just run LOCMetrics in order to get LOC metric
        analyze(src, dest);
    }
}
