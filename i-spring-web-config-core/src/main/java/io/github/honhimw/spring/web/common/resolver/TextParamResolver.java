package io.github.honhimw.spring.web.common.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import io.github.honhimw.spring.util.GZipUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * 常用参数类型包括
 *
 * @author hon_him
 * @see org.springframework.web.bind.annotation.RequestParam query参数
 * @see org.springframework.web.bind.annotation.PathVariable 路径参数
 * @see org.springframework.web.bind.annotation.RequestBody
 * 请求体application/json或{@link MediaType#APPLICATION_FORM_URLENCODED_VALUE}
 * @see org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor 将这些参数统一封装到一个实体类里,
 * 在使用时应该避免参数名重复
 * @see org.springframework.web.bind.annotation.RequestBody 类型不支持二级泛型参数
 * @since 2022-06-06
 */
@SuppressWarnings("unchecked")
@Slf4j
public class TextParamResolver extends BaseParamResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(TextParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        TextParam parameterAnnotation = parameter.getParameterAnnotation(TextParam.class);
        Assert.notNull(parameterAnnotation, "argument resolver annotation should not be null.");

        // URI参数
        Map<String, String> uriTemplateVars = (Map<String, String>) webRequest.getAttribute(
            HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        // parameter map
        Map<String, String[]> parameterMap = webRequest.getParameterMap();

        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);

        Assert.notNull(servletRequest, "请求类型错误");
        Charset charset = Charset.forName(servletRequest.getCharacterEncoding());

        Class<?> parameterType = parameter.getParameterType();
        assertBaseType(parameterType);

        ObjectNode paramNode = OBJECT_MAPPER.createObjectNode();

        injectParameterMap(paramNode, parameterMap);
        injectUriParam(paramNode, uriTemplateVars);

        if (MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(servletRequest.getContentType()))) {
            byte[] byteArray = servletRequest.getInputStream().readAllBytes();
            if (parameterAnnotation.gzip() && StringUtils.equals(servletRequest.getHeader(HttpHeaders.CONTENT_ENCODING), "gzip")) {
                byteArray = GZipUtils.decompress(byteArray);
            }
            String body = new String(byteArray, charset);
            if (StringUtils.isNoneBlank(body)) {
                injectBodyParam(paramNode, body);
            }
        }
        injectCustom(paramNode, parameter, servletRequest);
        Object parameterTarget = readValue(parameter, paramNode);

        validate(parameterTarget, parameterAnnotation.excludesValidate());
        return parameterTarget;
    }

    protected void injectBodyParam(ObjectNode objectNode, String jsonBody) throws JsonProcessingException {
        JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonBody);
        if (jsonNode instanceof ObjectNode bodyNode) {
            objectNode.setAll(bodyNode);
        }
    }

}
