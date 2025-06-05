package io.github.honhimw.ddd.jimmer.util;

import io.github.honhimw.ddd.jimmer.support.SpringConnectionManager;
import io.github.honhimw.ddd.jimmer.support.SpringTransientResolverProvider;
import jakarta.annotation.Nullable;
import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.PropId;
import org.babyfish.jimmer.meta.TypedProp;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.PropExpression;
import org.babyfish.jimmer.sql.ast.impl.query.FilterLevel;
import org.babyfish.jimmer.sql.ast.impl.query.MutableRootQueryImpl;
import org.babyfish.jimmer.sql.ast.impl.table.TableImplementor;
import org.babyfish.jimmer.sql.ast.query.Order;
import org.babyfish.jimmer.sql.ast.query.OrderMode;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.ast.table.spi.PropExpressionImplementor;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.babyfish.jimmer.sql.meta.EmbeddedColumns;
import org.babyfish.jimmer.sql.meta.MetadataStrategy;
import org.babyfish.jimmer.sql.runtime.ConnectionManager;
import org.babyfish.jimmer.sql.runtime.ExecutionPurpose;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author hon_him
 * @since 2025-02-26
 */

public class Utils {

    private Utils() {
    }

    public static <T> List<T> toList(Iterable<T> iterable) {
        Stream.Builder<T> builder = Stream.builder();
        iterable.forEach(builder::add);
        return builder.build().toList();
    }

    public static JSqlClientImplementor validateSqlClient(JSqlClient sqlClient) {
        JSqlClientImplementor implementor = (JSqlClientImplementor) sqlClient;
        if (!(implementor.getConnectionManager() instanceof SpringConnectionManager)) {
            throw new IllegalArgumentException(
                "The connection manager of sql client must be instance of \"" +
                SpringConnectionManager.class.getName() +
                "\""
            );
        }
        if (!(implementor.getTransientResolverProvider() instanceof SpringTransientResolverProvider)) {
            throw new IllegalArgumentException(
                "The transient resolver provider of sql client must be instance of \"" +
                SpringConnectionManager.class.getName() +
                "\""
            );
        }
        ConnectionManager slaveConnectionManager = implementor.getSlaveConnectionManager(false);
        if (slaveConnectionManager != null && !(slaveConnectionManager instanceof SpringConnectionManager)) {
            throw new IllegalArgumentException(
                "The slave connection manager of sql client must be null or instance of \"" +
                SpringConnectionManager.class.getName() +
                "\""
            );
        }
        return implementor;
    }

    public static Sort toSort(List<Order> orders, MetadataStrategy strategy) {
        if (orders == null || orders.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> springOrders = new ArrayList<>(orders.size());
        for (Order order : orders) {
            if (order.getExpression() instanceof PropExpression<?>) {
                PropExpressionImplementor<?> propExpr = (PropExpressionImplementor<?>) order.getExpression();
                String prefix = prefix(propExpr.getTable());
                EmbeddedColumns.Partial partial = propExpr.getPartial(strategy);
                String path = partial != null ? partial.path() : propExpr.getProp().getName();
                if (prefix != null) {
                    path = prefix + '.' + path;
                }
                Sort.NullHandling nullHandling = switch (order.getNullOrderMode()) {
                    case NULLS_FIRST -> Sort.NullHandling.NULLS_FIRST;
                    case NULLS_LAST -> Sort.NullHandling.NULLS_LAST;
                    default -> Sort.NullHandling.NATIVE;
                };
                springOrders.add(
                    new Sort.Order(
                        order.getOrderMode() == OrderMode.DESC ?
                            Sort.Direction.DESC :
                            Sort.Direction.ASC,
                        path,
                        nullHandling
                    )
                );
            }
        }
        return Sort.by(springOrders);
    }

    public static <T> MutableRootQueryImpl<Table<T>> creqteQuery(JSqlClientImplementor sqlClient, ImmutableType immutableType) {
        return new MutableRootQueryImpl<>(sqlClient, immutableType, ExecutionPurpose.QUERY, FilterLevel.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    public static <T> TableProxy<T> getTable(Class<T> clazz) {
        try {
            return (TableProxy<T>) Class.forName(clazz.getPackageName() + "." + clazz.getSimpleName() + "Table").getField("$").get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException("can not find Table by type.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Fetcher<T> getFetcher(Class<T> clazz) {
        try {
            return (Fetcher<T>) Class.forName(clazz.getPackageName() + "." + clazz.getSimpleName() + "Fetcher").getField("$").get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException("can not find Table by type.", e);
        }
    }

    @Nullable
    public static Object get(Object immutable, PropId prop) {
        if (ImmutableObjects.isLoaded(immutable, prop)) {
            return ImmutableObjects.get(immutable, prop);
        } else {
            return null;
        }
    }

    @Nullable
    public static Object get(Object immutable, String prop) {
        if (ImmutableObjects.isLoaded(immutable, prop)) {
            return ImmutableObjects.get(immutable, prop);
        } else {
            return null;
        }
    }

    @Nullable
    public static Object get(Object immutable, ImmutableProp prop) {
        if (ImmutableObjects.isLoaded(immutable, prop)) {
            return ImmutableObjects.get(immutable, prop);
        } else {
            return null;
        }
    }

    @Nullable
    public static <T, X> X get(T immutable, TypedProp<T, X> prop) {
        if (ImmutableObjects.isLoaded(immutable, prop)) {
            return ImmutableObjects.get(immutable, prop);
        } else {
            return null;
        }
    }

    private static String prefix(Table<?> table) {
        ImmutableProp prop = table instanceof TableProxy<?> ?
            ((TableProxy<?>) table).__prop() :
            ((TableImplementor<?>) table).getJoinProp();
        if (prop == null) {
            return null;
        }

        String name = prop.getName();

        boolean inverse = table instanceof TableProxy<?> ?
            ((TableProxy<?>) table).__isInverse() :
            ((TableImplementor<?>) table).isInverse();
        if (inverse) {
            name = "[←" + name + ']';
        }

        Table<?> parent = table instanceof TableProxy<?> ?
            ((TableProxy<?>) table).__parent() :
            ((TableImplementor<?>) table).getParent();
        if (parent == null) {
            return name;
        }
        String parentPrefix = prefix(parent);
        if (parentPrefix == null) {
            return name;
        }
        return parentPrefix + '.' + name;
    }
}