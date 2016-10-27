package web;

import data.SearchCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.RunTestsService;

import java.io.IOException;

@RestController
public class RunTestsController {

    private static final Logger LOGGER = LogManager.getLogger(RunTestsController.class);

    @Autowired
    RunTestsService runTestsService;

    @Autowired
    private SimpMessagingTemplate template;

    @RequestMapping(value = "/getOptionsFromCheckboxes", produces = MediaType.TEXT_PLAIN_VALUE)
    public void runTests(@RequestBody SearchCriteria searchCriteria) throws IOException {
        LOGGER.info(searchCriteria);
        runTestsService.runTestsForEnvironmentWithArgs(searchCriteria.getEnvironment(),
                runTestsService.prepareArguments(searchCriteria.getCheckBoxes()), "/topic/message", template);
    }

}
