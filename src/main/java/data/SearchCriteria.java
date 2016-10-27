package data;

import java.util.List;

public class SearchCriteria {
    String environment;
    List<String> checkBoxes;

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public List<String> getCheckBoxes() {
        return checkBoxes;
    }

    public void setCheckBoxes(List<String> checkBoxes) {
        this.checkBoxes = checkBoxes;
    }

    @Override
    public String toString() {
        return "SearchCriteria{" +
                "environment='" + environment + '\'' +
                ", checkBoxes=" + checkBoxes +
                '}';
    }
}
