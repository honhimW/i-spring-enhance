package io.github.honhimw.ddd.jimmer.expr;

import io.github.honhimw.ddd.jimmer.util.RuntimeDialect;
import org.babyfish.jimmer.sql.ast.Expression;
import org.babyfish.jimmer.sql.ast.impl.ExpressionPrecedences;
import org.babyfish.jimmer.sql.ast.impl.Literals;
import org.babyfish.jimmer.sql.ast.impl.OpenBinaryExpression;

/**
 * @author honhimW
 * @since 2025-06-06
 */

public class BitwiseBinaryExpression<N extends Number & Comparable<N>> extends OpenBinaryExpression<N> {

    private final String operator;

    public BitwiseBinaryExpression(Class<N> type, Expression<N> left, Expression<N> right, String operator) {
        super(type, left, right);
        this.operator = operator;
    }

    public static <N extends Number & Comparable<N>> BitwiseBinaryExpression<N> xor(Class<N> type, Expression<N> left, Expression<N> right) {
        if (RuntimeDialect.isPgSQL()) {
            return new BitwiseBinaryExpression<>(type, left, right, "#");
        }
        return new BitwiseBinaryExpression<>(type, left, right, "^");
    }

    public static <N extends Number & Comparable<N>> BitwiseBinaryExpression<N> and(Class<N> type, Expression<N> left, Expression<N> right) {
        return new BitwiseBinaryExpression<>(type, left, right, "&");
    }

    public static <N extends Number & Comparable<N>> BitwiseBinaryExpression<N> or(Class<N> type, Expression<N> left, Expression<N> right) {
        return new BitwiseBinaryExpression<>(type, left, right, "|");
    }

    public static BitwiseBinaryExpression<Integer> leftShift(Expression<Integer> left, int offset) {
        return new BitwiseBinaryExpression<>(Integer.class, left, Literals.number(offset), "<<");
    }

    public static BitwiseBinaryExpression<Integer> rightShift(Expression<Integer> left, int offset) {
        return new BitwiseBinaryExpression<>(Integer.class, left, Literals.number(offset), ">>");
    }

    @Override
    public int precedence() {
        return ExpressionPrecedences.PLUS;
    }

    @Override
    protected String operator() {
        return operator;
    }
}
