package service;

import data.SearchCriteria;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public interface RunTestsService {
    void setSimpMessagingTemplate(SimpMessagingTemplate messagingTemplate);
    void setSearchCriteria(SearchCriteria searchCriteria);
    void parseArguments();
    void runTests()
            throws IOException, ParserConfigurationException, SAXException, TransformerException;
}
