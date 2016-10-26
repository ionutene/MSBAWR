package service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import util.RuntimeProcessesUtil;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class RunTestsService {

    private static final Logger LOGGER = LogManager.getLogger(RunTestsService.class);

    @Value("${os.cmd.path}")
    private String osCMDPath;

    @Value("${os.cmd.option}")
    private String osCMDOption;

    @Value("${regressionFrameworkLocation}")
    private String regressionFrameworkLocation;

    @Value("${regressionFrameworkLocationCMD}")
    private String regressionFrameworkLocationCMD;

    public String prepareArguments(List<String> values) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String value : values) {
            stringBuilder.append(value).append(" ");
        }
        return stringBuilder.toString();
    }

    public void doTheThing(String args, String destination, SimpMessagingTemplate messagingTemplate) throws IOException {
        List<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(FileSystems.getDefault().getPath(regressionFrameworkLocation),
                             "*.{jar}")) {
            for (Path path : stream) {
                LOGGER.info(path.getFileName());
                paths.add(path);
            }
        }

        if (paths.size() != 1) throw new IOException("Too many .jar files!");

        String commandToExecute = regressionFrameworkLocationCMD + " && java -jar " + paths.get(0) + " buk30_8600 cta";
        LOGGER.info(commandToExecute);
        Process p = RuntimeProcessesUtil.getProcessFromBuilder(osCMDPath, osCMDOption, commandToExecute);
        RuntimeProcessesUtil.printCMDToWriter(p.getInputStream(), destination, messagingTemplate);
    }

}
