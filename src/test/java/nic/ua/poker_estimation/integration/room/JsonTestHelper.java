package nic.ua.poker_estimation.integration.room;

import com.jayway.jsonpath.JsonPath;

public final class JsonTestHelper {

    private JsonTestHelper() {
    }

    public static String readString(String json, String path) {
        return JsonPath.read(json, path);
    }
}
