package service;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.old.UtilsSsh;

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
            result = UtilsSsh.getInstallerKitPath(jenkinsProject, "/promotions/Approved/lastSuccessful",
                    ".zip", jenkinsSession);
            LOGGER.info(result);
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            jenkinsSession.disconnect();
        }
        return result;
    }
}
