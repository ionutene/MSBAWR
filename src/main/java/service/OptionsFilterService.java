package service;

import data.SearchCriteria;
import data.request.WebTestsJSONData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class OptionsFilterService {

    private static final Logger LOGGER = LogManager.getLogger(OptionsFilterService.class);

    private Properties properties;
    private Map<String, String> environmentSelect;
    private WebTestsJSONData tests;
    private SearchCriteria selectedOptions;

    public OptionsFilterService(Map<String, String> environmentSelect, WebTestsJSONData tests,
                                SearchCriteria selectedOptions) throws IOException {
        this.environmentSelect = environmentSelect;
        this.tests = tests;
        this.selectedOptions = selectedOptions;
        this.properties = getEndpointProperties();
    }

    public Map<String, String> getJSONOptionsFilter() {
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
                    }
                }
            }
        }

        String prefix = properties.getProperty("package.prefix");

        if (testosterone.size() > 0) {
            List<String> intermediate = new ArrayList<>();
            Set<String> basePackage = new HashSet<>();
            List<String> preFinal = new ArrayList<>();
            for (String temp : testosterone) {
                intermediate.add(temp.substring(prefix.length()));
            }
            LOGGER.info(intermediate);
            for (String temp : intermediate) {
                basePackage.add(temp.substring(0, temp.indexOf(".")));
            }
            LOGGER.info(basePackage);
            for (String temp : intermediate) {
                if (temp.startsWith(testType)) {
                    preFinal.add(temp.substring((testType.length() + 1)));
                }
            }
            LOGGER.info(preFinal);
            boolean hasSubpackages = false;
            for (String temp : preFinal) {
                if (temp.contains(".")) {
                    hasSubpackages = true;
                    break;
                }
            }
            if (hasSubpackages) {
                basePackage = new HashSet<>();
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
                response.put("Not Available!", "NotAvailable");
            }

            LOGGER.info(response);
        }

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
