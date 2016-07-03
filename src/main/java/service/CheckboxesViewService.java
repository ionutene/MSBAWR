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
    private StringBuilder stringBuilder;
    private String basePackage;
    private String packageRootUntilNow;
    private String packageRootForSingleClasses;
    private String save;
    private String newKindOfSave;

//  HTML non-sens-erie
    private String htmlCheckboxInit = "<input type=\"checkbox\" id=\"";
    private String htmlCheckboxIdValue = "\" value=\"";
    private String htmlCheckboxValueLabel = "\"><label for=\"";
    private String htmlCheckboxLabelText = "\">";
    private String htmlCheckboxFin = "</label>";

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

        String environment = selectedOptions.getEnv();
        String testType = selectedOptions.getType();
        String filter = selectedOptions.getFilter();
        List<String> environmentTests = new ArrayList<>();
        List<String> environmentTestsWithoutBasePackage = new ArrayList<>();
        List<String> testTypeTests = new ArrayList<>();
        List<String> filteredTests = new ArrayList<>();

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
//        LOGGER.info(finalTests);
    }

    public String getHTMLDump() {
        stringBuilder = new StringBuilder();

//      Type = ALL && Filter = ALL
        if (selectedOptions.getType().equals(allValue) && selectedOptions.getFilter().equals(allValue)) {
            basePackage = allKey;
        }

        if (!selectedOptions.getType().equals(allValue) && selectedOptions.getFilter().equals(allValue)) {
            basePackage = selectedOptions.getType();
        }

        if (!selectedOptions.getType().equals(allValue) && !selectedOptions.getFilter().equals(allValue)) {
            basePackage = selectedOptions.getFilter();
        }

        stringBuilder.append("<ul>").append("\n");
        stringBuilder.append("<li>")
                .append(htmlCheckboxInit)
                .append(prefix).append(basePackage.toLowerCase())
                .append(htmlCheckboxIdValue)
                .append(prefix).append(basePackage.toLowerCase())
                .append(htmlCheckboxValueLabel)
                .append(prefix).append(basePackage.toLowerCase())
                .append(htmlCheckboxLabelText)
                .append(basePackage).append(" ( ").append(finalTests.size()).append(" tests )")
                .append(htmlCheckboxFin)
                .append("\n");

        if (!hasSubpackages(finalTests)) {
            stringBuilder.append("<ul>").append("\n");
            for (String test: finalTests) {
                stringBuilder.append("<li>")
                        .append(htmlCheckboxInit)
                        .append(prefix).append(basePackage.toLowerCase()).append(".").append(test)
                        .append(htmlCheckboxIdValue)
                        .append(prefix).append(basePackage.toLowerCase()).append(".").append(test)
                        .append(htmlCheckboxValueLabel)
                        .append(prefix).append(basePackage.toLowerCase()).append(".").append(test)
                        .append(htmlCheckboxLabelText)
                        .append(test)
                        .append(htmlCheckboxFin)
                        .append("</li>").append("\n");
            }
            stringBuilder.append("</ul>").append("\n");
            stringBuilder.append("</li>").append("\n");
        } else {
            if (basePackage.equals(allKey)) {
                packageRootUntilNow = prefix;
            } else {
                packageRootUntilNow = prefix + basePackage + ".";
            }
            packageRootForSingleClasses = packageRootUntilNow;
            save = packageRootUntilNow;
            newKindOfSave = packageRootUntilNow;
            parsePackages(finalTests);
        }

        stringBuilder.append("</ul>").append("\n");

        return stringBuilder.toString();
    }

    private void parsePackages(List<String> tests) {
        newKindOfSave = packageRootForSingleClasses;
        if (hasSubpackages(tests)) {
            stringBuilder.append("<ul>").append("\n");
            for (String packageName : getRootPackageNames(tests)) {
                List<String> newTests = getTestsForPackage(tests, packageName);
                stringBuilder.append("<li>")
                        .append(htmlCheckboxInit)
                        .append(packageRootForSingleClasses).append(packageName)
                        .append(htmlCheckboxIdValue)
                        .append(packageRootForSingleClasses).append(packageName)
                        .append(htmlCheckboxValueLabel)
                        .append(packageRootForSingleClasses).append(packageName)
                        .append(htmlCheckboxLabelText)
                        .append(packageName).append(" ( ").append(newTests.size()).append(" tests )")
                        .append(htmlCheckboxFin)
                        .append("\n");
                packageRootForSingleClasses += packageName + ".";
                if (hasMixedClassesWithPackages(newTests) > 0) {
                    removeClassesFromPackages(newTests);
                }
                if (newTests.size() > 0) {
                    save = packageRootForSingleClasses;
                    parsePackages(newTests);
                } else {
                    packageRootForSingleClasses = save;
                    stringBuilder.append("</li>").append("\n");
                }
            }
            packageRootForSingleClasses = save;
            stringBuilder.append("</ul>").append("\n");
            stringBuilder.append("</li>").append("\n");
        }
        packageRootForSingleClasses = newKindOfSave;
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

    private int hasMixedClassesWithPackages(List<String> tests) {
        int counter = 0;
        for (String className : tests) {
            if (!className.contains(".")) counter++;
        }
        return counter;
    }

    private List<String> removeClassesFromPackages(List<String> tests) {
        Iterator<String> iterator = tests.iterator();
        stringBuilder.append("<ul>").append("\n");
        while (iterator.hasNext()) {
            String className = iterator.next();
            if (!className.contains(".")) {
                stringBuilder.append("<li>")
                        .append(htmlCheckboxInit)
                        .append(packageRootForSingleClasses).append(className)
                        .append(htmlCheckboxIdValue)
                        .append(packageRootForSingleClasses).append(className)
                        .append(htmlCheckboxValueLabel)
                        .append(packageRootForSingleClasses).append(className)
                        .append(htmlCheckboxLabelText)
                        .append(className)
                        .append(htmlCheckboxFin)
                        .append("</li>").append("\n");
                iterator.remove();
            }
        }
        stringBuilder.append("</ul>").append("\n");
        return tests;
    }

    private Set<String> getRootPackageNames(List<String> tests) {
        Set<String> collect = new HashSet<>();
        collect.addAll(tests.stream()
                .map(temp -> temp.substring(0, temp.indexOf(".")))
                .collect(Collectors.toList()));
        return collect;
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
