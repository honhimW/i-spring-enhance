package io.github.honhimw.ddd.jimmer.repository;

import io.github.honhimw.ddd.jimmer.convert.QueryByExamplePredicateBuilder;
import io.github.honhimw.ddd.jimmer.domain.Specification;
import io.github.honhimw.ddd.jimmer.support.SpringOrders;
import io.github.honhimw.ddd.jimmer.support.SpringPageFactory;
import io.github.honhimw.ddd.jimmer.util.IFetcher;
import io.github.honhimw.ddd.jimmer.util.IProps;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.Input;
import org.babyfish.jimmer.View;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.TypedProp;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.PropExpression;
import org.babyfish.jimmer.sql.ast.Selection;
import org.babyfish.jimmer.sql.ast.impl.mutation.MutableDeleteImpl;
import org.babyfish.jimmer.sql.ast.impl.mutation.Mutations;
import org.babyfish.jimmer.sql.ast.impl.query.FilterLevel;
import org.babyfish.jimmer.sql.ast.impl.query.MutableRootQueryImpl;
import org.babyfish.jimmer.sql.ast.impl.table.FetcherSelectionImpl;
import org.babyfish.jimmer.sql.ast.impl.table.TableImplementor;
import org.babyfish.jimmer.sql.ast.mutation.*;
import org.babyfish.jimmer.sql.ast.query.ConfigurableRootQuery;
import org.babyfish.jimmer.sql.ast.query.MutableRootQuery;
import org.babyfish.jimmer.sql.ast.query.Order;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.ast.table.TableEx;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;
import org.babyfish.jimmer.sql.fetcher.DtoMetadata;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.babyfish.jimmer.sql.fetcher.impl.FetcherImpl;
import org.babyfish.jimmer.sql.runtime.ExecutionPurpose;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.query.FluentQuery;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author hon_him
 * @since 2025-02-25
 */

public class SimpleJimmerRepository<E, ID> implements JimmerRepositoryImplementation<E, ID> {

    protected final JSqlClientImplementor sqlClient;
    protected final Class<E> entityType;
    protected final ImmutableType immutableType;

    protected final TableProxy<E> table;
    protected final TableEx<E> tableEx;
    protected final Fetcher<E> fetcher;

    private ProjectionFactory projectionFactory;

    @SuppressWarnings("unchecked")
    public SimpleJimmerRepository(RepositoryInformation metadata, JSqlClientImplementor sqlClient) {
        this.sqlClient = sqlClient;
        this.entityType = (Class<E>) metadata.getDomainType();
        this.immutableType = ImmutableType.tryGet(this.entityType);

        try {
            this.table = (TableProxy<E>) Class.forName(entityType.getPackageName() + "." + entityType.getSimpleName() + "Table").getField("$").get(null);
            this.tableEx = (TableEx<E>) Class.forName(entityType.getPackageName() + "." + entityType.getSimpleName() + "TableEx").getField("$").get(null);
            this.fetcher = (Fetcher<E>) Class.forName(entityType.getPackageName() + "." + entityType.getSimpleName() + "Fetcher").getField("$").get(null);
        } catch (Exception e) {
            throw new IllegalStateException("jimmer annotation-processor generated class not found for: %s".formatted(this.entityType.getName()), e);
        }
    }

    @Override
    public ImmutableType type() {
        return immutableType;
    }

    @Override
    public Class<E> entityType() {
        return entityType;
    }

    @Override
    public E findNullable(ID id) {
        return sqlClient.getEntities().findById(entityType, id);
    }

    @Override
    public E findNullable(ID id, Fetcher<E> fetcher) {
        if (fetcher == null) {
            return findNullable(id);
        }
        return sqlClient.getEntities().findById(fetcher, id);
    }

    @Nonnull
    @Override
    public List<E> findAllById(@Nonnull Iterable<ID> ids) {
        return sqlClient.getEntities().findByIds(entityType, ids);
    }

    @Override
    public List<E> findAllById(Iterable<ID> ids, Fetcher<E> fetcher) {
        if (fetcher == null) {
            return findAllById(ids);
        }
        return sqlClient.getEntities().findByIds(fetcher, ids);
    }

