package web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RunTestsController {

    private static final Logger LOGGER = LogManager.getLogger(RunTestsController.class);
    

    @RequestMapping(value = "/getOptionsFromCheckboxes", produces = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody String runTests(@RequestBody List<String> checkboxValues) {
        LOGGER.info(checkboxValues);

        return "";
    }
}
