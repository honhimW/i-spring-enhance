package io.github.honhimw.spring.ddd;

import io.github.honhimw.ddd.jimmer.TransactionCacheOperatorFlusherConfig;
import io.github.honhimw.ddd.jimmer.repository.JimmerRepositoriesConfig;
import org.babyfish.jimmer.Draft;
import org.babyfish.jimmer.jackson.ImmutableModule;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

/**
 * @author hon_him
 * @since 2025-03-06
 */

@AutoConfiguration(after = {
    DataSourceAutoConfiguration.class,
    TaskExecutionAutoConfiguration.class,
})
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnClass({
    Draft.class,
    JSqlClient.class,
})
@Import({
    JimmerConfiguration.class,
    TransactionCacheOperatorFlusherConfig.class,
    JimmerRepositoriesConfig.class,
})
public class JimmerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ImmutableModule.class)
    @ConditionalOnProperty(value = "spring.jimmer.jackson-module-enabled", havingValue = "true", matchIfMissing = true)
    public ImmutableModule immutableModule() {
        return new ImmutableModule();
    }

}
