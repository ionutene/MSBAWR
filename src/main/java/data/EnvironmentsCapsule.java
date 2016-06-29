package data;

import java.util.Map;

public class EnvironmentsCapsule {
    Map<String, String> environment;

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    @Override
    public String toString() {
        return "EnvironmentsCapsule{" +
                "environment=" + environment +
                '}';
    }
}
