package org.babyfish.jimmer.sql.ast.impl;

import org.babyfish.jimmer.sql.ast.Expression;
import org.babyfish.jimmer.sql.ast.impl.render.AbstractSqlBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2025-03-17
 */

public class FunctionExpression<T> extends AbstractExpression<T> {

    private final String functionName;

    private final Class<T> resultType;

    private List<Expression<?>> args;

    public FunctionExpression(String functionName, Class<T> resultType, List<Expression<?>> args) {
        this.functionName = functionName;
        this.resultType = resultType;
        this.args = args;
    }

    @Override
    public Class<T> getType() {
        return resultType;
    }

    @Override
    public void accept(@NotNull AstVisitor visitor) {
        for (Expression<?> other : args) {
            ((Ast) other).accept(visitor);
        }
    }

    @Override
    public void renderTo(@NotNull AbstractSqlBuilder<?> builder) {
        builder.sql(functionName + "(");
        for (Expression<?> other : args) {
            builder.sql(", ");
            renderChild((Ast) other, builder);
        }
        builder.sql(")");
    }

    @Override
    protected boolean determineHasVirtualPredicate() {
        return hasVirtualPredicate(args);
    }

    @Override
    protected Ast onResolveVirtualPredicate(AstContext ctx) {
        this.args = ctx.resolveVirtualPredicates(args);
        return this;
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionExpression<?> that = (FunctionExpression<?>) o;
        return args.equals(that.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(args);
    }

}
