package miltos.diploma.toolkit;

import miltos.diploma.qualitymodel.PropertySet;

public class CSharpMetricsAnalyzer implements Analyzer {
    @Override
    public void analyze(String src, String dest) {
        System.out.println("(TODO) temporarily skipping C# metrics analyzer step");
    }

    @Override
    public void analyze(String src, String dest, String ruleset, String filename) {
        throw new RuntimeException("Ruleset analyze not supported in CSharp Metrics");
    }

    @Override
    public void analyze(String src, String dest, PropertySet properties) {
        System.out.println("(TODO) temporarily skipping C# metrics analyzer step");
    }
}
