package service.impl;

import com.jcraft.jsch.Session;
import net.lingala.zip4j.core.ZipFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import service.ReindexTestsService;
import util.FilesAndDirectoryUtil;
import util.RuntimeProcessesUtil;
import util.SshUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ReindexTestsServiceImpl implements ReindexTestsService {

    private static final Logger LOGGER = LogManager.getLogger(ReindexTestsServiceImpl.class);

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

    @Value("${webTestsFileName}")
    private String webTestsFileName;

    @Value("${webRegressionFrameworkLocation}")
    private String webRegressionFrameworkLocation;

    @Value("${os.cmd.path}")
    private String osCMDPath;

    @Value("${os.cmd.option}")
    private String osCMDOption;

    @Value("${os.cmd.cd}")
    private String osCMDCd;

    @Value("${os.cmd.andJar}")
    private String osCMDAndJar;

    //    @Autowired
//  TODO Check if further versions of SpringWebSockets work with PROXY_CLASSES
    private Session jenkinsSession;

    public void getLatestRegressionFrameworkJar(String destination, SimpMessagingTemplate payload) throws Exception {
        try {

            jenkinsSession = SshUtil.initSSHAuth(jenkinsHost, jenkinsPort, jenkinsUserName, jenkinsPassword);

            String installerKitPath = SshUtil.getInstallerKitPath(jenkinsProject, jenkinsApproved,
                    ".zip", jenkinsSession);

            String kitFileName = installerKitPath.substring(installerKitPath.lastIndexOf("/") + 1);
            payload.convertAndSend(destination, "Looking for: " + kitFileName);
            LOGGER.info("Looking for: " + kitFileName);

            payload.convertAndSend(destination, "Check if the .zip file exists\n");
            LOGGER.info("Check if the .zip file exists");
            // check if the .zip file exists, if it doesn't clean up the folder
            if (!FilesAndDirectoryUtil.findFileInPath(kitFileName, regressionFrameworkLocation)) {
                LOGGER.info("Clean-up the folder");
                payload.convertAndSend(destination, "Clean-up the folder\n");

                FilesAndDirectoryUtil.deleteDirectory(regressionFrameworkLocation);
                FilesAndDirectoryUtil.createDirectory(regressionFrameworkLocation);

                // copy the new .zip file
                LOGGER.info("Copy the new .zip file");
                payload.convertAndSend(destination, "Copy the new .zip file\n");

                SshUtil.CopySftpFileToFile(installerKitPath, jenkinsSession, regressionFrameworkLocation + kitFileName);
                // Unzip and overwrite files silently
                LOGGER.info("Unzip and overwrite files silently");
                payload.convertAndSend(destination, "Unzip and overwrite files silently\n");

                ZipFile zipFile = new ZipFile(regressionFrameworkLocation + kitFileName);
                zipFile.extractAll(regressionFrameworkLocation);
            }
            for (Path fileToDelete : FilesAndDirectoryUtil.findFilesInPathWithPattern(regressionFrameworkLocation, "*.{jar,war}")) {
                Files.delete(fileToDelete);
            }

            // get the latest build from jenkins
            LOGGER.info("Get the latest build from jenkins");
            payload.convertAndSend(destination, "Get the latest build from jenkins\n");

            String latestBuildPath = SshUtil.getLatestBuildPath(jenkinsProject, ".jar", jenkinsSession);
            String buildFileName = latestBuildPath.substring(latestBuildPath.lastIndexOf("/") + 1);

            // copy the latest build
            LOGGER.info("Copy the latest build");
            payload.convertAndSend(destination, "Copy the latest build\n");

            SshUtil.CopySftpFileToFile(latestBuildPath, jenkinsSession, regressionFrameworkLocation + buildFileName);

            // execute generation of tests.xml
            LOGGER.info("Execute generation of " + webTestsFileName);
            payload.convertAndSend(destination, "Execute generation of " + webTestsFileName + "\n");

            String commandToExecute = osCMDCd + regressionFrameworkLocation + osCMDAndJar + buildFileName + " webtests";
            LOGGER.info(commandToExecute);
            payload.convertAndSend(destination, commandToExecute + "\n");

            Process p = RuntimeProcessesUtil.getProcessFromBuilder(osCMDPath, osCMDOption, commandToExecute);
            RuntimeProcessesUtil.printCMDToWriter(p.getInputStream(), destination, payload);

            while (p.isAlive()) { /* do nothing */}

            FilesAndDirectoryUtil.moveDirectory(Paths.get(regressionFrameworkLocation, webTestsFileName),
                    Paths.get(webRegressionFrameworkLocation, "/static/json/", webTestsFileName));

        } finally {
            jenkinsSession.disconnect();
        }
    }
}
