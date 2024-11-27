package io.github.honhimw.ddd.jpa.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.util.JsonUtils;

/**
 * @author hon_him
 * @since 2023-02-07
 */

public class JacksonUtils {

    public static ObjectMapper OBJECT_MAPPER = JsonUtils.mapper();

    public static <T> T readValue(Class<T> type, Object value) {
        try {
            return OBJECT_MAPPER.readerFor(type)
                .readValue(String.format("\"%s\"", value));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
