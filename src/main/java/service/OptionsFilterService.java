package service;

import data.EnvironmentsCapsule;
import data.SearchCriteria;
import data.request.WebTestsJSONData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class OptionsFilterService {

    private static final Logger LOGGER = LogManager.getLogger(OptionsFilterService.class);

    @Value("${package.prefix}")
    private String prefix;
    @Value("${framework.name.all}")
    String keywordALL;

    private Properties properties;
    private Map<String, String> environmentSelect;
    private EnvironmentsCapsule capsule;
    private WebTestsJSONData tests;
    private SearchCriteria selectedOptions;

    @Autowired
    public OptionsFilterService(EnvironmentsCapsule capsule, WebTestsJSONData tests) throws IOException {
        this.capsule = capsule;
        this.tests = tests;
//        this.properties = getEndpointProperties();
    }

    public void setSelectedOptions(SearchCriteria selectedOptions) {
        this.selectedOptions = selectedOptions;
    }

    public Map<String, String> getJSONOptionsFilter() {
        environmentSelect = capsule.getEnvironment();
//      LinkedHashMap to preserve insertion order!!!
        Map<String, String> response = new LinkedHashMap<>();
        String environment = selectedOptions.getEnv();
        String testType = selectedOptions.getType();
        List<String> testosterone = new ArrayList<>();

        for (Map.Entry<String, String> entry : environmentSelect.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(environment)) {
                for (Map.Entry<String, List<String>> testEntry : tests.getTests().getTests().entrySet()) {
                    if (testEntry.getKey().equalsIgnoreCase(entry.getValue())) {
                        testosterone.addAll(testEntry.getValue());
                        break;
                    }
                }
                break;
            }
        }

        if (testosterone.size() > 0) {
            List<String> intermediate = new ArrayList<>();
            Set<String> basePackage = new HashSet<>();
            List<String> preFinal = new ArrayList<>();
            for (String temp : testosterone) {
                intermediate.add(temp.substring(prefix.length()));
            }
            LOGGER.info(intermediate);
            if (testType.equals(keywordALL)) {
                preFinal.addAll(intermediate);
                if (preFinal.size() > 0) {
                    response.put("None", "NONE");
                    response.put("All", "ALL");
                } else {
                    response.put("Not Available!", "NotAvailable");
                }
            } else {
                for (String temp : intermediate) {
                    if (temp.startsWith(testType)) {
                        preFinal.add(temp.substring((testType.length() + 1)));
                    }
                }
                if (preFinal.size() > 0) {
                    boolean hasSubpackages = false;
                    for (String temp : preFinal) {
                        if (temp.contains(".")) {
                            hasSubpackages = true;
                            break;
                        }
                    }
                    if (hasSubpackages) {
                        for (String temp : preFinal) {
                            basePackage.add(temp.substring(0, temp.indexOf(".")));
                        }
                        LOGGER.info(basePackage);
                        response.put("None", "NONE");
                        response.put("All", "ALL");
                        for (String temp : basePackage) {
                            response.put(temp, temp);
                        }
                    } else {
                        response.put("None", "NONE");
                        response.put("All", "ALL");
                    }
                } else {
                    response.put("Not Available!", "NotAvailable");
                }
            }
            LOGGER.info(preFinal);

        } else {
            response.put("Not Available!", "NotAvailable");
        }
        LOGGER.info(response);
        return response;
    }

    private static Properties getEndpointProperties() throws IOException {
        Properties prop = new Properties();

//      TODO document it
        try (InputStream input = OptionsFilterService.class.getResourceAsStream("/view.properties")) {
            // load a properties file
            prop.load(input);
        }

        return prop;
    }


}
