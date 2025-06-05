package io.github.honhimw.ddd.jimmer.event;

import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.event.EntityEvent;
import org.babyfish.jimmer.sql.event.Triggers;
import org.babyfish.jimmer.sql.runtime.Initializer;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;

/**
 * @author honhimW
 * @since 2025-06-04
 */
public class CallbackInitializer implements Initializer {

    private final Callback callback;

    public CallbackInitializer(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void initialize(JSqlClient sqlClient) throws Exception {
        Triggers[] triggersArr = switch (((JSqlClientImplementor) sqlClient).getTriggerType()) {
            case TRANSACTION_ONLY -> new Triggers[]{sqlClient.getTriggers(true)};
            case BINLOG_ONLY -> new Triggers[]{sqlClient.getTriggers()};
            case BOTH -> new Triggers[]{sqlClient.getTriggers(), sqlClient.getTriggers(true)};
        };
        for (Triggers triggers : triggersArr) {
            triggers.addEntityListener(e -> {
                EntityEvent.Type type = e.getType();
                Object oldEntity = e.getOldEntity();
                Object newEntity = e.getNewEntity();
                switch (type) {
                    case INSERT -> callback.postCreate(newEntity);
                    case DELETE -> callback.postRemove(oldEntity);
                    case LOGICAL_DELETED -> callback.postSoftRemove(oldEntity);
                    case UPDATE -> callback.postUpdate(newEntity);
                }
            });
        }
    }
}
