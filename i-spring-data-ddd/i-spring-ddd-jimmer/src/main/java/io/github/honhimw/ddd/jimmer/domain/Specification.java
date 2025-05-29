package io.github.honhimw.ddd.jimmer.domain;

import io.github.honhimw.ddd.jimmer.util.IFetcher;
import io.github.honhimw.ddd.jimmer.util.IProps;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.mutation.MutableDelete;
import org.babyfish.jimmer.sql.ast.query.MutableRootQuery;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;

/**
 * @author hon_him
 * @since 2025-02-27
 */

public interface Specification {

    @FunctionalInterface
    interface Query {
        Predicate toPredicate(IProps root, MutableRootQuery<?> query, IFetcher<?> fetcher);
    }

    @FunctionalInterface
    interface Delete {
        Predicate toPredicate(IProps root, MutableDelete delete);
    }



}
