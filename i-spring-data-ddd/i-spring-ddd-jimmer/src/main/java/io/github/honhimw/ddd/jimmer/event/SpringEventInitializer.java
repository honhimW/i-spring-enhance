package io.github.honhimw.ddd.jimmer.event;

import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.event.Triggers;
import org.babyfish.jimmer.sql.runtime.Initializer;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author honhimW
 * @since 2025-06-04
 */
public class SpringEventInitializer implements Initializer {

    private final ApplicationEventPublisher publisher;

    private SpringEventInitializer(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void initialize(JSqlClient sqlClient) throws Exception {
        Triggers[] triggersArr = switch (((JSqlClientImplementor) sqlClient).getTriggerType()) {
            case TRANSACTION_ONLY -> new Triggers[]{sqlClient.getTriggers(true)};
            case BINLOG_ONLY -> new Triggers[]{sqlClient.getTriggers()};
            case BOTH -> new Triggers[]{sqlClient.getTriggers(), sqlClient.getTriggers(true)};
        };
        for (Triggers triggers : triggersArr) {
            triggers.addEntityListener(publisher::publishEvent);
            triggers.addAssociationListener(publisher::publishEvent);
        }
    }
}
