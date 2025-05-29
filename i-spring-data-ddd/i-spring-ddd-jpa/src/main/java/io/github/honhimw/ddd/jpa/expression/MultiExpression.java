package io.github.honhimw.ddd.jpa.expression;

import jakarta.annotation.Nullable;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.SemanticQueryWalker;
import org.hibernate.query.sqm.sql.internal.StandardSqmTranslator;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.AbstractSqmExpression;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.type.internal.BasicTypeImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hon_him
 * @since 2025-03-10
 */

public class MultiExpression<T> extends AbstractSqmExpression<T> {

    private final List<SqmExpression<?>> sqmExpressions;

    private final BasicTypeImpl<T> type;

    public MultiExpression(List<SqmExpression<?>> sqmExpressions, NodeBuilder criteriaBuilder) {
        this(sqmExpressions, criteriaBuilder, null);
    }

    public MultiExpression(List<SqmExpression<?>> sqmExpressions, NodeBuilder criteriaBuilder, @Nullable BasicTypeImpl<T> type) {
        super(null, criteriaBuilder);
        this.sqmExpressions = sqmExpressions;
        this.type = type;
    }

    @Override
    public MultiExpression<T> copy(SqmCopyContext context) {
        final MultiExpression<T> existing = context.getCopy(this);
        if (existing != null) {
            return existing;
        }
        final MultiExpression<T> expression = context.registerCopy(
            this,
            new MultiExpression<>(
                sqmExpressions,
                nodeBuilder(),
                type
            )
        );
        copyTo(expression, context);
        return expression;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X> X accept(SemanticQueryWalker<X> walker) {
        if (walker instanceof StandardSqmTranslator) {
            List<Expression> expressions = new ArrayList<>(sqmExpressions.size());
            for (SqmExpression<?> expression : sqmExpressions) {
                Expression e = (Expression) expression.accept(walker);
                expressions.add(e);
            }
            return (X) new SelfRenderingExpressions<>(expressions, type);
        } else {
            return null;
        }
    }

    @Override
    public void appendHqlString(StringBuilder sb) {
        for (SqmExpression<?> expression : sqmExpressions) {
            expression.appendHqlString(sb);
        }
    }
}
