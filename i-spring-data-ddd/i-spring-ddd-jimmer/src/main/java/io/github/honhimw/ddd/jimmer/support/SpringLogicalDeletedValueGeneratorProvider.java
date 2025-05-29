package io.github.honhimw.ddd.jimmer.support;

import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.di.LogicalDeletedValueGeneratorProvider;
import org.babyfish.jimmer.sql.meta.LogicalDeletedValueGenerator;
import org.springframework.context.ApplicationContext;

/**
 * @author hon_him
 * @since 2025-01-13
 */

public class SpringLogicalDeletedValueGeneratorProvider implements LogicalDeletedValueGeneratorProvider {

    private final ApplicationContext ctx;

    public SpringLogicalDeletedValueGeneratorProvider(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public LogicalDeletedValueGenerator<?> get(String ref, JSqlClient sqlClient) throws Exception {
        return ctx.getBean(ref, LogicalDeletedValueGenerator.class);
    }
}
