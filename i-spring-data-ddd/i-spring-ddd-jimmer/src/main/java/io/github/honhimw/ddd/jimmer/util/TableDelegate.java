package io.github.honhimw.ddd.jimmer.util;

import org.babyfish.jimmer.View;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.TypedProp;
import org.babyfish.jimmer.sql.JoinType;
import org.babyfish.jimmer.sql.ast.NumericExpression;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.PropExpression;
import org.babyfish.jimmer.sql.ast.Selection;
import org.babyfish.jimmer.sql.ast.query.Example;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.ast.table.TableEx;
import org.babyfish.jimmer.sql.fetcher.Fetcher;

import java.util.function.Function;

/**
 * @author hon_him
 * @since 2025-02-28
 */

public class TableDelegate<T> implements Table<T> {

    protected final Table<T> delegate;

    public TableDelegate(Table<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Predicate eq(Table<T> other) {
        return delegate.eq(other);
    }

    @Override
    public Predicate eq(Example<T> example) {
        return delegate.eq(example);
    }

    @Override
    public Predicate eq(T example) {
        return delegate.eq(example);
    }

    @Override
    public Predicate eq(View<T> view) {
        return delegate.eq(view);
    }

    @Override
    public Predicate isNull() {
        return delegate.isNull();
    }

    @Override
    public Predicate isNotNull() {
        return delegate.isNotNull();
    }

    @Override
    public NumericExpression<Long> count() {
        return delegate.count();
    }

    @Override
    public NumericExpression<Long> count(boolean distinct) {
        return delegate.count(distinct);
    }

    @Override
    public Selection<T> fetch(Fetcher<T> fetcher) {
        return delegate.fetch(fetcher);
    }

    @Override
    public <V extends View<T>> Selection<V> fetch(Class<V> viewType) {
        return delegate.fetch(viewType);
    }

    @Override
    public TableEx<T> asTableEx() {
        return delegate.asTableEx();
    }

    @Override
    public ImmutableType getImmutableType() {
        return delegate.getImmutableType();
    }

    @Override
    public <X> PropExpression<X> get(ImmutableProp prop) {
        return delegate.get(prop);
    }

    @Override
    public <X> PropExpression<X> get(String prop) {
        return delegate.get(prop);
    }

    @Override
    public <X> PropExpression<X> getId() {
        return delegate.getId();
    }

    @Override
    public <X> PropExpression<X> getAssociatedId(ImmutableProp prop) {
        return delegate.getAssociatedId(prop);
    }

    @Override
    public <X> PropExpression<X> getAssociatedId(String prop) {
        return delegate.getAssociatedId(prop);
    }

    @Override
    public <XT extends Table<?>> XT join(ImmutableProp prop) {
        return delegate.join(prop);
    }

    @Override
    public <XT extends Table<?>> XT join(String prop) {
        return delegate.join(prop);
    }

    @Override
    public <XT extends Table<?>> XT join(ImmutableProp prop, JoinType joinType) {
        return delegate.join(prop, joinType);
    }

    @Override
    public <XT extends Table<?>> XT join(String prop, JoinType joinType) {
        return delegate.join(prop, joinType);
    }

    @Override
    public <XT extends Table<?>> XT join(ImmutableProp prop, JoinType joinType, ImmutableType treatedAs) {
        return delegate.join(prop, joinType, treatedAs);
    }

    @Override
    public <XT extends Table<?>> XT join(String prop, JoinType joinType, ImmutableType treatedAs) {
        return delegate.join(prop, joinType, treatedAs);
    }

    @Override
    public <X> PropExpression<X> inverseGetAssociatedId(ImmutableProp prop) {
        return delegate.inverseGetAssociatedId(prop);
    }

    @Override
    public <XT extends Table<?>> XT inverseJoin(ImmutableProp prop) {
        return delegate.inverseJoin(prop);
    }

    @Override
    public <XT extends Table<?>> XT inverseJoin(ImmutableProp prop, JoinType joinType) {
        return delegate.inverseJoin(prop, joinType);
    }

    @Override
    public <XT extends Table<?>> XT inverseJoin(TypedProp.Association<?, ?> prop) {
        return delegate.inverseJoin(prop);
    }

    @Override
    public <XT extends Table<?>> XT inverseJoin(TypedProp.Association<?, ?> prop, JoinType joinType) {
        return delegate.inverseJoin(prop, joinType);
    }

    @Override
    public <XT extends Table<?>> XT inverseJoin(Class<XT> targetTableType, Function<XT, ? extends Table<?>> backPropBlock) {
        return delegate.inverseJoin(targetTableType, backPropBlock);
    }

    @Override
    public <XT extends Table<?>> XT inverseJoin(Class<XT> targetTableType, Function<XT, ? extends Table<?>> backPropBlock, JoinType joinType) {
        return delegate.inverseJoin(targetTableType, backPropBlock, joinType);
    }

    @Override
    public <XT extends Table<?>> Predicate exists(String prop, Function<XT, Predicate> block) {
        return delegate.exists(prop, block);
    }

    @Override
    public <XT extends Table<?>> Predicate exists(ImmutableProp prop, Function<XT, Predicate> block) {
        return delegate.exists(prop, block);
    }
}
