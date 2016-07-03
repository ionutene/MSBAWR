package web;

import data.AdvancedSearchCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import service.CheckboxesTreeViewService;
import service.CheckboxesViewService;

@RestController
public class CheckboxesController {

    private static final Logger LOGGER = LogManager.getLogger(CheckboxesController.class);

    @Autowired
    CheckboxesViewService checkboxesViewService;

    @Autowired
    CheckboxesTreeViewService checkboxesTreeViewService;

    @RequestMapping(value = "/getCheckboxes", produces = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody String getCheckboxesViaAjax(@RequestBody AdvancedSearchCriteria search) {
        LOGGER.info(search);
        checkboxesViewService.setSelectedOptions(search);
        checkboxesViewService.getFilteredTests();

        checkboxesTreeViewService.setSelectedOptions(search);
        checkboxesTreeViewService.getFilteredTests();
        checkboxesTreeViewService.rebuildFullPath();
        checkboxesTreeViewService.createTreeFromPackages();

        return checkboxesViewService.getHTMLDump();
    }
}
