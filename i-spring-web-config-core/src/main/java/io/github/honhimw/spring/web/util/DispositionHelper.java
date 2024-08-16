package io.github.honhimw.spring.web.util;

import io.github.honhimw.spring.SpringBeanUtils;
import io.github.honhimw.spring.web.reactive.ExchangeHolder;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMessage;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

/**
 * @author hon_him
 * @since 2024-08-07
 */

public class DispositionHelper {

    public static void attachment(String filename) {
        if (SpringBeanUtils.isWebMvc()) {
            servletAttachment(filename);
        } else if (SpringBeanUtils.isWebFlux()) {
            webFluxAttachment(filename);
        }
    }

    private static void servletAttachment(String filename) {
        Assert.isTrue(SpringBeanUtils.isWebMvc(), "current application does not running with web-mvc attachment.");
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Optional.ofNullable(requestAttributes)
            .map(ServletRequestAttributes::getResponse)
            .ifPresent(response -> response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).toString()));
    }

    private static void webFluxAttachment(String filename) {
        Assert.isTrue(SpringBeanUtils.isWebMvc(), "current application does not running with web-flux attachment.");
        Optional.ofNullable(ExchangeHolder.getExchange())
            .map(ServerWebExchange::getResponse)
            .map(HttpMessage::getHeaders)
            .ifPresent(httpHeaders -> httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).toString()));
    }

}
