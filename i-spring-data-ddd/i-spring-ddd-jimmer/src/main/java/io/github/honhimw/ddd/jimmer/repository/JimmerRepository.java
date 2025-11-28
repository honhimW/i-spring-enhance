package io.github.honhimw.ddd.jimmer.repository;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.babyfish.jimmer.Input;
import org.babyfish.jimmer.View;
import org.babyfish.jimmer.impl.util.CollectionUtils;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.TypedProp;
import org.babyfish.jimmer.sql.ast.mutation.*;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;
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

    TableProxy<E> tableProxy();

    Fetcher<E> fetcher();

    /*
     * For consumer
     */
    @Nullable
    E findNullable(ID id);

    @Nullable
    E findNullable(ID id, Fetcher<E> fetcher);

    @NonNull
    @Override
    default Optional<E> findById(@NonNull ID id) {
        return Optional.ofNullable(findNullable(id));
    }

    @NonNull
    default Optional<E> findById(ID id, Fetcher<E> fetcher) {
        return Optional.ofNullable(findNullable(id, fetcher));
    }

    @NonNull
    @Override
    List<E> findAllById(@NonNull Iterable<ID> ids);

    List<E> findAllById(Iterable<ID> ids, Fetcher<E> fetcher);

    Map<ID, E> findMapByIds(Iterable<ID> ids);

    Map<ID, E> findMapByIds(Iterable<ID> ids, Fetcher<E> fetcher);

    @NonNull
    @Override
    List<E> findAll();

    List<E> findAll(TypedProp.Scalar<?, ?> ... sortedProps);

    List<E> findAll(Fetcher<E> fetcher, TypedProp.Scalar<?, ?> ... sortedProps);

    @NonNull
    @Override
    List<E> findAll(@NonNull Sort sort);

    List<E> findAll(Fetcher<E> fetcher, Sort sort);

    Page<E> findAll(int pageIndex, int pageSize);

    Page<E> findAll(int pageIndex, int pageSize, Fetcher<E> fetcher);

    Page<E> findAll(int pageIndex, int pageSize, TypedProp.Scalar<?, ?> ... sortedProps);

    Page<E> findAll(int pageIndex, int pageSize, Fetcher<E> fetcher, TypedProp.Scalar<?, ?> ... sortedProps);

    Page<E> findAll(int pageIndex, int pageSize, Sort sort);

    Page<E> findAll(int pageIndex, int pageSize, Fetcher<E> fetcher, Sort sort);

    @NonNull
    @Override
    Page<E> findAll(@NonNull Pageable pageable);

    Page<E> findAll(Pageable pageable, Fetcher<E> fetcher);

    @Override
    default boolean existsById(@NonNull ID id) {
        return findNullable(id) != null;
    }

    @Override
    long count();

    @NonNull
    @Override
    default <S extends E> S save(@NonNull S entity) {
        return saveCommand(entity).execute().getModifiedEntity();
    }

    @NonNull
    default <S extends E> SimpleSaveResult<S> save(@NonNull S entity, SaveMode mode) {
        return saveCommand(entity).setMode(mode).execute();
    }

    @NonNull
    default <S extends E> SimpleSaveResult<S> save(@NonNull S entity, AssociatedSaveMode associatedMode) {
        return saveCommand(entity).setAssociatedModeAll(associatedMode).execute();
    }

    @NonNull
    default <S extends E> SimpleSaveResult<S> save(@NonNull S entity, SaveMode mode, AssociatedSaveMode associatedMode) {
        return saveCommand(entity).setMode(mode).setAssociatedModeAll(associatedMode).execute();
    }

    @NonNull
    default E save(@NonNull Input<E> input) {
        return saveCommand(input.toEntity()).execute().getModifiedEntity();
    }

    @NonNull
    default SimpleSaveResult<E> save(@NonNull Input<E> input, SaveMode mode) {
        return saveCommand(input.toEntity()).setMode(mode).execute();
    }

    @NonNull
    default SimpleSaveResult<E> save(@NonNull Input<E> input, AssociatedSaveMode associatedMode) {
        return saveCommand(input.toEntity()).setAssociatedModeAll(associatedMode).execute();
    }

    @NonNull
    default SimpleSaveResult<E> save(@NonNull Input<E> input, SaveMode mode, AssociatedSaveMode associatedMode) {
        return saveCommand(input.toEntity()).setMode(mode).setAssociatedModeAll(associatedMode).execute();
    }

    @NonNull
    default E insert(@NonNull E entity) {
        return save(entity, SaveMode.INSERT_ONLY, AssociatedSaveMode.APPEND).getModifiedEntity();
    }

    @NonNull
    default E insert(@NonNull E entity, AssociatedSaveMode associatedMode) {
        return save(entity, SaveMode.INSERT_ONLY, associatedMode).getModifiedEntity();
    }

    @NonNull
    default E insert(@NonNull Input<E> input) {
        return save(input.toEntity(), SaveMode.INSERT_ONLY, AssociatedSaveMode.APPEND_IF_ABSENT).getModifiedEntity();
    }

    @NonNull
    default E insert(@NonNull Input<E> input, AssociatedSaveMode associatedMode) {
        return save(input.toEntity(), SaveMode.INSERT_ONLY, associatedMode).getModifiedEntity();
    }

    @NonNull
    default E insertIfAbsent(@NonNull E entity) {
        return save(entity, SaveMode.INSERT_IF_ABSENT, AssociatedSaveMode.APPEND_IF_ABSENT).getModifiedEntity();
    }

    @NonNull
    default E insertIfAbsent(@NonNull E entity, AssociatedSaveMode associatedMode) {
        return save(entity, SaveMode.INSERT_IF_ABSENT, associatedMode).getModifiedEntity();
    }

    @NonNull
    default E insertIfAbsent(@NonNull Input<E> input) {
        return save(input.toEntity(), SaveMode.INSERT_IF_ABSENT, AssociatedSaveMode.APPEND_IF_ABSENT).getModifiedEntity();
    }

    @NonNull
    default E insertIfAbsent(@NonNull Input<E> input, AssociatedSaveMode associatedMode) {
        return save(input.toEntity(), SaveMode.INSERT_IF_ABSENT, associatedMode).getModifiedEntity();
    }

    @NonNull
    default E update(@NonNull E entity) {
        return save(entity, SaveMode.UPDATE_ONLY, AssociatedSaveMode.UPDATE).getModifiedEntity();
    }

    @NonNull
    default E update(@NonNull E entity, AssociatedSaveMode associatedMode) {
        return save(entity, SaveMode.UPDATE_ONLY, associatedMode).getModifiedEntity();
    }

    @NonNull
    default E update(@NonNull Input<E> input) {
        return save(input.toEntity(), SaveMode.UPDATE_ONLY, AssociatedSaveMode.UPDATE).getModifiedEntity();
    }

    @NonNull
    default E update(@NonNull Input<E> input, AssociatedSaveMode associatedMode) {
        return save(input.toEntity(), SaveMode.UPDATE_ONLY, associatedMode).getModifiedEntity();
    }

    @NonNull
    default E merge(@NonNull E entity) {
        return save(entity, SaveMode.UPSERT, AssociatedSaveMode.MERGE).getModifiedEntity();
    }

    @NonNull
    default E merge(@NonNull E entity, AssociatedSaveMode associatedMode) {
        return save(entity, SaveMode.UPSERT, associatedMode).getModifiedEntity();
    }

    @NonNull
    default E merge(@NonNull Input<E> input) {
        return save(input.toEntity(), SaveMode.UPSERT, AssociatedSaveMode.MERGE).getModifiedEntity();
    }

    @NonNull
    default E merge(@NonNull Input<E> input, AssociatedSaveMode associatedMode) {
        return save(input.toEntity(), SaveMode.UPSERT, associatedMode).getModifiedEntity();
    }

    @NonNull
    SimpleEntitySaveCommand<E> saveCommand(@NonNull Input<E> input);

    @NonNull
    <S extends E> SimpleEntitySaveCommand<S> saveCommand(@NonNull S entity);

    @NonNull
    @Override
    default <S extends E> List<S> saveAll(@NonNull Iterable<S> entities) {
        return saveAll(entities, null, null);
    }

    @NonNull
    default <S extends E> List<S> saveAll(@NonNull Iterable<S> entities, SaveMode mode) {
        return saveAll(entities, mode, null);
    }

    @NonNull
    default <S extends E> List<S> saveAll(@NonNull Iterable<S> entities, AssociatedSaveMode mode) {
        return saveAll(entities, null, mode);
    }

    @NonNull
    default <S extends E> List<S> saveAll(
        @NonNull Iterable<S> entities,
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

    @NonNull
    default <S extends E> Iterable<S> saveInputs(@NonNull Iterable<? extends Input<S>> entities) {
        return saveInputs(entities, null, null);
    }

    @NonNull
    default <S extends E> Iterable<S> saveInputs(@NonNull Iterable<? extends Input<S>> entities, SaveMode mode) {
        return saveInputs(entities, mode, null);
    }

    @NonNull
    default <S extends E> Iterable<S> saveInputs(@NonNull Iterable<? extends Input<S>> entities, AssociatedSaveMode mode) {
        return saveInputs(entities, null, mode);
    }

    @NonNull
    default <S extends E> Iterable<S> saveInputs(
        @NonNull Iterable<? extends Input<S>> entities,
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

    @NonNull
    <S extends E> BatchEntitySaveCommand<S> saveEntitiesCommand(@NonNull Iterable<S> entities);

    @NonNull
    default <S extends E> BatchEntitySaveCommand<S> saveInputsCommand(@NonNull Iterable<? extends Input<S>> inputs) {
        return saveEntitiesCommand(CollectionUtils.map(inputs, Input::toEntity));
    }

    @Override
    default void delete(@NonNull E entity) {
        delete(entity, DeleteMode.PHYSICAL);
    }

    int delete(@NonNull E entity, DeleteMode mode);

    @Override
    default void deleteAll(@NonNull Iterable<? extends E> entities) {
        deleteAll(entities, DeleteMode.PHYSICAL);
    }

    int deleteAll(@NonNull Iterable<? extends E> entities, DeleteMode mode);

    @Override
    default void deleteById(@NonNull ID id) {
        deleteById(id, DeleteMode.PHYSICAL);
    }

    int deleteById(@NonNull ID id, DeleteMode mode);

    @Override
    default void deleteAllById(@NonNull Iterable<? extends ID> ids) {
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
