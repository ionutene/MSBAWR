package service.impl;

import data.AdvancedSearchCriteria;
import data.EnvironmentsCapsule;
import data.tree.Node;
import data.tree.Tree;
import data.request.WebTestsJSONData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckboxesTreeViewService {

    private static final Logger LOGGER = LogManager.getLogger(CheckboxesTreeViewService.class);

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
    private Tree<String> stringTree;

    //  HTML non-sens-erie
    private String htmlCheckboxInit = "<input type=\"checkbox\" id=\"";
    private String htmlCheckboxIdValue = "\" value=\"";
    private String htmlCheckboxValueLabel = "\"><label for=\"";
    private String htmlCheckboxLabelText = "\">";
    private String htmlCheckboxFin = "</label>";

    @Autowired
    public CheckboxesTreeViewService(EnvironmentsCapsule capsule, WebTestsJSONData tests) {
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
        LOGGER.info(finalTests);
    }

    public void rebuildFullPath() {
        String testType = selectedOptions.getType();
        String filter = selectedOptions.getFilter();
        List<String> fullPathTests = new ArrayList<>();
        List<String> almostFullPathTests = new ArrayList<>();
        //      ALL & ALL case
        if (testType.equals(allValue)) {
            if (filter.equals(allValue)) {
                almostFullPathTests.addAll(finalTests.stream().map(temp -> prefix + temp).collect(Collectors.toList()));
            }
//      If testType != ALL
        } else {
            almostFullPathTests.addAll(finalTests.stream().map(temp -> prefix + testType + ".").collect(Collectors.toList()));
            //          If filter != ALL
            if (!filter.equals(allValue)) {
                for (int i = 0; i < finalTests.size(); i++) {
                    fullPathTests.add(almostFullPathTests.get(i) + filter + "." + finalTests.get(i));
                }
            } else {
                for (int i = 0; i < finalTests.size(); i++) {
                    fullPathTests.add(almostFullPathTests.get(i) + finalTests.get(i));
                }
            }
        }
        finalTests = new ArrayList<>();
        finalTests.addAll(fullPathTests);
        LOGGER.info(finalTests);
    }

    public void createTreeFromPackages() {
        stringTree = new Tree<>();
        Node<String> rootNode = new Node<>("net", null);
        stringTree.setRoot(rootNode);
        for (String packageName : getRootPackageNames(finalTests)) {
            List<String> newTests = getTestsForPackage(finalTests, packageName);
            parsePackages(newTests, rootNode);
            LOGGER.info(stringTree.toString());
        }

    }

    public static void main(String args[]) {
        List<String> someTests = new LinkedList<>();
        someTests.add("Writer.WriterRandomStringTest");
        someTests.add("Writer.DefaultWriter.DefaultWriterOtherWritersNegativeTest");
        someTests.add("Writer.DefaultWriter.DefaultWriterPositiveTest");
        someTests.add("Writer.MD5FileWriter.MD5FileWriterNegativeTest");
        someTests.add("Writer.MD5FileWriter.MD5FileWriterOtherWritersNegativeTest");
        someTests.add("Writer.MD5FileWriter.MD5FileWriterPositiveTest");
        someTests.add("Writer.RealEmergencyWriter.RealEmergencyWriterAdditionTest");
        someTests.add("Writer.RealEmergencyWriter.RealEmergencyWriterNegativeTest");
        someTests.add("Writer.RealEmergencyWriter.RealEmergencyWriterOtherWritersNegativeTest");
        someTests.add("Writer.RealEmergencyWriter.RealEmergencyWriterPositiveTest");
        someTests.add("Writer.RealEmergencyWriter.RealEmergencyWriterNegativeTest");
        someTests.add("Writer.StagingWriter.StagingWriterOtherWritersNegativeTest");

        Tree<String> lelTree = new Tree<>();
        Node<String> rootNode = new Node<>("Writer", null);
        lelTree.setRoot(rootNode);
        for (String packageName : getRootPackageNames(someTests)) {
            List<String> newTests = getTestsForPackage(someTests, packageName);

            parsePackages(newTests, rootNode);
            LOGGER.info(lelTree.toString());
        }


    }

    public static void parsePackages(List<String> tests, Node<String> parent) {
        if (hasSubpackages(tests)) {
            Set<String> uniqueChildren = getRootPackageNames(tests);
            List<Node<String>> children = uniqueChildren.stream().map(temp -> new Node<>(temp, parent)).collect(Collectors.toCollection(LinkedList::new));
            parent.setChildren(children);
            for (String packageName : uniqueChildren) {
                Node<String> uniqueChild = getChildFromParent(parent, packageName);
                List<String> newTests = getTestsForPackage(tests, packageName);
                List<Node<String>> subChildren = newTests.stream().map(temp -> new Node<>(temp, uniqueChild)).collect(Collectors.toCollection(LinkedList::new));
                uniqueChild.setChildren(subChildren);
                if (newTests.size() > 0) {
                    parsePackages(newTests, uniqueChild);
                }
            }
        }
    }

    public static Node<String> getChildFromParent(Node<String> parent, String childName) {
        Node<String> child = new Node<>();
        List<Node<String>> children = parent.getChildren();
        for (Node<String> xy : children) {
            if (xy.getData().equals(childName)) {
                child = xy;
                break;
            }
        }
        return child;
    }

    private static boolean hasSubpackages(List<String> tests) {
        boolean hasSubpackages = false;
        for (String temp : tests) {
            if (temp.contains(".")) {
                hasSubpackages = true;
                break;
            }
        }
        return hasSubpackages;
    }

    private static int hasMixedClassesWithPackages(List<String> tests) {
        int counter = 0;
        for (String className : tests) {
            if (!className.contains(".")) counter++;
        }
        return counter;
    }

    private static List<String> removeClassesFromPackages(List<String> tests) {
        Iterator<String> iterator = tests.iterator();
        while (iterator.hasNext()) {
            String className = iterator.next();
            if (!className.contains(".")) {
                iterator.remove();
            }
        }
        return tests;
    }

    private static List<String> getSingleClassesFromPackages(List<String> tests) {
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

    private static Set<String> getRootPackageNames(List<String> tests) {
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

    private static List<String> getTestsForPackage(List<String> tests, String packageName) {
        List<String> collect = new ArrayList<>();
        collect.addAll(tests.stream()
                .filter(temp -> temp.startsWith(packageName))
                .map(temp -> temp.substring((packageName.length() + 1)))
                .collect(Collectors.toList()));
        return collect;
    }
}
