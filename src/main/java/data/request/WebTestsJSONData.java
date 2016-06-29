package data.request;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class WebTestsJSONData {

    @JsonUnwrapped
    private TestData tests;

    public TestData getTests() {
        return tests;
    }

    public void setTests(TestData tests) {
        this.tests = tests;
    }

    @Override
    public String toString() {
        return "WebTestsJSONData{" +
                "tests=" + tests +
                '}';
    }
}
