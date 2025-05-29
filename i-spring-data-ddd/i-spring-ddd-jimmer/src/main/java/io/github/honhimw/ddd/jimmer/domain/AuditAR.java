package io.github.honhimw.ddd.jimmer.domain;

import jakarta.annotation.Nonnull;
import org.babyfish.jimmer.sql.MappedSuperclass;

/**
 * @author hon_him
 * @since 2025-03-20
 */

@MappedSuperclass
public interface AuditAR extends AggregateRoot {

    @Nonnull
    Auditor auditor();

}
