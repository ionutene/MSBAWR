package web;

import data.SearchCriteria;
import data.request.WebTestsJSONData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.OptionsFilterService;

import java.util.HashMap;
import java.util.Map;

@RestController
public class OptionsController {

    private static final Logger LOGGER = LogManager.getLogger(OptionsController.class);

/*    @Autowired
    ServletContext servletContext;*/

    @Value("${environments}")
    String environments;

    @Autowired
    OptionsFilterService optionsFilterService;

    @RequestMapping(value = "/getOptions")
    public Map<String, String> getSearchResultViaAjax(@RequestBody SearchCriteria search) {
        Map<String, String> data = new HashMap<>();
        WebTestsJSONData webTestsJSONData;
//        try {
            LOGGER.info(search);
            LOGGER.info("From properties: " + environments);

            // convert JSON string to Map
/*            data = getJSONDataFromStatic("/static/json/envCorrelation.json");
            LOGGER.info(data);
            data = getJSONDataFromStatic("/static/json/testTypes.json");*/
            LOGGER.info(data);
//            webTestsJSONData = getJSONRequestDataFromStatic("/static/json/webTestsData.json");
//            LOGGER.info(webTestsJSONData);

/*            OptionsFilterService service = new OptionsFilterService(
                    getJSONDataFromStatic("/static/json/envCorrelation.json"),
                    webTestsJSONData, search);*/

            optionsFilterService.setSelectedOptions(search);
            data = optionsFilterService.getJSONOptionsFilter();

/*        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }*/

        return data;
    }

}
