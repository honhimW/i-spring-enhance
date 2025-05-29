package io.github.honhimw.ddd.jimmer.repository;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.babyfish.jimmer.Input;
import org.babyfish.jimmer.View;
import org.babyfish.jimmer.impl.util.CollectionUtils;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.TypedProp;
import org.babyfish.jimmer.sql.ast.mutation.*;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author hon_him
 * @since 2025-01-13
 */

@NoRepositoryBean
public interface JimmerRepository<E, ID> extends ListCrudRepository<E, ID>, ListPagingAndSortingRepository<E, ID> {

    ImmutableType type();

    Class<E> entityType();

    /*
     * For consumer
     */
    @Nullable
    E findNullable(ID id);

    @Nullable
    E findNullable(ID id, Fetcher<E> fetcher);

    @Nonnull
    @Override
    default Optional<E> findById(@Nonnull ID id) {
        return Optional.ofNullable(findNullable(id));
    }

    @Nonnull
    default Optional<E> findById(ID id, Fetcher<E> fetcher) {
        return Optional.ofNullable(findNullable(id, fetcher));
    }

    @Nonnull
    @Override
    List<E> findAllById(@Nonnull Iterable<ID> ids);

    List<E> findAllById(Iterable<ID> ids, Fetcher<E> fetcher);

    Map<ID, E> findMapByIds(Iterable<ID> ids);

    Map<ID, E> findMapByIds(Iterable<ID> ids, Fetcher<E> fetcher);

    @Nonnull
    @Override
    List<E> findAll();

    List<E> findAll(TypedProp.Scalar<?, ?> ... sortedProps);

    List<E> findAll(Fetcher<E> fetcher, TypedProp.Scalar<?, ?> ... sortedProps);

    @Nonnull
    @Override
    List<E> findAll(@Nonnull Sort sort);

    List<E> findAll(Fetcher<E> fetcher, Sort sort);

    Page<E> findAll(int pageIndex, int pageSize);

    Page<E> findAll(int pageIndex, int pageSize, Fetcher<E> fetcher);

    Page<E> findAll(int pageIndex, int pageSize, TypedProp.Scalar<?, ?> ... sortedProps);

    Page<E> findAll(int pageIndex, int pageSize, Fetcher<E> fetcher, TypedProp.Scalar<?, ?> ... sortedProps);

    Page<E> findAll(int pageIndex, int pageSize, Sort sort);

    Page<E> findAll(int pageIndex, int pageSize, Fetcher<E> fetcher, Sort sort);

    @Nonnull
    @Override
    Page<E> findAll(@Nonnull Pageable pageable);

    Page<E> findAll(Pageable pageable, Fetcher<E> fetcher);

    @Override
    default boolean existsById(@Nonnull ID id) {
        return findNullable(id) != null;
    }

    @Override
    long count();

    @Nonnull
    @Override
    default <S extends E> S save(@Nonnull S entity) {
        return saveCommand(entity).execute().getModifiedEntity();
    }

    @Nonnull
    default <S extends E> SimpleSaveResult<S> save(@Nonnull S entity, SaveMode mode) {
        return saveCommand(entity).setMode(mode).execute();
    }

    @Nonnull
    default <S extends E> SimpleSaveResult<S> save(@Nonnull S entity, AssociatedSaveMode associatedMode) {
        return saveCommand(entity).setAssociatedModeAll(associatedMode).execute();
    }

    @Nonnull
    default <S extends E> SimpleSaveResult<S> save(@Nonnull S entity, SaveMode mode, AssociatedSaveMode associatedMode) {
        return saveCommand(entity).setMode(mode).setAssociatedModeAll(associatedMode).execute();
    }

    @Nonnull
    default E save(@Nonnull Input<E> input) {
        return saveCommand(input.toEntity()).execute().getModifiedEntity();
    }

    @Nonnull
    default SimpleSaveResult<E> save(@Nonnull Input<E> input, SaveMode mode) {
        return saveCommand(input.toEntity()).setMode(mode).execute();
    }

    @Nonnull
    default SimpleSaveResult<E> save(@Nonnull Input<E> input, AssociatedSaveMode associatedMode) {
        return saveCommand(input.toEntity()).setAssociatedModeAll(associatedMode).execute();
    }

