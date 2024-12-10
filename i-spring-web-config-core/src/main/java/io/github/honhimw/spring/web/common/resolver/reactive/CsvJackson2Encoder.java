package io.github.honhimw.spring.web.common.resolver.reactive;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.github.honhimw.spring.ResolvableTypes;
import jakarta.annotation.Nonnull;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;

import java.util.*;

/**
 * @author hon_him
 * @since 2024-08-07
 */

public class CsvJackson2Encoder extends AbstractFileJackson2Encoder {

    private final CsvMapper CSV_MAPPER;

    public CsvJackson2Encoder(ObjectMapper objectMapper) {
        this((CsvMapper) new CsvMapper().setSerializerFactory(objectMapper.getSerializerFactory()));
    }

    public CsvJackson2Encoder(CsvMapper csvMapper) {
        super(csvMapper, MediaType.parseMediaType("text/csv"));
        csvMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.CSV_MAPPER = csvMapper;
    }

    @Override
    public boolean canEncode(@Nonnull ResolvableType elementType, MimeType mimeType) {
        return super.canEncode(elementType, mimeType) && ResolvableTypes.COLLECTION_TYPE.isAssignableFrom(elementType);
    }

    @Nonnull
    protected ObjectWriter customizeWriter(@Nonnull ObjectWriter writer, ResolvableType type, Object object) {
        CsvSchema schema = getSchema(type.getGeneric(0), (Collection<?>) object);
        return writer.with(schema);
    }

    @SuppressWarnings("unchecked")
    protected CsvSchema getSchema(ResolvableType resolvableType, Collection<?> collection) {
        if (ResolvableTypes.MAP_TYPE.isAssignableFrom(resolvableType)) {
            if (ResolvableTypes.STRING_TYPE.isAssignableFrom(resolvableType.getGeneric(0))) {
                Set<String> set = new TreeSet<>();
                for (Object o : collection) {
                    Map<String, ?> map = ((Map<String, ?>) o);
                    if (Objects.nonNull(map) && !map.isEmpty()) {
                        set.addAll(map.keySet());
                    }
                }
                CsvSchema.Builder schemaBuilder = CsvSchema.builder();
                set.forEach(schemaBuilder::addColumn);
                return schemaBuilder.build().withHeader();
            } else {
                throw new IllegalArgumentException("Not support type: " + resolvableType.getGeneric(0));
            }
        } else {
            Class<?> rawClass = resolvableType.getRawClass();
            return CSV_MAPPER.schemaFor(rawClass).withHeader();
        }
    }


}
