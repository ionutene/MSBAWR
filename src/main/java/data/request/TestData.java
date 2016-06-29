package data.request;

import java.util.List;
import java.util.Map;

public class TestData {

    private Map<String, List<String>> tests;

    public Map<String, List<String>> getTests() {
        return tests;
    }

    public void setTests(Map<String, List<String>> tests) {
        this.tests = tests;
    }

    @Override
    public String toString() {
        return "TestData{" +
                "tests=" + tests +
                '}';
    }
}
