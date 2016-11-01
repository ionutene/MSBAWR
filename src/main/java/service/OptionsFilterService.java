package service;

import data.AdvancedSearchCriteria;

import java.util.Map;

public interface OptionsFilterService {
    void setSelectedOptions(AdvancedSearchCriteria advancedSearchCriteria);
    Map<String, String> getJSONOptionsFilter();
}
