package web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.ReindexTestsService;

import javax.servlet.ServletResponse;
import java.io.IOException;

@RestController
public class ReindexTestsController {

    private static final Logger LOGGER = LogManager.getLogger(ReindexTestsController.class);

    @Autowired
    ReindexTestsService reindexTestsService;

    @RequestMapping(value = "/getZip", produces = MediaType.TEXT_HTML_VALUE)
    public void getReIndexedTests(ServletResponse response) {
        try {
            reindexTestsService.reindexTests(response.getWriter());
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }
}
