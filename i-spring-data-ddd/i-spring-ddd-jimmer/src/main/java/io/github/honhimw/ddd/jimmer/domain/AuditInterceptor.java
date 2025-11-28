package io.github.honhimw.ddd.jimmer.domain;

import io.github.honhimw.ddd.jimmer.util.Utils;
import org.babyfish.jimmer.sql.DraftInterceptor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.AuditorAware;

import java.time.Instant;
import java.util.Objects;
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
    public void beforeSave(@NonNull AuditARDraft draft, @Nullable AuditAR original) {
        Auditor current = Utils.get(draft, AuditARProps.AUDITOR);
        Optional<?> currentAuditor = auditorAware.getCurrentAuditor();
        String _auditor = currentAuditor.map(Object::toString).orElse("");
        Instant now = Instant.now();
        if (original == null) {
            // INSERT
            if (current == null) {
                Auditor produce = AuditorDraft.$.produce(_draft -> _draft
                    .setCreatedBy(_auditor)
                    .setCreatedAt(now)
                    .setUpdatedBy(_auditor)
                    .setUpdatedAt(now)
                );
                draft.setAuditor(produce);
            }
        } else {
            // UPDATE
            Auditor originalAuditor = Utils.get(original, AuditARProps.AUDITOR);
            if (Objects.equals(current, originalAuditor) || originalAuditor == null) {
                Auditor produce = AuditorDraft.$.produce(originalAuditor, _draft -> _draft
                    .setUpdatedBy(_auditor)
                    .setUpdatedAt(now)
                );
                draft.setAuditor(produce);
            }

        }
    }
}
