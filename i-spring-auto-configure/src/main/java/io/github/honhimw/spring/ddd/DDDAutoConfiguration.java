package io.github.honhimw.spring.ddd;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author hon_him
 * @since 2022-10-25
 */
@AutoConfigureAfter({
    DataSourceAutoConfiguration.class,
})
@Configuration(proxyBeanMethods = false)
@Import({
    DataOperationConfiguration.JpaConfiguration.class,
})
public class DDDAutoConfiguration {

}
