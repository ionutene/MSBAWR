package service.impl;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import service.ReindexTestsService;
import util.FilesAndDirectoryUtil;
import util.RuntimeProcessesUtil;
import util.SshUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class SimpleReindexServiceImpl implements ReindexTestsService {

    private static final Logger LOGGER = LogManager.getLogger(SimpleReindexServiceImpl.class);

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
    @Value("${jenkins.full}")
    private String jenkinsFull;


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

    @Value("${os.cmd.anyJar}")
    private String osCMDAnyJar;

    //    @Autowired
//  TODO Check if further versions of SpringWebSockets work with PROXY_CLASSES
    private Session jenkinsSession;

    private String installerKitPath;
    private String kitFileName;

    public void getLatestRegressionFrameworkJar(String destination, SimpMessagingTemplate payload) {
        try {

            // connect to Jenkins Server
            LOGGER.info("Obtaining connection with Jenkins Server");
            payload.convertAndSend(destination, "Obtaining connection with Jenkins Server\n");
            jenkinsSession = SshUtil.initSSHAuth(jenkinsHost, jenkinsPort, jenkinsUserName, jenkinsPassword);

            // obtaining the last stable buildPath and zip name
            LOGGER.info("Obtaining the last stable buildPath and zip name");
            payload.convertAndSend(destination, "Obtaining the last stable build path and build name\n");
            installerKitPath = SshUtil.getZipFilePath(jenkinsFull, ".zip", jenkinsSession);
            kitFileName = installerKitPath.substring(installerKitPath.lastIndexOf("/") + 1);
            LOGGER.info("Found latest: " + installerKitPath);
            payload.convertAndSend(destination, "Found latest: " + installerKitPath + "\n");

            LOGGER.info("Clean-up MSBAR folder: " + regressionFrameworkLocation);
            payload.convertAndSend(destination, "Clean-up MSBAR folder: " + regressionFrameworkLocation + "\n");
            FilesAndDirectoryUtil.deleteDirectoryContents(regressionFrameworkLocation);

            // copy the new .zip file
            LOGGER.info("Copy the latest MSBAR .zip");
            payload.convertAndSend(destination, "Copy the latest MSBAR .zip\n");
            SshUtil.CopySftpFileToFile(installerKitPath, jenkinsSession, regressionFrameworkLocation + kitFileName);

            // Unzip and overwrite files silently
            LOGGER.info("Proceed to unzip files");
            payload.convertAndSend(destination, "Proceed to unzip files\n");
            ZipFile zipFile = new ZipFile(regressionFrameworkLocation + kitFileName);
            zipFile.extractAll(regressionFrameworkLocation);

            // Unzip and overwrite files silently
            LOGGER.info("Verify that there's only one jar in MSBAR folder and obtain it's name!");
            payload.convertAndSend(destination, "Verify that there's only one jar in MSBAR folder and obtain it's name!\n");
            List<Path> paths = FilesAndDirectoryUtil.findFilesInPathWithPattern(regressionFrameworkLocation, osCMDAnyJar);
            if (paths.size() != 1) throw new IOException("Too many .jar files!");
            String buildFileName = paths.get(0).toString();

            // execute generation of tests.xml
            LOGGER.info("Execute generation of " + webTestsFileName);
            payload.convertAndSend(destination, "Execute generation of " + webTestsFileName + "\n");
            String commandToExecute = osCMDCd + regressionFrameworkLocation + osCMDAndJar + buildFileName + " webtests";
            LOGGER.info(commandToExecute);
            payload.convertAndSend(destination, commandToExecute + "\n");
            Process p = RuntimeProcessesUtil.getProcessFromBuilder(osCMDPath, osCMDOption, commandToExecute);
            RuntimeProcessesUtil.printCMDToWriter(p.getInputStream(), destination, payload);
            while (p.isAlive()) { /* do nothing */}

            // move webTests to their final place
            Path webTests = Paths.get(regressionFrameworkLocation, webTestsFileName);
            Path webTestsLocation = Paths.get(webRegressionFrameworkLocation, "/static/json/", webTestsFileName);
            LOGGER.info("Proceeding to replace file from: " + webTests + " to: " + webTestsLocation);
            payload.convertAndSend(destination, "Proceeding to replace file from: " + webTests + " to: " + webTestsLocation + "\n");
            FilesAndDirectoryUtil.moveDirectory(webTests, webTestsLocation);

        } catch (JSchException e) {
            payload.convertAndSend(destination, "JSchException\n");
            payload.convertAndSend(destination, e.toString());
        } catch (ZipException e) {
            payload.convertAndSend(destination, "ZipException\n");
            payload.convertAndSend(destination, e.toString());
        } catch (IOException e) {
            payload.convertAndSend(destination, "IOException\n");
            payload.convertAndSend(destination, e.toString());
        } catch (SftpException e) {
            payload.convertAndSend(destination, "SftpException\n");
            payload.convertAndSend(destination, e.toString());
        } finally {
            jenkinsSession.disconnect();
        }
    }
}
