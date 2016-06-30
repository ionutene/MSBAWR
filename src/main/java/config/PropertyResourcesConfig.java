package config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.EnvironmentsCapsule;
import data.request.WebTestsJSONData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Configuration
@PropertySource("classpath:view.properties")
public class PropertyResourcesConfig {

    @Autowired
    ServletContext servletContext;

    @Value("${environments}")
    String environments;

    @Value("${webTests}")
    String webTests;

    @Bean
    public EnvironmentsCapsule getEnvironmentsCapsule() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        EnvironmentsCapsule capsule = new EnvironmentsCapsule();
        capsule.setEnvironment(mapper.readValue(Files.readAllBytes(Paths.get(servletContext.getRealPath(environments))),
                new TypeReference<Map<String, String>>(){}));
        return capsule;
    }

    @Bean
    public WebTestsJSONData getWebTestsJSONData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        return mapper.readValue(Files.readAllBytes(Paths.get(servletContext.getRealPath(webTests))),
                WebTestsJSONData.class);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
