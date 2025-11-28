package io.github.honhimw.ddd.jpa.expression;

import org.jspecify.annotations.Nullable;
import org.hibernate.metamodel.mapping.JdbcMappingContainer;
import org.hibernate.metamodel.mapping.SqlExpressible;
import org.hibernate.query.sqm.sql.internal.DomainResultProducer;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.ast.tree.expression.SelfRenderingSqlFragmentExpression;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.basic.BasicResult;

import java.util.Objects;

/**
 * @author hon_him
 * @since 2025-03-11
 */

public class ISelfRenderingSqlFragmentExpression<T> extends SelfRenderingSqlFragmentExpression implements DomainResultProducer<T> {

    private final SqlExpressible expressible;

    public ISelfRenderingSqlFragmentExpression(String expression) {
        this(expression, null);
    }

    public ISelfRenderingSqlFragmentExpression(String expression, @Nullable SqlExpressible expressible) {
        super(expression);
        this.expressible = expressible;
    }

    @Override
    public JdbcMappingContainer getExpressionType() {
        return expressible;
    }

    @Override
    public DomainResult<T> createDomainResult(
        String resultVariable,
        DomainResultCreationState creationState) {
        Objects.requireNonNull(expressible, "Using as a DomainResult requires a SqlExpressible.");
        final SqlSelection sqlSelection = creationState.getSqlAstCreationState().getSqlExpressionResolver()
            .resolveSqlSelection(
                this,
                expressible.getJdbcMapping().getJdbcJavaType(),
                null,
                creationState.getSqlAstCreationState()
                    .getCreationContext()
                    .getSessionFactory()
                    .getTypeConfiguration()
            );
        return new BasicResult<>(
            sqlSelection.getValuesArrayPosition(),
            resultVariable,
            expressible.getJdbcMapping()
        );
    }

    @Override
    public void applySqlSelections(DomainResultCreationState creationState) {
        Objects.requireNonNull(expressible, "Using as a DomainResult requires a SqlExpressible.");
        creationState.getSqlAstCreationState().getSqlExpressionResolver().resolveSqlSelection(
            this,
            expressible.getJdbcMapping().getJdbcJavaType(),
            null,
            creationState.getSqlAstCreationState().getCreationContext().getMappingMetamodel().getTypeConfiguration()
        );
    }
}
