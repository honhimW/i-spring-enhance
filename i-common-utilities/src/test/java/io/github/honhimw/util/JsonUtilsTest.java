package io.github.honhimw.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author hon_him
 * @since 2024-11-18
 */

public class JsonUtilsTest {

    @Test
    @SneakyThrows
    void allowBackslash() {
        String json = """
            {
                "msg": "\\u6210\\u529f",
                "url": "https:\\/\\/www.baidu.com"
            }
            """;
        JsonNode jsonNode = JsonUtils.mapper().reader().withFeatures(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER).readTree(json);
        System.out.println(jsonNode.toPrettyString());
    }

    @Test
    @SneakyThrows
    void allowSingleQuote() {
        JsonUtils.mapper().enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        Map<String, Object> stringObjectMap = JsonUtils.readAsMap("{'foo':'bar'}");
        System.out.println(stringObjectMap);
    }

    @Test
    @SneakyThrows
    void allowUnquotedFieldNames() {
        JsonUtils.mapper().enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        JsonUtils.mapper().enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        Map<String, Object> stringObjectMap = JsonUtils.readAsMap("{foo:'bar', \"foo2\":'bar2'}");
        System.out.println(stringObjectMap);
    }

}
