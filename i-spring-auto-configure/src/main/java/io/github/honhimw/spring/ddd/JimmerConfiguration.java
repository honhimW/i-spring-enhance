package io.github.honhimw.spring.ddd;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.ddd.jimmer.JSqlClientFactoryBean;
import io.github.honhimw.ddd.jimmer.JSqlClientFactoryBuilder;
import io.github.honhimw.ddd.jimmer.JimmerProperties;
import io.github.honhimw.ddd.jimmer.JimmerTransactionManager;
import io.github.honhimw.ddd.jimmer.support.DialectDetector;
import io.github.honhimw.ddd.jimmer.support.SpringMetaStringResolver;
import jakarta.transaction.TransactionManager;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.di.AopProxyProvider;
import org.babyfish.jimmer.sql.meta.MetaStringResolver;
import org.babyfish.jimmer.sql.runtime.DefaultExecutor;
import org.babyfish.jimmer.sql.runtime.Executor;
import org.babyfish.jimmer.sql.runtime.SqlFormatter;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author hon_him
 * @since 2025-03-05
 */

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JimmerProperties.class)
class JimmerConfiguration {

    private final JimmerProperties properties;

    JimmerConfiguration(JimmerProperties jimmerProperties) {
        this.properties = jimmerProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    JSqlClientFactoryBuilder jSqlClientFactoryBuilder() {
        return new JSqlClientFactoryBuilder(properties);
    }

    @Bean("sqlClient")
    @ConditionalOnMissingBean(JSqlClientFactoryBean.class)
    JSqlClientFactoryBean sqlClientFactoryBean(JSqlClientFactoryBuilder factoryBuilder, ObjectProvider<DataSource> dataSource) {
        return factoryBuilder
            .dataSource(dataSource.getIfAvailable())
            .build();
    }

    @Bean("transactionManager")
    @ConditionalOnMissingBean(value = TransactionManager.class, name = "transactionManager")
    JimmerTransactionManager transactionManager(JSqlClient sqlClient) {
        return new JimmerTransactionManager(sqlClient);
    }

}
