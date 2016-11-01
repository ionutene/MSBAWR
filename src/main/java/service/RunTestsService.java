package service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;

public interface RunTestsService {
    void setArguments(List<String> values);
    void setEnvironment(String environment);
    void runTestsForEnvironmentWithArgs(String destination, SimpMessagingTemplate messagingTemplate)
            throws IOException, ParserConfigurationException, SAXException, TransformerException;
}
