package service;

import data.AdvancedSearchCriteria;
import data.EnvironmentsCapsule;
import data.request.WebTestsJSONData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckboxesViewService {

    private static final Logger LOGGER = LogManager.getLogger(CheckboxesViewService.class);

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

    private EnvironmentsCapsule capsule;
    private WebTestsJSONData tests;
    private AdvancedSearchCriteria selectedOptions;
    private List<String> finalTests;

    @Autowired
    public CheckboxesViewService(EnvironmentsCapsule capsule, WebTestsJSONData tests) {
        this.capsule = capsule;
        this.tests = tests;
    }

    public void setSelectedOptions(AdvancedSearchCriteria selectedOptions) {
        this.selectedOptions = selectedOptions;
    }

    public void getFilteredTests() {
//      Remove the Bean encapsulation
        Map<String, String> environmentSelect = capsule.getEnvironment();
//      LinkedHashMap to preserve insertion order!!!
        Map<String, String> response = new LinkedHashMap<>();
        String environment = selectedOptions.getEnv();
        String testType = selectedOptions.getType();
        String filter = selectedOptions.getFilter();
        List<String> environmentTests = new ArrayList<>();
        List<String> environmentTestsWithoutBasePackage = new ArrayList<>();
        List<String> testTypeTests = new ArrayList<>();
        List<String> filteredTests = new ArrayList<>();
//        List<String> finalTests = new ArrayList<>();

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

//      Remove basePackage prefix from classNames
        environmentTestsWithoutBasePackage.addAll(environmentTests.stream()
                .map(temp -> temp.substring(prefix.length()))
                .collect(Collectors.toList()));
//      ALL & ALL case
        if (testType.equals(allValue)) {
            if (filter.equals(allValue)) {
                testTypeTests.addAll(environmentTestsWithoutBasePackage);
            }
//      If testType != ALL
        } else {
            testTypeTests.addAll(environmentTestsWithoutBasePackage.stream()
                    .filter(temp -> temp.startsWith(testType))
                    .map(temp -> temp.substring((testType.length() + 1)))
                    .collect(Collectors.toList()));
//          If filter != ALL
            if (!filter.equals(allValue)) {
                filteredTests.addAll(testTypeTests.stream()
                        .filter(temp -> temp.startsWith(filter))
                        .map(temp -> temp.substring((filter.length() + 1)))
                        .collect(Collectors.toList()));
                testTypeTests = new ArrayList<>();
                testTypeTests.addAll(filteredTests);
            }
        }
        finalTests = new ArrayList<>();
        finalTests.addAll(testTypeTests);
        LOGGER.info(finalTests);
    }

    public String getHTMLDump() {
        StringBuilder stringBuilder = new StringBuilder();
        Set<String> packageFilter;

        while (hasSubpackages(finalTests)) {
            packageFilter = getRootPackageNames(finalTests);
            stringBuilder.append("PackageNames: " + packageFilter).append("\n");
            List<String> intermediate = new ArrayList<>();

            for (String packageName : packageFilter) {
                List<String> temp = getTestsForPackage(finalTests, packageName);
                stringBuilder.append("Package: " + packageName + " has the following tests: " + temp).append("\n");
                if (hasSubpackages(temp)) {
                    intermediate.addAll(temp);
                }
            }

            finalTests = new ArrayList<>();
            finalTests.addAll(intermediate);
        }

        return stringBuilder.toString();
    }

    private Set<String> getRootPackageNames(List<String> tests) {
        Set<String> collect = new HashSet<>();
        collect.addAll(tests.stream()
                .map(temp -> temp.substring(0, temp.indexOf(".")))
                .collect(Collectors.toList()));
        return collect;
    }

    private boolean hasSubpackages(List<String> tests) {
        boolean hasSubpackages = false;
        for (String temp : tests) {
            if (temp.contains(".")) {
                hasSubpackages = true;
                break;
            }
        }
        return hasSubpackages;
    }

    private List<String> getTestsForPackage(List<String> tests, String packageName) {
        List<String> collect = new ArrayList<>();
        collect.addAll(tests.stream()
                .filter(temp -> temp.startsWith(packageName))
                .map(temp -> temp.substring((packageName.length() + 1)))
                .collect(Collectors.toList()));
        return collect;
    }
}
