package io.github.honhimw.spring.web.common.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import io.github.honhimw.util.GZipUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.IResolvableTypeSupports;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Resolve common parameters
 *
 * @author hon_him
 * @see org.springframework.web.bind.annotation.RequestParam query
 * @see org.springframework.web.bind.annotation.PathVariable url vars
 * @see org.springframework.web.bind.annotation.RequestBody body application/json or {@link MediaType#APPLICATION_FORM_URLENCODED_VALUE}
 * @since 2022-06-06
 */
@SuppressWarnings("unchecked")
@Slf4j
public class TextParamResolver extends BaseParamResolver {

    protected final AbstractJackson2HttpMessageConverter jackson2HttpMessageConverter;

    public TextParamResolver(AbstractJackson2HttpMessageConverter jackson2HttpMessageConverter) {
        this.jackson2HttpMessageConverter = jackson2HttpMessageConverter;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(TextParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        TextParam parameterAnnotation = parameter.getParameterAnnotation(TextParam.class);
        Assert.notNull(parameterAnnotation, "argument resolver annotation should not be null.");

        // URI vars
        Map<String, String> uriTemplateVars = (Map<String, String>) webRequest.getAttribute(
            HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        // parameter map
        Map<String, String[]> parameterMap = webRequest.getParameterMap();

        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        Assert.notNull(servletRequest, "servlet request should not be null.");
        Charset charset = Charset.forName(servletRequest.getCharacterEncoding());

        Class<?> parameterType = parameter.getParameterType();
        assertBaseType(parameterType);

        ObjectNode paramNode = jackson2HttpMessageConverter.getObjectMapper().createObjectNode();

        injectParameterMap(paramNode, parameterMap);
        injectUriParam(paramNode, uriTemplateVars);

        if (MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(servletRequest.getContentType()))) {
            byte[] byteArray = IOUtils.toByteArray(servletRequest.getInputStream());
            if (parameterAnnotation.gzip() && StringUtils.equals(servletRequest.getHeader(HttpHeaders.CONTENT_ENCODING), "gzip")) {
                byteArray = GZipUtils.decompress(byteArray);
            }
            String body = new String(byteArray, charset);
            if (StringUtils.isNoneBlank(body)) {
                injectBodyParam(paramNode, body);
            }
        }
        injectCustom(paramNode, parameter, servletRequest);
        Object parameterTarget = IResolvableTypeSupports.readValue(parameter, paramNode, jackson2HttpMessageConverter.getObjectMapper());

        validate(parameterTarget, parameterAnnotation.excludesValidate());
        return parameterTarget;
    }

    protected void injectBodyParam(ObjectNode objectNode, String jsonBody) throws JsonProcessingException {
        JsonNode jsonNode = jackson2HttpMessageConverter.getObjectMapper().readTree(jsonBody);
        if (jsonNode instanceof ObjectNode bodyNode) {
            objectNode.setAll(bodyNode);
        }
    }

}
