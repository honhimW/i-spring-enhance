package io.github.honhimw.spring.web.common.resolver;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.github.honhimw.spring.annotation.resolver.FileReturn;
import io.github.honhimw.spring.web.mvc.FetcherJacksonConverter;
import io.github.honhimw.spring.web.util.BodyWithReturnType;
import io.github.honhimw.spring.web.util.MimeTypeSupports;
import io.github.honhimw.spring.web.util.ResolvableTypes;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.util.TypeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author hon_him
 * @since 2024-08-07
 */

public class CsvHttpMessageConverter extends FetcherJacksonConverter {

    private static final Charset GBK = Charset.forName("GBK");

    private final CsvMapper CSV_MAPPER;

    private final CsvSchema CSV_SCHEMA;

    public CsvHttpMessageConverter(ObjectMapper objectMapper) {
        this((CsvMapper) new CsvMapper()
            .setSerializerFactory(objectMapper.getSerializerFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        );
    }

    public CsvHttpMessageConverter(CsvMapper csvMapper) {
        super(csvMapper, MimeTypeSupports.TEXT_CSV);
        this.CSV_MAPPER = csvMapper;
        this.CSV_SCHEMA = csvMapper.schemaWithHeader();
    }

    @Override
    public boolean canRead(@Nonnull Type type, Class<?> contextClass, MediaType mediaType) {
        return MimeTypeSupports.TEXT_CSV.isCompatibleWith(mediaType) && ResolvableTypes.COLLECTION_TYPE.isAssignableFrom(ResolvableType.forType(type));
    }

    @Override
    public boolean canWrite(@Nullable Type type, @Nonnull Class<?> clazz, MediaType mediaType) {
        return canWrite(mediaType) && ResolvableTypes.COLLECTION_TYPE.isAssignableFrom(ResolvableType.forType(type));
    }

    @Nonnull
    @Override
    public Object read(@Nonnull Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage)
        throws IOException, HttpMessageNotReadableException {
        JavaType javaType = getJavaType(type, contextClass);
        MediaType contentType = inputMessage.getHeaders().getContentType();

        CsvMapper csvMapper = selectObjectMapper(javaType.getRawClass(), contentType);
        Assert.state(csvMapper != null, () -> "No ObjectMapper for " + javaType);

        try {
            InputStream inputStream = StreamUtils.nonClosing(inputMessage.getBody());

            ObjectReader objectReader = csvMapper.readerFor(ObjectNode.class).with(CSV_SCHEMA);
            objectReader = customizeReader(objectReader, javaType);
            byte[] bytes = inputStream.readAllBytes();

            Charset charset = getCharset(contentType, bytes);
            String csvBody = new String(bytes, charset);
            ArrayNode arrayNode = CSV_MAPPER.createArrayNode();
            try (MappingIterator<ObjectNode> iterator = objectReader.readValues(csvBody)) {
                iterator.forEachRemaining(arrayNode::add);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }

            return CSV_MAPPER.readValue(arrayNode.traverse(), javaType);
        }
        catch (InvalidDefinitionException ex) {
            throw new HttpMessageConversionException("Type definition error: " + ex.getType(), ex);
        }
        catch (JsonProcessingException ex) {
            throw new HttpMessageNotReadableException("CSV parse error: " + ex.getOriginalMessage(), ex, inputMessage);
        }
    }

    @Override
    protected void writeInternal(@Nonnull Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        MediaType contentType = outputMessage.getHeaders().getContentType();
        Charset charset = getCharset(contentType);
        byte[] prefix = {};
        byte[] suffix = {};
        if (object instanceof BodyWithReturnType bodyWithReturnType) {
            MethodParameter returnType = bodyWithReturnType.getReturnType();
            object = bodyWithReturnType.getBody();
            Assert.notNull(object, "Body should not be null");
            if (returnType.hasMethodAnnotation(FileReturn.class)) {
                FileReturn fileReturn = returnType.getMethodAnnotation(FileReturn.class);
                Assert.notNull(fileReturn, "FileReturn should not be null");
                FileReturn.Encoding encoding = fileReturn.encoding();
                charset = encoding.getCharset();
                prefix = encoding.getPrefix();
                suffix = encoding.getSuffix();
            }
        }
        CsvMapper csvMapper = selectObjectMapper(object.getClass(), contentType);

        OutputStream outputStream = StreamUtils.nonClosing(outputMessage.getBody());
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset);
        JsonGenerator generator = csvMapper.getFactory().createGenerator(writer);
        generator = decorateGenerator(generator);
        try {
            outputStream.write(prefix);
            writePrefix(generator, object);

            Object value = object;
            Class<?> serializationView = null;
            FilterProvider filters = null;
            JavaType javaType = null;

            if (object instanceof MappingJacksonValue mappingJacksonValue) {
                value = mappingJacksonValue.getValue();
                serializationView = mappingJacksonValue.getSerializationView();
                filters = mappingJacksonValue.getFilters();
            }
            if (type != null && TypeUtils.isAssignable(type, value.getClass())) {
                javaType = getJavaType(type, null);
            }
            ObjectWriter objectWriter = (serializationView != null ?
                csvMapper.writerWithView(serializationView) : csvMapper.writer());
            objectWriter = customizeWriter(objectWriter, type, object);
            if (filters != null) {
                objectWriter = objectWriter.with(filters);
            }
            if (javaType != null && (javaType.isContainerType() || javaType.isTypeOrSubTypeOf(Optional.class))) {
                objectWriter = objectWriter.forType(javaType);
            }
            objectWriter = customizeWriter(objectWriter, javaType, contentType);
            objectWriter.writeValue(generator, value);

            writeSuffix(generator, object);
            generator.flush();
            outputStream.write(suffix);
        } catch (InvalidDefinitionException ex) {
            throw new HttpMessageConversionException("Type definition error: " + ex.getType(), ex);
        } catch (JsonProcessingException ex) {
            throw new HttpMessageNotWritableException("Could not write CSV: " + ex.getOriginalMessage(), ex);
        }
    }

    @Nonnull
    protected ObjectWriter customizeWriter(@Nonnull ObjectWriter writer, Type type, Object object) {
        CsvSchema schema = getSchema(ResolvableType.forType(type).getGeneric(0), (Collection<?>) object);
        return writer.with(schema);
    }

    @Nonnull
    protected Charset getCharset(MediaType contentType, byte[] bytes) {
        if (contentType != null && contentType.getCharset() != null) {
            return contentType.getCharset();
        } else if (bytes != null && bytes.length > 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            return StandardCharsets.UTF_8;
        } else {
            return GBK;
        }
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

    protected CsvMapper selectObjectMapper(Class<?> targetType, MediaType targetMediaType) {
        return CSV_MAPPER;
    }
}
