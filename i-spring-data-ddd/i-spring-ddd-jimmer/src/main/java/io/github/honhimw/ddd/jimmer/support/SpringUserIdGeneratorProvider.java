package io.github.honhimw.ddd.jimmer.support;

import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.di.DefaultUserIdGeneratorProvider;
import org.babyfish.jimmer.sql.meta.UserIdGenerator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;

/**
 * @author hon_him
 * @since 2025-01-13
 */

public class SpringUserIdGeneratorProvider extends DefaultUserIdGeneratorProvider {

    private final ApplicationContext ctx;

    public SpringUserIdGeneratorProvider(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public UserIdGenerator<?> get(Class<UserIdGenerator<?>> type, JSqlClient sqlClient) throws Exception {
        ObjectProvider<UserIdGenerator<?>> beanProvider = ctx.getBeanProvider(type);
        return beanProvider.getIfUnique(() -> {
            try {
                return super.get(type, sqlClient);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        });
    }

    @Override
    public UserIdGenerator<?> get(String ref, JSqlClient sqlClient) throws Exception {
        return ctx.getBean(ref, UserIdGenerator.class);
    }
}
