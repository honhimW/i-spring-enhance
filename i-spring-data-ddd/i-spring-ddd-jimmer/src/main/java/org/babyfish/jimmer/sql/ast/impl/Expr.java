package org.babyfish.jimmer.sql.ast.impl;

import io.github.honhimw.ddd.jimmer.expr.BitwiseBinaryExpression;
import io.github.honhimw.ddd.jimmer.expr.FunctionExpression;
import io.github.honhimw.ddd.jimmer.expr.UnaryExpression;
import jakarta.annotation.Nullable;
import org.babyfish.jimmer.sql.ast.*;

import java.util.*;

/**
 * @author hon_him
 * @since 2025-03-14
 */

@SuppressWarnings({"SuspiciousNameCombination", "unchecked", "unused"})
public class Expr {

    private Expr() {
    }

    public static Predicate _true() {
        return new ComparisonPredicate.Eq(Literals.number(1), Literals.number(1));
    }

    public static Predicate _false() {
        return new ComparisonPredicate.Ne(Literals.number(1), Literals.number(1));
    }

    public static Predicate isTrue(Expression<Boolean> x) {
        return x.eq(true);
    }

    public static Predicate isFalse(Expression<Boolean> x) {
        return x.eq(false);
    }

    public static Predicate isNull(Expression<?> x) {
        return x.isNull();
    }

    public static Predicate isNotNull(Expression<?> x) {
        return x.isNotNull();
    }

    public static Predicate eq(Expression<?> x, Expression<?> y) {
        return new ComparisonPredicate.Eq(x, y);
    }

    public static Predicate eq(Expression<?> x, Object y) {
        return new ComparisonPredicate.Eq(x, Literals.any(y));
    }

    public static Predicate ne(Expression<?> x, Expression<?> y) {
        return new ComparisonPredicate.Ne(x, y);
    }

    public static Predicate ne(Expression<?> x, Object y) {
        return new ComparisonPredicate.Ne(x, Literals.any(y));
    }

    public static <T extends Comparable<? super T>> Predicate gt(Expression<? extends T> x, Expression<? extends T> y) {
        return new ComparisonPredicate.Gt(x, y);
    }

    public static <T extends Comparable<? super T>> Predicate gt(Expression<? extends T> x, T y) {
        return new ComparisonPredicate.Gt(x, Literals.comparable(y));
    }

    public static <T extends Comparable<? super T>> Predicate ge(Expression<? extends T> x, Expression<? extends T> y) {
        return new ComparisonPredicate.Ge(x, y);
    }

    public static <T extends Comparable<? super T>> Predicate ge(Expression<? extends T> x, T y) {
        return new ComparisonPredicate.Ge(x, Literals.comparable(y));
    }

    public static <T extends Comparable<? super T>> Predicate lt(Expression<? extends T> x, Expression<? extends T> y) {
        return new ComparisonPredicate.Lt(x, y);
    }

    public static <T extends Comparable<? super T>> Predicate lt(Expression<? extends T> x, T y) {
        return new ComparisonPredicate.Lt(x, Literals.comparable(y));
    }

    public static <T extends Comparable<? super T>> Predicate le(Expression<? extends T> x, Expression<? extends T> y) {
        return new ComparisonPredicate.Le(x, y);
    }

    public static <T extends Comparable<? super T>> Predicate le(Expression<? extends T> x, T y) {
        return new ComparisonPredicate.Le(x, Literals.comparable(y));
    }

    public static <T extends Comparable<? super T>> Predicate between(Expression<? extends T> v, Expression<? extends T> x, Expression<? extends T> y) {
        return new BetweenPredicate(false, v, x, y);
    }

    public static <T extends Comparable<? super T>> Predicate between(Expression<? extends T> v, T x, T y) {
        return new BetweenPredicate(false, v, Literals.comparable(x), Literals.comparable(y));
    }

    /**
     * x like 'pattern'
     *
     * @param x       expression
     * @param pattern pattern with or without wildcard like `%abc%`
     * @return predicate
     */
    public static Predicate like(Expression<String> x, String pattern) {
        return asString(x).like(pattern, LikeMode.EXACT);
    }

    /**
     * x like y
     *
     * @param x expression
     * @param y pattern
     * @param m mode
     * @return predicate
     */
    public static Predicate like(Expression<String> x, String y, LikeMode m) {
        return asString(x).like(y, m);
    }

    public static Predicate startsWith(Expression<String> x, String y) {
        return asString(x).like(y, LikeMode.START);
    }

    public static Predicate endsWith(Expression<String> x, String y) {
        return asString(x).like(y, LikeMode.END);
    }

    public static Predicate contains(Expression<String> x, String y) {
        return asString(x).like(y, LikeMode.ANYWHERE);
    }

    public static <T> Predicate in(Expression<T> x, Collection<T> y) {
        return x.in(y);
    }

    public static <T> Predicate in(Expression<T> x, T y, T... z) {
        List<T> list = new ArrayList<>(1 + z.length);
        list.add(y);
        Collections.addAll(list, z);
        return x.in(list);
    }

    public static Predicate and(Predicate... predicates) {
        return CompositePredicate.and(predicates);
    }

    public static Predicate and(Collection<Predicate> predicates) {
        return CompositePredicate.and(predicates.toArray(Predicate[]::new));
    }

    public static Predicate or(Predicate... predicates) {
        return CompositePredicate.or(predicates);
    }

