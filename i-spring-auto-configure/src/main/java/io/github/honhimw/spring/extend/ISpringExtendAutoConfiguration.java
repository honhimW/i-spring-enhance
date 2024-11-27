package io.github.honhimw.spring.extend;

import io.github.honhimw.spring.BuildIn;
import io.github.honhimw.spring.SpringBeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author hon_him
 * @since 2023-06-01
 */

public class ISpringExtendAutoConfiguration {

    @Bean(name = "buildInConfig")
    @ConditionalOnClass(BuildIn.class)
    @ConditionalOnBean(BuildIn.class)
    @ConditionalOnMissingBean(BuildInConfig.class)
    BuildInConfig buildInConfig(ObjectProvider<BuildIn> buildInList) {
        return new BuildInConfig(buildInList);
    }

    @Bean(name = "springBeanUtils")
    @ConditionalOnClass(SpringBeanUtils.class)
    @ConditionalOnMissingBean(SpringBeanUtils.class)
    SpringBeanUtils springBeanUtils() {
        return new SpringBeanUtils();
    }

    @Bean(name = "bootstrapExecutor")
    @ConditionalOnMissingBean(name = "bootstrapExecutor")
    BootstrapExecutor bootstrapExecutor() {
        return new BootstrapExecutor();
    }

    @Bean(name = "applicationPhaseExtendPublisher")
    @ConditionalOnMissingBean(ApplicationPhaseExtendPublisher.class)
    ApplicationPhaseExtendPublisher applicationPhaseExtendPublisher() {
        return new ApplicationPhaseExtendPublisher();
    }

}
