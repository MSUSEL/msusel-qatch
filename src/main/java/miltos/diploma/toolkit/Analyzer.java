package miltos.diploma.toolkit;

import miltos.diploma.qualitymodel.PropertySet;

public interface Analyzer {

    void analyze(String src, String dest);
    void analyze(String src, String dest, String ruleset, String filename);
    void analyze(String src, String dest, PropertySet properties);
}
