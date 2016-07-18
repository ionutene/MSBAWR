package service;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import net.lingala.zip4j.core.ZipFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import service.old.UtilsSsh;
import util.FilesAndDirectoryUtil;
import util.RuntimeProcessesUtil;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class WebSocketReindexTestsService {

    private static final Logger LOGGER = LogManager.getLogger(ReindexTestsService.class);

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

    @Value("${regressionFrameworkLocationCMD}")
    private String regressionFrameworkLocationCMD;

    @Value("${os.cmd.path}")
    private String osCMDPath;

    @Value("${os.cmd.option}")
    private String osCMDOption;

//    @Autowired
    private Session jenkinsSession;

    public void webSocketReindexTests(SimpMessagingTemplate session) throws Exception {
        try {

            jenkinsSession = initSSHAuth();

            String installerKitPath = UtilsSsh.getInstallerKitPath(jenkinsProject, jenkinsApproved,
                    ".zip", jenkinsSession);

            String kitFileName = installerKitPath.substring(installerKitPath.lastIndexOf("/") + 1);
            session.convertAndSend("Looking for: " + kitFileName);
            LOGGER.info("Looking for: " + kitFileName);

            session.convertAndSend("Check if the .zip file exists<br/>");
            LOGGER.info("Check if the .zip file exists<br/>");
            // check if the .zip file exists, if it doesn't clean up the folder
            if (!FilesAndDirectoryUtil.findFileInPath(kitFileName, regressionFrameworkLocation)) {
                LOGGER.info("Clean-up the folder<br/>");
                session.convertAndSend("Clean-up the folder<br/>");

                FilesAndDirectoryUtil.deleteDirectory(regressionFrameworkLocation);
                FilesAndDirectoryUtil.createDirectory(regressionFrameworkLocation);

                // copy the new .zip file
                LOGGER.info("Copy the new .zip file<br/>");
                session.convertAndSend("Copy the new .zip file<br/>");

                UtilsSsh.CopySftpFileToFile(installerKitPath, jenkinsSession, regressionFrameworkLocation + kitFileName);
                // Unzip and overwrite files silently
                LOGGER.info("Unzip and overwrite files silently<br/>");
                session.convertAndSend("Unzip and overwrite files silently<br/>");

                ZipFile zipFile = new ZipFile(regressionFrameworkLocation + kitFileName);
                zipFile.extractAll(regressionFrameworkLocation);
            }
            for (Path fileToDelete : FilesAndDirectoryUtil.findFilesInPathWithPattern(regressionFrameworkLocation, "*.{jar,war}")) {
                Files.delete(fileToDelete);
            }

            // get the latest build from jenkins
            LOGGER.info("Get the latest build from jenkins<br/>");
            session.convertAndSend("Get the latest build from jenkins<br/>");

            String latestBuildPath = UtilsSsh.getLatestBuildPath(jenkinsProject, ".jar", jenkinsSession);
            String buildFileName = latestBuildPath.substring(latestBuildPath.lastIndexOf("/") + 1);

            // copy the latest build
            LOGGER.info("Copy the latest build<br/>");
            session.convertAndSend("Copy the latest build<br/>");

            UtilsSsh.CopySftpFileToFile(latestBuildPath, jenkinsSession, regressionFrameworkLocation + buildFileName);

            // execute generation of tests.xml
            LOGGER.info("Execute generation of tests.xml<br/>");
            session.convertAndSend("Execute generation of tests.xml<br/>");

            String commandToExecute = regressionFrameworkLocationCMD + " && java -jar " + buildFileName + " webtests";
            LOGGER.info(commandToExecute);
            session.convertAndSend(commandToExecute);

            Process p = RuntimeProcessesUtil.getProcessFromBuilder(osCMDPath, osCMDOption, commandToExecute);
            RuntimeProcessesUtil.printCMDToWriter(p.getInputStream(), session);

        } finally {
            jenkinsSession.disconnect();
        }
    }

    public Session initSSHAuth() throws JSchException {
        Session sshCon;

        JSch jsch = new JSch();
        sshCon = jsch.getSession(jenkinsUserName, jenkinsHost, Integer.parseInt(jenkinsPort));
        sshCon.setConfig("StrictHostKeyChecking", "no");
        sshCon.setConfig("PreferredAuthentications", "password");
        sshCon.setPassword(jenkinsPassword);
        sshCon.connect();

        return sshCon;
    }
}
