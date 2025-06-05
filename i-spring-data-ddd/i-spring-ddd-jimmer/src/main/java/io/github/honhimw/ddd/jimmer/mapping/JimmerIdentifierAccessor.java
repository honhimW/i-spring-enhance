package io.github.honhimw.ddd.jimmer.mapping;

import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.springframework.data.mapping.IdentifierAccessor;

/**
 * @author honhimW
 * @since 2025-06-03
 */

public class JimmerIdentifierAccessor implements IdentifierAccessor {

    private final Object bean;

    private final ImmutableProp idProp;

    public JimmerIdentifierAccessor(Object bean, ImmutableProp idProp) {
        this.bean = bean;
        this.idProp = idProp;
    }

    @Override
    public Object getIdentifier() {
        return ImmutableObjects.get(bean, idProp);
    }
}
