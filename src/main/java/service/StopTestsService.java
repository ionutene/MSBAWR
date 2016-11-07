package service;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;

public interface StopTestsService {
    void stopRunningTestsOnEnvironment(String environment, SimpMessagingTemplate messagingTemplate) throws IOException;
}
