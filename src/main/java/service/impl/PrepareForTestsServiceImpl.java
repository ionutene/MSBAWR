package service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import service.PrepareForTestsService;
import util.FilesAndDirectoryUtil;
import util.RuntimeProcessesUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class PrepareForTestsServiceImpl implements PrepareForTestsService {

    private static final Logger LOGGER = LogManager.getLogger(PrepareForTestsServiceImpl.class);

    @Value("${os.cmd.path}")
    private String osCMDPath;

    @Value("${os.cmd.option}")
    private String osCMDOption;

    @Value("${regressionFrameworkLocation}")
    private String regressionFrameworkLocation;

    @Value("${regressionFrameworkLocationCMD}")
    private String regressionFrameworkLocationCMD;

    private String masVersion;
    private String mposVersion;
    private String environment;

    public void getMachinesVersion(String destination, SimpMessagingTemplate messagingTemplate) throws IOException {
        List<Path> paths = FilesAndDirectoryUtil.findFilesInPathWithPattern(regressionFrameworkLocation, "*.{jar}");

        if (paths.size() != 1) throw new IOException("Too many .jar files!");

        String commandToExecute = regressionFrameworkLocationCMD + " && java -jar " + paths.get(0) + " " + environment + " version";
        LOGGER.info(commandToExecute);
        Process p = RuntimeProcessesUtil.getProcessFromBuilder(osCMDPath, osCMDOption, commandToExecute);
        RuntimeProcessesUtil.printCMDToWriter(p.getInputStream(), destination, messagingTemplate);
    }

    public void zipResults(String destination, SimpMessagingTemplate messagingTemplate) {

    }

    public void processMSBAdapterVersions() throws IOException {
        List<Path> paths = FilesAndDirectoryUtil.findFilesInPathWithPattern(regressionFrameworkLocation, "*.{jar}");

        if (paths.size() != 1) throw new IOException("Too many .jar files!");

        String commandToExecute = regressionFrameworkLocationCMD + " && java -jar " + paths.get(0) + " " + environment + " version";
        LOGGER.info(commandToExecute);
        Process p = RuntimeProcessesUtil.getProcessFromBuilder(osCMDPath, osCMDOption, commandToExecute);
        String content = RuntimeProcessesUtil.getMSBAdapterVersionFromInputStream(p.getInputStream());

        if(content.length() > 1){
            if (content.contains("MAS:") && content.contains("MPOS:")) {
                masVersion = content.substring(content.indexOf("MAS:") + 4, content.indexOf(")") + 1 );
                masVersion = masVersion.substring(0, masVersion.indexOf(" (")) + "\\&lt\\;br\\/\\&gt\\;" + masVersion.substring(masVersion.indexOf("("));
                mposVersion = content.substring(content.indexOf("MPOS:") + 5, content.lastIndexOf(")") + 1 );
                mposVersion = mposVersion.substring(0, mposVersion.indexOf(" (")) + "\\&lt\\;br\\/\\&gt\\;" + mposVersion.substring(mposVersion.indexOf("("));
            }
            else if (content.contains("AIX") || content.contains("Linux")) {
                masVersion = content.substring(content.indexOf(":") + 1, content.indexOf(")") + 1 );
                masVersion = masVersion.substring(0, masVersion.indexOf(" (")) + "\\&lt\\;br\\/\\&gt\\;" + masVersion.substring(masVersion.indexOf("("));
                mposVersion = "-";
            }
        }
    }

    public String getMasVersion() {
        return masVersion;
    }

    public String getMposVersion() {
        return mposVersion;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
