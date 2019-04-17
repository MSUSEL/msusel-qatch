package miltos.diploma.unittests;

import miltos.diploma.toolkit.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class LOCMetricsResultsImporterTest {
    /**
     * This test should successfully find the LOCMetrics .CSV file, parse the found metrics in to a Metrics object,
     * add the Metric objects to a MetricSet, and name the MetricSet.
     */
    @Test
    public void testParseIssues() throws IOException {
        String resultsPath = "src/test/resources/ScannerResults/LocMetricsResults.csv";
        LOCMetricsResultsImporter ri = new LOCMetricsResultsImporter();

        MetricSet ms = ri.parse(resultsPath);
        Metrics firstMetric = ms.getMetricSet().get(0);

        Assert.assertEquals(firstMetric.getName(), "Total");
        Assert.assertEquals(firstMetric.getLoc(), 50);
    }
}
