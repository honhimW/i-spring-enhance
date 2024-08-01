package io.github.honhimw.spring.web.common.resolver;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.honhimw.spring.annotation.resolver.FormDataParam;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link MediaType#MULTIPART_FORM_DATA_VALUE} 格式请求类型参数解析, 该类型同时支持binary(文件)和text(文本参数)
 * Controller接口参数使用{@link FormDataParam}注解,
 * 注解对象需实现{@link java.io.Serializable}且参数类型为{@link Number,Boolean,String,MultipartFile} 多个key, 只取第一个
 *
 * @author hon_him
 * @since 2022-06-06
 */
public class FormDataParamResolver extends BaseParamResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(FormDataParam.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // URI参数
        Map<String, String> uriTemplateVars = (Map<String, String>) webRequest.getAttribute(
            HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        // parameter map
        Map<String, String[]> parameterMap = webRequest.getParameterMap();

        MultipartHttpServletRequest multipartHttpServletRequest = webRequest
            .getNativeRequest(MultipartHttpServletRequest.class);
        Objects.requireNonNull(multipartHttpServletRequest);
        Class<?> parameterType = parameter.getParameterType();
        assertBaseType(parameterType);

        MultiValueMap<String, MultipartFile> multipartFileMultiValueMap = multipartHttpServletRequest
            .getMultiFileMap();

        ObjectNode paramNode = OBJECT_MAPPER.createObjectNode();
        injectParameterMap(paramNode, parameterMap);
        injectUriParam(paramNode, uriTemplateVars);
        injectCustom(paramNode, parameter, multipartHttpServletRequest);

        Object parameterTarget = readValue(parameter, paramNode);

        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(parameterType);
        for (PropertyDescriptor pd : propertyDescriptors) {
            Class<?> propertyType = pd.getPropertyType();
            if (MultipartFile.class.isAssignableFrom(propertyType)) {
                List<MultipartFile> multipartFiles = multipartFileMultiValueMap.get(pd.getName());
                if (CollectionUtils.isNotEmpty(multipartFiles)) {
                    MultipartFile multipartFile = multipartFiles.get(0);
                    pd.getWriteMethod().invoke(parameterTarget, multipartFile);
                }
            }
        }
        validate(parameterTarget, Objects.requireNonNull(parameter.getParameterAnnotation(FormDataParam.class)).excludesValidate());
        return parameterTarget;
    }

}
