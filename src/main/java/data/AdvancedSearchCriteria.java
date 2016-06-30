package data;

public class AdvancedSearchCriteria {

    String env;
    String type;
    String filter;

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

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "AdvancedSearchCriteria{" +
                "env='" + env + '\'' +
                ", type='" + type + '\'' +
                ", filter='" + filter + '\'' +
                '}';
    }
}
