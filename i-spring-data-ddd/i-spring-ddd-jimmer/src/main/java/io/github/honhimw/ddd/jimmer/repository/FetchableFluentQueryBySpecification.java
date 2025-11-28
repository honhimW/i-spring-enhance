package io.github.honhimw.ddd.jimmer.repository;

import io.github.honhimw.ddd.jimmer.domain.Specification;
import io.github.honhimw.ddd.jimmer.support.SpringOrders;
import io.github.honhimw.ddd.jimmer.support.SpringPageFactory;
import io.github.honhimw.ddd.jimmer.util.IFetcher;
import io.github.honhimw.ddd.jimmer.util.IProps;
import org.jspecify.annotations.NonNull;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.query.ConfigurableRootQuery;
import org.babyfish.jimmer.sql.ast.query.MutableRootQuery;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.query.FluentQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author hon_him
 * @since 2025-03-18
 */

public class FetchableFluentQueryBySpecification<S, R> implements FluentQuery.FetchableFluentQuery<R> {

    private final Specification.Query spec;
    private final Fetcher<S> fetcher;
    private final Sort sort;
    private final Class<S> entityType;
    private final Class<R> resultType;
    private final TableProxy<S> tableProxy;
    private final JSqlClientImplementor sqlClient;

    private final ProjectionFactory projectionFactory;

    public FetchableFluentQueryBySpecification(Specification.Query spec, Fetcher<S> fetcher, Sort sort, Class<S> entityType, Class<R> resultType, TableProxy<S> tableProxy, JSqlClientImplementor sqlClient, ProjectionFactory projectionFactory) {
        this.spec = spec;
        this.fetcher = fetcher;
        this.sort = sort;
        this.entityType = entityType;
        this.resultType = resultType;
        this.tableProxy = tableProxy;
        this.sqlClient = sqlClient;
        this.projectionFactory = projectionFactory;
    }

    @NonNull
    @Override
    public FetchableFluentQuery<R> sortBy(@NonNull Sort sort) {
        return new FetchableFluentQueryBySpecification<>(spec, fetcher, sort, entityType, resultType, tableProxy, sqlClient, projectionFactory);
    }

    @NonNull
    @Override
    public <R1> FetchableFluentQuery<R1> as(@NonNull Class<R1> resultType) {
        return new FetchableFluentQueryBySpecification<>(spec, fetcher, sort, entityType, resultType, tableProxy, sqlClient, projectionFactory);
    }

    @NonNull
    @Override
    public FetchableFluentQuery<R> project(Collection<String> properties) {
        Fetcher<S> fetcher = this.fetcher;
        for (String property : properties) {
            fetcher = fetcher.add(property);
        }
        return new FetchableFluentQueryBySpecification<>(spec, fetcher, sort, entityType, resultType, tableProxy, sqlClient, projectionFactory);
    }

    @Override
    public R oneValue() {
        List<S> results = createSortedAndProjectedQuery(sort)
            .limit(2)
            .execute();

        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1);
        }

        return results.isEmpty() ? null : getConversionFunction().apply(results.get(0));
    }

    @Override
    public R firstValue() {
        List<S> results = createSortedAndProjectedQuery(sort)
            .limit(1)
            .execute();

        return results.isEmpty() ? null : getConversionFunction().apply(results.get(0));
    }

    @NonNull
    @Override
    public List<R> all() {
        List<S> results = createSortedAndProjectedQuery(sort)
            .execute();

        Function<Object, R> conversionFunction = getConversionFunction();
        List<R> mapped = new ArrayList<>(results.size());
        for (S entity : results) {
            mapped.add(conversionFunction.apply(entity));
        }
        return mapped;
    }

    @NonNull
    @Override
    public Page<R> page(Pageable pageable) {
        Page<S> results = createSortedAndProjectedQuery(sort)
            .fetchPage(pageable.getPageNumber(), pageable.getPageSize(), SpringPageFactory.getInstance());
        Function<Object, R> conversionFunction = getConversionFunction();
        return results.map(conversionFunction);
    }

    @NonNull
    @Override
    public Stream<R> stream() {
        return all().stream();
    }

    @Override
    public long count() {
        return createSortedAndProjectedQuery(sort).fetchUnlimitedCount();
    }

    @Override
    public boolean exists() {
        return createSortedAndProjectedQuery(sort).exists();
    }

    private ConfigurableRootQuery<TableProxy<S>, S> createSortedAndProjectedQuery(Sort sort) {
        MutableRootQuery<TableProxy<S>> query = this.sqlClient.createQuery(tableProxy)
            .orderBy(SpringOrders.toOrders(tableProxy, sort));
        IFetcher<S> iFetcher = IFetcher.of(tableProxy, fetcher);
        Predicate predicate = spec.toPredicate(IProps.of(tableProxy), query, iFetcher);
        // if user does not configure fields fetcher, select all fields as default behavior
        // JPA-like behavior, useful for most cases
        if (!iFetcher.isConfigured()) {
            iFetcher.allFields();
        }
        return query
            .where(predicate)
            .select(iFetcher.toSelection());
    }

    private Function<Object, R> getConversionFunction() {
        return getConversionFunction(entityType, resultType);
    }

    @SuppressWarnings("unchecked")
    private Function<Object, R> getConversionFunction(Class<S> inputType, Class<R> targetType) {

        if (targetType.isAssignableFrom(inputType)) {
            return (Function<Object, R>) Function.identity();
        }

        if (targetType.isInterface()) {
            return o -> projectionFactory.createProjection(targetType, o);
        }

        return o -> DefaultConversionService.getSharedInstance().convert(o, targetType);
    }

}
