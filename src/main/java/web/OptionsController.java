package web;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.AjaxResponseBody;
import data.GenericJacksonConverter;
import data.SearchCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
public class OptionsController {

    private static Logger LOGGER = LoggerFactory.getLogger(OptionsController.class);

    @RequestMapping(value = "/getOptions")
    public AjaxResponseBody getSearchResultViaAjax(@RequestBody SearchCriteria search) {
        AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
        ObjectMapper mapper = new ObjectMapper();
        try {
            LOGGER.info(String.valueOf(search));
            byte[] environmentsData = Files.readAllBytes(Paths.get("/static/json/envCorrelation.json"));
            byte[] testTypesData = Files.readAllBytes(Paths.get("/static/json/testTypes.json"));

            GenericJacksonConverter environments = mapper.readValue(environmentsData, GenericJacksonConverter.class);
            GenericJacksonConverter testTypes = mapper.readValue(testTypesData, GenericJacksonConverter.class);

            LOGGER.info(String.valueOf(environments));
            LOGGER.info(String.valueOf(testTypes));

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }
}
