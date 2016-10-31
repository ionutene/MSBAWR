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
import service.PrepareForTestsService;

import java.io.IOException;

@RestController
public class PrepareForTestsController {

    private static final Logger LOGGER = LogManager.getLogger(PrepareForTestsController.class);

    @Autowired
    PrepareForTestsService prepareForTestsService;

    @Autowired
    private SimpMessagingTemplate template;

    @RequestMapping(value = "/prepareForTestsOnEnv", produces = MediaType.TEXT_PLAIN_VALUE)
    public void prepareForTests(@RequestBody AdvancedSearchCriteria advancedSearchCriteria) throws IOException {
        LOGGER.info(advancedSearchCriteria);
        prepareForTestsService.getMachinesVersion(advancedSearchCriteria.getEnv(), "/topic/message", template);
        prepareForTestsService.zipResults(advancedSearchCriteria.getEnv(), "/topic/message", template);
    }
}
