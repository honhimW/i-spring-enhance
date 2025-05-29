package io.github.honhimw.ddd.jimmer.util;

import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.PaginationContext;
import org.babyfish.jimmer.sql.dialect.UpdateJoin;
import org.babyfish.jimmer.sql.runtime.Reader;
import jakarta.annotation.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author hon_him
 * @since 2025-03-04
 */

public class DialectDelegate implements Dialect {

    protected final Dialect delegate;

    public DialectDelegate(Dialect delegate) {
        this.delegate = delegate;
    }

    @Override
    public void paginate(PaginationContext ctx) {
        delegate.paginate(ctx);
    }

    @Override
    public @Nullable UpdateJoin getUpdateJoin() {
        return delegate.getUpdateJoin();
    }

    @Override
    public String getSelectIdFromSequenceSql(String sequenceName) {
        return delegate.getSelectIdFromSequenceSql(sequenceName);
    }

    @Override
    public @Nullable String getOverrideIdentityIdSql() {
        return delegate.getOverrideIdentityIdSql();
    }

    @Override
    public boolean isDeletedAliasRequired() {
        return delegate.isDeletedAliasRequired();
    }

    @Override
    public boolean isDeleteAliasSupported() {
        return delegate.isDeleteAliasSupported();
    }

    @Override
    public boolean isUpdateAliasSupported() {
        return delegate.isUpdateAliasSupported();
    }

    @Override
    public @Nullable String getOffsetOptimizationNumField() {
        return delegate.getOffsetOptimizationNumField();
    }

    @Override
    public boolean isMultiInsertionSupported() {
        return delegate.isMultiInsertionSupported();
    }

    @Override
    public boolean isArraySupported() {
        return delegate.isArraySupported();
    }

    @Override
    public boolean isAnyEqualityOfArraySupported() {
        return delegate.isAnyEqualityOfArraySupported();
    }

    @Override
    public <T> T[] getArray(ResultSet rs, int col, Class<T[]> arrayType) throws SQLException {
        return delegate.getArray(rs, col, arrayType);
    }

    @Override
    public boolean isTupleSupported() {
        return delegate.isTupleSupported();
    }

    @Override
    public boolean isTupleComparisonSupported() {
        return delegate.isTupleComparisonSupported();
    }

    @Override
    public boolean isTupleCountSupported() {
        return delegate.isTupleCountSupported();
    }

    @Override
    public boolean isTableOfSubQueryMutable() {
        return delegate.isTableOfSubQueryMutable();
    }

    @Override
    public @Nullable String getConstantTableName() {
        return delegate.getConstantTableName();
    }

    @Override
    public Class<?> getJsonBaseType() {
        return delegate.getJsonBaseType();
    }

    @Override
    public @Nullable Object jsonToBaseValue(@Nullable String json) throws SQLException {
        return delegate.jsonToBaseValue(json);
    }

    @Override
    public @Nullable String baseValueToJson(@Nullable Object baseValue) throws SQLException {
        return delegate.baseValueToJson(baseValue);
    }

    @Override
    public boolean isForeignKeySupported() {
        return delegate.isForeignKeySupported();
    }

    @Override
    public boolean isIgnoreCaseLikeSupported() {
        return delegate.isIgnoreCaseLikeSupported();
    }

    @Override
    public int resolveJdbcType(Class<?> sqlType) {
        return delegate.resolveJdbcType(sqlType);
    }

    @Override
    public Reader<?> unknownReader(Class<?> sqlType) {
        return delegate.unknownReader(sqlType);
    }

    @Override
    public String transCacheOperatorTableDDL() {
        return delegate.transCacheOperatorTableDDL();
    }

    @Override
    public int getMaxInListSize() {
        return delegate.getMaxInListSize();
    }

    @Override
    public String arrayTypeSuffix() {
        return delegate.arrayTypeSuffix();
    }

    @Override
    public boolean isIdFetchableByKeyUpdate() {
        return delegate.isIdFetchableByKeyUpdate();
    }

    @Override
    public boolean isInsertedIdReturningRequired() {
        return delegate.isInsertedIdReturningRequired();
    }

    @Override
    public boolean isExplicitBatchRequired() {
        return delegate.isExplicitBatchRequired();
    }

    @Override
    public boolean isBatchDumb() {
        return delegate.isBatchDumb();
    }

    @Override
    public boolean isUpsertSupported() {
        return delegate.isUpsertSupported();
    }

    @Override
    public boolean isNoIdUpsertSupported() {
        return delegate.isNoIdUpsertSupported();
    }

    @Override
    public boolean isUpsertWithOptimisticLockSupported() {
        return delegate.isUpsertWithOptimisticLockSupported();
    }

    @Override
    public boolean isUpsertWithMultipleUniqueConstraintSupported() {
        return delegate.isUpsertWithMultipleUniqueConstraintSupported();
    }

    @Override
    public boolean isUpsertWithNullableKeySupported() {
        return delegate.isUpsertWithNullableKeySupported();
    }

    @Override
    public boolean isTransactionAbortedByError() {
        return delegate.isTransactionAbortedByError();
    }

    @Override
    public boolean isBatchUpdateExceptionUnreliable() {
        return delegate.isBatchUpdateExceptionUnreliable();
    }

    @Override
    public void update(UpdateContext ctx) {
        delegate.update(ctx);
    }

    @Override
    public void upsert(UpsertContext ctx) {
        delegate.upsert(ctx);
    }

    @Override
    public String sqlType(Class<?> elementType) {
        return delegate.sqlType(elementType);
    }
}
