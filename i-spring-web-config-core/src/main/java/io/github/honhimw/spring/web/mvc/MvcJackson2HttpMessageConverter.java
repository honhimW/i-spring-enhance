package io.github.honhimw.spring.web.mvc;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import io.github.honhimw.core.IResult;
import io.github.honhimw.spring.web.common.i18n.I18nUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.util.StreamUtils;
import org.springframework.util.TypeUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * @author hon_him
 * @since 2023-05-17
 */

public class MvcJackson2HttpMessageConverter extends FetcherJacksonConverter {

    public MvcJackson2HttpMessageConverter(ObjectMapper objectMapper,
                                           MediaType supportedMediaType) {
        super(objectMapper, supportedMediaType);
    }

    @Override
    public boolean canWrite(@NonNull Class<?> clazz, MediaType mediaType) {
        if (String.class.isAssignableFrom(clazz)) {
            return false;
        }
        return super.canWrite(clazz, mediaType);
    }

    @Override
    protected void writeInternal(@NonNull Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        MediaType contentType = outputMessage.getHeaders().getContentType();
        JsonEncoding encoding = getJsonEncoding(contentType);

        ObjectMapper objectMapper = getObjectMapper();

        OutputStream outputStream = StreamUtils.nonClosing(outputMessage.getBody());
        JsonGenerator generator = objectMapper.getFactory().createGenerator(outputStream, encoding);
        generator = decorateGenerator(generator);
        try {
            if (object instanceof IResult<?> Result) {
                I18nUtils.i18n(Result);
            }
            writePrefix(generator, object);

            Object value = object;
            Class<?> serializationView = null;
            FilterProvider filters = null;
            JavaType javaType = null;

            if (object instanceof MappingJacksonValue container) {
                value = container.getValue();
                serializationView = container.getSerializationView();
                filters = container.getFilters();
            }
            if (type != null && TypeUtils.isAssignable(type, value.getClass())) {
                javaType = getJavaType(type, null);
            }

            ObjectWriter objectWriter = (serializationView != null ?
                objectMapper.writerWithView(serializationView) : objectMapper.writer());
            if (filters != null) {
                objectWriter = objectWriter.with(filters);
            }
            if (javaType != null && javaType.isContainerType()) {
                objectWriter = objectWriter.forType(javaType);
            }
            SerializationConfig config = objectWriter.getConfig();
            if (contentType != null && contentType.isCompatibleWith(MediaType.TEXT_EVENT_STREAM) &&
                config.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
                objectWriter = objectWriter.withDefaultPrettyPrinter();
            }
            objectWriter.writeValue(generator, value);

            writeSuffix(generator, object);
            generator.flush();
        } catch (InvalidDefinitionException ex) {
            throw new HttpMessageConversionException("Type definition error: " + ex.getType(), ex);
        } catch (JsonProcessingException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getOriginalMessage(), ex);
        } finally {
            generator.close();
        }
    }

}
