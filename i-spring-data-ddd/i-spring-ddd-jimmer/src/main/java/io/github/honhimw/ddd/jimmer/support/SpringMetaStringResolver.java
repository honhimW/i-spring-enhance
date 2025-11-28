package io.github.honhimw.ddd.jimmer.support;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.babyfish.jimmer.sql.meta.MetaStringResolver;
import org.springframework.util.StringValueResolver;

/**
 * @author hon_him
 * @since 2025-01-13
 */

public class SpringMetaStringResolver implements MetaStringResolver {

    private final StringValueResolver stringValueResolver;

    public SpringMetaStringResolver(StringValueResolver stringValueResolver) {
        this.stringValueResolver = stringValueResolver;
    }

    @Override
    @Nullable
    public String resolve(@NonNull String value) {
        return stringValueResolver.resolveStringValue(value);
    }
}
