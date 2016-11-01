package service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import service.AfterTestsService;
import service.RunTestsService;
import util.FilesAndDirectoryUtil;
import util.RuntimeProcessesUtil;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class RunTestsServiceImpl implements RunTestsService {

    private static final Logger LOGGER = LogManager.getLogger(RunTestsServiceImpl.class);

    @Value("${os.cmd.path}")
    private String osCMDPath;

    @Value("${os.cmd.option}")
    private String osCMDOption;

    @Value("${regressionFrameworkLocation}")
    private String regressionFrameworkLocation;

    @Value("${regressionFrameworkLocationCMD}")
    private String regressionFrameworkLocationCMD;

    private String processedArguments;
    private String environment;

    @Autowired
    AfterTestsService afterTestsService;

    public void setArguments(List<String> values) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String value : values) {
            stringBuilder.append(value).append(" ");
        }
        this.processedArguments = stringBuilder.toString();
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void runTestsForEnvironmentWithArgs(String destination, SimpMessagingTemplate messagingTemplate) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        List<Path> paths = FilesAndDirectoryUtil.findFilesInPathWithPattern(regressionFrameworkLocation, "*.{jar}");

        if (paths.size() != 1) throw new IOException("Too many .jar files!");

//      TODO remove CTA test hardcoding
        String commandToExecute = regressionFrameworkLocationCMD + " && java -jar " + paths.get(0) + " " + environment + " cta";
        LOGGER.info(commandToExecute);
        Process p = RuntimeProcessesUtil.getProcessFromBuilder(osCMDPath, osCMDOption, commandToExecute);
        RuntimeProcessesUtil.printCMDToWriter(p.getInputStream(), destination, messagingTemplate);
        while (p.isAlive()) {/* wait until process finishes */}
        afterTestsService.setEnvironment(environment);
        afterTestsService.moveTestsOutputToResults();
        afterTestsService.updateResultsXML();
        afterTestsService.deleteRecentTestsLogFile();
    }

}
