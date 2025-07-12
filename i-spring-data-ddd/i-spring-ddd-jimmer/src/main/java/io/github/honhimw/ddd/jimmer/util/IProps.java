package io.github.honhimw.ddd.jimmer.util;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.Strings;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.TargetLevel;
import org.babyfish.jimmer.sql.JoinType;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.PropExpression;
import org.babyfish.jimmer.sql.ast.impl.PropExpressionImpl;
import org.babyfish.jimmer.sql.ast.table.Table;

import java.util.Map;
import java.util.function.Function;

/**
 * @author hon_him
 * @since 2025-03-07
 */

public abstract class IProps {

    private IProps() {
    }

    public static IProps of(Table<?> tableProxy) {
        return new ITable(tableProxy);
    }

    public static IProps of(PropExpression.Embedded<?> embedded) {
        return new IEmbedded(embedded);
    }

    /**
     * Get prop expression by path
     *
     * @param iProps IProps
     * @param path   e.g. "name.lastName"
     * @param <R>    Any
     * @return PropExpression nullable
     */
    @Nullable
    public static <R> PropExpression<R> get(IProps iProps, String path) {
        Map<String, ImmutableProp> props = iProps.props();
        String[] split = path.split("\\.", 2);
        for (Map.Entry<String, ImmutableProp> entry : props.entrySet()) {
            String key = entry.getKey();
            if (Strings.CS.equals(split[0], key)) {
                if (split.length == 2) {
                    ImmutableProp prop = entry.getValue();
                    IProps next;
                    if (prop.isReference(TargetLevel.ENTITY)) {
                        next = iProps.join(key);
                    } else if (prop.isReference(TargetLevel.OBJECT)) {
                        next = iProps.embed(key);
                    } else {
                        return null;
                    }
                    return get(next, split[1]);
                } else {
                    return iProps.get(key);
                }
            }
        }
        return null;
    }

    public abstract Object unwrap();

    public abstract ImmutableType getType();

    public abstract <R> PropExpression<R> get(String prop);

    public abstract Map<String, ImmutableProp> props();

    public IProps join(String prop) {
        throw new UnsupportedOperationException("join is not supported");
    }

    public IProps join(String prop, JoinType type) {
        throw new UnsupportedOperationException("join is not supported");
    }

    public PropExpression.Str str(String prop) {
        PropExpression<?> objectPropExpression = get(prop);
        if (objectPropExpression instanceof PropExpression.Str str) {
            return str;
        } else {
            throw typeError(prop, "string", objectPropExpression.getClass().getName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Comparable<?>> PropExpression.Cmp<T> cmp(String prop) {
        PropExpression<?> objectPropExpression = get(prop);
        if (objectPropExpression instanceof PropExpression.Cmp<?> comparable) {
            return (PropExpression.Cmp<T>) comparable;
        } else {
            throw typeError(prop, "comparable", objectPropExpression.getClass().getName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Number & Comparable<T>> PropExpression.Num<T> num(String prop) {
        PropExpression<?> objectPropExpression = get(prop);
        if (objectPropExpression instanceof PropExpression.Num<?> number) {
            return (PropExpression.Num<T>) number;
        } else {
            throw typeError(prop, "number", objectPropExpression.getClass().getName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> PropExpression.Embedded<T> embedded(String prop) {
        PropExpression<?> objectPropExpression = get(prop);
        if (objectPropExpression instanceof PropExpression.Embedded<?> embedded) {
            return (PropExpression.Embedded<T>) embedded;
        } else {
            throw typeError(prop, "embedded", objectPropExpression.getClass().getName());
        }
    }

    public IProps embed(String prop) {
        PropExpression<?> objectPropExpression = get(prop);
        if (objectPropExpression instanceof PropExpression.Embedded<?> embedded) {
            return of(embedded);
        } else {
            throw typeError(prop, "embedded", objectPropExpression.getClass().getName());
        }
    }

    public <R> PropExpression<R> prop(String prop) {
        return get(prop);
    }

    public Predicate str(String prop, Function<PropExpression.Str, Predicate> block) {
        return block.apply(str(prop));
    }

    public <T extends Comparable<?>> Predicate cmp(String prop, Function<PropExpression.Cmp<T>, Predicate> block) {
        return block.apply(cmp(prop));
    }

    public <T extends Number & Comparable<T>> Predicate num(String prop, Function<PropExpression.Num<T>, Predicate> block) {
        return block.apply(num(prop));
    }

    public <T> Predicate embedded(String prop, Function<PropExpression.Embedded<T>, Predicate> block) {
        return block.apply(embedded(prop));
    }

    public Predicate embed(String prop, Function<IProps, Predicate> block) {
        return block.apply(embed(prop));
    }

    public <T> Predicate prop(String prop, Function<PropExpression<T>, Predicate> block) {
        return block.apply(prop(prop));
    }

    private static RuntimeException typeError(String prop, String expectedType, String actualType) {
        return new IllegalArgumentException(
            "`%s` is not a %s, but %s".formatted(prop, expectedType, actualType)
        );
    }

    private static class ITable extends IProps {

        private final Table<?> tableProxy;

        private ITable(Table<?> tableProxy) {
            this.tableProxy = tableProxy;
        }

        @Override
        public Table<?> unwrap() {
            return tableProxy;
        }

        @Override
        public <R> PropExpression<R> get(String prop) {
            return tableProxy.get(prop);
        }

        @Override
        public IProps join(String prop) {
            return of((Table<?>) tableProxy.join(prop));
        }

        @Override
        public IProps join(String prop, JoinType type) {
            return of((Table<?>) tableProxy.join(prop, type));
        }

        @Override
        public ImmutableType getType() {
            return tableProxy.getImmutableType();
        }

        @Override
        public Map<String, ImmutableProp> props() {
            return getType().getProps();
        }
    }

    private static class IEmbedded extends IProps {

        private final PropExpression.Embedded<?> embedded;

        private IEmbedded(PropExpression.Embedded<?> embedded) {
            this.embedded = embedded;
        }

        public PropExpression.Embedded<?> unwrap() {
            return embedded;
        }

        @Override
        public <R> PropExpression<R> get(String prop) {
            return embedded.get(prop);
        }

        @Override
        public ImmutableType getType() {
            if (embedded instanceof PropExpressionImpl<?> impl) {
                return impl.getDeepestProp().getTargetType();
            }
            throw new IllegalArgumentException("Unsupported embedded type: " + embedded);
        }

        @Override
        public Map<String, ImmutableProp> props() {
            return getType().getProps();
        }
    }

}
