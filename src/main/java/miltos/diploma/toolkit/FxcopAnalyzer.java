package miltos.diploma.toolkit;

/**
 * This class is responsible for analyzing a single C# project
 * against:
 *
 * 	1. a certain ruleset (i.e. property) or
 *  2. a set of rulesets (i.e. properties)
 * by invoking the FxCopExe tool.
 */
public class FxcopAnalyzer {
    public static final String TOOL_NAME = "FxCop";

    /**
     * This method is used in order to analyze a single project against a certain ruleset (property)
     * by calling the PMD tool through the command line with the appropriate configuration.
     *
     * ATTENTION:
     *  - The appropriate build.xml ant file should be placed inside the base directory.
     */
    public void analyze(String src, String dest, String ruleset, String filename) {
        throw new RuntimeException("(TODO): finish writing method");
    }
}
