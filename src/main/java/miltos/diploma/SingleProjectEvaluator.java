package miltos.diploma;

import miltos.diploma.utility.ProjectInfo;
import miltos.diploma.utility.ProjectLanguage;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Driver class for single project evaluation
 */
public class SingleProjectEvaluator {

    private static File root = new File(FileSystems.getDefault().getPath(".").toAbsolutePath().toString());

    private static final boolean INSPECTION_RESULTS = false;
    private static final boolean RERUN = true;
    private static final String QM_LOC = "resources/Models/csharp/qualityModel_csharp.xml";

    private static Properties properties = new Properties();

    /**
     * @param args configuration array in following order:
     *             0: path to project root folder
     *             1: path to folder to place results
     *    These arg paths can be relative or full path
     */
    public static void main(String[] args) throws IOException {

        clean("resources", "config.properties");

        System.out.println("******************************  Project Evaluator *******************************");
        System.out.println();

        // Initialize configurations
        try {
            String projectLoc = args[0];
            String resultsLoc = args[1];
            setConfig(projectLoc, resultsLoc);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Application was not provided project location and results location command line arguments");
        }
        properties.load(new FileInputStream("config.properties"));
        ProjectLanguage projectLanguage = ProjectInfo.getProjectLanguage(properties.getProperty("project.location"));
        extractResources();
    }

    private static void clean(String... filePaths) throws IOException {
        for (String f : filePaths) {
            File toDelete = new File(root.toString(), f);
            if (Files.exists(toDelete.toPath())) {
                FileUtils.forceDelete(toDelete);
                System.out.println("Deleted File " + f);
            }
        }
    }

    private static void extractResources() {

        String protocol = SingleProjectEvaluator.class.getResource("").getProtocol();

        if (Objects.equals(protocol, "jar")) {
            try {
                extractResourcesToTempFolder(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if (Objects.equals(protocol, "file")) {

            String resourcesLoc = "src/main/resources/";
            File resourcesFolder = new File(resourcesLoc);
            try {
                FileUtils.copyDirectoryToDirectory(resourcesFolder, root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else {
            throw new RuntimeException("Unable to determine if project is running from IDE or JAR");
        }
    }

    /**
     * Code from https://stackoverflow.com/questions/1529611/how-to-write-a-java-program-which-can-extract-a-jar-file-and-store-its-data-in-s/1529707#1529707
     * by user Jamesst20
     *
     * Used when running program as a JAR.
     *
     * Takes resources in the resources folder within the JAR and copies them to a
     * resources folder in the same directory as the JAR. Also moves the ant build.xml
     * file to root directory.
     */
    private static void extractResourcesToTempFolder(File root) throws IOException {

        String rootPath = SingleProjectEvaluator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String destPath = root.getCanonicalPath().concat(File.separator);

        //Recursively build resources folder from JAR sibling to JAR file
        JarFile jarFile = new JarFile(rootPath);
        Enumeration<JarEntry> enums = jarFile.entries();
        while (enums.hasMoreElements()) {
            JarEntry entry = enums.nextElement();
            if (entry.getName().startsWith("resources")) {
                File toWrite = new File(destPath + entry.getName());
                if (entry.isDirectory()) {
                    boolean mkdirsResult = toWrite.mkdirs();
                    continue;
                }
                InputStream in = new BufferedInputStream(jarFile.getInputStream(entry));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(toWrite));
                byte[] buffer = new byte[2048];
                for (;;) {
                    int nBytes = in.read(buffer);
                    if (nBytes <= 0) {
                        break;
                    }
                    out.write(buffer, 0, nBytes);
                }
                out.flush();
                out.close();
                in.close();
            }
        }
    }

    /**
     * Update config.properties with desired user input
     */
    private static void setConfig(String projectLoc, String resultsLocation) {
        try {
            properties.load(SingleProjectEvaluator.class.getClassLoader().getResourceAsStream("resources/config.properties"));

            properties.setProperty("project.location", projectLoc);
            properties.setProperty("qm.location", QM_LOC);
            properties.setProperty("analysis.rerun", Boolean.toString(RERUN));
            properties.setProperty("output.inspectionresults", Boolean.toString(INSPECTION_RESULTS));
            properties.setProperty("results.location", resultsLocation);

            properties.store(new FileOutputStream(new File(System.getProperty("user.dir") + File.separator + "config.properties")), null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
