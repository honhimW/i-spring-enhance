package io.github.honhimw.ddd.jimmer.mapping;

import org.babyfish.jimmer.meta.ImmutableProp;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.springframework.data.mapping.Association;
import org.springframework.data.util.TypeInformation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;

/**
 * @author honhimW
 * @since 2025-06-03
 */

public class JimmerPersistentPropertyImpl implements JimmerPersistentProperty {

    private final JimmerPersistentEntity<?> entity;

    private final ImmutableProp prop;

    public JimmerPersistentPropertyImpl(JimmerPersistentEntity<?> entity, ImmutableProp prop) {
        this.entity = entity;
        this.prop = prop;
    }

    @Override
    public @Nullable ImmutableProp getImmutableProp() {
        return this.prop;
    }

    @NonNull
    @Override
    public JimmerPersistentEntity<?> getOwner() {
        return this.entity;
    }

    @NonNull
    @Override
    public String getName() {
        return prop.getName();
    }

    @NonNull
    @Override
    public Class<?> getType() {
        return prop.getReturnClass();
    }

    @NonNull
    @Override
    public TypeInformation<?> getTypeInformation() {
        return TypeInformation.of(getType());
    }

    @NonNull
    @Override
    public Iterable<? extends TypeInformation<?>> getPersistentEntityTypeInformation() {
        return Collections.emptyList();
    }

    @Override
    public Method getGetter() {
        return null;
    }

    @Override
    public Method getSetter() {
        return null;
    }

    @Override
    public Method getWither() {
        return null;
    }

    @Override
    public Field getField() {
        return null;
    }

    @Override
    public String getSpelExpression() {
        return "";
    }

    @Override
    public Association<JimmerPersistentProperty> getAssociation() {
        return null;
    }

    @Override
    public boolean isEntity() {
        return false;
    }

    @Override
    public boolean isIdProperty() {
        return this.prop.isId();
    }

    @Override
    public boolean isVersionProperty() {
        return this.prop.isVersion();
    }

    @Override
    public boolean isCollectionLike() {
        return false;
    }

    @Override
    public boolean isMap() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isTransient() {
        return this.prop.isTransient();
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean isReadable() {
        return false;
    }

    @Override
    public boolean isImmutable() {
        return false;
    }

    @Override
    public boolean isAssociation() {
        return false;
    }

    @Override
    public Class<?> getComponentType() {
        return null;
    }

    @NonNull
    @Override
    public Class<?> getRawType() {
        return this.prop.getReturnClass();
    }

    @Override
    public Class<?> getMapValueType() {
        return null;
    }

    @NonNull
    @Override
    public Class<?> getActualType() {
        return this.prop.getReturnClass();
    }

    @Override
    public <A extends Annotation> A findAnnotation(@NonNull Class<A> annotationType) {
        return this.prop.getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A findPropertyOrOwnerAnnotation(@NonNull Class<A> annotationType) {
        return this.prop.getAnnotation(annotationType);
    }

    @Override
    public boolean isAnnotationPresent(@NonNull Class<? extends Annotation> annotationType) {
        Annotation annotation = this.prop.getAnnotation(annotationType);
        return annotation != null;
    }

    @Override
    public boolean usePropertyAccess() {
        return false;
    }

    @Override
    public Class<?> getAssociationTargetType() {
        return null;
    }

    @Override
    public TypeInformation<?> getAssociationTargetTypeInformation() {
        return null;
    }
}
