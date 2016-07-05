package service;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import net.lingala.zip4j.core.ZipFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.old.UtilsSsh;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Service
public class CopyZipFromJenkinsService {

    private static final Logger LOGGER = LogManager.getLogger(CopyZipFromJenkinsService.class);

    @Value("${local.host}")
    private String localHost;
    @Value("${local.port}")
    private String localPort;
    @Value("${local.username}")
    private String localUserName;
    @Value("${local.password}")
    private String localPassword;


    @Value("${jenkins.host}")
    private String jenkinsHost;
    @Value("${jenkins.port}")
    private String jenkinsPort;
    @Value("${jenkins.username}")
    private String jenkinsUserName;
    @Value("${jenkins.password}")
    private String jenkinsPassword;
    @Value("${jenkins.project}")
    private String jenkinsProject;
    @Value("${jenkins.approved}")
    private String jenkinsApproved;

    @Value("${regressionFrameworkLocation}")
    private String regressionFrameworkLocation;

    private Session localSession;
    private Session jenkinsSession;

    public String getInstallPath() {
        String result = "NO_BONO";
        try {
//            localSession = UtilsSsh.initSSHAuth(localHost, localPort, localUserName, localPassword);
            jenkinsSession = UtilsSsh.initSSHAuth(jenkinsHost, jenkinsPort, jenkinsUserName, jenkinsPassword);

        } catch (JSchException e) {
            LOGGER.error(e);
        }
        try {
            String installerKitPath = UtilsSsh.getInstallerKitPath(jenkinsProject, jenkinsApproved,
                    ".zip", jenkinsSession);

            String kitFileName = installerKitPath.substring(installerKitPath.lastIndexOf("/") + 1);
            LOGGER.info("Looking for: " + kitFileName);

            LOGGER.info("Check if the .zip file exists<br/>");
            // check if the .zip file exists, if it doesn't clean up the folder
            if (!findFileInPath(kitFileName, regressionFrameworkLocation)) {
                LOGGER.info("Clean-up the folder<br/>");
                deleteDirectory(regressionFrameworkLocation);
                createDirectory(regressionFrameworkLocation);

                // copy the new .zip file
                LOGGER.info("Copy the new .zip file<br/>");
                UtilsSsh.CopySftpFileToFile(installerKitPath, jenkinsSession, regressionFrameworkLocation + kitFileName);
                // Unzip and overwrite files silently
                LOGGER.info("Unzip and overwrite files silently<br/>");
                ZipFile zipFile = new ZipFile(regressionFrameworkLocation + kitFileName);
                zipFile.extractAll(regressionFrameworkLocation);
            }
            for (Path fileToDelete : findFilesInPathWithPattern(regressionFrameworkLocation, "*.{jar,war}")) {
                Files.delete(fileToDelete);
            }

            // get the latest build from jenkins
            LOGGER.info("Get the latest build from jenkins<br/>");
            String latestBuildPath = UtilsSsh.getLatestBuildPath(jenkinsProject, ".jar", jenkinsSession);
            String buildFileName = latestBuildPath.substring(latestBuildPath.lastIndexOf("/") + 1);

            // copy the latest build
            LOGGER.info("Copy the latest build<br/>");
            UtilsSsh.CopySftpFileToFile(latestBuildPath, jenkinsSession, regressionFrameworkLocation + buildFileName);

            JarClassLoader jarClassLoader = new JarClassLoader(Paths.get(regressionFrameworkLocation + buildFileName).toUri().toURL());
            jarClassLoader.invokeClass("net.metrosystems.msb.main.Main", new String[]{"webtests"});

        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            jenkinsSession.disconnect();
        }
        return result;
    }

    private List<Path> findFilesInPathWithPattern(String pathToSearch, String pattern) throws IOException {
        List<Path> filesFound = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(pathToSearch), pattern)) {
            for (Path file : ds) {
                filesFound.add(file);
            }
        }
        return filesFound;
    }

    private boolean findFileInPath(String fileToFind, String pathToSearch) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(pathToSearch))) {
            for (Path file : ds) {
//                LOGGER.info(file.getFileName());
                if (fileToFind.equals(file.getFileName().toString()))
                    return true;
            }
        }
        return false;
    }

    private void deleteDirectory(String directoryPath) throws IOException {
        Files.walkFileTree(Paths.get(directoryPath), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void createDirectory(String directoryPath) throws IOException {
        Files.createDirectory(FileSystems.getDefault().getPath(directoryPath));
    }
}
