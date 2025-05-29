package io.github.honhimw.ddd.jpa.expression;

import jakarta.annotation.Nullable;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.JdbcMappingContainer;
import org.hibernate.metamodel.mapping.SqlExpressible;
import org.hibernate.query.sqm.sql.internal.DomainResultProducer;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.expression.SelfRenderingExpression;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.basic.BasicResult;

import java.util.List;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2025-03-10
 */

public class SelfRenderingExpressions<T> implements SelfRenderingExpression, DomainResultProducer<T> {

    private final List<Expression> expressions;

    private final SqlExpressible expressible;

    public SelfRenderingExpressions(List<Expression> expressions, @Nullable SqlExpressible expressible) {
        this.expressions = expressions;
        this.expressible = expressible;
    }

    @Override
    public JdbcMappingContainer getExpressionType() {
        return expressible;
    }

    @Override
    public void renderToSql(SqlAppender sqlAppender, SqlAstTranslator<?> walker, SessionFactoryImplementor sessionFactory) {
        for (Expression expression : expressions) {
            expression.accept(walker);
        }
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
