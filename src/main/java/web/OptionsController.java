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
import java.util.Map;

@RestController
public class OptionsController {

    private static final Logger LOGGER = LogManager.getLogger(OptionsController.class);

    @Autowired
    ServletContext servletContext;

    @RequestMapping(value = "/getOptions")
    public AjaxResponseBody getSearchResultViaAjax(@RequestBody SearchCriteria search) {
        AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
        Map<String, String> data;
        try {
            LOGGER.info(search);

            // convert JSON string to Map
            data = getJSONDataFromStatic("/static/json/envCorrelation.json");
            LOGGER.info(data);
            data = getJSONDataFromStatic("/static/json/testTypes.json");
            LOGGER.info(data);

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    private Map<String, String> getJSONDataFromStatic(String staticPathToJSON) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(Files.readAllBytes(Paths.get(servletContext.getRealPath(staticPathToJSON))),
                new TypeReference<Map<String, String>>(){});
    }
}
