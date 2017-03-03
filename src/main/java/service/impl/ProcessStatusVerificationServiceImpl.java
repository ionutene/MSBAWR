package service.impl;

import config.WebSocketConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import service.ProcessVerificationService;
import util.RuntimeProcessesUtil;

import java.io.IOException;

public class ProcessStatusVerificationServiceImpl implements ProcessVerificationService {

    private static final Logger LOGGER = LogManager.getLogger(ProcessStatusVerificationServiceImpl.class);

    @Value("${os.cmd.path}")
    private String osCMDPath;

    @Value("${os.cmd.option}")
    private String osCMDOption;

    @Value("${regressionFrameworkLogFileName}")
    private String regressionFrameworkLogFileName;

    private String stompDestination;

    private static final String OPERATING_SYSTEM = System.getProperty("os.name");

    public boolean verifyRunningProcesses(String environment, SimpMessagingTemplate messagingTemplate) throws IOException {
        stompDestination = WebSocketConfig.BROKER_QUEUE_NAME_PREFIX + environment;
        String commandToExecute;
        if (OPERATING_SYSTEM.contains("Windows")) {
            commandToExecute = "jps -m | find \"" + regressionFrameworkLogFileName + "\"";
        } else {
            commandToExecute = "jps -m | grep -i \"" + regressionFrameworkLogFileName + "\"";
        }
        LOGGER.info(commandToExecute);
        messagingTemplate.convertAndSend(stompDestination, commandToExecute);

        Process p = RuntimeProcessesUtil.getProcessFromBuilder(osCMDPath, osCMDOption, commandToExecute);
        String processes = RuntimeProcessesUtil.getStringFromInputStream(p.getInputStream());

        if (processes.trim().isEmpty()) {
            LOGGER.info("Haven't found any java processes running!");
            messagingTemplate.convertAndSend(stompDestination, "Haven't found any java processes running!");
        } else {
            LOGGER.info("Found the following processes: ");
            LOGGER.info(processes);
            messagingTemplate.convertAndSend(stompDestination, "Found the following processes: ");
            messagingTemplate.convertAndSend(stompDestination, processes);
        }

        return !processes.trim().isEmpty();
    }
}