    @Override
    public Map<ID, E> findMapByIds(Iterable<ID> ids) {
        return sqlClient.getEntities().findMapByIds(entityType, ids);
    }

    @Override
    public Map<ID, E> findMapByIds(Iterable<ID> ids, Fetcher<E> fetcher) {
        if (fetcher == null) {
            return findMapByIds(ids);
        }
        return sqlClient.getEntities().findMapByIds(fetcher, ids);
    }

    @Nonnull
    @Override
    public List<E> findAll() {
        return createQuery(null, (Function<?, E>) null, null, null).execute();
    }

    @Override
    public List<E> findAll(TypedProp.Scalar<?, ?>... sortedProps) {
        return createQuery(null, (Function<?, E>) null, sortedProps, null).execute();
    }

    @Override
    public List<E> findAll(Fetcher<E> fetcher, TypedProp.Scalar<?, ?>... sortedProps) {
        return createQuery(fetcher, (Function<?, E>) null, sortedProps, null).execute();
    }

    @Nonnull
    @Override
    public List<E> findAll(@Nonnull Sort sort) {
        return createQuery(null, (Function<?, E>) null, null, sort).execute();
    }

    @Override
    public List<E> findAll(Fetcher<E> fetcher, Sort sort) {
        return createQuery(fetcher, (Function<?, E>) null, null, sort).execute();
    }

    @Override
    public Page<E> findAll(int pageIndex, int pageSize) {
        return this.<E>createQuery(null, null, null, null)
            .fetchPage(pageIndex, pageSize, SpringPageFactory.getInstance());
    }

    @Override
    public Page<E> findAll(int pageIndex, int pageSize, Fetcher<E> fetcher) {
        return this.<E>createQuery(fetcher, null, null, null)
            .fetchPage(pageIndex, pageSize, SpringPageFactory.getInstance());
    }

    @Override
    public Page<E> findAll(int pageIndex, int pageSize, TypedProp.Scalar<?, ?>... sortedProps) {
        return this.<E>createQuery(null, null, sortedProps, null)
            .fetchPage(pageIndex, pageSize, SpringPageFactory.getInstance());
    }

    @Override
    public Page<E> findAll(int pageIndex, int pageSize, Fetcher<E> fetcher, TypedProp.Scalar<?, ?>... sortedProps) {
        return this.<E>createQuery(fetcher, null, sortedProps, null)
            .fetchPage(pageIndex, pageSize, SpringPageFactory.getInstance());
    }

    @Override
    public Page<E> findAll(int pageIndex, int pageSize, Sort sort) {
        return this.<E>createQuery(null, null, null, sort)
            .fetchPage(pageIndex, pageSize, SpringPageFactory.getInstance());
    }

    @Override
    public Page<E> findAll(int pageIndex, int pageSize, Fetcher<E> fetcher, Sort sort) {
        return this.<E>createQuery(fetcher, null, null, sort)
            .fetchPage(pageIndex, pageSize, SpringPageFactory.getInstance());
    }

    @Nonnull
    @Override
    public Page<E> findAll(@Nonnull Pageable pageable) {
        return this.<E>createQuery(null, null, null, pageable.getSort())
            .fetchPage(pageable.getPageNumber(), pageable.getPageSize(), SpringPageFactory.getInstance());
    }

    @Override
    public Page<E> findAll(Pageable pageable, Fetcher<E> fetcher) {
        return this.<E>createQuery(fetcher, null, null, pageable.getSort())
            .fetchPage(pageable.getPageNumber(), pageable.getPageSize(), SpringPageFactory.getInstance());
    }

    @Override
    public long count() {
        return createQuery(null, null, null, null).fetchUnlimitedCount();
    }

    @Nonnull
    @Override
    public SimpleEntitySaveCommand<E> saveCommand(@Nonnull Input<E> input) {
        return sqlClient.getEntities().saveCommand(input);
    }

    @Nonnull
    @Override
    public <S extends E> SimpleEntitySaveCommand<S> saveCommand(@Nonnull S entity) {
        return sqlClient.getEntities().saveCommand(entity);
    }

