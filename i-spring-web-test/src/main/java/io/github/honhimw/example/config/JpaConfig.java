package io.github.honhimw.example.config;

import io.github.honhimw.ddd.jpa.acl.AclJpaRepositoryFactoryBean;
import io.github.honhimw.example.domain.jimmer.BuildInJimmerData;
import io.github.honhimw.example.domain.jpa.BuildInData;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author hon_him
 * @since 2024-11-29
 */

@Configuration
@EnableJpaAuditing
@ComponentScan(basePackageClasses = BuildInData.class)
@EntityScan(basePackageClasses = {BuildInData.class, BuildInJimmerData.class})
@EnableJpaRepositories(basePackageClasses = BuildInData.class, repositoryFactoryBeanClass = AclJpaRepositoryFactoryBean.class)
public class JpaConfig {

    @Primary
    @Bean
    public PlatformTransactionManager transactionManager(
        ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManagerCustomizers.ifAvailable((customizers) -> customizers.customize(transactionManager));
        return transactionManager;
    }

}
