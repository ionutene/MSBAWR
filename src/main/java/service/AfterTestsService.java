package service;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public interface AfterTestsService {
    void setEnvironment(String environment);
    void moveTestsOutputToResults() throws IOException;
    void updateResultsXML() throws ParserConfigurationException, SAXException, IOException, TransformerException;
    void deleteRecentTestsLogFile() throws IOException;
}
