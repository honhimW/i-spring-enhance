package io.github.honhimw.example.config;

import io.github.honhimw.ddd.jimmer.EnableJimmerRepositories;
import io.github.honhimw.ddd.jimmer.JimmerTransactionManager;
import io.github.honhimw.ddd.jimmer.acl.AclJimmerRepositoryFactoryBean;
import io.github.honhimw.example.domain.jimmer.Player;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * @author hon_him
 * @since 2025-03-06
 */

@Configuration
@EnableJimmerRepositories(basePackageClasses = Player.class, repositoryFactoryBeanClass = AclJimmerRepositoryFactoryBean.class)
public class JimmerConfig {

    @Bean
    JimmerTransactionManager jimmerTransactionManager(JSqlClient sqlClient) {
        return new JimmerTransactionManager(sqlClient);
    }

    @Bean
    AuditorAware<String> auditorProvider() {
        return () -> Optional.of("honhim");
    }

}
