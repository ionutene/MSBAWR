package web;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.SearchCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.xml.sax.SAXException;
import service.RunTestsService;
import service.WebSocketReindexTestsService;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

@Controller
public class WebSocketController {

    private static final Logger LOGGER = LogManager.getLogger(WebSocketController.class);

    @Autowired
    WebSocketReindexTestsService webSocketReindexTestsService;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private RunTestsService runTestsService;

    @MessageMapping("/section")
    public void result(String message) throws Exception {
        if (message != null && message.equals("reindex"))
            webSocketReindexTestsService.webSocketReindexTests("/topic/message", template);
    }

    @MessageMapping("/runTests")
    public void runTests(String message) throws IOException, ParserConfigurationException, SAXException, TransformerException {
//      TODO find out how to use MessageConverters with WebSockets so there's no need to parse the String into an Object
        ObjectMapper objectMapper = new ObjectMapper();
        SearchCriteria searchCriteria = objectMapper.readValue(message, SearchCriteria.class);
        LOGGER.info(searchCriteria);
        runTestsService.setEnvironment(searchCriteria.getEnvironment());
        runTestsService.setArguments(searchCriteria.getCheckBoxes());
        runTestsService.runTestsForEnvironmentWithArgs("/topic/message", template);
    }

}
