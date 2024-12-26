package io.github.honhimw.spring.web.common.i18n;

import io.github.honhimw.core.IResult;
import io.github.honhimw.spring.SpringBeanUtils;
import io.github.honhimw.spring.web.reactive.ExchangeHolder;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author hon_him
 * @since 2023-07-17
 */

public class I18nUtils implements MessageSourceAware {

    @Getter
    private static MessageSource messageSource;

    @Nonnull
    public static Locale getLocale() {
        LocaleContext localeContext = null;
        if (SpringBeanUtils.isWebFlux()) {
            ServerWebExchange exchange = ExchangeHolder.getExchange();
            if (Objects.nonNull(exchange)) {
                localeContext = exchange.getLocaleContext();
            }
        } else if (SpringBeanUtils.isWebMvc()) {
            localeContext = LocaleContextHolder.getLocaleContext();
        }
        return LocaleContextHolder.getLocale(localeContext);
    }

    public static String getMessage(String code) {
        return getMessage(code, getLocale());
    }

    public static String getMessage(String code, Object[] args) {
        return getMessage(code, args, getLocale());
    }

    public static String getMessage(String code, Locale locale) {
        return getMessage(code, null, locale);
    }

    public static String getMessage(String code, Object[] args, Locale locale) {
        return messageSource.getMessage(code, args, locale);
    }

    /**
     * @see IResult#msg() Replace message content by code in '{xxx}' format
     */
    public static void i18n(IResult<?> result) {
        i18n(result, null);
    }

    /**
     * @see IResult#msg() Replace message content by code in '{xxx}' format
     */
    public static void i18n(IResult<?> result, List<Object> arguments) {
        if (Objects.nonNull(messageSource)) {
            Object[] args = null;
            if (CollectionUtils.isNotEmpty(arguments)) {
                args = arguments.toArray(Object[]::new);
            }

            String msg = result.msg();
            if (StringUtils.length(msg) > 2) {
                Optional<String> first = msg.lines().findFirst();
                if (first.isPresent()) {
                    String firstLine = first.get();
                    if (StringUtils.startsWith(firstLine, "{") && StringUtils.endsWith(firstLine, "}")) {
                        if (ArrayUtils.isEmpty(args)) {
                            args = msg.lines().skip(1).toArray();
                        }
                        String code = firstLine.substring(1, firstLine.length() - 1).trim();
                        msg = messageSource.getMessage(code, args, code, getLocale());
                        result.msg(msg);
                    }
                }
            }
        }
    }

    @Override
    public void setMessageSource(@Nonnull MessageSource messageSource) {
        I18nUtils.messageSource = messageSource;
    }
}
