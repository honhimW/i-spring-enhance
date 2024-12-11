package io.github.honhimw.spring.extend;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.spring.BuildIn;
import io.github.honhimw.spring.SpringBeanUtils;
import io.github.honhimw.spring.web.ResolverAutoConfiguration;
import io.github.honhimw.util.JsonUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * @author hon_him
 * @since 2023-06-01
 */

@AutoConfiguration(before = {JacksonAutoConfiguration.class})
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

    @Bean
    @Primary
    @ConditionalOnClass(ObjectMapper.class)
    @ConditionalOnProperty(value = "i.spring.json.enable", havingValue = "true", matchIfMissing = true)
    ObjectMapper extendObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = JsonUtils.mapper().copy();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        builder.createXmlMapper(false).configure(mapper);
        return mapper;
    }

}
