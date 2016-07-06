package service;

import com.jcraft.jsch.Session;
import net.lingala.zip4j.core.ZipFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.old.UtilsSsh;
import util.FilesAndDirectoryUtil;
import util.RuntimeProcessesUtil;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ReindexTestsService {

    private static final Logger LOGGER = LogManager.getLogger(ReindexTestsService.class);

    @Value("${jenkins.project}")
    private String jenkinsProject;
    @Value("${jenkins.approved}")
    private String jenkinsApproved;

    @Value("${regressionFrameworkLocation}")
    private String regressionFrameworkLocation;

    @Value("${regressionFrameworkLocationCMD}")
    private String regressionFrameworkLocationCMD;

    @Value("${os.cmd.path}")
    private String osCMDPath;

    @Value("${os.cmd.option}")
    private String osCMDOption;

    @Autowired
    private Session jenkinsSession;

    public void reindexTests(PrintWriter writer) {
        try {


            String installerKitPath = UtilsSsh.getInstallerKitPath(jenkinsProject, jenkinsApproved,
                    ".zip", jenkinsSession);

            String kitFileName = installerKitPath.substring(installerKitPath.lastIndexOf("/") + 1);
            writer.println("Looking for: " + kitFileName);
            writer.flush();
            LOGGER.info("Looking for: " + kitFileName);

            writer.println("Check if the .zip file exists<br/>");
            writer.flush();
            LOGGER.info("Check if the .zip file exists<br/>");
            // check if the .zip file exists, if it doesn't clean up the folder
            if (!FilesAndDirectoryUtil.findFileInPath(kitFileName, regressionFrameworkLocation)) {
                LOGGER.info("Clean-up the folder<br/>");
                writer.println("Clean-up the folder<br/>");
                writer.flush();
                FilesAndDirectoryUtil.deleteDirectory(regressionFrameworkLocation);
                FilesAndDirectoryUtil.createDirectory(regressionFrameworkLocation);

                // copy the new .zip file
                LOGGER.info("Copy the new .zip file<br/>");
                writer.println("Copy the new .zip file<br/>");
                writer.flush();
                UtilsSsh.CopySftpFileToFile(installerKitPath, jenkinsSession, regressionFrameworkLocation + kitFileName);
                // Unzip and overwrite files silently
                LOGGER.info("Unzip and overwrite files silently<br/>");
                writer.println("Unzip and overwrite files silently<br/>");
                writer.flush();
                ZipFile zipFile = new ZipFile(regressionFrameworkLocation + kitFileName);
                zipFile.extractAll(regressionFrameworkLocation);
            }
            for (Path fileToDelete : FilesAndDirectoryUtil.findFilesInPathWithPattern(regressionFrameworkLocation, "*.{jar,war}")) {
                Files.delete(fileToDelete);
            }

            // get the latest build from jenkins
            LOGGER.info("Get the latest build from jenkins<br/>");
            writer.println("Get the latest build from jenkins<br/>");
            writer.flush();
            String latestBuildPath = UtilsSsh.getLatestBuildPath(jenkinsProject, ".jar", jenkinsSession);
            String buildFileName = latestBuildPath.substring(latestBuildPath.lastIndexOf("/") + 1);

            // copy the latest build
            LOGGER.info("Copy the latest build<br/>");
            writer.println("Copy the latest build<br/>");
            writer.flush();
            UtilsSsh.CopySftpFileToFile(latestBuildPath, jenkinsSession, regressionFrameworkLocation + buildFileName);

            // execute generation of tests.xml
            LOGGER.info("Execute generation of tests.xml<br/>");
            writer.println("Execute generation of tests.xml<br/>");
            writer.flush();

            String commandToExecute = regressionFrameworkLocationCMD + " && java -jar " + buildFileName + " webtests";
            LOGGER.info(commandToExecute);
            writer.println(commandToExecute);
            writer.flush();
            Process p = RuntimeProcessesUtil.getProcessFromBuilder(osCMDPath, osCMDOption, commandToExecute);
            RuntimeProcessesUtil.printCMDToWriter(p.getInputStream(), writer);

        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            jenkinsSession.disconnect();
        }
    }


}
