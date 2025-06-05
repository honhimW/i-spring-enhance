package io.github.honhimw.ddd.jimmer.domain;

import io.github.honhimw.ddd.jimmer.util.Utils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.sql.DraftInterceptor;
import org.springframework.data.domain.AuditorAware;

import java.time.Instant;
import java.util.Optional;

/**
 * @author honhimW
 * @since 2025-06-05
 */

public class AuditInterceptor implements DraftInterceptor<AuditAR, AuditARDraft> {
    
    private final AuditorAware<?> auditorAware;

    public AuditInterceptor(AuditorAware<?> auditorAware) {
        this.auditorAware = auditorAware;
    }

    @Override
    public void beforeSave(@Nonnull AuditARDraft draft, @Nullable AuditAR original) {
        Auditor current = Utils.get(draft, AuditARProps.AUDITOR);
        if (current != null) {
            return;
        }
        Optional<?> currentAuditor = auditorAware.getCurrentAuditor();
        currentAuditor
            .map(Object::toString)
            .ifPresent(s -> {
                Instant now = Instant.now();
                if (original == null) {
                    // INSERT
                    Auditor produce = AuditorDraft.$.produce(_draft -> _draft
                        .setCreatedBy(s)
                        .setCreatedAt(now)
                        .setUpdatedBy(s)
                        .setUpdatedAt(now)
                    );
                    draft.setAuditor(produce);
                } else {
                    // UPDATE
                    Auditor auditor = Utils.get(original, AuditARProps.AUDITOR);
                    Auditor produce = AuditorDraft.$.produce(auditor, _draft -> _draft
                        .setUpdatedBy(s)
                        .setUpdatedAt(now)
                    );
                    draft.setAuditor(produce);
                }
            });
    }
}
