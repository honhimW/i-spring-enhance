package io.github.honhimw.ddd.jimmer;

import io.github.honhimw.ddd.jimmer.support.DataSourceAwareConnectionManager;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.runtime.ConnectionManager;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * @author hon_him
 * @since 2025-03-21
 */

public class JimmerTransactionManager extends DataSourceTransactionManager {

    private final JSqlClient sqlClient;

    public JimmerTransactionManager(JSqlClient sqlClient) {
        super(dataSourceOf(sqlClient));
        this.sqlClient = sqlClient;
    }

    private static DataSource dataSourceOf(JSqlClient sqlClient) {
        ConnectionManager connectionManager = ((JSqlClientImplementor) sqlClient).getConnectionManager();
        if (!(connectionManager instanceof DataSourceAwareConnectionManager)) {
            throw new IllegalArgumentException(
                "The data source of sql client must be an instance of \"" +
                DataSourceAwareConnectionManager.class.getName() +
                "\""
            );
        }
        return ((DataSourceAwareConnectionManager) connectionManager).getDataSource();
    }

}