    @Nonnull
    default SimpleSaveResult<E> save(@Nonnull Input<E> input, SaveMode mode, AssociatedSaveMode associatedMode) {
        return saveCommand(input.toEntity()).setMode(mode).setAssociatedModeAll(associatedMode).execute();
    }

    @Nonnull
    default E insert(@Nonnull E entity) {
        return save(entity, SaveMode.INSERT_ONLY, AssociatedSaveMode.APPEND).getModifiedEntity();
    }

    @Nonnull
    default E insert(@Nonnull E entity, AssociatedSaveMode associatedMode) {
        return save(entity, SaveMode.INSERT_ONLY, associatedMode).getModifiedEntity();
    }

    @Nonnull
    default E insert(@Nonnull Input<E> input) {
        return save(input.toEntity(), SaveMode.INSERT_ONLY, AssociatedSaveMode.APPEND_IF_ABSENT).getModifiedEntity();
    }

    @Nonnull
    default E insert(@Nonnull Input<E> input, AssociatedSaveMode associatedMode) {
        return save(input.toEntity(), SaveMode.INSERT_ONLY, associatedMode).getModifiedEntity();
    }

    @Nonnull
    default E insertIfAbsent(@Nonnull E entity) {
        return save(entity, SaveMode.INSERT_IF_ABSENT, AssociatedSaveMode.APPEND_IF_ABSENT).getModifiedEntity();
    }

    @Nonnull
    default E insertIfAbsent(@Nonnull E entity, AssociatedSaveMode associatedMode) {
        return save(entity, SaveMode.INSERT_IF_ABSENT, associatedMode).getModifiedEntity();
    }

    @Nonnull
    default E insertIfAbsent(@Nonnull Input<E> input) {
        return save(input.toEntity(), SaveMode.INSERT_IF_ABSENT, AssociatedSaveMode.APPEND_IF_ABSENT).getModifiedEntity();
    }

    @Nonnull
    default E insertIfAbsent(@Nonnull Input<E> input, AssociatedSaveMode associatedMode) {
        return save(input.toEntity(), SaveMode.INSERT_IF_ABSENT, associatedMode).getModifiedEntity();
    }

    @Nonnull
    default E update(@Nonnull E entity) {
        return save(entity, SaveMode.UPDATE_ONLY, AssociatedSaveMode.UPDATE).getModifiedEntity();
    }

    @Nonnull
    default E update(@Nonnull E entity, AssociatedSaveMode associatedMode) {
        return save(entity, SaveMode.UPDATE_ONLY, associatedMode).getModifiedEntity();
    }

    @Nonnull
    default E update(@Nonnull Input<E> input) {
        return save(input.toEntity(), SaveMode.UPDATE_ONLY, AssociatedSaveMode.UPDATE).getModifiedEntity();
    }

    @Nonnull
    default E update(@Nonnull Input<E> input, AssociatedSaveMode associatedMode) {
        return save(input.toEntity(), SaveMode.UPDATE_ONLY, associatedMode).getModifiedEntity();
    }

    @Nonnull
    default E merge(@Nonnull E entity) {
        return save(entity, SaveMode.UPSERT, AssociatedSaveMode.MERGE).getModifiedEntity();
    }

    @Nonnull
    default E merge(@Nonnull E entity, AssociatedSaveMode associatedMode) {
        return save(entity, SaveMode.UPSERT, associatedMode).getModifiedEntity();
    }

    @Nonnull
    default E merge(@Nonnull Input<E> input) {
        return save(input.toEntity(), SaveMode.UPSERT, AssociatedSaveMode.MERGE).getModifiedEntity();
    }

    @Nonnull
    default E merge(@Nonnull Input<E> input, AssociatedSaveMode associatedMode) {
        return save(input.toEntity(), SaveMode.UPSERT, associatedMode).getModifiedEntity();
    }

    @Nonnull
    SimpleEntitySaveCommand<E> saveCommand(@Nonnull Input<E> input);

    @Nonnull
    <S extends E> SimpleEntitySaveCommand<S> saveCommand(@Nonnull S entity);

