package util;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;

public class RuntimeProcessesUtil {

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

    public static void printCMDToWriter(InputStream stream, SimpMessagingTemplate messagingTemplate) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        String line;
        while (((line = r.readLine()) != null)) {
            messagingTemplate.convertAndSend(line);
        }
    }
}
