package data;

import java.util.Map;

public class GenericJacksonConverter {
    Map<String, String> data;

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "GenericJacksonConverter{" +
                "data=" + data +
                '}';
    }
}