    @Nonnull
    @Override
    default <S extends E> List<S> saveAll(@Nonnull Iterable<S> entities) {
        return saveAll(entities, null, null);
    }

    @Nonnull
    default <S extends E> List<S> saveAll(@Nonnull Iterable<S> entities, SaveMode mode) {
        return saveAll(entities, mode, null);
    }

    @Nonnull
    default <S extends E> List<S> saveAll(@Nonnull Iterable<S> entities, AssociatedSaveMode mode) {
        return saveAll(entities, null, mode);
    }

    @Nonnull
    default <S extends E> List<S> saveAll(
        @Nonnull Iterable<S> entities,
        @Nullable SaveMode mode,
        @Nullable AssociatedSaveMode associatedMode
    ) {
        return saveEntitiesCommand(entities)
            .setMode(mode)
            .setAssociatedModeAll(associatedMode)
            .execute()
            .getItems()
            .stream()
            .map(BatchSaveResult.Item::getModifiedEntity)
            .toList();
    }

    @Nonnull
    default <S extends E> Iterable<S> saveInputs(@Nonnull Iterable<? extends Input<S>> entities) {
        return saveInputs(entities, null, null);
    }

    @Nonnull
    default <S extends E> Iterable<S> saveInputs(@Nonnull Iterable<? extends Input<S>> entities, SaveMode mode) {
        return saveInputs(entities, mode, null);
    }

    @Nonnull
    default <S extends E> Iterable<S> saveInputs(@Nonnull Iterable<? extends Input<S>> entities, AssociatedSaveMode mode) {
        return saveInputs(entities, null, mode);
    }

    @Nonnull
    default <S extends E> Iterable<S> saveInputs(
        @Nonnull Iterable<? extends Input<S>> entities,
        SaveMode mode,
        AssociatedSaveMode associatedMode
    ) {
        return saveInputsCommand(entities)
            .setMode(mode)
            .setAssociatedModeAll(associatedMode)
            .execute()
            .getItems()
            .stream()
            .map(BatchSaveResult.Item::getModifiedEntity)
            .toList();
    }

    @Nonnull
    <S extends E> BatchEntitySaveCommand<S> saveEntitiesCommand(@Nonnull Iterable<S> entities);

    @Nonnull
    default <S extends E> BatchEntitySaveCommand<S> saveInputsCommand(@Nonnull Iterable<? extends Input<S>> inputs) {
        return saveEntitiesCommand(CollectionUtils.map(inputs, Input::toEntity));
    }

    @Override
    default void delete(@Nonnull E entity) {
        delete(entity, DeleteMode.PHYSICAL);
    }

    int delete(@Nonnull E entity, DeleteMode mode);

    @Override
    default void deleteAll(@Nonnull Iterable<? extends E> entities) {
        deleteAll(entities, DeleteMode.PHYSICAL);
    }

    int deleteAll(@Nonnull Iterable<? extends E> entities, DeleteMode mode);

    @Override
    default void deleteById(@Nonnull ID id) {
        deleteById(id, DeleteMode.PHYSICAL);
    }

    int deleteById(@Nonnull ID id, DeleteMode mode);

    @Override
    default void deleteAllById(@Nonnull Iterable<? extends ID> ids) {
        deleteAllById(ids, DeleteMode.PHYSICAL);
    }

    int deleteAllById(Iterable<? extends ID> ids, DeleteMode mode);

    @Override
    void deleteAll();

    <V extends View<E>> Viewer<E, ID, V> viewer(Class<V> viewType);

    interface Viewer<E, ID, V extends View<E>> {

        V findNullable(ID id);

        List<V> findByIds(Iterable<ID> ids);

        Map<ID, V> findMapByIds(Iterable<ID> ids);

        List<V> findAll();

        List<V> findAll(TypedProp.Scalar<?, ?> ... sortedProps);

        List<V> findAll(Sort sort);

        Page<V> findAll(Pageable pageable);

        Page<V> findAll(int pageIndex, int pageSize);

        Page<V> findAll(int pageIndex, int pageSize, TypedProp.Scalar<?, ?> ... sortedProps);

        Page<V> findAll(int pageIndex, int pageSize, Sort sort);
    }
}
