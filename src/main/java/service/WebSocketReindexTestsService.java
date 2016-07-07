package service;

import com.jcraft.jsch.Session;
import net.lingala.zip4j.core.ZipFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import service.old.UtilsSsh;
import util.FilesAndDirectoryUtil;
import util.RuntimeProcessesUtil;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class WebSocketReindexTestsService {

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

    public void webSocketReindexTests(WebSocketSession session) {
        try {

            String installerKitPath = UtilsSsh.getInstallerKitPath(jenkinsProject, jenkinsApproved,
                    ".zip", jenkinsSession);

            String kitFileName = installerKitPath.substring(installerKitPath.lastIndexOf("/") + 1);
            session.sendMessage(new TextMessage("Looking for: " + kitFileName));
            LOGGER.info("Looking for: " + kitFileName);

            session.sendMessage(new TextMessage("Check if the .zip file exists<br/>"));
            LOGGER.info("Check if the .zip file exists<br/>");
            // check if the .zip file exists, if it doesn't clean up the folder
            if (!FilesAndDirectoryUtil.findFileInPath(kitFileName, regressionFrameworkLocation)) {
                LOGGER.info("Clean-up the folder<br/>");
                session.sendMessage(new TextMessage("Clean-up the folder<br/>"));

                FilesAndDirectoryUtil.deleteDirectory(regressionFrameworkLocation);
                FilesAndDirectoryUtil.createDirectory(regressionFrameworkLocation);

                // copy the new .zip file
                LOGGER.info("Copy the new .zip file<br/>");
                session.sendMessage(new TextMessage("Copy the new .zip file<br/>"));

                UtilsSsh.CopySftpFileToFile(installerKitPath, jenkinsSession, regressionFrameworkLocation + kitFileName);
                // Unzip and overwrite files silently
                LOGGER.info("Unzip and overwrite files silently<br/>");
                session.sendMessage(new TextMessage("Unzip and overwrite files silently<br/>"));

                ZipFile zipFile = new ZipFile(regressionFrameworkLocation + kitFileName);
                zipFile.extractAll(regressionFrameworkLocation);
            }
            for (Path fileToDelete : FilesAndDirectoryUtil.findFilesInPathWithPattern(regressionFrameworkLocation, "*.{jar,war}")) {
                Files.delete(fileToDelete);
            }

            // get the latest build from jenkins
            LOGGER.info("Get the latest build from jenkins<br/>");
            session.sendMessage(new TextMessage("Get the latest build from jenkins<br/>"));

            String latestBuildPath = UtilsSsh.getLatestBuildPath(jenkinsProject, ".jar", jenkinsSession);
            String buildFileName = latestBuildPath.substring(latestBuildPath.lastIndexOf("/") + 1);

            // copy the latest build
            LOGGER.info("Copy the latest build<br/>");
            session.sendMessage(new TextMessage("Copy the latest build<br/>"));

            UtilsSsh.CopySftpFileToFile(latestBuildPath, jenkinsSession, regressionFrameworkLocation + buildFileName);

            // execute generation of tests.xml
            LOGGER.info("Execute generation of tests.xml<br/>");
            session.sendMessage(new TextMessage("Execute generation of tests.xml<br/>"));

            String commandToExecute = regressionFrameworkLocationCMD + " && java -jar " + buildFileName + " webtests";
            LOGGER.info(commandToExecute);
            session.sendMessage(new TextMessage(commandToExecute));

            Process p = RuntimeProcessesUtil.getProcessFromBuilder(osCMDPath, osCMDOption, commandToExecute);
            RuntimeProcessesUtil.printCMDToWriter(p.getInputStream(), session);

        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            jenkinsSession.disconnect();
        }
    }


}
