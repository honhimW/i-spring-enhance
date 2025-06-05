package io.github.honhimw.ddd.jimmer.mapping;

import jakarta.annotation.Nonnull;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.springframework.data.mapping.IdentifierAccessor;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;

import java.util.Map;
import java.util.Set;

/**
 * @author honhimW
 * @since 2025-06-03
 */

public class JimmerPersistentEntityImpl<T> extends BasicPersistentEntity<T, JimmerPersistentProperty>
    implements JimmerPersistentEntity<T> {

    private final ImmutableType immutableType;

    public JimmerPersistentEntityImpl(TypeInformation<T> information) {
        super(information, null);
        information.getType();
        this.immutableType = ImmutableType.tryGet(information.getType());
        this.addProperties();
    }

    private void addProperties() {
        Map<String, ImmutableProp> props = this.immutableType.getProps();
        Set<Map.Entry<String, ImmutableProp>> entries = props.entrySet();
        for (Map.Entry<String, ImmutableProp> entry : entries) {
            ImmutableProp prop = entry.getValue();
            addPersistentProperty(new JimmerPersistentPropertyImpl(this, prop));
        }
    }

    @Override
    public ImmutableProp getIdProp() {
        return immutableType.getIdProp();
    }

    @Override
    protected JimmerPersistentProperty returnPropertyIfBetterIdPropertyCandidateOrNull(JimmerPersistentProperty property) {
        return property.isIdProperty() ? property : null;
    }

    @Nonnull
    @Override
    public IdentifierAccessor getIdentifierAccessor(@Nonnull Object bean) {
        return new JimmerIdentifierAccessor(bean, immutableType.getIdProp());
    }

}
