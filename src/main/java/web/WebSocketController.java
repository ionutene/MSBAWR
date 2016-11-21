package web;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.AdvancedSearchCriteria;
import data.SearchCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xml.sax.SAXException;
import service.PrepareForTestsService;
import service.ReindexTestsService;
import service.RunTestsService;
import service.StopTestsService;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

@Controller
public class WebSocketController {

    private static final Logger LOGGER = LogManager.getLogger(WebSocketController.class);

    @Autowired
    private ReindexTestsService reindexTestsService;

    @Autowired
    private RunTestsService runTestsService;

    @Autowired
    private StopTestsService stopTestsService;

    @Autowired
    private PrepareForTestsService prepareForTestsService;

    @Value("${stompDestination}")
    private String stompDestination;

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/reindex")
    public void reindex(String message) throws Exception {
        reindexTestsService.getLatestRegressionFrameworkJar(stompDestination, template);
    }

    @RequestMapping(value = "/runTestsFallback")
    public void runTestsFallback(@RequestBody SearchCriteria searchCriteria)
            throws ParserConfigurationException, TransformerException, SAXException, IOException {
        LOGGER.info(searchCriteria);
        runTestsService.setSimpMessagingTemplate(template);
        runTestsService.setSearchCriteria(searchCriteria);
        runTestsService.parseArguments();
        runTestsService.runTests();
    }

    @MessageMapping("/runTests")
    public void runTests(String message) throws IOException, ParserConfigurationException, SAXException, TransformerException {
//      TODO find out how to use MessageConverters with WebSockets so there's no need to parse the String into an Object
        ObjectMapper objectMapper = new ObjectMapper();
        SearchCriteria searchCriteria = objectMapper.readValue(message, SearchCriteria.class);
        LOGGER.info(searchCriteria);
        runTestsService.setSimpMessagingTemplate(template);
        runTestsService.setSearchCriteria(searchCriteria);
        runTestsService.parseArguments();
        runTestsService.runTests();
    }

    @MessageMapping("/stopTests")
    public void stopTests(String message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        AdvancedSearchCriteria advancedSearchCriteria = objectMapper.readValue(message, AdvancedSearchCriteria.class);
        LOGGER.info(advancedSearchCriteria);
        stopTestsService.stopRunningTestsOnEnvironment(advancedSearchCriteria.getEnv(), template);
    }

    @MessageMapping(value = "/prepareForTests")
    public void prepareForTests(String message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        AdvancedSearchCriteria advancedSearchCriteria = objectMapper.readValue(message, AdvancedSearchCriteria.class);
        LOGGER.info(advancedSearchCriteria);
        prepareForTestsService.setEnvironment(advancedSearchCriteria.getEnv());
        prepareForTestsService.setSimpMessageTemplate(template);
        prepareForTestsService.getMachinesVersion();
        prepareForTestsService.zipResults();
    }

}
