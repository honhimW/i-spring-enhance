package io.github.honhimw.ddd.jimmer.expr;

import org.babyfish.jimmer.sql.ast.Expression;
import org.babyfish.jimmer.sql.ast.impl.Ast;
import org.babyfish.jimmer.sql.ast.impl.ExpressionPrecedences;
import org.babyfish.jimmer.sql.ast.impl.Literals;
import org.babyfish.jimmer.sql.ast.impl.OpenBinaryExpression;
import org.babyfish.jimmer.sql.ast.impl.render.AbstractSqlBuilder;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.PostgresDialect;
import org.jetbrains.annotations.NotNull;

/**
 * @author honhimW
 * @since 2025-06-06
 */

public class BitwiseBinaryExpression<N extends Number & Comparable<N>> extends OpenBinaryExpression<N> {

    private final Operator operator;

    public BitwiseBinaryExpression(Class<N> type, Expression<N> left, Expression<N> right, Operator operator) {
        super(type, left, right);
        this.operator = operator;
    }

    public static <N extends Number & Comparable<N>> BitwiseBinaryExpression<N> xor(Class<N> type, Expression<N> left, Expression<N> right) {
        return new BitwiseBinaryExpression<>(type, left, right, Operator.XOR);
    }

    public static <N extends Number & Comparable<N>> BitwiseBinaryExpression<N> and(Class<N> type, Expression<N> left, Expression<N> right) {
        return new BitwiseBinaryExpression<>(type, left, right, Operator.AND);
    }

    public static <N extends Number & Comparable<N>> BitwiseBinaryExpression<N> or(Class<N> type, Expression<N> left, Expression<N> right) {
        return new BitwiseBinaryExpression<>(type, left, right, Operator.OR);
    }

    public static BitwiseBinaryExpression<Integer> leftShift(Expression<Integer> left, int offset) {
        return new BitwiseBinaryExpression<>(Integer.class, left, Literals.number(offset), Operator.LEFT_SHIFT);
    }

    public static BitwiseBinaryExpression<Integer> rightShift(Expression<Integer> left, int offset) {
        return new BitwiseBinaryExpression<>(Integer.class, left, Literals.number(offset), Operator.RIGHT_SHIFT);
    }

    @Override
    public int precedence() {
        return ExpressionPrecedences.PLUS;
    }

    @Override
    protected String operator() {
        return operator.raw();
    }

    @Override
    public void renderTo(@NotNull AbstractSqlBuilder<?> builder) {
        Dialect dialect = builder.sqlClient().getDialect();
        renderChild(Ast.of(left), builder);
        builder.sql(" ");
        builder.sql(operator.resolve(dialect));
        builder.sql(" ");
        renderChild(Ast.of(right), builder);
    }

    public static class Operator {
        public static final Operator XOR = new Operator("^") {
            @Override
            public String resolve(Dialect dialect) {
                if (dialect instanceof PostgresDialect) {
                    return "#";
                }
                return super.resolve(dialect);
            }
        };

        public static final Operator AND = new Operator("&");
        public static final Operator OR = new Operator("|");
        public static final Operator LEFT_SHIFT = new Operator("<<");
        public static final Operator RIGHT_SHIFT = new Operator(">>");

        private final String raw;

        public Operator(String raw) {
            this.raw = raw;
        }
        public String raw() {
            return raw;
        }
        public String resolve(Dialect dialect) {
            return raw();
        }
    }

}
