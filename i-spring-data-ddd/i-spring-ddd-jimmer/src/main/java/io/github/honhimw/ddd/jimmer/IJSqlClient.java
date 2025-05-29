package io.github.honhimw.ddd.jimmer;

import io.github.honhimw.ddd.jimmer.event.CallbackEntities;
import org.babyfish.jimmer.sql.Entities;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.di.AbstractJSqlClientDelegate;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;

/**
 * @author hon_him
 * @since 2025-03-21
 */

public class IJSqlClient extends AbstractJSqlClientDelegate {

    private final JSqlClientImplementor sqlClient;

    private final Entities entities;

    public IJSqlClient(JSqlClient sqlClient) {
        this.sqlClient = (JSqlClientImplementor) sqlClient;
        this.entities = new CallbackEntities(sqlClient.getEntities());
    }

    @Override
    protected JSqlClientImplementor sqlClient() {
        return sqlClient;
    }

    @Override
    public Entities getEntities() {
        return entities;
    }

}
