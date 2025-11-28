package io.github.honhimw.ddd.jimmer.domain;

import io.github.honhimw.ddd.jimmer.util.IFetcher;
import io.github.honhimw.ddd.jimmer.util.IProps;
import org.jspecify.annotations.Nullable;
import org.babyfish.jimmer.sql.ast.Predicate;
import org.babyfish.jimmer.sql.ast.impl.Expr;
import org.babyfish.jimmer.sql.ast.mutation.MutableDelete;
import org.babyfish.jimmer.sql.ast.query.MutableRootQuery;

import java.util.stream.StreamSupport;

/**
 * @author hon_him
 * @since 2025-02-27
 */

public interface Specification {

    @FunctionalInterface
    interface Query {
        Predicate toPredicate(IProps root, MutableRootQuery<?> query, IFetcher<?> fetcher);

        default Query and(Query other) {
            return Specification.and(this, other);
        }

        default Query or(Query other) {
            return Specification.or(this, other);
        }

        static Specification.Query _true() {
            return (root, query, fetcher) -> Expr._true();
        }

        static Specification.Query _false() {
            return (root, query, fetcher) -> Expr._false();
        }

    }

    @FunctionalInterface
    interface Delete {
        Predicate toPredicate(IProps root, MutableDelete delete);

        default Delete and(Delete other) {
            return Specification.and(this, other);
        }

        default Delete or(Delete other) {
            return Specification.or(this, other);
        }

    }

    static Specification.Query where(Specification.@Nullable Query spec) {
        return spec == null ? (root, query, fetcher) -> null : spec;
    }

    static Specification.Query not(final Specification.Query query) {
        return (r, q, f) -> Expr.not(query.toPredicate(r, q, f));
    }

    static Specification.Delete not(final Specification.Delete delete) {
        return (r, d) -> Expr.not(delete.toPredicate(r, d));
    }

    static Specification.Query and(final Specification.Query l, final Specification.Query r) {
        return (root, query, fetcher) -> {
            Predicate lp = l.toPredicate(root, query, fetcher);
            Predicate rp = r.toPredicate(root, query, fetcher);
            return Expr.and(lp, rp);
        };
    }

    static Specification.Query or(final Specification.Query l, final Specification.Query r) {
        return (root, query, fetcher) -> {
            Predicate lp = l.toPredicate(root, query, fetcher);
            Predicate rp = r.toPredicate(root, query, fetcher);
            return Expr.or(lp, rp);
        };
    }

    static Specification.Delete and(final Specification.Delete l, final Specification.Delete r) {
        return (root, delete) -> {
            Predicate lp = l.toPredicate(root, delete);
            Predicate rp = r.toPredicate(root, delete);
            return Expr.and(lp, rp);
        };
    }

    static Specification.Delete or(final Specification.Delete l, final Specification.Delete r) {
        return (root, delete) -> {
            Predicate lp = l.toPredicate(root, delete);
            Predicate rp = r.toPredicate(root, delete);
            return Expr.or(lp, rp);
        };
    }

    static Specification.Query anyOf(Iterable<Specification.Query> specifications) {
        return StreamSupport.stream(specifications.spliterator(), false) //
            .reduce(Specification.where(null), Specification::or);
    }

    static Specification.Query allOf(Iterable<Specification.Query> specifications) {
        return StreamSupport.stream(specifications.spliterator(), false) //
            .reduce(Specification.where(null), Specification::and);
    }

}
