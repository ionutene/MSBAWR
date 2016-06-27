package data;

public class SearchCriteria {
    String env;
    String type;

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SearchCriteria{" +
                "env='" + env + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
