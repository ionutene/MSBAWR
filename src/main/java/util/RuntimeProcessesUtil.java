package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RuntimeProcessesUtil {

    private static final Logger LOGGER = LogManager.getLogger(RuntimeProcessesUtil.class);

    public static Process getProcessFromBuilder(String osCMDPath, String osCMDOption,
                                                String commandToExecute) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(osCMDPath, osCMDOption, commandToExecute);
        builder.redirectErrorStream(true);
        return builder.start();
    }

    public static void printCMDToWriter(InputStream stream, String destination,
                                        SimpMessagingTemplate messagingTemplate) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while (((line = r.readLine()) != null)) {
                LOGGER.debug(line);
                messagingTemplate.convertAndSend(destination, line + "\n");
            }
        }
    }

    public static String getStringFromInputStream(InputStream stream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while (((line = r.readLine()) != null)) {
                LOGGER.debug(line);
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }
}
