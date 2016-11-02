package service;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;

public interface PrepareForTestsService {
    void getMachinesVersion(String destination, SimpMessagingTemplate messagingTemplate)
            throws IOException;
    void zipResults(String destination, SimpMessagingTemplate messagingTemplate);
    void processMSBAdapterVersions() throws IOException;
    String getMasVersion();
    String getMposVersion();
    void setEnvironment(String environment);
}
