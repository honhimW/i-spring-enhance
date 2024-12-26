package io.github.honhimw.test.i18n;

import io.github.honhimw.core.IResult;
import io.github.honhimw.example.WebApp;
import io.github.honhimw.spring.web.common.i18n.I18nUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
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
            Locale.setDefault(Locale.CHINESE);
            IResult<Object> result = IResult.okWithMsg("{okay}");
            I18nUtils.i18n(result);
            Assertions.assertThat(result.msg()).isEqualTo("成功");
        }
        {
            String message = I18nUtils.getMessage("okay", Locale.ENGLISH);
            Assertions.assertThat(message).isEqualTo("Okay");
        }
        {
            String message = I18nUtils.getMessage("common.missing", new Object[]{"123"}, Locale.CHINESE);
            Assertions.assertThat(message).isEqualTo("id: 123 不存在");
        }
    }

    @Test
    void resultMessage() {
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
