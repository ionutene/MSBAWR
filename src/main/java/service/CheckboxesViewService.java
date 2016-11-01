package service;

import data.AdvancedSearchCriteria;

public interface CheckboxesViewService {
    void setSelectedOptions(AdvancedSearchCriteria selectedOptions);
    void getFilteredTests();
    String getHTMLDump();
}