    @Nonnull
    @Override
    public <S extends E> BatchEntitySaveCommand<S> saveEntitiesCommand(@Nonnull Iterable<S> entities) {
        return sqlClient
            .getEntities()
            .saveEntitiesCommand(entities);
    }

    @Override
    public int delete(@Nonnull E entity, DeleteMode mode) {
        return sqlClient.getEntities().delete(
            entityType,
            ImmutableObjects.get(entity, immutableType.getIdProp().getId()),
            mode
        ).getAffectedRowCount(AffectedTable.of(immutableType));
    }

    @Override
    public int deleteAll(@Nonnull Iterable<? extends E> entities, DeleteMode mode) {
        Iterator<? extends E> iterator = entities.iterator();
        Stream.Builder<Object> builder = Stream.builder();
        while (iterator.hasNext()) {
            E next = iterator.next();
            builder.add(ImmutableObjects.get(next, immutableType.getIdProp().getId()));
        }
        List<Object> ids = builder.build().toList();
        return sqlClient.getEntities().deleteAll(entityType, ids, mode).getAffectedRowCount(AffectedTable.of(immutableType));
    }

    @Override
    public int deleteById(@Nonnull ID id, DeleteMode mode) {
        return sqlClient
            .getEntities()
            .delete(entityType, id, mode)
            .getAffectedRowCount(AffectedTable.of(immutableType));
    }

    @Override
    public int deleteAllById(Iterable<? extends ID> ids, DeleteMode mode) {
        return sqlClient
            .getEntities()
            .deleteAll(entityType, ids, mode)
            .getAffectedRowCount(AffectedTable.of(immutableType));
    }

    @Override
    public void deleteAll() {
        Mutations
            .createDelete(sqlClient, immutableType, (d, t) -> {
            })
            .execute();
    }

    @Nonnull
    @Override
    public <S extends E> Optional<S> findOne(@Nonnull Example<S> example) {
        return createQuery(example, null).fetchOptional();
    }

    @Nonnull
    @Override
    public <S extends E> List<S> findAll(@Nonnull Example<S> example) {
        return createQuery(example, null).execute();
    }

    @Nonnull
    @Override
    public <S extends E> List<S> findAll(@Nonnull Example<S> example, @Nonnull Sort sort) {
        return createQuery(example, sort).execute();
    }

    @Nonnull
    @Override
    public <S extends E> Page<S> findAll(@Nonnull Example<S> example, @Nonnull Pageable pageable) {
        return createQuery(example, pageable.getSort())
            .fetchPage(pageable.getPageNumber(), pageable.getPageSize(), SpringPageFactory.getInstance());
    }

    @Override
    public <S extends E> long count(@Nonnull Example<S> example) {
        return createQuery(example, null).fetchUnlimitedCount();
    }

