package web;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.WebSocketConfig;
import data.AdvancedSearchCriteria;
import data.SearchCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xml.sax.SAXException;
import service.*;

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

    @Autowired
    private ProcessVerificationService processVerificationService;

    @Autowired
    private SimpMessagingTemplate template;

//  TODO CHECK IF TESTS ARE RUNNING AND IF SO, DON'T TRY TO REINDEX!!!!!
    @MessageMapping("/reindex")
    public void reindex(String message) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        AdvancedSearchCriteria advancedSearchCriteria = objectMapper.readValue(message, AdvancedSearchCriteria.class);
        LOGGER.info(advancedSearchCriteria);
        String stompDestination = WebSocketConfig.BROKER_QUEUE_NAME_PREFIX + advancedSearchCriteria.getEnv();
        if (processVerificationService.verifyRunningProcesses(advancedSearchCriteria.getEnv(), template)) {
            template.convertAndSend(stompDestination, "Some java processes are still running please stop them manually or wait for them to finish!");
            LOGGER.info("Some java processes are still running please stop them manually or wait for them to finish!");
        } else {
            reindexTestsService.getLatestRegressionFrameworkJar(stompDestination, template);
        }
    }

    @RequestMapping(value = "/runTestsFallback")
    public void runTestsFallback(@RequestBody SearchCriteria searchCriteria)
            throws ParserConfigurationException, TransformerException, SAXException, IOException {
        LOGGER.info(searchCriteria);
        runTestsService.setSimpMessagingTemplate(template);
        runTestsService.setSearchCriteria(searchCriteria);
        runTestsService.setStompDestination();
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
        runTestsService.setStompDestination();
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
