package io.github.honhimw.ddd.jimmer;

import org.babyfish.jimmer.sql.cache.TransactionCacheOperator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

/**
 * @author hon_him
 * @since 2025-01-13
 */

@ConditionalOnBean(TransactionCacheOperator.class)
@EnableScheduling
@Configuration
public class TransactionCacheOperatorFlusherConfig {

    @Bean
    public TransactionCacheOperatorFlusher transactionCacheOperatorFlusher(
        List<TransactionCacheOperator> transactionCacheOperators
    ) {
        return new TransactionCacheOperatorFlusher(transactionCacheOperators);
    }
}
