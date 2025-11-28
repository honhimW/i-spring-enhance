package io.github.honhimw.example.domain.jpa.player;

import io.github.honhimw.ddd.common.DomainEvent;
import io.github.honhimw.ddd.common.DomainListenerSupports;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

/**
 * @author honhimW
 * @since 2025-06-03
 */

@Slf4j
//@Component
public class PlayerJpaListener extends DomainListenerSupports {

    @EventListener
    public void doListener(DomainEvent<?, ?> domainEvent) {
        boolean pre = domainEvent.isPre();
        if (pre) {
            log.info("pre event: {}", domainEvent.getEntity());
        } else {
            log.info("post event: {}", domainEvent.getEntity());
        }
    }

}
