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
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author hon_him
 * @since 2024-11-18
 */

public class JsonUtils {

    private static final ObjectMapper MAPPER = newMapper();

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static ObjectMapper newMapper() {
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
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("can't serialize as json.", e);
        }
    }

    public static String toPrettyJson(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("can't serialize as json.", e);
        }
    }

    @Nonnull
    public static <T> T fromJson(String json, Class<T> clazz) {
        return fromJson(json, MAPPER.constructType(clazz));
    }

    @Nonnull
    public static <T> T fromJson(String json, Type type) {
        return fromJson(json, MAPPER.constructType(type));
    }

    @Nonnull
    public static <T> T fromJson(String json, TypeReference<T> type) {
        return fromJson(json, MAPPER.constructType(type));
    }

    @Nonnull
    public static <T> T fromJson(String json, JavaType javaType) {
        try {
            return MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("can't deserialize from json.", e);
        }
    }

    @Nonnull
    public static Map<String, Object> readAsMap(String json) {
        if (StringUtils.isNotBlank(json)) {
            try {
                return MAPPER.readerForMapOf(Object.class).readValue(json);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("can't deserialize from json", e);
            }
        }
        return new LinkedHashMap<>();
    }

    @Nonnull
    public static <T extends JsonNode> T readTree(String json) {
        if (StringUtils.isNotBlank(json)) {
            try {
                JsonNode jsonNode = MAPPER.readTree(json);
                return jsonNode.require();
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("can't deserialize from json", e);
            }
        }
        return MAPPER.nullNode().require();
    }

    @Nonnull
    public static <T extends JsonNode> T valueToTree(Object value) {
        if (Objects.nonNull(value)) {
            JsonNode jsonNode = MAPPER.valueToTree(value);
            return jsonNode.require();
        }
        return MAPPER.nullNode().require();
    }

    public static void update(Object toBeUpdate, String json) {
        try {
            MAPPER.readerForUpdating(toBeUpdate).readValue(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
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
        String separator = String.valueOf(JsonPointer.SEPARATOR);
        return flatten(o, separator, true);
    }

    public static Map<String, Object> flatten(Object o, String separator, boolean ignoreNull) {
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
    public static void flatten(String path, Object o, Map<String, Object> map, String separator, boolean ignoreNull) {
        try {
            JsonNode jsonNode = MAPPER.valueToTree(o);
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
