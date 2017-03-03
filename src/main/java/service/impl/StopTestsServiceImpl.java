package service.impl;

import config.WebSocketConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import service.StopTestsService;
import util.RuntimeProcessesUtil;

import java.io.IOException;

@Service
public class StopTestsServiceImpl implements StopTestsService {

    private static final Logger LOGGER = LogManager.getLogger(StopTestsServiceImpl.class);

    @Value("${os.cmd.path}")
    private String osCMDPath;

    @Value("${os.cmd.option}")
    private String osCMDOption;

    private String stompDestination;

    private static final String OPERATING_SYSTEM = System.getProperty("os.name");

    public void stopRunningTestsOnEnvironment(String environment, SimpMessagingTemplate messagingTemplate) throws IOException {
        String commandToExecute;
        if (OPERATING_SYSTEM.contains("Windows")) {
            commandToExecute = "for /f \"tokens=1\" %i in ('jps -m ^| find \"" + environment + "\"') do ( taskkill /F /PID %i )";
        } else {
            commandToExecute = "var=`jps -m | grep -i \"" + environment + "\" | awk {'print $1'}` && kill -9 $var";
        }
        LOGGER.info(commandToExecute);
        messagingTemplate.convertAndSend(stompDestination, commandToExecute);
        stompDestination = WebSocketConfig.BROKER_QUEUE_NAME_PREFIX + environment;
        Process p = RuntimeProcessesUtil.getProcessFromBuilder(osCMDPath, osCMDOption, commandToExecute);
        RuntimeProcessesUtil.printCMDToWriter(p.getInputStream(), stompDestination, messagingTemplate);
    }
}
