package io.github.honhimw.spring.ddd;

import io.github.honhimw.ddd.jimmer.JSqlClientFactoryBean;
import io.github.honhimw.ddd.jimmer.JSqlClientFactoryBuilder;
import io.github.honhimw.ddd.jimmer.JimmerProperties;
import io.github.honhimw.ddd.jimmer.JimmerTransactionManager;
import io.github.honhimw.ddd.jimmer.domain.AuditInterceptor;
import io.github.honhimw.ddd.jimmer.event.CRUDCallback;
import io.github.honhimw.ddd.jimmer.event.Callback;
import io.github.honhimw.ddd.jimmer.repository.JimmerRepository;
import io.github.honhimw.ddd.jimmer.util.PageUtils;
import io.github.honhimw.ddl.DDLAutoRunner;
import jakarta.transaction.TransactionManager;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.AuditorAware;

import javax.sql.DataSource;
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
    @ConditionalOnProperty(value = "spring.jimmer.ddl-auto")
    DDLAutoRunner jimmerDdlAutoRunner(JSqlClient sqlClient, Environment environment, ObjectProvider<JimmerRepository<?, ?>> repositoryProvider) {
        return new DDLAutoRunner((JSqlClientImplementor) sqlClient, environment, repositoryProvider);
    }

    @Bean
    @ConditionalOnProperty(value = "spring.data.web.pageable.one-indexed-parameters", havingValue = "true", matchIfMissing = true)
    InitializingBean initializingPageUtilsFirstPageNo() {
        return () -> PageUtils.setFirstPageNo(1);
    }

//    @Value("${spring.data.web.pageable.one-indexed-parameters:true}")
//    private Boolean firstPageNo;
//
//    @PostConstruct
//    void setupPageUtils() {
//        if (firstPageNo) {
//            PageUtils.setFirstPageNo(1);
//        } else {
//            PageUtils.setFirstPageNo(0);
//        }
//    }

}
