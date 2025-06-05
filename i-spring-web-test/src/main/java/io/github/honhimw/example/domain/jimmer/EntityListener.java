package io.github.honhimw.example.domain.jimmer;

import io.github.honhimw.ddd.common.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.sql.event.EntityEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author hon_him
 * @since 2025-03-20
 */

@Slf4j
@Component
public class EntityListener {

    @Autowired
    private PlayerRepository playerRepository;

//    @Transactional("jimmerTransactionManager")
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onEntityChanged(EntityEvent<?> e) {
        log.info("entity changed: {}", e);
        log.info("player count: {}", playerRepository.count());
//        throw new RuntimeException();
    }

    @EventListener
//    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onCallback(PlayerEvent e) {
        log.info("callback event, pre: {}, action {}", e.isPre(), e.getAction());
//        throw new RuntimeException();
    }

}
