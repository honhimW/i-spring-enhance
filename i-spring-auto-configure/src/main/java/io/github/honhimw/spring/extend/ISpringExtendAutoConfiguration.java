package io.github.honhimw.spring.extend;

import io.github.honhimw.spring.BuildIn;
import io.github.honhimw.spring.SpringBeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * @author hon_him
 * @since 2023-06-01
 */

public class ISpringExtendAutoConfiguration {

    @Bean
    @ConditionalOnClass(BuildIn.class)
    @ConditionalOnBean(BuildIn.class)
    BuildInConfig buildInConfig(List<BuildIn> buildInList) {
        return new BuildInConfig(buildInList);
    }

    @Bean
    @ConditionalOnClass(SpringBeanUtils.class)
    @ConditionalOnMissingBean(SpringBeanUtils.class)
    SpringBeanUtils springBeanUtils() {
        return new SpringBeanUtils();
    }

}
