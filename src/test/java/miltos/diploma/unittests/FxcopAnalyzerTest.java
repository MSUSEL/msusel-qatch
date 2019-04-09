package miltos.diploma.unittests;

import miltos.diploma.toolkit.FxcopAnalyzer;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class FxcopAnalyzerTest {

    /**
     * This test should cause the FxCop tool to successfully run on a small C# project
     * and generate an XML results file in the expected directory with the expected analysis
     * results.
     */
    @Test
    public void testAnalyze() throws IOException {
        clean();

        String src = "../sample-analysis-projects/csharp/SimpleCSharp/SimpleCSharp/bin/Debug",
               dest = "src/test/output",
               ruleset = "src/test/resources/tools/FxCop/Rules",
               filename = "fxcopresults.xml";

        FxcopAnalyzer analyzer = new FxcopAnalyzer();
        analyzer.analyze(src, dest, ruleset, filename);

        Assert.fail();
    }

    private void clean() throws IOException {
        File output = new File("src/test/output");
        FileUtils.cleanDirectory(output);
    }
}
