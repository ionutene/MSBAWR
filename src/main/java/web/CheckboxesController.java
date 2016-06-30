package web;

import data.AdvancedSearchCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.CheckboxesViewService;

import java.util.Map;

@RestController
public class CheckboxesController {

    private static final Logger LOGGER = LogManager.getLogger(CheckboxesController.class);

    @Autowired
    CheckboxesViewService checkboxesViewService;

    @RequestMapping(value = "/getCheckboxes")
    public Map<String, String> getCheckboxesViaAjax(@RequestBody AdvancedSearchCriteria search) {
        LOGGER.info(search);
        checkboxesViewService.setSelectedOptions(search);
        checkboxesViewService.getFilteredTests();
        LOGGER.info(checkboxesViewService.getHTMLDump());
        return null;
    }
}
