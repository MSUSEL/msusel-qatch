package miltos.diploma.unittests;

import miltos.diploma.utility.FileUtility;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FileUtilityTest {

    @Rule
    public TemporaryFolder tempRoot = new TemporaryFolder(new File(System.getProperty("user.dir")));

    /**
     * Create source files of varying extention types and directory depths and
     * ensure the method returns the set of the correct containing folders.
     *
     * Passes if findAssemblyDirectories(".exe", ".dll") returns dir01 and dir03
     * but not root and dir02.
     */
    @Test
    public void testFindAssemblyDirectories() throws IOException {
        File root = tempRoot.newFolder();

        File dir01 = new File(root, "dir01");
        File dir02 = new File(root, "dir02");
        File dir03 = new File(dir02, "dir03");
        boolean d01Succ = dir01.mkdirs();
        boolean d02Succ = dir02.mkdirs();
        boolean d03Succ = dir03.mkdirs();

        File exe01 = new File(dir01, "assembly01.exe");
        File exe02 = new File(dir03, "assembly02.dll");
        File notExe = new File(dir02, "notAssembly.java");
        boolean e01Succ = exe01.createNewFile();
        boolean e02Succ = exe02.createNewFile();
        boolean neSucc = notExe.createNewFile();

        Set<String> assemblyDirectories = FileUtility.findAssemblyDirectories(root.toString(), ".exe", ".dll");
        Set<String> expectedSet = new HashSet<>(Arrays.asList(
            root.toString() + File.separator + "dir01",
            root.toString() + File.separator + "dir02" +  File.separator + "dir03")
        );

        Assert.assertTrue(d01Succ && d02Succ && d03Succ && e01Succ && e02Succ && neSucc);
        Assert.assertTrue(assemblyDirectories.containsAll(expectedSet));
        Assert.assertFalse(assemblyDirectories.contains(root.toString() + File.separator + "dir02"));
    }
}
