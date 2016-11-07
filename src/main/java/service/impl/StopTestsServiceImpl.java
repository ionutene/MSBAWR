package service.impl;

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

    @Value("${stompDestination}")
    private String stompDestination;

    public void stopRunningTestsOnEnvironment(String environment, SimpMessagingTemplate messagingTemplate) throws IOException {
        String commandToExecute = "for /f \"tokens=1\" %i in ('jps -m ^| find \"" + environment + "\"') do ( taskkill /F /PID %i )";
        LOGGER.info(commandToExecute);
        Process p = RuntimeProcessesUtil.getProcessFromBuilder(osCMDPath, osCMDOption, commandToExecute);
        RuntimeProcessesUtil.printCMDToWriter(p.getInputStream(), stompDestination, messagingTemplate);
    }
}
