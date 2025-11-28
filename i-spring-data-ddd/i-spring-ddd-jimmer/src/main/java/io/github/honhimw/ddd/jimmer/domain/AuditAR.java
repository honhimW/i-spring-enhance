package io.github.honhimw.ddd.jimmer.domain;

import org.babyfish.jimmer.sql.MappedSuperclass;
//import org.jspecify.annotations.Nullable;
import jakarta.annotation.Nullable;

/**
 * @author hon_him
 * @since 2025-03-20
 */

@MappedSuperclass
public interface AuditAR extends AggregateRoot {

    @Nullable
    Auditor auditor();

}
