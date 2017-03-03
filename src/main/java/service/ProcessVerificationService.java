package service;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;

public interface ProcessVerificationService {

    boolean verifyRunningProcesses(String environment, SimpMessagingTemplate messagingTemplate) throws IOException;
}
