package io.github.honhimw.ddd.jimmer.util;

import org.jspecify.annotations.Nullable;
import lombok.Getter;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.TargetLevel;
import org.babyfish.jimmer.sql.ast.Selection;
import org.babyfish.jimmer.sql.ast.impl.table.FetcherSelectionImpl;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.babyfish.jimmer.sql.fetcher.Field;
import org.babyfish.jimmer.sql.fetcher.FieldConfig;
import org.babyfish.jimmer.sql.fetcher.IdOnlyFetchType;
import org.babyfish.jimmer.sql.fetcher.impl.FetcherImpl;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author hon_him
 * @since 2025-02-28
 */

@SuppressWarnings("unused")
public class IFetcher<E> {

    private final Table<E> table;

    @Getter
    protected boolean configured = false;

    @Getter
    private Fetcher<E> delegate;

    protected IFetcher(@Nullable Table<E> table, Fetcher<E> delegate) {
        this.table = table;
        this.delegate = delegate;
    }

    public static <E> IFetcher<E> of(Fetcher<E> delegate) {
        return of(null, delegate);
    }

    public static <E> IFetcher<E> of(@Nullable Table<E> table, Fetcher<E> delegate) {
        return new IFetcher<>(table, delegate);
    }

    public Class<E> getJavaClass() {
        return delegate.getJavaClass();
    }

    public ImmutableType getImmutableType() {
        return delegate.getImmutableType();
    }

    public Map<String, Field> getFieldMap() {
        return delegate.getFieldMap();
    }

    public IFetcher<E> allScalarFields() {
        delegate = delegate.allScalarFields();
        configured = true;
        return this;
    }

    public IFetcher<E> allReferenceFields() {
        delegate = delegate.allReferenceFields();
        configured = true;
        return this;
    }

    public IFetcher<E> allTableFields() {
        delegate = delegate.allTableFields();
        configured = true;
        return this;
    }

    public IFetcher<E> add(String prop) {
        delegate = delegate.add(prop);
        configured = true;
        return this;
    }

    public IFetcher<E> add(String prop, Fetcher<?> childFetcher) {
        delegate = delegate.add(prop, childFetcher);
        configured = true;
        return this;
    }

    public IFetcher<E> add(String prop, Fetcher<?> childFetcher, Consumer<? extends FieldConfig<?, ? extends Table<?>>> loaderBlock) {
        delegate = delegate.add(prop, childFetcher, loaderBlock);
        configured = true;
        return this;
    }

    public IFetcher<E> addRecursion(String prop, Consumer<? extends FieldConfig<?, ? extends Table<?>>> loaderBlock) {
        delegate = delegate.addRecursion(prop, loaderBlock);
        configured = true;
        return this;
    }

    public IFetcher<E> add(String prop, IdOnlyFetchType referenceType) {
        delegate = delegate.add(prop, referenceType);
        configured = true;
        return this;
    }

    public IFetcher<E> remove(String prop) {
        delegate = delegate.remove(prop);
        configured = true;
        return this;
    }

    public String toString(boolean multiLine) {
        return delegate.toString(multiLine);
    }

    public Selection<E> toSelection() {
        return toSelection(this.table);
    }

    public <X> Selection<X> toSelection(Function<?, X> converter) {
        return toSelection(this.table, converter);
    }

    public Selection<E> toSelection(Table<?> t) {
        return toSelection(t, null);
    }

    public <X> Selection<X> toSelection(Table<?> t, Function<?, X> converter) {
        Objects.requireNonNull(t, "table should not be null");
        return new FetcherSelectionImpl<>(t, delegate, converter);
    }

    /**
     * All all fields into fetcher, including nested and scalar fields
     *
     * @return this
     */
    @SuppressWarnings("UnusedReturnValue")
    public IFetcher<E> allFields() {
        delegate = findAndAddFields(delegate);
        configured = true;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public IFetcher<E> clear() {
        Map<String, Field> fieldMap = delegate.getFieldMap();
        for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
            Field field = entry.getValue();
            if (!field.getProp().isId()) {
                delegate = delegate.remove(entry.getKey());
            }
        }
        configured = true;
        return this;
    }

    /**
     * Copy a new IFetcher
     *
     * @return new IFetcher
     */
    public IFetcher<E> copy() {
        IFetcher<E> copy = of(table, delegate);
        copy.configured = this.configured;
        return copy;
    }

    private static <T> Fetcher<T> findAndAddFields(Fetcher<T> fetcher) {
        Map<String, ImmutableProp> props = fetcher.getImmutableType().getProps();
        for (Map.Entry<String, ImmutableProp> entry : props.entrySet()) {
            String name = entry.getKey();
            ImmutableProp immutableProp = entry.getValue();
            if (immutableProp.isColumnDefinition()) {
                if (immutableProp.isAssociation(TargetLevel.OBJECT)) {
                    ImmutableType targetType = immutableProp.getTargetType();
                    if (targetType != null) {
                        Fetcher<?> nextFetcher = new FetcherImpl<>(targetType.getJavaClass());
                        nextFetcher = findAndAddFields(nextFetcher);
                        fetcher = fetcher.add(name, nextFetcher);
                    }
                } else {
                    fetcher = fetcher.add(name);
                }
            }
        }
        return fetcher;
    }

}
