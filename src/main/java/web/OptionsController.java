package web;

import data.AdvancedSearchCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.OptionsFilterService;

import java.util.Map;

@RestController
public class OptionsController {

    private static final Logger LOGGER = LogManager.getLogger(OptionsController.class);

    @Autowired
    OptionsFilterService optionsFilterService;

    @RequestMapping(value = "/getOptions")
    public Map<String, String> getSearchResultViaAjax(@RequestBody AdvancedSearchCriteria advancedSearchCriteria) {
        LOGGER.info(advancedSearchCriteria);
        optionsFilterService.setSelectedOptions(advancedSearchCriteria);
        return optionsFilterService.getJSONOptionsFilter();
    }

}
