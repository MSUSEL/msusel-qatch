package miltos.diploma.unittests;

import miltos.diploma.qualitymodel.Measure;
import miltos.diploma.qualitymodel.Property;
import miltos.diploma.qualitymodel.PropertySet;
import miltos.diploma.toolkit.FxcopAnalyzer;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class FxcopAnalyzerTest {

     // FxCop analysis needs a compiled CSharp project located at 'src' in order to work
    private final String src = "../sample-analysis-projects/csharp/SimpleCSharp";
    private final String dest = "src/test/output";

    @Test
    public void testAnalyze() throws IOException {
        clean();

        Measure measure01 = new Measure(
                1,
                "metricName01",
                "src/main/resources/tools/FxCop/Rules/DesignRules.dll",
                "FxCop");
        Measure measure02 = new Measure(
                1,
                "metricName02",
                "src/main/resources/tools/FxCop/Rules/DesignRules.dll",
                "FxCop");
        Property property01 = new Property("propertyName01", measure01);
        Property property02 = new Property("propertyName02", measure02);

        PropertySet ps = new PropertySet();
        ps.addProperty(property01);
        ps.addProperty(property02);

        FxcopAnalyzer analyzer = new FxcopAnalyzer();
        analyzer.analyze(this.src, this.dest, ps);

        File result01 = new File(dest + "/" + property01.getName() + ".xml");
        File result02 = new File(dest + "/" + property02.getName() + ".xml");

        // XML file exists in expected location with correct name
        Assert.assertTrue(result01.exists());
        Assert.assertTrue(result02.exists());
        Assert.assertTrue(result01.isFile());
        Assert.assertTrue(result02.isFile());
        Assert.assertEquals("propertyName01.xml", result01.getName());
        Assert.assertEquals("propertyName02.xml", result02.getName());

        // XML file has expected number of bytes
        Assert.assertEquals(3464, result01.length(), 500);
        Assert.assertEquals(result01.length(), result02.length(), 500);
    }

    /**
     * This test should cause the FxCop tool to successfully run on a small C# project
     * and generate an XML results file in the expected directory with the expected analysis
     * results.
     */
    @Test
    public void testAnalyzeSubroutine() throws IOException {
        clean();

        String ruleset = "src/main/resources/Rulesets/CSharpTestModel/DesignRules.dll",
               filename = "fxcopresults.xml";

        FxcopAnalyzer analyzer = new FxcopAnalyzer();
        analyzer.analyze(this.src, this.dest, ruleset, filename);

        File results = new File(this.dest + File.separator + filename);

        // XML file exists in expected location with correct name
        Assert.assertTrue(results.exists());
        Assert.assertTrue(results.isFile());
        Assert.assertEquals("fxcopresults.xml", results.getName());

        // XML file has approximate expected number of bytes. A better way to test
        // this would be to parse the XML output for expected entries, but
        // that approach adds substantial run time to the unit test
        Assert.assertEquals(3464, results.length(), 200);

    }

    private void clean() throws IOException {
        File outputFolder = new File(this.dest);

        if (outputFolder.exists()) {
            File output = new File(this.dest);
            FileUtils.cleanDirectory(output);
        }
        outputFolder.mkdirs();
    }
}
