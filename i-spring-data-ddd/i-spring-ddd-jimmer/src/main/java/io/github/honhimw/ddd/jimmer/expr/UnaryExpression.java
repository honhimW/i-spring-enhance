package io.github.honhimw.ddd.jimmer.expr;

import org.babyfish.jimmer.sql.ast.NumericExpression;
import org.babyfish.jimmer.sql.ast.impl.*;
import org.babyfish.jimmer.sql.ast.impl.render.AbstractSqlBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * @author honhimW
 * @since 2025-06-06
 */

public class UnaryExpression<N extends Number & Comparable<N>>
    extends AbstractExpression<N>
    implements NumericExpressionImplementor<N> {

    private NumericExpression<N> expression;

    private final String unaryOperator;

    public UnaryExpression(NumericExpression<N> expression, String unaryOperator) {
        this.expression = expression;
        this.unaryOperator = unaryOperator;
    }

    public static <N extends Number & Comparable<N>> NumericExpression<N> of(NumericExpression<N> expr, String unaryOperator) {
        return new UnaryExpression<>(expr, unaryOperator);
    }

    public static <N extends Number & Comparable<N>> NumericExpression<N> bitwiseNot(NumericExpression<N> expr) {
        return of(expr, "~");
    }

    @Override
    protected boolean determineHasVirtualPredicate() {
        return hasVirtualPredicate(expression);
    }

    @Override
    protected Ast onResolveVirtualPredicate(AstContext ctx) {
        expression = ctx.resolveVirtualPredicate(expression);
        return this;
    }

    @Override
    public void accept(@NotNull AstVisitor visitor) {
        ((Ast) expression).accept(visitor);
    }

    @Override
    public void renderTo(@NotNull AbstractSqlBuilder<?> builder) {
        builder.sql(unaryOperator);
        renderChild((Ast) expression, builder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<N> getType() {
        return ((ExpressionImplementor<N>) expression).getType();
    }

    @Override
    public int precedence() {
        return 0;
    }

}
