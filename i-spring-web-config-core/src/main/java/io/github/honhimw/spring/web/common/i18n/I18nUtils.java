package io.github.honhimw.spring.web.common.i18n;

import io.github.honhimw.core.IResult;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hon_him
 * @since 2023-07-17
 */

public class I18nUtils implements MessageSourceAware {

    private static final Pattern PATTERN = Pattern.compile("^\\{(?<code>(?!.*\\.\\.)[\\w\\-][\\w\\-.]*[\\w\\-])}$");

    private static MessageSource messageSource;

    public static MessageSource getMessageSource() {
        return messageSource;
    }

    public static String getMessage(String code) {
        return getMessage(code, Objects.requireNonNullElseGet(LocaleContextHolder.getLocale(), Locale::getDefault));
    }

    public static String getMessage(String code, Object[] args) {
        return getMessage(code, args, Objects.requireNonNullElseGet(LocaleContextHolder.getLocale(), Locale::getDefault));
    }

    public static String getMessage(String code, Locale locale) {
        return getMessage(code, null, locale);
    }

    public static String getMessage(String code, Object[] args, Locale locale) {
        return messageSource.getMessage(code, args, locale);
    }

    /**
     * @see IResult#msg() Replace message content by code in '${xxx}' format
     */
    public static void i18n(IResult<?> result) {
        if (Objects.nonNull(messageSource)) {
            String msg = result.msg();
            if (StringUtils.isNotBlank(msg)) {
                Matcher matcher = PATTERN.matcher(msg);
                if (matcher.find()) {
                    String code = matcher.group("code");
                    msg = messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());
                    result.msg(msg);
                }
            }
        }
    }

    @Override
    public void setMessageSource(@Nonnull MessageSource messageSource) {
        I18nUtils.messageSource = messageSource;
    }
}
