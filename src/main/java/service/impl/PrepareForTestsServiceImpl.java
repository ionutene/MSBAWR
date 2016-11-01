package service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import util.FilesAndDirectoryUtil;
import util.RuntimeProcessesUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class PrepareForTestsServiceImpl {

    private static final Logger LOGGER = LogManager.getLogger(PrepareForTestsServiceImpl.class);

    @Value("${os.cmd.path}")
    private String osCMDPath;

    @Value("${os.cmd.option}")
    private String osCMDOption;

    @Value("${regressionFrameworkLocation}")
    private String regressionFrameworkLocation;

    @Value("${regressionFrameworkLocationCMD}")
    private String regressionFrameworkLocationCMD;

    public void getMachinesVersion(String environment, String destination, SimpMessagingTemplate messagingTemplate) throws IOException {
        List<Path> paths = FilesAndDirectoryUtil.findFilesInPathWithPattern(regressionFrameworkLocation, "*.{jar}");

        if (paths.size() != 1) throw new IOException("Too many .jar files!");

        String commandToExecute = regressionFrameworkLocationCMD + " && java -jar " + paths.get(0) + " " + environment + " version";
        LOGGER.info(commandToExecute);
        Process p = RuntimeProcessesUtil.getProcessFromBuilder(osCMDPath, osCMDOption, commandToExecute);
        RuntimeProcessesUtil.printCMDToWriter(p.getInputStream(), destination, messagingTemplate);
    }

    public void zipResults(String environment, String destination, SimpMessagingTemplate messagingTemplate) {

    }
}
