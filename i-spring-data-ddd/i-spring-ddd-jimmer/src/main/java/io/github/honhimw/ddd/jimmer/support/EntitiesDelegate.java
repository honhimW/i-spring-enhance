package io.github.honhimw.ddd.jimmer.support;

import org.babyfish.jimmer.Input;
import org.babyfish.jimmer.View;
import org.babyfish.jimmer.meta.TypedProp;
import org.babyfish.jimmer.sql.Entities;
import org.babyfish.jimmer.sql.ast.mutation.*;
import org.babyfish.jimmer.sql.ast.query.Example;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.jspecify.annotations.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2025-03-21
 */

@SuppressWarnings("deprecation")
public class EntitiesDelegate implements Entities {

    private final Entities delegate;

    public EntitiesDelegate(Entities delegate) {
        this.delegate = delegate;
    }

    @NonNull
    @Override
    public Entities forUpdate() {
        return delegate.forUpdate();
    }

    @Override
    public Entities forConnection(Connection con) {
        return delegate.forConnection(con);
    }

    @Override
    public <T> @Nullable T findById(Class<T> type, Object id) {
        return delegate.findById(type, id);
    }

    @Override
    public <T> @NotNull T findOneById(Class<T> type, Object id) {
        return delegate.findOneById(type, id);
    }

    @Override
    public @NotNull <T> List<T> findByIds(Class<T> type, Iterable<?> ids) {
        return delegate.findByIds(type, ids);
    }

    @Override
    public @NotNull <ID, T> Map<ID, T> findMapByIds(Class<T> type, Iterable<ID> ids) {
        return delegate.findMapByIds(type, ids);
    }

    @Override
    public <E> @Nullable E findById(Fetcher<E> fetcher, Object id) {
        return delegate.findById(fetcher, id);
    }

    @Override
    public <E> @NotNull E findOneById(Fetcher<E> fetcher, Object id) {
        return delegate.findOneById(fetcher, id);
    }

    @Override
    public @NotNull <E> List<E> findByIds(Fetcher<E> fetcher, Iterable<?> ids) {
        return delegate.findByIds(fetcher, ids);
    }

    @Override
    public @NotNull <ID, E> Map<ID, E> findMapByIds(Fetcher<E> fetcher, Iterable<ID> ids) {
        return delegate.findMapByIds(fetcher, ids);
    }

    @Override
    public <T> List<T> findAll(Class<T> type) {
        return delegate.findAll(type);
    }

    @Override
    public <T> List<T> findAll(Class<T> type, TypedProp.Scalar<?, ?>... sortedProps) {
        return delegate.findAll(type, sortedProps);
    }

    @Override
    public <E> List<E> findAll(Fetcher<E> fetcher, TypedProp.Scalar<?, ?>... sortedProps) {
        return delegate.findAll(fetcher, sortedProps);
    }

    @Override
    public <E> List<E> findByExample(Example<E> example, TypedProp.Scalar<?, ?>... sortedProps) {
        return delegate.findByExample(example, sortedProps);
    }

    @Override
    public <E> List<E> findByExample(Example<E> example, Fetcher<E> fetcher, TypedProp.Scalar<?, ?>... sortedProps) {
        return delegate.findByExample(example, fetcher, sortedProps);
    }

    @Override
    public <E, V extends View<E>> List<V> findExample(Class<V> viewType, Example<E> example, TypedProp.Scalar<?, ?>... sortedProps) {
        return delegate.findExample(viewType, example, sortedProps);
    }

    @Override
    public DeleteResult delete(Class<?> type, Object id) {
        return delegate.delete(type, id);
    }

    @Override
    public DeleteResult delete(Class<?> type, Object id, DeleteMode mode) {
        return delegate.delete(type, id, mode);
    }

    @Override
    public DeleteCommand deleteCommand(Class<?> type, Object id) {
        return delegate.deleteCommand(type, id);
    }

    @Override
    public DeleteCommand deleteCommand(Class<?> type, Object id, DeleteMode mode) {
        return delegate.deleteCommand(type, id, mode);
    }

    @Override
    public DeleteResult deleteAll(Class<?> type, Iterable<?> ids) {
        return delegate.deleteAll(type, ids);
    }

    @Override
    public DeleteResult deleteAll(Class<?> type, Iterable<?> ids, DeleteMode mode) {
        return delegate.deleteAll(type, ids, mode);
    }

