package miltos.diploma.toolkit;

import miltos.diploma.qualitymodel.PropertySet;

import java.io.IOException;

public interface Analyzer {

    void analyze(String src, String dest) throws InterruptedException, IOException;
    void analyze(String src, String dest, String ruleset, String filename) throws IOException, InterruptedException;
    void analyze(String src, String dest, PropertySet properties) throws InterruptedException, IOException;
}
