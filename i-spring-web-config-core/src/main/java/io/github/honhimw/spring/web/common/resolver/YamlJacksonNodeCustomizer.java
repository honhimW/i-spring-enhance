package io.github.honhimw.spring.web.common.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import io.github.honhimw.spring.util.GZipUtils;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author hon_him
 * @since 2024-07-18
 */

public class YamlJacksonNodeCustomizer implements JacksonNodeCustomizer {

    private final MediaType mediaType;

    private final YAMLMapper YAML_MAPPER;

    public YamlJacksonNodeCustomizer(ObjectMapper objectMapper) {
        this((YAMLMapper) new YAMLMapper().setSerializerFactory(objectMapper.getSerializerFactory()));
    }

    public YamlJacksonNodeCustomizer(YAMLMapper yamlMapper) {
        this(yamlMapper, MediaType.parseMediaType("application/yaml"));
    }

    public YamlJacksonNodeCustomizer(YAMLMapper yamlMapper, MediaType mediaType) {
        this.YAML_MAPPER = yamlMapper;
        this.mediaType = mediaType;
    }

    @Override
    public void customize(ObjectNode objectNode, MethodParameter parameter, HttpServletRequest servletRequest) {
        if (!guard(objectNode, parameter, servletRequest)) {
            return;
        }
        TextParam parameterAnnotation = parameter.getParameterAnnotation(TextParam.class);
        Validate.validState(parameterAnnotation != null, "argument resolver annotation should not be null.");

        try {
            ServletInputStream inputStream = servletRequest.getInputStream();
            byte[] bytes = inputStream.readAllBytes();
            bytes = tryDecompressGzip(parameterAnnotation, servletRequest, bytes);
            Charset charset = Optional.of(servletRequest.getCharacterEncoding())
                .map(Charset::forName)
                .orElse(StandardCharsets.UTF_8);
            String body = new String(bytes, charset);
            if (StringUtils.isNoneBlank(body)) {
                injectBodyParam(objectNode, body);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected boolean guard(ObjectNode objectNode, MethodParameter parameter, HttpServletRequest request) {
        return mediaType.isCompatibleWith(MediaType.parseMediaType(request.getContentType()));
    }

    protected void injectBodyParam(ObjectNode objectNode, String yamlBody) {
        try {
            JsonNode jsonNode = YAML_MAPPER.readTree(yamlBody);
            if (jsonNode instanceof ObjectNode bodyNode) {
                objectNode.setAll(bodyNode);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected byte[] tryDecompressGzip(TextParam annotation, HttpServletRequest request, byte[] body) {
        String contentEncoding = request.getHeader(HttpHeaders.CONTENT_ENCODING);
        if (annotation.gzip() && StringUtils.equals(contentEncoding, "gzip")) {
            try {
                return GZipUtils.decompress(body);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            return body;
        }
    }

}
