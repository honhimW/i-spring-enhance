package io.github.honhimw.spring.web.mvc;

import io.github.honhimw.spring.annotation.resolver.FileReturn;
import io.github.honhimw.spring.annotation.resolver.PartParam;
import io.github.honhimw.spring.data.common.ValidatorUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.multipart.support.RequestPartServletServerHttpRequest;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * @author hon_him
 * @since 2024-08-06
 */

public abstract class AbstractFileMessageConverterProcessor extends AbstractMessageConverterMethodProcessor implements HandlerMethodReturnValueHandler {

    protected AbstractFileMessageConverterProcessor(List<HttpMessageConverter<?>> converters) {
        super(converters, null, List.of(new FileResponseBodyAdvice()));
    }

    @Override
    public boolean supportsParameter(@Nonnull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PartParam.class);
    }

    @Override
    public boolean supportsReturnType(@Nonnull MethodParameter returnType) {
        return returnType.hasMethodAnnotation(FileReturn.class);
    }

    @Nullable
    @Override
    public Object resolveArgument(@Nonnull MethodParameter parameter,
                                  @Nullable ModelAndViewContainer mavContainer,
                                  @Nonnull NativeWebRequest webRequest,
                                  @Nullable WebDataBinderFactory binderFactory) throws Exception {
        parameter = parameter.nestedIfOptional();
        Object arg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());

        if (binderFactory != null) {
            String name = Conventions.getVariableNameForParameter(parameter);
            ResolvableType type = ResolvableType.forMethodParameter(parameter);
            WebDataBinder binder = binderFactory.createBinder(webRequest, arg, name, type);
            if (arg != null) {
                validateIfApplicable(binder, parameter);
                if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
                    throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
                }
            }
            if (mavContainer != null) {
                mavContainer.addAttribute(BindingResult.MODEL_KEY_PREFIX + name, binder.getBindingResult());
            }
        }

        return adaptArgumentIfNecessary(arg, parameter);
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue,
                                  @Nonnull MethodParameter returnType,
                                  @Nonnull ModelAndViewContainer mavContainer,
                                  @Nonnull NativeWebRequest webRequest) throws Exception {

        FileReturn fileReturn = returnType.getMethodAnnotation(FileReturn.class);
        Assert.notNull(fileReturn, "return value resolver annotation should not be null.");
        String defaultFileName = fileReturn.value();
        FileReturn.Encoding encoding = fileReturn.encoding();
        Assert.state(StringUtils.isNotBlank(defaultFileName), "default file name should not be blank.");

        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse servletResponse = webRequest.getNativeResponse(HttpServletResponse.class);

        Assert.notNull(servletRequest, "HttpServletRequest should not be null.");
        Assert.notNull(servletResponse, "HttpServletResponse should not be null.");

        if (!servletResponse.containsHeader(HttpHeaders.CONTENT_DISPOSITION)) {
            String attachmentHeader = ContentDisposition.attachment().filename(defaultFileName).build().toString();
            servletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, attachmentHeader);
        }

        mavContainer.setRequestHandled(true);
        ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
        ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

        if (returnValue instanceof ProblemDetail detail) {
            outputMessage.setStatusCode(HttpStatusCode.valueOf(detail.getStatus()));
            if (detail.getInstance() == null) {
                URI path = URI.create(inputMessage.getServletRequest().getRequestURI());
                detail.setInstance(path);
            }
        }

//        byte[] prefix = encoding.getPrefix();
//        byte[] suffix = encoding.getSuffix();
//        OutputStream outputStream = StreamUtils.nonClosing(outputMessage.getBody());
//
//        if (ArrayUtils.isNotEmpty(prefix)) {
//            outputStream.write(prefix);
//        }
        writeWithMessageConverters(returnValue, returnType, inputMessage, outputMessage);
//        if (ArrayUtils.isNotEmpty(suffix)) {
//            outputStream.write(suffix);
//        }
    }

    @Override
    @Nullable
    protected Object readWithMessageConverters(@Nonnull NativeWebRequest webRequest,
                                               @Nonnull MethodParameter parameter,
                                               @Nonnull Type paramType) throws IOException, HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {
        PartParam partParam = parameter.getParameterAnnotation(PartParam.class);
        Assert.notNull(partParam, "argument resolver annotation should not be null.");
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        Assert.notNull(servletRequest, "servlet request should not be null.");

        ServletServerHttpRequest inputMessage = createInputMessage(webRequest, partParam.value());
        Object arg = readWithMessageConverters(inputMessage, parameter, paramType);
        if (arg == null && checkRequired(parameter)) {
            throw new HttpMessageNotReadableException("Required request body is missing: " +
                                                      parameter.getExecutable().toGenericString(), inputMessage);
        }
        Collection<?> arguments = (Collection<?>) arg;
        if (CollectionUtils.isNotEmpty(arguments)) {
            for (Object argument : arguments) {
                ValidatorUtils.validate(argument, partParam.excludesValidate());
            }
        }
        return arg;
    }

    @Nonnull
    protected ServletServerHttpRequest createInputMessage(@Nonnull NativeWebRequest webRequest, String partName) {
        MultipartHttpServletRequest multipartHttpServletRequest = webRequest
            .getNativeRequest(MultipartHttpServletRequest.class);
        Assert.state(multipartHttpServletRequest != null, "No MultipartHttpServletRequest found");
        try {
            @Nullable
            Part part = multipartHttpServletRequest.getPart(partName);
            if (part == null) {
                throw new MissingServletRequestPartException(partName);
            }
            return new RequestPartServletServerHttpRequest(multipartHttpServletRequest, partName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean checkRequired(MethodParameter parameter) {
        PartParam partParam = parameter.getParameterAnnotation(PartParam.class);
        return (partParam != null && partParam.required() && !parameter.isOptional());
    }

}
