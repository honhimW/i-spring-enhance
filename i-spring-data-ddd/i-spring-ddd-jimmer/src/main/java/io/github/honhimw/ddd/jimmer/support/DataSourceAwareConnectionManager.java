package io.github.honhimw.ddd.jimmer.support;

import jakarta.annotation.Nonnull;
import org.babyfish.jimmer.sql.runtime.ConnectionManager;

import javax.sql.DataSource;

/**
 * @author hon_him
 * @since 2025-01-13
 */

public interface DataSourceAwareConnectionManager extends ConnectionManager {

    @Nonnull
    DataSource getDataSource();

}
