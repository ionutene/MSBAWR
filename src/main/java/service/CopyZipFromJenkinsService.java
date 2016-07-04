package service;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.old.UtilsSsh;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        LOGGER.info(jenkinsHost);
        String result = "NO_BONO";
        try {
//            localSession = UtilsSsh.initSSHAuth(localHost, localPort, localUserName, localPassword);
            jenkinsSession = UtilsSsh.initSSHAuth(jenkinsHost, jenkinsPort, jenkinsUserName, jenkinsPassword);

        } catch (JSchException e) {
            LOGGER.error(e);
        }
        try {
            result = UtilsSsh.getInstallerKitPath(jenkinsProject, jenkinsApproved,
                    ".zip", jenkinsSession);
            LOGGER.info(result);

            String kitFileName = result.substring(result.lastIndexOf("/") + 1);
            LOGGER.info("Looking for: " + kitFileName);

            // check if the .Zip file already exist
            LOGGER.info("Check if the .Zip file already exist<br/>");
            File localDir = new File(regressionFrameworkLocation);
            boolean found = false;
            for (String aux : localDir.list()) {
                if (aux.equals(kitFileName)) {
                    found = true;
                    LOGGER.info("The .zip file already exist:" + aux + "<br/>");
                    break;
                }
            }
            findJar(kitFileName);
            // if other clean up the folder
            if (!found) {
                LOGGER.info("Clean-up the folder<br/>");

/*                CmdRun clearCmd = new CmdRun("rm -rf " + regressionFrameworkLocation + "*", out, false, "", false);
                clearCmd.run();*/
            }


        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            jenkinsSession.disconnect();
        }
        return result;
    }

    private boolean findJar(String zipToFind) {
        Path path = Paths.get(regressionFrameworkLocation);
        //no filter applied
        LOGGER.info("\nNo filter applied:");
//        ds = Files.newDirectoryStream(path, "*.{png,jpg,bmp}")
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
            for (Path file : ds) {
                LOGGER.info(file.getFileName());
                if (zipToFind.equals(file.getFileName().toString()))
                    return true;
            }
        }catch(IOException e) {
            LOGGER.error(e);
        }
        return false;
    }
}
