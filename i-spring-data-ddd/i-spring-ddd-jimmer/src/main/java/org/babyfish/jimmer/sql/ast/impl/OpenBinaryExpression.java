package org.babyfish.jimmer.sql.ast.impl;

import org.babyfish.jimmer.sql.ast.Expression;

/**
 * Make it public to be extends
 * @author honhimW
 * @since 2025-07-11
 */

public abstract class OpenBinaryExpression<N extends Number & Comparable<N>> extends BinaryExpression<N> {

    protected final Class<N> type;

    protected Expression<N> left;

    protected Expression<N> right;

    protected OpenBinaryExpression(Class<N> type, Expression<N> left, Expression<N> right) {
        super(type, left, right);
        this.type = type;
        this.left = left;
        this.right = right;
    }

}