    public static Predicate or(Collection<Predicate> predicates) {
        return CompositePredicate.or(predicates.toArray(Predicate[]::new));
    }

    public static Predicate not(Predicate predicate) {
        return Predicate.not(predicate);
    }

    /*
    Expressions
     */

    public static StringExpression asString(Expression<?> x) {
        StringExpression se = null;
        if (x instanceof StringExpression string) {
            se = string;
        } else if (x instanceof LiteralExpressionImplementor<?> literal) {
            String string = (String) literal.getValue();
            se = Literals.string(string);
        }
        Objects.requireNonNull(se, "expression `x` cannot recognized as string-expression");
        return se;
    }

    public static <T> Expression<T> fn(String name, Class<T> type, Expression<?>... args) {
        return new FunctionExpression<>(name, type, Arrays.asList(args));
    }

    public static <T> Expression<T> fn(String name, Class<T> type, @Nullable List<Expression<?>> args) {
        if (args == null) {
            args = Collections.emptyList();
        }
        return new FunctionExpression<>(name, type, args);
    }

    public static <T> Expression<T> sql(Class<T> type, String sql) {
        return NativeBuilderImpl.any(type, sql).build();
    }

    public static StringExpression literal(String x) {
        return Literals.string(x);
    }

    public static <N extends Number & Comparable<N>> NumericExpression<N> literal(N x) {
        return Literals.number(x);
    }

    public static <T extends Comparable<?>> ComparableExpression<T> literal(T x) {
        return Literals.comparable(x);
    }

    public static <T> Expression<T> literal(T x) {
        return Literals.any(x);
    }

    public static Expression<String> concat(Expression<String> x, Expression<String> y) {
        return new ConcatExpression(x, Collections.singletonList(y));
    }

    public static Expression<String> concat(Expression<String> x, String y) {
        return new ConcatExpression(x, Collections.singletonList(literal(y)));
    }

    public static Expression<String> concat(String x, Expression<String> y) {
        return new ConcatExpression(literal(x), Collections.singletonList(y));
    }

    public static <N extends Number & Comparable<N>> NumericExpression<N> bitwiseNot(NumericExpression<N> x) {
        return UnaryExpression.bitwiseNot(x);
    }

    /**
     * In PostgreSQL, bitwise xor syntax is `1 # 2`
     */
    @SuppressWarnings("unchecked")
    public static <N extends Number & Comparable<N>> NumericExpression<N> bitwiseXor(NumericExpression<N> x, N num) {
        Class<N> type = (Class<N>) num.getClass();
        return BitwiseBinaryExpression.xor(type, x, Literals.number(num));
    }

    @SuppressWarnings("unchecked")
    public static <N extends Number & Comparable<N>> NumericExpression<N> bitwiseAnd(NumericExpression<N> x, N num) {
        Class<N> type = (Class<N>) num.getClass();
        return BitwiseBinaryExpression.and(type, x, Literals.number(num));
    }

    @SuppressWarnings("unchecked")
    public static <N extends Number & Comparable<N>> NumericExpression<N> bitwiseOr(NumericExpression<N> x, N num) {
        Class<N> type = (Class<N>) num.getClass();
        return BitwiseBinaryExpression.or(type, x, Literals.number(num));
    }

    public static NumericExpression<Integer> leftShift(NumericExpression<Integer> x, int num) {
        return BitwiseBinaryExpression.leftShift(x, num);
    }

    public static NumericExpression<Integer> rightShift(NumericExpression<Integer> x, int num) {
        return BitwiseBinaryExpression.rightShift(x, num);
    }

    @SuppressWarnings("unchecked")
    public static <N extends Number & Comparable<N>> NumericExpression<N> bitwiseXor(NumericExpression<N> l, NumericExpression<N> r) {
        Class<N> type;
        if (l instanceof ExpressionImplementor<?> implementor) {
            type = (Class<N>) implementor.getType();
        } else if (r instanceof ExpressionImplementor<?> implementor) {
            type = (Class<N>) implementor.getType();
        } else {
            throw new IllegalArgumentException("Unsupported expression type: " + l);
        }
        return BitwiseBinaryExpression.xor(type, l, r);
    }

    @SuppressWarnings("unchecked")
    public static <N extends Number & Comparable<N>> NumericExpression<N> bitwiseAnd(NumericExpression<N> l, NumericExpression<N> r) {
        Class<N> type;
        if (l instanceof ExpressionImplementor<?> implementor) {
            type = (Class<N>) implementor.getType();
        } else if (r instanceof ExpressionImplementor<?> implementor) {
            type = (Class<N>) implementor.getType();
        } else {
            throw new IllegalArgumentException("Unsupported expression type: " + l);
        }
        return BitwiseBinaryExpression.xor(type, l, r);
    }

    @SuppressWarnings("unchecked")
    public static <N extends Number & Comparable<N>> NumericExpression<N> bitwiseOr(NumericExpression<N> l, NumericExpression<N> r) {
        Class<N> type;
        if (l instanceof ExpressionImplementor<?> implementor) {
            type = (Class<N>) implementor.getType();
        } else if (r instanceof ExpressionImplementor<?> implementor) {
            type = (Class<N>) implementor.getType();
        } else {
            throw new IllegalArgumentException("Unsupported expression type: " + l);
        }
        return BitwiseBinaryExpression.xor(type, l, r);
    }

}
