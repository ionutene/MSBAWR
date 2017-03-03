package service.impl;

import data.AdvancedSearchCriteria;
import data.EnvironmentsCapsule;
import data.request.WebTestsJSONData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.CheckboxesViewService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckboxesViewServiceImpl implements CheckboxesViewService {

    private static final Logger LOGGER = LogManager.getLogger(CheckboxesViewServiceImpl.class);

    @Value("${package.prefix}")
    String prefix;

    @Value("${package.prefix.all}")
    String prefixAll;

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
    private String rootPrefix;

    //  HTML non-sens-erie
    private String htmlCheckboxInit = "<input type=\"checkbox\" id=\"";
    private String htmlCheckboxIdValue = "\" value=\"";
    private String htmlCheckboxValueLabel = "\"><label for=\"";
    private String htmlCheckboxLabelText = "\">";
    private String htmlCheckboxFin = "</label>";

    @Autowired
    public CheckboxesViewServiceImpl(EnvironmentsCapsule capsule, WebTestsJSONData tests) {
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

//          Remove basePackage prefix from classNames
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
        String parentName = "";

//      Start the Unordered List
        stringBuilder.append("<ul class=\"checkboxes\">").append("\n");

//      Type = ALL && Filter = ALL
        if (selectedOptions.getType().equals(allValue) && selectedOptions.getFilter().equals(allValue)) {
            basePackage = allKey;
            parentName = "tests";
            rootPrefix = prefixAll + "." + parentName;
        }

//      Type != ALL && Filter = ALL
        if (!selectedOptions.getType().equals(allValue) && selectedOptions.getFilter().equals(allValue)) {
            basePackage = selectedOptions.getType();
            parentName = selectedOptions.getType().toLowerCase();
//            .substring(0, (prefix.length() - 1))
            rootPrefix = prefix + parentName;
        }

//      Type != ALL && Filter != ALL
        if (!selectedOptions.getType().equals(allValue) && !selectedOptions.getFilter().equals(allValue)) {
            basePackage = selectedOptions.getFilter();
            parentName = selectedOptions.getFilter();
            rootPrefix = prefix + selectedOptions.getType().toLowerCase() + "." + parentName;
        }

        stringBuilder.append("<li>")
                .append(htmlCheckboxInit)
                .append(rootPrefix)
                .append(htmlCheckboxIdValue)
                .append(rootPrefix)
                .append(htmlCheckboxValueLabel)
                .append(rootPrefix)
                .append(htmlCheckboxLabelText)
                .append(basePackage).append("<span class='badge'> ").append(finalTests.size()).append("</span>")
                .append(htmlCheckboxFin)
                .append("\n");

//      Type = ALL && Filter = ALL
/*        if (selectedOptions.getType().equals(allValue) && selectedOptions.getFilter().equals(allValue)) {
            rootPrefix = prefixAll;
        }*/
//+ "." + parentName
        parsePackages(finalTests, rootPrefix);

//      Close the top most Unordered List
        stringBuilder.append("</ul>").append("\n");

        return stringBuilder.toString();
    }

    private void parsePackages(List<String> tests, String parent) {
        if (hasSubpackages(tests)) {
            stringBuilder.append("<ul>").append("\n");
            Set<String> uniquePackages = getRootPackageNames(tests);
            for (String packageName : uniquePackages) {
                List<String> newTests = getTestsForPackage(tests, packageName);
                stringBuilder.append("<li>")
                        .append(htmlCheckboxInit)
                        .append(parent).append(".").append(packageName)
                        .append(htmlCheckboxIdValue)
                        .append(parent).append(".").append(packageName)
                        .append(htmlCheckboxValueLabel)
                        .append(parent).append(".").append(packageName)
                        .append(htmlCheckboxLabelText);


                if (newTests.size() > 0) {
                    stringBuilder.append(packageName).append("<span class='badge'> ").append(newTests.size()).append("</span>")
                            .append(htmlCheckboxFin)
                            .append("\n");
                    parsePackages(newTests, parent + "." + packageName);
                } else {
                    stringBuilder.append(packageName)
                            .append(htmlCheckboxFin)
                            .append("\n");
                    stringBuilder.append("</li>").append("\n");
                }
            }
            stringBuilder.append("</ul>").append("\n");
            stringBuilder.append("</li>").append("\n");
        } else {
            stringBuilder.append("<ul>").append("\n");
            for (String testName : tests) {
                stringBuilder.append("<li>")
                        .append(htmlCheckboxInit)
                        .append(parent).append(".").append(testName)
                        .append(htmlCheckboxIdValue)
                        .append(parent).append(".").append(testName)
                        .append(htmlCheckboxValueLabel)
                        .append(parent).append(".").append(testName)
                        .append(htmlCheckboxLabelText)
                        .append(testName)
                        .append(htmlCheckboxFin)
                        .append("</li>").append("\n");
            }
            stringBuilder.append("</ul>").append("\n");
            stringBuilder.append("</li>").append("\n");
        }
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
        while (iterator.hasNext()) {
            String className = iterator.next();
            if (!className.contains(".")) {
                iterator.remove();
            }
        }
        return tests;
    }

    private Set<String> getRootPackageNames(List<String> tests) {
        Set<String> collect = new HashSet<>();
        if (hasMixedClassesWithPackages(tests) > 0) {
            collect.addAll(getSingleClassesFromPackages(tests));
            removeClassesFromPackages(tests);
        }
        collect.addAll(tests.stream()
                .map(temp -> temp.substring(0, temp.indexOf(".")))
                .collect(Collectors.toList()));
        return collect;
    }

    private List<String> getSingleClassesFromPackages(List<String> tests) {
        List<String> singleLadies = new LinkedList<>();
        Iterator<String> iterator = tests.iterator();
        while (iterator.hasNext()) {
            String className = iterator.next();
            if (!className.contains(".")) {
                singleLadies.add(className);
                iterator.remove();
            }
        }
        return singleLadies;
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
