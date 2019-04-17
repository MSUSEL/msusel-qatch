package miltos.diploma.toolkit;

import java.io.IOException;

public interface MetricsResultsImporter {
    MetricSet parse(String path) throws IOException;
}
