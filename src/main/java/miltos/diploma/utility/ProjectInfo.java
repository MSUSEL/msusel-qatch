package miltos.diploma.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Static utilty class for obtaining information about a project
 */
public final class ProjectInfo {
    private ProjectInfo() { }

    /**
     * Recursively searches a directory and counts the number of occurances of
     * language-specific files (.java, .cs, etc) in order to find the majority
     * language.
     *
     * @param   rootDirectory
     *          the root directory of a project
     * @return  a ProjectLanguage enumeration of the majority language represented
     *          within the rootDirectory
     */
    public static ProjectLanguage getProjectLanguage(String rootDirectory) {
        // Holds count of number of file extention findings of each language type
        Map<ProjectLanguage, Integer> languageCount = new HashMap<>();
        String javaExt = ".java";
        String csExt = ".cs";
        Path root = Paths.get(rootDirectory);

        languageCount.put(ProjectLanguage.Java, 0);
        languageCount.put(ProjectLanguage.CSharp, 0);

        try {
            Files
                .find(root, Integer.MAX_VALUE, (path, attr) -> path.toString().endsWith(javaExt))
                .forEach(path -> languageCount.put(ProjectLanguage.Java, languageCount.get(ProjectLanguage.Java) + 1)
            );
            Files
                .find(root, Integer.MAX_VALUE, (path, attr) -> path.toString().endsWith(csExt))
                .forEach(path -> languageCount.put(ProjectLanguage.CSharp, languageCount.get(ProjectLanguage.CSharp) + 1)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return languageCount.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
    }
}
