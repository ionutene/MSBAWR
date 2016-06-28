package web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.AjaxResponseBody;
import data.SearchCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OptionsController {

    private static final Logger LOGGER = LogManager.getLogger(OptionsController.class);

    @Autowired
    ServletContext servletContext;

    @RequestMapping(value = "/getOptions")
    public AjaxResponseBody getSearchResultViaAjax(@RequestBody SearchCriteria search) {
        AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> data = new HashMap<>();
        try {
            LOGGER.info(search);

            byte[] environmentsData = Files.readAllBytes(Paths.get(servletContext.getRealPath("/static/json/envCorrelation.json")));
            byte[] testTypesData = Files.readAllBytes(Paths.get(servletContext.getRealPath("/static/json/testTypes.json")));

            // convert JSON string to Map
            data = mapper.readValue(environmentsData, new TypeReference<Map<String, String>>(){});
            LOGGER.info(data);
            data = mapper.readValue(testTypesData, new TypeReference<Map<String, String>>(){});
            LOGGER.info(data);

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }
}
