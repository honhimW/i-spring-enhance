package io.github.honhimw.ddd.jpa.expression;

import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.hibernate.query.sqm.NodeBuilder;
import org.hibernate.query.sqm.SemanticQueryWalker;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.expression.AbstractSqmExpression;
import org.hibernate.type.internal.BasicTypeImpl;

/**
 * @author hon_him
 * @since 2025-03-10
 */

public class FragmentExpression<T> extends AbstractSqmExpression<T> {

    private final CharSequence raw;

    private final BasicTypeImpl<T> type;

    public FragmentExpression(CharSequence raw, CriteriaBuilder criteriaBuilder) {
        this(raw, (NodeBuilder) criteriaBuilder, null);
    }

    public FragmentExpression(CharSequence raw, NodeBuilder criteriaBuilder, @Nullable BasicTypeImpl<T> type) {
        super(null, criteriaBuilder);
        this.raw = raw;
        this.type = type;
    }

    @Override
    public FragmentExpression<T> copy(SqmCopyContext context) {
        final FragmentExpression<T> existing = context.getCopy(this);
        if (existing != null) {
            return existing;
        }
        final FragmentExpression<T> expression = context.registerCopy(
            this,
            new FragmentExpression<>(
                unwrap(),
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
        return (X) new ISelfRenderingSqlFragmentExpression<>(raw.toString(), type);
    }

    @Override
    public void appendHqlString(StringBuilder sb) {
        sb.append(raw);
    }

    public CharSequence unwrap() {
        return raw;
    }

}
