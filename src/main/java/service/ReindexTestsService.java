package service;

import org.springframework.messaging.simp.SimpMessagingTemplate;

public interface ReindexTestsService {
    void getLatestRegressionFrameworkJar(String destination, SimpMessagingTemplate payload) throws Exception;
}
