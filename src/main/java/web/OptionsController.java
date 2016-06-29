package web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.AjaxResponseBody;
import data.SearchCriteria;
import data.request.WebTestsJSONData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.OptionsFilterService;

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
    public Map<String, String> getSearchResultViaAjax(@RequestBody SearchCriteria search) {
        AjaxResponseBody ajaxResponseBody = new AjaxResponseBody();
        Map<String, String> data = new HashMap<>();
        WebTestsJSONData webTestsJSONData;
        try {
            LOGGER.info(search);

            // convert JSON string to Map
            data = getJSONDataFromStatic("/static/json/envCorrelation.json");
            LOGGER.info(data);
            data = getJSONDataFromStatic("/static/json/testTypes.json");
            LOGGER.info(data);
            webTestsJSONData = getJSONRequestDataFromStatic("/static/json/webTestsData.json");
            LOGGER.info(webTestsJSONData);

            OptionsFilterService service = new OptionsFilterService(
                    getJSONDataFromStatic("/static/json/envCorrelation.json"),
                    webTestsJSONData, search);

            data = service.getJSONOptionsFilter();

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        return data;
    }

    private Map<String, String> getJSONDataFromStatic(String staticPathToJSON) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(Files.readAllBytes(Paths.get(servletContext.getRealPath(staticPathToJSON))),
                new TypeReference<Map<String, String>>(){});
    }

    private WebTestsJSONData getJSONRequestDataFromStatic(String staticPathToJSON) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        return mapper.readValue(Files.readAllBytes(Paths.get(servletContext.getRealPath(staticPathToJSON))),
                WebTestsJSONData.class);
    }
}
