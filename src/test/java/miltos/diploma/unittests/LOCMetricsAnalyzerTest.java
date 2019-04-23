package miltos.diploma.unittests;

import miltos.diploma.toolkit.LOCMetricsAnalyzer;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class LOCMetricsAnalyzerTest {

    // LOCMetrics analysis needs a compiled CSharp project located at 'src' in order to work
    private final String src = "../sample-analysis-projects/csharp/SimpleCSharp";
    private final String dest = "src/test/output";

    /**
     * Ensures the LOCMetrics tool successfully runs on a small C# project
     * and generates an XML results file in the expected directory with the expected analysis
     * results.
     */
    @Test
    public void testAnalyzeSubroutine() throws IOException {
        clean();

        LOCMetricsAnalyzer analyzer = new LOCMetricsAnalyzer();
        analyzer.analyze(src, dest);

        File results = new File(this.dest + File.separator + LOCMetricsAnalyzer.RESULT_FILE_NAME);

        // XML file exists in expected location with correct name
        Assert.assertTrue(results.exists());
        Assert.assertTrue(results.isFile());
        Assert.assertEquals("LocMetricsFolders.csv", results.getName());

        // XML file has approximate expected number of bytes. A better way to test
        // this would be to parse the XML output for expected entries, but
        // that approach adds substantial run time to the unit test
        Assert.assertEquals(553, results.length(), 150);
    }

    private void clean() throws IOException {
        File output = new File(this.dest);
        if (output.exists()) {
            FileUtils.cleanDirectory(output);
        }
        else output.mkdirs();
    }
}
