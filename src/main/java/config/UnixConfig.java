package config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySources({
        @PropertySource("classpath:connection.properties"),
        @PropertySource("classpath:server.properties")
})
public class UnixConfig {

    @Value("${jenkins.host}")
    private String jenkinsHost;
    @Value("${jenkins.port}")
    private String jenkinsPort;
    @Value("${jenkins.username}")
    private String jenkinsUserName;
    @Value("${jenkins.password}")
    private String jenkinsPassword;

    @Bean
    public Session initSSHAuth() throws JSchException {
        Session sshCon;

        JSch jsch = new JSch();
        sshCon = jsch.getSession(jenkinsUserName, jenkinsHost, Integer.parseInt(jenkinsPort));
        sshCon.setConfig("StrictHostKeyChecking", "no");
        sshCon.setConfig("PreferredAuthentications", "password");
        sshCon.setPassword(jenkinsPassword);
        sshCon.connect();

        return sshCon;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
