package config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.EnvironmentsCapsule;
import data.request.WebTestsJSONData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Configuration
@EnableWebMvc
@ComponentScan("web")
@PropertySource("classpath:view.properties")
public class WebConfig extends WebMvcConfigurerAdapter {

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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("/static/");
    }

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver
                = new InternalResourceViewResolver();
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
