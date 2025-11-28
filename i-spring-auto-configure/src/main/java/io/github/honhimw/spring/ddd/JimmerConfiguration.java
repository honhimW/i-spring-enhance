package io.github.honhimw.spring.ddd;

import io.github.honhimw.ddd.jimmer.JSqlClientFactoryBean;
import io.github.honhimw.ddd.jimmer.JSqlClientFactoryBuilder;
import io.github.honhimw.ddd.jimmer.JimmerProperties;
import io.github.honhimw.ddd.jimmer.JimmerTransactionManager;
import io.github.honhimw.ddd.jimmer.domain.AuditInterceptor;
import io.github.honhimw.ddd.jimmer.event.CRUDCallback;
import io.github.honhimw.ddd.jimmer.event.Callback;
import io.github.honhimw.ddd.jimmer.persist.PersistenceManagedTypes;
import io.github.honhimw.ddd.jimmer.persist.PersistenceManagedTypesScanner;
import io.github.honhimw.ddd.jimmer.util.PageUtils;
import jakarta.transaction.TransactionManager;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.AuditorAware;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

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

    @Bean
    Callback crudCallback(ApplicationEventPublisher publisher) {
        return new CRUDCallback(publisher);
    }

    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    AuditorAware<?> auditorAware() {
        return Optional::empty;
    }

    @Bean
    AuditInterceptor auditInterceptor(AuditorAware<?> auditorAware) {
        return new AuditInterceptor(auditorAware);
    }

    @Bean
    EnvironmentAware initializingPageUtilsFirstPageNo() {
        return environment -> {
            Boolean flag = environment.getProperty("spring.data.web.pageable.one-indexed-parameters", boolean.class, true);
            if (flag) {
                PageUtils.setFirstPageNo(1);
            } else {
                PageUtils.setFirstPageNo(0);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    PersistenceManagedTypes jimmerPersistenceManagedTypes(BeanFactory beanFactory, ResourceLoader resourceLoader) {
        List<String> packages = EntityScanPackages.get(beanFactory).getPackageNames();
        if (packages.isEmpty() && AutoConfigurationPackages.has(beanFactory)) {
            packages = AutoConfigurationPackages.get(beanFactory);
        }
        return new PersistenceManagedTypesScanner(resourceLoader).scan(StringUtils.toStringArray(packages));
    }

}
