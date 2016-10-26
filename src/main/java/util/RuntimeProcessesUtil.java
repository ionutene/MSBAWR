package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;

public class RuntimeProcessesUtil {

    private static final Logger LOGGER = LogManager.getLogger(RuntimeProcessesUtil.class);

    public static Process getProcessFromBuilder(String osCMDPath, String osCMDOption,
                                                String commandToExecute) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(osCMDPath, osCMDOption, commandToExecute);
        builder.redirectErrorStream(true);
        return builder.start();
    }

    public static void printCMDToWriter(InputStream stream, PrintWriter writer) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        String line;
        while (((line = r.readLine()) != null)) {
            writer.println(line);
            writer.flush();
        }
    }

    public static void printCMDToWriter(InputStream stream, WebSocketSession session) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        String line;
        while (((line = r.readLine()) != null)) {
            session.sendMessage(new TextMessage(line));
        }
    }

    public static void printCMDToWriter(InputStream stream, String destination,
                                        SimpMessagingTemplate messagingTemplate) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        String line;
        while (((line = r.readLine()) != null)) {
            LOGGER.debug(line);
            messagingTemplate.convertAndSend(destination, line + "<br/>");
        }
    }
}
