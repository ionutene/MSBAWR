package web;

import data.AdvancedSearchCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.StopTestsService;

import java.io.IOException;

@RestController
public class StopTestsController {
    private static final Logger LOGGER = LogManager.getLogger(StopTestsController.class);

    @Autowired
    StopTestsService stopTestsService;

    @Autowired
    private SimpMessagingTemplate template;

    @RequestMapping(value = "/stopTestsOnEnv", produces = MediaType.TEXT_PLAIN_VALUE)
    public void stopTests(@RequestBody AdvancedSearchCriteria advancedSearchCriteria) throws IOException {
        LOGGER.info(advancedSearchCriteria);
        stopTestsService.stopRunningTestsOnEnvironment(advancedSearchCriteria.getEnv(), "/topic/message", template);
    }

}