    @Override
    public <S extends E> boolean exists(@Nonnull Example<S> example) {
        return createQuery(example, null).exists();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <S extends E, R> R findBy(@Nonnull Example<S> example, @Nonnull Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        FetchableFluentQueryBySpecification<E, R> fluent = new FetchableFluentQueryBySpecification<>(new ExampleSpecification<>(example), fetcher, Sort.unsorted(), entityType, (Class<R>) entityType, table, sqlClient, getProjectionFactory());
        return queryFunction.apply((FluentQuery.FetchableFluentQuery<S>) fluent);
    }

    @Override
    public <V extends View<E>> Viewer<E, ID, V> viewer(Class<V> viewType) {
        return new ViewerImpl<>(viewType);
    }

    @SuppressWarnings("unchecked")
    private <X> ConfigurableRootQuery<?, X> createQuery(
        @Nullable Fetcher<?> fetcher,
        @Nullable Function<?, X> converter,
        @Nullable TypedProp.Scalar<?, ?>[] sortedProps,
        @Nullable Sort sort
    ) {
        MutableRootQueryImpl<Table<?>> query =
            new MutableRootQueryImpl<>(sqlClient, immutableType, ExecutionPurpose.QUERY, FilterLevel.DEFAULT);
        TableImplementor<?> table = query.getTableImplementor();
        if (sortedProps != null) {
            for (TypedProp.Scalar<?, ?> sortedProp : sortedProps) {
                if (sortedProp == null) {
                    continue;
                }
                if (!sortedProp.unwrap().getDeclaringType().isAssignableFrom(immutableType)) {
                    throw new IllegalArgumentException(
                        "The sorted field \"" +
                        sortedProp +
                        "\" does not belong to the type \"" +
                        immutableType +
                        "\" or its super types"
                    );
                }
                PropExpression<?> expr = table.get(sortedProp.unwrap());
                Order astOrder;
                if (sortedProp.isDesc()) {
                    astOrder = expr.desc();
                } else {
                    astOrder = expr.asc();
                }
                if (sortedProp.isNullsFirst()) {
                    astOrder = astOrder.nullsFirst();
                }
                if (sortedProp.isNullsLast()) {
                    astOrder = astOrder.nullsLast();
                }
                query.orderBy(astOrder);
            }
        }
        if (sort != null) {
            query.orderBy(SpringOrders.toOrders(table, sort));
        }
        return query.select(
            fetcher != null ?
                new FetcherSelectionImpl<>(table, fetcher, converter) :
                (Selection<X>) table
        );
    }

    private <S> ConfigurableRootQuery<?, S> createQuery(Example<S> example, @Nullable Sort sort) {
        try {
            Class<S> probeType = example.getProbeType();
            ImmutableType immutableType = ImmutableType.get(probeType);
            MutableRootQueryImpl<TableProxy<S>> query =
                new MutableRootQueryImpl<>(sqlClient, immutableType, ExecutionPurpose.QUERY, FilterLevel.DEFAULT);

            TableProxy<S> _table = query.getTable();
            FetcherImpl<S> _fetcher = new FetcherImpl<>(probeType);

            ExampleSpecification<S> spec = new ExampleSpecification<>(example);
            IFetcher<S> allFields = IFetcher.of(_table, _fetcher).allFields();
            Predicate predicate = spec.toPredicate(IProps.of(_table), query, allFields);
            query.where(predicate);

            if (sort != null) {
                query.orderBy(SpringOrders.toOrders(table, sort));
            }

            return query.select(allFields.toSelection());
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> TableProxy<T> getTableProxy(Class<T> type) {
        try {
            return (TableProxy<T>) Class.forName(type.getPackageName() + "." + type.getSimpleName() + "Table").getField("$").get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException("");
        }
    }

    private class ViewerImpl<V extends View<E>> implements Viewer<E, ID, V> {

        private final Class<V> viewType;

        private final DtoMetadata<E, V> metadata;

        private ViewerImpl(Class<V> viewType) {
            this.viewType = viewType;
            this.metadata = DtoMetadata.of(viewType);
        }

        @Override
        public V findNullable(ID id) {
            return sqlClient.getEntities().findById(viewType, id);
        }

        @Override
        public List<V> findByIds(Iterable<ID> ids) {
            return sqlClient.getEntities().findByIds(viewType, ids);
        }

        @Override
        public Map<ID, V> findMapByIds(Iterable<ID> ids) {
            return sqlClient.getEntities().findMapByIds(viewType, ids);
        }

        @Override
        public List<V> findAll() {
            return createQuery(metadata.getFetcher(), metadata.getConverter(), null, null).execute();
        }

        @Override
        public List<V> findAll(TypedProp.Scalar<?, ?>... sortedProps) {
            return createQuery(metadata.getFetcher(), metadata.getConverter(), sortedProps, null).execute();
        }

        @Override
        public List<V> findAll(Sort sort) {
            return createQuery(metadata.getFetcher(), metadata.getConverter(), null, sort).execute();
        }

        @Override
        public Page<V> findAll(Pageable pageable) {
            return createQuery(metadata.getFetcher(), metadata.getConverter(), null, pageable.getSort())
                .fetchPage(pageable.getPageNumber(), pageable.getPageSize(), SpringPageFactory.getInstance());
        }

        @Override
        public Page<V> findAll(int pageIndex, int pageSize) {
            return createQuery(metadata.getFetcher(), metadata.getConverter(), null, null)
                .fetchPage(pageIndex, pageSize, SpringPageFactory.getInstance());
        }

        @Override
        public Page<V> findAll(int pageIndex, int pageSize, TypedProp.Scalar<?, ?>... sortedProps) {
            return createQuery(metadata.getFetcher(), metadata.getConverter(), sortedProps, null)
                .fetchPage(pageIndex, pageSize, SpringPageFactory.getInstance());
        }

        @Override
        public Page<V> findAll(int pageIndex, int pageSize, Sort sort) {
            return createQuery(metadata.getFetcher(), metadata.getConverter(), null, sort)
                .fetchPage(pageIndex, pageSize, SpringPageFactory.getInstance());
        }
    }

    @Override
    public List<E> findAll(Specification.Query spec) {
        MutableRootQuery<TableProxy<E>> query = this.sqlClient.createQuery(this.table);
        return applySpecification(query, spec).execute();
    }

    @Override
    public Optional<E> findOne(Specification.Query spec) {
        MutableRootQuery<TableProxy<E>> query = this.sqlClient.createQuery(this.table);
        return applySpecification(query, spec).fetchOptional();
    }

    @Override
    public Page<E> findAll(Specification.Query spec, Pageable pageable) {
        MutableRootQuery<TableProxy<E>> query = this.sqlClient.createQuery(this.table);
        ConfigurableRootQuery<TableProxy<E>, E> rootQuery = applySpecification(query, spec);
        return rootQuery.fetchPage(pageable.getPageNumber(), pageable.getPageSize(), SpringPageFactory.getInstance());
    }

    @Override
    public List<E> findAll(Specification.Query spec, Sort sort) {
        MutableRootQuery<TableProxy<E>> query = this.sqlClient.createQuery(this.table)
            .orderBy(SpringOrders.toOrders(table, sort));
        return applySpecification(query, spec).execute();
    }

    @Override
    public long count(Specification.Query spec) {
        MutableRootQuery<TableProxy<E>> query = this.sqlClient.createQuery(this.table);
        return applySpecification(query, spec).fetchUnlimitedCount();
    }

    @Override
    public long delete(Specification.Delete spec) {
        MutableDeleteImpl delete = new MutableDeleteImpl(this.sqlClient, immutableType);
        Predicate predicate = spec.toPredicate(IProps.of(table), delete);
        MutableDelete where = delete.where(predicate);
        return where.execute();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends E, R> R findBy(Specification.Query spec, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        FetchableFluentQueryBySpecification<E, R> fluent = new FetchableFluentQueryBySpecification<>(spec, fetcher, Sort.unsorted(), entityType, (Class<R>) entityType, table, sqlClient, getProjectionFactory());
        return queryFunction.apply((FluentQuery.FetchableFluentQuery<S>) fluent);
    }

    private ProjectionFactory getProjectionFactory() {
        if (projectionFactory == null) {
            projectionFactory = new SpelAwareProxyProjectionFactory();
        }
        return projectionFactory;
    }

    private ConfigurableRootQuery<TableProxy<E>, E> applySpecification(MutableRootQuery<TableProxy<E>> query, Specification.Query spec) {
        return applySpecification(query, spec, this.table, this.fetcher);
    }

    private static <T> ConfigurableRootQuery<TableProxy<T>, T> applySpecification(MutableRootQuery<TableProxy<T>> query, Specification.Query spec, TableProxy<T> table, Fetcher<T> fetcher) {
        IFetcher<T> iFetcher = IFetcher.of(table, fetcher);
        Predicate predicate = spec.toPredicate(IProps.of(table), query, iFetcher);
        // if user does not configure fields fetcher, select all fields as default behavior
        // JPA-like behavior, useful for most cases
        if (!iFetcher.isConfigured()) {
            iFetcher.allFields();
        }
        return query
            .where(predicate)
            .select(iFetcher.toSelection());
    }

    private static class ExampleSpecification<T> implements Specification.Query {

        private final Example<T> example;

        /**
         * Creates new {@link ExampleSpecification}.
         *
         * @param example the example to base the specification of. Must not be {@literal null}.
         */
        ExampleSpecification(Example<T> example) {
            this.example = example;
        }

        @Override
        public Predicate toPredicate(IProps root, MutableRootQuery<?> query, IFetcher<?> fetcher) {
            return QueryByExamplePredicateBuilder.getPredicate(root, this.example);
        }

    }

}
