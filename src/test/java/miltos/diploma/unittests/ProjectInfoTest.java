package miltos.diploma.unittests;

import miltos.diploma.utility.ProjectInfo;
import miltos.diploma.utility.ProjectLanguage;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public class ProjectInfoTest {

    @Rule
    public TemporaryFolder tempRoot = new TemporaryFolder(new File(System.getProperty("user.dir")));

    @Test
    public void testProjectLanguageJava() throws IOException {
        File root = tempRoot.newFolder();

        File javaDir = new File(root, "java");
        File java01 = new File(javaDir, "java01.java");
        File java02 = new File(javaDir, "java02.java");
        boolean jdSuccess = javaDir.mkdirs();
        boolean j01Success = java01.createNewFile();
        boolean j02Success = java02.createNewFile();

        File csharpDir = new File(root, "csharp");
        File cs01 = new File(csharpDir, "cs01.cs");
        boolean csdSuccess = csharpDir.mkdirs();
        boolean cs01Success = cs01.createNewFile();

        Assert.assertTrue(jdSuccess && j01Success && j02Success && csdSuccess && cs01Success);
        Assert.assertEquals(
                ProjectInfo.getProjectLanguage(root.toString()),
                ProjectLanguage.Java
        );
    }

    @Test
    public void testProjectLanguageCSharp() throws IOException {
        File root = tempRoot.newFolder();

        File javaDir = new File(root, "java");
        File java01 = new File(javaDir, "java01.java");
        boolean jdSuccess = javaDir.mkdirs();
        boolean j01Success = java01.createNewFile();

        File csharpDir = new File(root, "csharp");
        File cs01 = new File(csharpDir, "cs01.cs");
        File cs02 = new File(csharpDir, "cs02.cs");
        boolean csdSuccess = csharpDir.mkdirs();
        boolean cs01Success = cs01.createNewFile();
        boolean cs02Success = cs02.createNewFile();

        Assert.assertTrue(jdSuccess && j01Success && csdSuccess && cs01Success && cs02Success);
        Assert.assertEquals(
                ProjectInfo.getProjectLanguage(root.toString()),
                ProjectLanguage.CSharp
        );
    }
}
