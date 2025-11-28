package io.github.honhimw.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author honhimW
 * @since 2025-08-26
 */

public class JsonUtils {

    private static final JsonHandler INSTANCE = new JsonHandler(newMapper());

    public static JsonMapper mapper() {
        return INSTANCE.jsonMapper;
    }

    public static JsonMapper newMapper() {
        return defaultBuilder().build();
    }

    public static JsonMapper.Builder defaultBuilder() {
        JsonMapper.Builder builder = JsonMapper.builder();

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_DATE));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_DATE));

        javaTimeModule.addSerializer(LocalDateTime.class,
            new LocalDateTimeSerializer(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class,
            new LocalDateTimeDeserializer(DateTimeUtils.DEFAULT_DATE_TIME_FORMATTER));

        javaTimeModule.addSerializer(Date.class, new JsonSerializer<>() {
            @Override
            public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
                SimpleDateFormat formatter = new SimpleDateFormat(DateTimeUtils.DEFAULT_DATE_TIME_PATTERN);
                String formattedDate = formatter.format(date);
                jsonGenerator.writeString(formattedDate);
            }
        });
        javaTimeModule.addDeserializer(Date.class, new JsonDeserializer<>() {
            @Override
            public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
                SimpleDateFormat format = new SimpleDateFormat(DateTimeUtils.DEFAULT_DATE_TIME_PATTERN);
                String date = jsonParser.getText();
                try {
                    return format.parse(date);
                } catch (ParseException e) {
                    throw new IOException("date parsing error.", e);
                }
            }
        });

        SimpleModule longAsStringModule = new SimpleModule();
        longAsStringModule.addSerializer(Long.class, ToStringSerializer.instance);
        longAsStringModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        builder
            .addModules(javaTimeModule, longAsStringModule)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
            .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
            .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            .defaultDateFormat(SafeDateFormat.create(DateTimeUtils.DEFAULT_DATE_TIME_PATTERN))
            .defaultTimeZone(TimeZone.getTimeZone(DateTimeUtils.DEFAULT_ZONE_OFFSET))
        ;
        return builder;
    }

    public static String toJson(Object obj) {
        return INSTANCE.toJson(obj);
    }

    public static String toPrettyJson(Object obj) {
        return INSTANCE.toPrettyJson(obj);
    }

    @NonNull
    public static <T> T fromJson(String json, Class<T> clazz) {
        return INSTANCE.fromJson(json, clazz);
    }

    @NonNull
    public static <T> T fromJson(String json, Type type) {
        return INSTANCE.fromJson(json, type);
    }

    @NonNull
    public static <T> T fromJson(String json, TypeReference<T> type) {
        return INSTANCE.fromJson(json, type);
    }

    @NonNull
    public static <T> T fromJson(String json, JavaType javaType) {
        return INSTANCE.fromJson(json, javaType);
    }

    @NonNull
    public static Map<String, Object> readAsMap(String json) {
        return INSTANCE.readAsMap(json);
    }

    @NonNull
    public static <T extends JsonNode> T readTree(String json) {
        return INSTANCE.readTree(json);
    }

    @NonNull
    public static <T extends JsonNode> T readTree(byte[] bytes) {
        return INSTANCE.readTree(bytes);
    }

    @NonNull
    public static <T extends JsonNode> T valueToTree(Object value) {
        return INSTANCE.valueToTree(value);
    }

    public static void update(Object toBeUpdate, String json) {
        INSTANCE.update(toBeUpdate, json);
    }

    /**
     * Transform single quote json string to double quote.
     *
     * @param str "{'foo':'bar'}"
     * @return "{\"foo\":\"bar\"}"
     * @see JsonReadFeature#ALLOW_SINGLE_QUOTES
     */
    public static String quote(String str) {
        return StringUtils.replaceChars(str, '\'', '"');
    }

    /**
     * <pre>{@code
     * JsonNode wanted = node.at("/path/to/node")
     * if (JsonUtils.exists(wanted)) {
     *     // ...
     * }
     * }</pre>
     *
     * @param node JsonNode
     * @return whether the node contains value
     */
    public static boolean exists(JsonNode node) {
        return node != null && !node.isMissingNode() && !node.isNull();
    }

    public static Map<String, Object> flatten(Object o) {
        return INSTANCE.flatten(o);
    }

    public static Map<String, Object> flatten(Object o, String separator, boolean ignoreNull) {
        return INSTANCE.flatten(o, separator, ignoreNull);
    }

    /**
     * Flatten simple object to JsonPath-Value map
     *
     * @param path       base path
     * @param o          object
     * @param map        container
     * @param separator  separator
     * @param ignoreNull ignore null value
     */
    public static void flatten(String path, Object o, Map<String, Object> map, String separator, boolean ignoreNull) {
        INSTANCE.flatten(path, o, map, separator, ignoreNull);
    }

    public static class JsonHandler {

        private final JsonMapper jsonMapper;

        public JsonHandler(JsonMapper jsonMapper) {
            this.jsonMapper = jsonMapper;
        }

        public JsonMapper mapper() {
            return jsonMapper;
        }

        public String toJson(Object obj) {
            try {
                return jsonMapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("can't serialize as json.", e);
            }
        }

        public String toPrettyJson(Object obj) {
            try {
                return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("can't serialize as json.", e);
            }
        }

        @NonNull
        public <T> T fromJson(String json, Class<T> clazz) {
            return fromJson(json, jsonMapper.constructType(clazz));
        }

        @NonNull
        public <T> T fromJson(String json, Type type) {
            return fromJson(json, jsonMapper.constructType(type));
        }

        @NonNull
        public <T> T fromJson(String json, TypeReference<T> type) {
            return fromJson(json, jsonMapper.constructType(type));
        }

        @NonNull
        public <T> T fromJson(String json, JavaType javaType) {
            try {
                return jsonMapper.readValue(json, javaType);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("can't deserialize from json.", e);
            }
        }

        @NonNull
        public Map<String, Object> readAsMap(String json) {
            if (StringUtils.isNotBlank(json)) {
                try {
                    return jsonMapper.readerForMapOf(Object.class).readValue(json);
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("can't deserialize from json", e);
                }
            }
            return new LinkedHashMap<>();
        }

        @NonNull
        public <T extends JsonNode> T readTree(String json) {
            if (StringUtils.isNotBlank(json)) {
                try {
                    JsonNode jsonNode = jsonMapper.readTree(json);
                    return jsonNode.require();
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("can't deserialize from json", e);
                }
            }
            return jsonMapper.nullNode().require();
        }

        @NonNull
        public <T extends JsonNode> T readTree(byte[] bytes) {
            if (bytes != null && bytes.length > 0) {
                try {
                    JsonNode jsonNode = jsonMapper.readTree(bytes);
                    return jsonNode.require();
                } catch (IOException e) {
                    throw new IllegalArgumentException("can't deserialize from json", e);
                }
            }
            return jsonMapper.nullNode().require();
        }

        @NonNull
        public <T extends JsonNode> T valueToTree(Object value) {
            if (Objects.nonNull(value)) {
                JsonNode jsonNode = jsonMapper.valueToTree(value);
                return jsonNode.require();
            }
            return jsonMapper.nullNode().require();
        }

        public void update(Object toBeUpdate, String json) {
            try {
                jsonMapper.readerForUpdating(toBeUpdate).readValue(json);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public Map<String, Object> flatten(Object o) {
            String separator = String.valueOf(JsonPointer.SEPARATOR);
            return flatten(o, separator, true);
        }

        public Map<String, Object> flatten(Object o, String separator, boolean ignoreNull) {
            Map<String, Object> map = new LinkedHashMap<>();
            flatten("", o, map, separator, ignoreNull);
            return map;
        }

        /**
         * Flatten simple object to JsonPath-Value map
         *
         * @param path       base path
         * @param o          object
         * @param map        container
         * @param separator  separator
         * @param ignoreNull ignore null value
         */
        public void flatten(String path, Object o, Map<String, Object> map, String separator, boolean ignoreNull) {
            try {
                JsonNode jsonNode = jsonMapper.valueToTree(o);
                if (jsonNode.isObject()) {
                    jsonNode.properties().forEach(entry -> {
                        String key = entry.getKey();
                        JsonNode node = entry.getValue();
                        String p = path + separator + key;
                        flatten(p, node, map, separator, ignoreNull);
                    });
                } else if (jsonNode.isArray()) {
                    for (int i = 0; i < jsonNode.size(); i++) {
                        JsonNode node = jsonNode.get(i);
                        String p = path + separator + i;
                        flatten(p, node, map, separator, ignoreNull);
                    }
                } else {
                    Object v;
                    if (jsonNode.isTextual()) {
                        v = jsonNode.asText();
                    } else if (jsonNode.isNumber()) {
                        v = jsonNode.numberValue();
                    } else if (jsonNode.isBoolean()) {
                        v = jsonNode.booleanValue();
                    } else {
                        if (jsonNode.isNull() && ignoreNull) {
                            return;
                        }
                        v = jsonNode.asText();
                    }
                    map.put(path, v);
                }

            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }

        }
    }

    public static void removeNullEntries(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = node.require();
            objectNode.forEachEntry((s, jsonNode) -> removeNullEntries(jsonNode));
            objectNode.removeNulls();
        }
    }

}
