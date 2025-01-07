package h2.ext;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author hon_him
 * @since 2024-01-16
 */

@Slf4j
public class H2CustomFunctions {

    /**
     * Setup ObjectMapper
     */
    public static ObjectMapper OBJECT_MAPPER;

    public static final String SEPARATOR = String.valueOf(JsonPointer.SEPARATOR);

    /**
     * <pre>{@code
     * @Transactional
     * void duringInitialization() {
     *   if (RuntimeDialect.isH2()) {
     *     Query nativeQuery = em.createNativeQuery("""
     *       create
     *       alias json_extract_path_text
     *       for "h2.ext.H2CustomFunctions.jsonExtractPathText"
     *     """);
     *     nativeQuery.executeUpdate();
     *   }
     * }
     *
     * void usage() {
     *   em.createNativeQuery("""
     *     select *
     *     from table t
     *     where json_extract_path_text(t.json_data, 'key1', 'key2') = 'value'
     *     """)
     * }
     * }</pre>
     *
     * @param json json format value
     * @param keys path to extract from json: ['key1', 'key2'] means json['key1']['key2']
     * @return extracted value
     */
    public static Object jsonExtractPathText(String json, String... keys) {
        String pointer = SEPARATOR + String.join(SEPARATOR, keys);
        return jsonExtractPath(json, pointer);
    }

    public static Object jsonExtractPath(String json, String pointer) {
        try {
            pointer = StringUtils.prependIfMissing(pointer, SEPARATOR);
            JsonNode jsonNode = getMapper().readTree(json);
            JsonNode at = jsonNode.at(pointer);
            if (log.isTraceEnabled()) {
                log.trace("H2 extract json pointer: {}", pointer);
            }
            if (at.isTextual()) {
                return at.asText();
            } else if (at.isNumber()) {
                return at.numberValue();
            } else if (at.isBoolean()) {
                return at.booleanValue();
            } else if (at.isObject() || at.isArray()) {
                return at.toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("json extract error.", e);
            throw new IllegalArgumentException("Unable to extract json during runtime.", e);
        }
    }

    private static ObjectMapper getMapper() {
        return Objects.requireNonNull(OBJECT_MAPPER, "H2 custom functions `ObjectMapper` is not initialized.");
    }

}
