package io.github.honhimw.ddd.jimmer.support;

import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.TransientResolver;
import org.babyfish.jimmer.sql.di.TransientResolverProvider;
import org.springframework.context.ApplicationContext;

/**
 * @author hon_him
 * @since 2025-01-13
 */

public class SpringTransientResolverProvider implements TransientResolverProvider {

    private final ApplicationContext ctx;

    public SpringTransientResolverProvider(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public TransientResolver<?, ?> get(
        Class<TransientResolver<?, ?>> type,
        JSqlClient sqlClient
    ) throws Exception {
        return ctx.getBean(type);
    }

    @Override
    public TransientResolver<?, ?> get(String ref, JSqlClient sqlClient) throws Exception {
        return ctx.getBean(ref, TransientResolver.class);
    }
}
