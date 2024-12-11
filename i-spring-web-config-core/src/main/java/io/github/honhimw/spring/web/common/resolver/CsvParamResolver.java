package io.github.honhimw.spring.web.common.resolver;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.github.honhimw.util.GZipUtils;
import io.github.honhimw.spring.ValidatorUtils;
import io.github.honhimw.spring.annotation.resolver.PartParam;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2024-08-02
 */

public class CsvParamResolver extends BaseParamResolver implements HandlerMethodArgumentResolver {

    private final CsvMapper CSV_MAPPER;

    private final CsvSchema CSV_SCHEMA;

    public CsvParamResolver(ObjectMapper objectMapper) {
        this((CsvMapper) new CsvMapper().setSerializerFactory(objectMapper.getSerializerFactory()));
    }

    public CsvParamResolver(CsvMapper csvMapper) {
        csvMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.CSV_MAPPER = csvMapper;
        this.CSV_SCHEMA = csvMapper.schemaWithHeader();
    }

    @Override
    public boolean supportsParameter(@Nonnull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PartParam.class) &&
               Collection.class.isAssignableFrom(parameter.getParameterType());
    }

    @Nullable
    @Override
    public Object resolveArgument(@Nonnull MethodParameter parameter,
                                  @Nullable ModelAndViewContainer mavContainer,
                                  @Nonnull NativeWebRequest webRequest,
                                  @Nullable WebDataBinderFactory binderFactory) throws Exception {
        PartParam partParam = parameter.getParameterAnnotation(PartParam.class);
        Assert.notNull(partParam, "argument resolver annotation should not be null.");

        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.notNull(servletRequest, "servlet request should not be null.");

        String contentType = servletRequest.getContentType();

        if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(MediaType.parseMediaType(contentType))) {
            String name = partParam.value();
            MultipartHttpServletRequest multipartHttpServletRequest = webRequest
                .getNativeRequest(MultipartHttpServletRequest.class);
            Objects.requireNonNull(multipartHttpServletRequest);
            MultiValueMap<String, MultipartFile> multipartFileMultiValueMap = multipartHttpServletRequest
                .getMultiFileMap();

            MultipartFile first = multipartFileMultiValueMap.getFirst(name);

            if (first != null) {
                ArrayNode arrayNode = CSV_MAPPER.createArrayNode();
                Type parameterType = parameter.getGenericParameterType();
                JavaType javaType = CSV_MAPPER.constructType(parameterType);
                InputStream inputStream = first.getInputStream();

                byte[] bytes = inputStream.readAllBytes();

                if (partParam.gzip()) {
                    bytes = GZipUtils.decompress(bytes);
                }
                Charset charset;
                if (bytes.length > 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                    charset = StandardCharsets.UTF_8;
                } else {
                    charset = Charset.forName("GBK");
                }
                String csvBody = new String(bytes, charset);

                try (MappingIterator<ObjectNode> iterator = CSV_MAPPER.readerFor(ObjectNode.class).with(CSV_SCHEMA).readValues(csvBody)) {
                    iterator.forEachRemaining(arrayNode::add);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
                Collection<?> arguments = CSV_MAPPER.readValue(arrayNode.traverse(), javaType);

                for (Object argument : arguments) {
                    ValidatorUtils.validate(argument, partParam.excludesValidate());
                }

                return arguments;
            }

        }
        if (partParam.required()) {
            throw new IllegalArgumentException("Missing required argument: " + parameter.getParameterName());
        } else {
            return null;
        }
    }

}
