package service;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;

public interface PrepareForTestsService {
    void getMachinesVersion() throws IOException;
    void zipResults();
    void processMSBAdapterVersions() throws IOException;
    String getMasVersion();
    String getMposVersion();
    void setEnvironment(String environment);
    void setSimpMessageTemplate(SimpMessagingTemplate simpMessageTemplate);
}
