package web;

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
import java.util.List;

@RestController
public class RunTestsController {

    private static final Logger LOGGER = LogManager.getLogger(RunTestsController.class);

    @Autowired
    RunTestsService runTestsService;

    @Autowired
    private SimpMessagingTemplate template;

    @RequestMapping(value = "/getOptionsFromCheckboxes", produces = MediaType.TEXT_PLAIN_VALUE)
    public void runTests(@RequestBody List<String> checkboxValues) throws IOException {
        LOGGER.info(checkboxValues);
        runTestsService.doTheThing(runTestsService.prepareArguments(checkboxValues), "/topic/message", template);
    }

}
