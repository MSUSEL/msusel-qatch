package miltos.diploma.unittests;

import miltos.diploma.evaluation.EvaluationResultsExporter;
import miltos.diploma.evaluation.Project;
import miltos.diploma.qualitymodel.*;
import miltos.diploma.toolkit.Issue;
import miltos.diploma.toolkit.IssueSet;
import miltos.diploma.toolkit.MetricSet;
import miltos.diploma.toolkit.Metrics;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

/**
 * Unit tests for EvaluationResultsExporterTest.
 *
 * NOTE: the test for exportProjecToMongoDB() is included in the integration tests due to a database and
 *  config file needed.
 */
public class EvaluationResultsExporterTest {

    private Issue issue = new Issue(
            "Rule_name","Rule_set_name","Package_name","description",
            "exterinal_info_url",3,444,450,12,20,"Class_path"
    );
    private Metrics metrics = new Metrics(123);
    private Measure measure = new Measure(1, "Measure_name");
    private Property property = new Property(
            "Property_name", "description", new double[]{0.11, 0.22, 0.33},
            0.95, null, false, measure
    );
    private Characteristic characteristic = new Characteristic(
            "Characteristic_name", "standard",
            "description", new Vector<>(Arrays.asList(0.12, 0.23, 0.34))
    );
    private IssueSet issueSet = new IssueSet("Issue_set_property_name", new Vector<>(Arrays.asList(issue)));
    private Vector<IssueSet> issues = new Vector<>(Arrays.asList(issueSet));
    private MetricSet metricSet = new MetricSet(new Vector<>(Arrays.asList(metrics)));
    private PropertySet propertySet = new PropertySet(new Vector<>(Arrays.asList(property)));
    private CharacteristicSet characteristicSet = new CharacteristicSet(new Vector<>(Arrays.asList(characteristic)));
    private Tqi tqi = new Tqi(0.9321, new Vector<>(Arrays.asList(0.33, 0.33, 0.33)));
    Project project = new Project(
            "TestProject",
            null,
            issues,
            metricSet,
            propertySet,
            characteristicSet,
            tqi
    );

    @Test
    public void testExportProjectToJson() throws IOException {
        String outputDir = "src/test/output/";
        clean(outputDir);

        String path = outputDir + project.getName() + "_evalResults.json";
        EvaluationResultsExporter.exportProjectToJson(project, path);

        File results = new File(path);
        Assert.assertTrue(results.exists());
        Assert.assertTrue(results.isFile());
    }

    private void clean(String outputDir) throws IOException {
        File output = new File(outputDir);
        if (output.exists()) {
            FileUtils.cleanDirectory(output);
        }
        else output.mkdirs();
    }
}