    @Override
    public DeleteCommand deleteAllCommand(Class<?> type, Iterable<?> ids) {
        return delegate.deleteAllCommand(type, ids);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> save(E entity, AssociatedSaveMode associatedMode) {
        return delegate.save(entity, associatedMode);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> save(E entity, SaveMode mode) {
        return delegate.save(entity, mode);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> saveEntities(Iterable<E> entities, AssociatedSaveMode associatedMode) {
        return delegate.saveEntities(entities, associatedMode);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> saveEntities(Iterable<E> entities, SaveMode mode) {
        return delegate.saveEntities(entities, mode);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> save(Input<E> input, AssociatedSaveMode associatedMode) {
        return delegate.save(input, associatedMode);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> save(Input<E> input, SaveMode mode) {
        return delegate.save(input, mode);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> saveInputs(Iterable<? extends Input<E>> inputs, AssociatedSaveMode associatedMode) {
        return delegate.saveInputs(inputs, associatedMode);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> saveInputs(Iterable<? extends Input<E>> inputs, SaveMode mode) {
        return delegate.saveInputs(inputs, mode);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> save(E entity, AssociatedSaveMode associatedMode, Fetcher<E> fetcher) {
        return delegate.save(entity, associatedMode, fetcher);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> save(E entity, SaveMode mode, Fetcher<E> fetcher) {
        return delegate.save(entity, mode, fetcher);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> saveEntities(Iterable<E> entities, AssociatedSaveMode associatedMode, Fetcher<E> fetcher) {
        return delegate.saveEntities(entities, associatedMode, fetcher);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> saveEntities(Iterable<E> entities, SaveMode mode, Fetcher<E> fetcher) {
        return delegate.saveEntities(entities, mode, fetcher);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> save(Input<E> input, AssociatedSaveMode associatedMode, Fetcher<E> fetcher) {
        return delegate.save(input, associatedMode, fetcher);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> save(Input<E> input, SaveMode mode, Fetcher<E> fetcher) {
        return delegate.save(input, mode, fetcher);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> saveInputs(Iterable<? extends Input<E>> inputs, AssociatedSaveMode associatedMode, Fetcher<E> fetcher) {
        return delegate.saveInputs(inputs, associatedMode, fetcher);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> saveInputs(Iterable<? extends Input<E>> inputs, SaveMode mode, Fetcher<E> fetcher) {
        return delegate.saveInputs(inputs, mode, fetcher);
    }

    @Deprecated
    @Override
    public <E, V extends View<E>> SimpleSaveResult.View<E, V> save(E entity, AssociatedSaveMode associatedMode, Class<V> viewType) {
        return delegate.save(entity, associatedMode, viewType);
    }

    @Deprecated
    @Override
    public <E, V extends View<E>> SimpleSaveResult.View<E, V> save(E entity, SaveMode mode, Class<V> viewType) {
        return delegate.save(entity, mode, viewType);
    }

    @Deprecated
    @Override
    public <E, V extends View<E>> BatchSaveResult.View<E, V> saveEntities(Iterable<E> entities, AssociatedSaveMode associatedMode, Class<V> viewType) {
        return delegate.saveEntities(entities, associatedMode, viewType);
    }

    @Deprecated
    @Override
    public <E, V extends View<E>> BatchSaveResult.View<E, V> saveEntities(Iterable<E> entities, SaveMode mode, Class<V> viewType) {
        return delegate.saveEntities(entities, mode, viewType);
    }

    @Deprecated
    @Override
    public <E, V extends View<E>> SimpleSaveResult.View<E, V> save(Input<E> input, AssociatedSaveMode associatedMode, Class<V> viewType) {
        return delegate.save(input, associatedMode, viewType);
    }

    @Deprecated
    @Override
    public <E, V extends View<E>> SimpleSaveResult.View<E, V> save(Input<E> input, SaveMode mode, Class<V> viewType) {
        return delegate.save(input, mode, viewType);
    }

    @Deprecated
    @Override
    public <E, V extends View<E>> BatchSaveResult.View<E, V> saveInputs(Iterable<? extends Input<E>> inputs, AssociatedSaveMode associatedMode, Class<V> viewType) {
        return delegate.saveInputs(inputs, associatedMode, viewType);
    }

    @Deprecated
    @Override
    public <E, V extends View<E>> BatchSaveResult.View<E, V> saveInputs(Iterable<? extends Input<E>> inputs, SaveMode mode, Class<V> viewType) {
        return delegate.saveInputs(inputs, mode, viewType);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> insert(@NotNull E entity) {
        return delegate.insert(entity);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> insert(@NotNull E entity, @NotNull AssociatedSaveMode associatedMode) {
        return delegate.insert(entity, associatedMode);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> insert(@NotNull Input<E> input) {
        return delegate.insert(input);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> insert(@NotNull Input<E> input, @NotNull AssociatedSaveMode associatedMode) {
        return delegate.insert(input, associatedMode);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> insertIfAbsent(@NotNull E entity) {
        return delegate.insertIfAbsent(entity);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> insertIfAbsent(@NotNull E entity, @NotNull AssociatedSaveMode associatedMode) {
        return delegate.insertIfAbsent(entity, associatedMode);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> insertIfAbsent(@NotNull Input<E> input) {
        return delegate.insertIfAbsent(input);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> insertIfAbsent(@NotNull Input<E> input, @NotNull AssociatedSaveMode associatedMode) {
        return delegate.insertIfAbsent(input, associatedMode);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> update(@NotNull E entity) {
        return delegate.update(entity);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> update(@NotNull E entity, @NotNull AssociatedSaveMode associatedMode) {
        return delegate.update(entity, associatedMode);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> update(@NotNull Input<E> input) {
        return delegate.update(input);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> update(@NotNull Input<E> input, @NotNull AssociatedSaveMode associatedMode) {
        return delegate.update(input, associatedMode);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> merge(@NotNull E entity) {
        return delegate.merge(entity);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> merge(@NotNull Input<E> input) {
        return delegate.merge(input);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> insertEntities(@NotNull Iterable<E> entities, @NotNull AssociatedSaveMode associatedMode) {
        return delegate.insertEntities(entities, associatedMode);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> insertEntities(@NotNull Iterable<E> entities) {
        return delegate.insertEntities(entities);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> insertInputs(@NotNull Iterable<? extends Input<E>> inputs, @NotNull AssociatedSaveMode associatedMode) {
        return delegate.insertInputs(inputs, associatedMode);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> insertInputs(@NotNull Iterable<? extends Input<E>> inputs) {
        return delegate.insertInputs(inputs);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> insertEntitiesIfAbsent(@NotNull Iterable<E> entities, @NotNull AssociatedSaveMode associatedMode) {
        return delegate.insertEntitiesIfAbsent(entities, associatedMode);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> insertEntitiesIfAbsent(@NotNull Iterable<E> entities) {
        return delegate.insertEntitiesIfAbsent(entities);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> insertInputsIfAbsent(@NotNull Iterable<? extends Input<E>> inputs, @NotNull AssociatedSaveMode associatedMode) {
        return delegate.insertInputsIfAbsent(inputs, associatedMode);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> insertInputsIfAbsent(@NotNull Iterable<? extends Input<E>> inputs) {
        return delegate.insertInputsIfAbsent(inputs);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> updateEntities(@NotNull Iterable<E> entities, @NotNull AssociatedSaveMode associatedMode) {
        return delegate.updateEntities(entities, associatedMode);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> updateEntities(@NotNull Iterable<E> entities) {
        return delegate.updateEntities(entities);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> updateInputs(@NotNull Iterable<? extends Input<E>> inputs, @NotNull AssociatedSaveMode associatedMode) {
        return delegate.updateInputs(inputs, associatedMode);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> updateInputs(@NotNull Iterable<? extends Input<E>> inputs) {
        return delegate.updateInputs(inputs);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> mergeEntities(@NotNull Iterable<E> entities) {
        return delegate.mergeEntities(entities);
    }

    @Deprecated
    @Override
    public <E> BatchSaveResult<E> mergeInputs(@NotNull Iterable<? extends Input<E>> inputs) {
        return delegate.mergeInputs(inputs);
    }

    @Deprecated
    @Override
    public <E> SimpleSaveResult<E> save(E entity, Fetcher<E> fetcher) {
        return delegate.save(entity, fetcher);
    }

    @Override
    public <E> SimpleSaveResult<E> save(E entity, SaveMode mode, AssociatedSaveMode associatedMode, Fetcher<E> fetcher) {
        return delegate.save(entity, mode, associatedMode, fetcher);
    }

    @Override
    public <E> BatchSaveResult<E> saveEntities(Iterable<E> entities, Fetcher<E> fetcher) {
        return delegate.saveEntities(entities, fetcher);
    }

    @Override
    public <E> BatchSaveResult<E> saveEntities(Iterable<E> entities, SaveMode mode, AssociatedSaveMode associatedMode, Fetcher<E> fetcher) {
        return delegate.saveEntities(entities, mode, associatedMode, fetcher);
    }

    @Override
    public <E> SimpleSaveResult<E> save(Input<E> input, Fetcher<E> fetcher) {
        return delegate.save(input, fetcher);
    }

    @Override
    public <E> SimpleSaveResult<E> save(Input<E> input, SaveMode mode, AssociatedSaveMode associatedMode, Fetcher<E> fetcher) {
        return delegate.save(input, mode, associatedMode, fetcher);
    }

    @Override
    public <E> BatchSaveResult<E> saveInputs(Iterable<? extends Input<E>> inputs, Fetcher<E> fetcher) {
        return delegate.saveInputs(inputs, fetcher);
    }

    @Override
    public <E> BatchSaveResult<E> saveInputs(Iterable<? extends Input<E>> inputs, SaveMode mode, AssociatedSaveMode associatedMode, Fetcher<E> fetcher) {
        return delegate.saveInputs(inputs, mode, associatedMode, fetcher);
    }

    @Override
    public <E, V extends View<E>> SimpleSaveResult.View<E, V> save(E entity, Class<V> viewType) {
        return delegate.save(entity, viewType);
    }

    @Override
    public <E, V extends View<E>> SimpleSaveResult.View<E, V> save(E entity, SaveMode mode, AssociatedSaveMode associatedMode, Class<V> viewType) {
        return delegate.save(entity, mode, associatedMode, viewType);
    }

    @Override
    public <E, V extends View<E>> BatchSaveResult.View<E, V> saveEntities(Iterable<E> entities, Class<V> viewType) {
        return delegate.saveEntities(entities, viewType);
    }

    @Override
    public <E, V extends View<E>> BatchSaveResult.View<E, V> saveEntities(Iterable<E> entities, SaveMode mode, AssociatedSaveMode associatedMode, Class<V> viewType) {
        return delegate.saveEntities(entities, mode, associatedMode, viewType);
    }

    @Override
    public <E, V extends View<E>> SimpleSaveResult.View<E, V> save(Input<E> input, Class<V> viewType) {
        return delegate.save(input, viewType);
    }

    @Override
    public <E, V extends View<E>> SimpleSaveResult.View<E, V> save(Input<E> input, SaveMode mode, AssociatedSaveMode associatedMode, Class<V> viewType) {
        return delegate.save(input, mode, associatedMode, viewType);
    }

    @Override
    public <E, V extends View<E>> BatchSaveResult.View<E, V> saveInputs(Iterable<? extends Input<E>> inputs, Class<V> viewType) {
        return delegate.saveInputs(inputs, viewType);
    }

    @Override
    public <E, V extends View<E>> BatchSaveResult.View<E, V> saveInputs(Iterable<? extends Input<E>> inputs, SaveMode mode, AssociatedSaveMode associatedMode, Class<V> viewType) {
        return delegate.saveInputs(inputs, mode, associatedMode, viewType);
    }

    @Override
    public <E> SimpleSaveResult<E> save(E entity) {
        return delegate.save(entity);
    }

    @Override
    public <E> SimpleSaveResult<E> save(E entity, SaveMode mode, AssociatedSaveMode associatedMode) {
        return delegate.save(entity, mode, associatedMode);
    }

    @Override
    public <E> BatchSaveResult<E> saveEntities(Iterable<E> entities) {
        return delegate.saveEntities(entities);
    }

    @Override
    public <E> BatchSaveResult<E> saveEntities(Iterable<E> entities, SaveMode mode, AssociatedSaveMode associatedMode) {
        return delegate.saveEntities(entities, mode, associatedMode);
    }

    @Override
    public <E> SimpleSaveResult<E> save(Input<E> input) {
        return delegate.save(input);
    }

    @Override
    public <E> SimpleSaveResult<E> save(Input<E> input, SaveMode mode, AssociatedSaveMode associatedMode) {
        return delegate.save(input, mode, associatedMode);
    }

    @Override
    public <E> BatchSaveResult<E> saveInputs(Iterable<? extends Input<E>> inputs) {
        return delegate.saveInputs(inputs);
    }

    @Override
    public <E> BatchSaveResult<E> saveInputs(Iterable<? extends Input<E>> inputs, SaveMode mode, AssociatedSaveMode associatedMode) {
        return delegate.saveInputs(inputs, mode, associatedMode);
    }

    @Override
    public <E> SimpleEntitySaveCommand<E> saveCommand(E entity) {
        return delegate.saveCommand(entity);
    }

    @Override
    public <E> SimpleEntitySaveCommand<E> saveCommand(Input<E> input) {
        return delegate.saveCommand(input);
    }

    @Override
    public <E> BatchEntitySaveCommand<E> saveEntitiesCommand(Iterable<E> entities) {
        return delegate.saveEntitiesCommand(entities);
    }

    @Override
    public <E> BatchEntitySaveCommand<E> saveInputsCommand(Iterable<? extends Input<E>> inputs) {
        return delegate.saveInputsCommand(inputs);
    }
}
