package io.github.honhimw.test.i18n;

import io.github.honhimw.core.IResult;
import io.github.honhimw.spring.web.common.i18n.I18nUtils;
import io.github.honhimw.util.tool.Formats;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.WebApp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;

import java.util.Locale;

/**
 * @author hon_him
 * @since 2023-07-17
 */

@Slf4j
@SpringBootTest(classes = WebApp.class)
public class I18nTests {

    @Test
    void getMessage() {
        {
            String message = I18nUtils.getMessage("common.missing", Locale.ENGLISH);
            String formatted = Formats.format(message, "123");
            log.info(formatted);
            Assert.state(StringUtils.equals(formatted, "id: 123 is not present"), "message should match");
        }
        {
            String message = I18nUtils.getMessage("common.missing", Locale.CHINESE);
            String formatted = Formats.format(message, "123");
            log.info(formatted);
            Assert.state(StringUtils.equals(formatted, "id: 123 不存在"), "message should match");
        }
    }

    @Test
    void IResultMessage() {
        {
            IResult<Object> result = IResult.okWithMsg("{common.missing}");
            LocaleContextHolder.setLocale(Locale.ENGLISH);
            I18nUtils.i18n(result);
            log.info(result.toString());
            Assert.state(StringUtils.equals(result.msg(), "id: {} is not present"), "message should match");
        }
        {
            IResult<Object> result = IResult.okWithMsg("{common.missing}");
            LocaleContextHolder.setLocale(Locale.CHINESE);
            I18nUtils.i18n(result);
            log.info(result.toString());
            Assert.state(StringUtils.equals(result.msg(), "id: {} 不存在"), "message should match");
        }
    }

}
