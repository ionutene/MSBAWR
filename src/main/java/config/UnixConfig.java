package config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
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

    public static Session initSSHAuth(String host, String port, String userName, String password) throws Exception {
        Session sshCon;

        JSch jsch = new JSch();
        sshCon = jsch.getSession(userName, host, Integer.parseInt(port));
        sshCon.setConfig("StrictHostKeyChecking", "no");
        sshCon.setConfig("PreferredAuthentications", "password");
        sshCon.setPassword(password);
        sshCon.connect();

        return sshCon;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
