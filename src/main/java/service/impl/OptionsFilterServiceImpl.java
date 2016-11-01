package service.impl;

import data.AdvancedSearchCriteria;
import data.EnvironmentsCapsule;
import data.request.WebTestsJSONData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.OptionsFilterService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OptionsFilterServiceImpl implements OptionsFilterService {

    private static final Logger LOGGER = LogManager.getLogger(OptionsFilterServiceImpl.class);

    @Value("${package.prefix}")
    String prefix;

    @Value("${framework.name.all.key}")
    String allKey;
    @Value("${framework.name.all.value}")
    String allValue;
    @Value("${framework.name.none.key}")
    String noneKey;
    @Value("${framework.name.none.value}")
    String noneValue;
    @Value("${framework.name.not.key}")
    String notAvailableKey;
    @Value("${framework.name.not.value}")
    String notAvailableValue;

    private EnvironmentsCapsule capsule;
    private WebTestsJSONData tests;
    private AdvancedSearchCriteria advancedSearchCriteria;

    @Autowired
    public OptionsFilterServiceImpl(EnvironmentsCapsule capsule, WebTestsJSONData tests) {
        this.capsule = capsule;
        this.tests = tests;
    }

    public void setSelectedOptions(AdvancedSearchCriteria advancedSearchCriteria) {
        this.advancedSearchCriteria = advancedSearchCriteria;
    }

    public Map<String, String> getJSONOptionsFilter() {
//      Remove the Bean encapsulation
        Map<String, String> environmentSelect = capsule.getEnvironment();
//      LinkedHashMap to preserve insertion order!!!
        Map<String, String> response = new LinkedHashMap<>();
        String environment = advancedSearchCriteria.getEnv();
        String testType = advancedSearchCriteria.getType();
        List<String> environmentTests = new ArrayList<>();

        for (Map.Entry<String, String> entry : environmentSelect.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(environment)) {
                for (Map.Entry<String, List<String>> testEntry : tests.getTests().getTests().entrySet()) {
                    if (testEntry.getKey().equalsIgnoreCase(entry.getValue())) {
                        environmentTests.addAll(testEntry.getValue());
                        break;
                    }
                }
                break;
            }
        }

        if (environmentTests.size() > 0) {
            List<String> environmentTestsWithoutBasePackage = new ArrayList<>();
            Set<String> basePackage = new HashSet<>();
            List<String> preFinal = new ArrayList<>();

//          Remove basePackage prefix from classNames
            environmentTestsWithoutBasePackage.addAll(environmentTests.stream()
                    .map(temp -> temp.substring(prefix.length()))
                    .collect(Collectors.toList()));
            LOGGER.info(environmentTestsWithoutBasePackage);

//          Check if the testType passed == ALL
            if (testType.equals(allValue)) {
                preFinal.addAll(environmentTestsWithoutBasePackage);
                if (preFinal.size() > 0) {
                    response.put(noneKey, noneValue);
                    response.put(allKey, allValue);
                } else {
                    response.put(notAvailableKey, notAvailableValue);
                }
            } else {
//              Add all tests that have the packageName == testType
                preFinal.addAll(environmentTestsWithoutBasePackage.stream()
                        .filter(temp -> temp.startsWith(testType))
                        .map(temp -> temp.substring((testType.length() + 1)))
                        .collect(Collectors.toList()));
//              If tests were found
                if (preFinal.size() > 0) {
//                  Find out if they still have subpackages
                    boolean hasSubpackages = false;
                    for (String temp : preFinal) {
                        if (temp.contains(".")) {
                            hasSubpackages = true;
                            break;
                        }
                    }
//                  If they have subPackages find them and add them to the OptionsFilter
                    if (hasSubpackages) {
                        basePackage.addAll(preFinal.stream()
                                .map(temp -> temp.substring(0, temp.indexOf(".")))
                                .collect(Collectors.toList()));
                        LOGGER.info(basePackage);
                        response.put(noneKey, noneValue);
                        response.put(allKey, allValue);
                        for (String temp : basePackage) {
                            response.put(temp, temp);
                        }
//                  If there aren't any subpackages just tests, put a simple filter to show None or All
                    } else {
                        response.put(noneKey, noneValue);
                        response.put(allKey, allValue);
                    }
//              If there are no tests that startWith testType, disable the filter
                } else {
                    response.put(notAvailableKey, notAvailableValue);
                }
            }
            LOGGER.info(preFinal);
//      If the environmentTests is empty, disable the filter
        } else {
            response.put(notAvailableKey, notAvailableValue);
        }
        LOGGER.info(response);
        return response;
    }

}
