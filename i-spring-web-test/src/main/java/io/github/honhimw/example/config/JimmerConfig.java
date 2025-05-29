package io.github.honhimw.example.config;

import io.github.honhimw.ddd.jimmer.EnableJimmerRepositories;
import io.github.honhimw.ddd.jimmer.JimmerTransactionManager;
import io.github.honhimw.example.domain.jimmer.Player;
import jakarta.transaction.TransactionManager;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hon_him
 * @since 2025-03-06
 */

@Configuration
@EnableJimmerRepositories(basePackageClasses = Player.class)
public class JimmerConfig {

    @Bean
    JimmerTransactionManager jimmerTransactionManager(JSqlClient sqlClient) {
        return new JimmerTransactionManager(sqlClient);
    }

}
