package service;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;

public interface PrepareForTestsService {
    void getMachinesVersion(String environment, String destination, SimpMessagingTemplate messagingTemplate)
            throws IOException;
    void zipResults(String environment, String destination, SimpMessagingTemplate messagingTemplate);
}
