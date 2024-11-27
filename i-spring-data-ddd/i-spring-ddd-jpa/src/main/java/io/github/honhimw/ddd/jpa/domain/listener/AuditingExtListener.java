package io.github.honhimw.ddd.jpa.domain.listener;

import io.github.honhimw.ddd.jpa.domain.AbstractAuditAR;
import io.github.honhimw.ddd.jpa.domain.ext.Auditor;
import jakarta.persistence.PrePersist;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author hon_him
 * @since 2022-10-27
 */
@Transactional
public class AuditingExtListener {

    @PrePersist
    public void postPersist(Object entity) {
        if (entity instanceof AbstractAuditAR<?, ?> abstractAuditAR && Objects.isNull(abstractAuditAR.getAuditor())) {
            abstractAuditAR.setAuditor(new Auditor());
        }
    }

}
