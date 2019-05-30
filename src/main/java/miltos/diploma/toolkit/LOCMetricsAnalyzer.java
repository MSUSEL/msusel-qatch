package miltos.diploma.toolkit;

import miltos.diploma.qualitymodel.PropertySet;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    public void analyze(String src, String dest) throws InterruptedException, IOException {

        ProcessBuilder pb;
        if(System.getProperty("os.name").contains("Windows")){
            String sep = File.separator;
            pb = new ProcessBuilder(
                "cmd.exe", "/c",
                "QA"+sep+"resources"+sep+"tools"+sep+"LocMetrics.exe", "-i", src, "-o", dest
            );
        }
        else throw new RuntimeException("LOCMetrics tool only supported on Windows operating systems.");

        pb.redirectErrorStream(true);
        Process p = pb.start();
        p.waitFor();

        cleanAllButOne(new File(dest), RESULT_FILE_NAME);
    }

    @Override
    public void analyze(String src, String dest, String ruleset, String filename) {
        throw new RuntimeException("Ruleset analyze not supported in CKJM");
    }

    @Override
    public void analyze(String src, String dest, PropertySet properties) throws InterruptedException, IOException {
        // temp solution: just run LOCMetrics in order to get LOC metric
        analyze(src, dest);
    }

    private void cleanAllButOne(File directory, String toKeep) {
        for (File f : Objects.requireNonNull(directory.listFiles())) {
            if (!f.getName().equals(toKeep)) {
                f.delete();
            }
        }

    }
}
