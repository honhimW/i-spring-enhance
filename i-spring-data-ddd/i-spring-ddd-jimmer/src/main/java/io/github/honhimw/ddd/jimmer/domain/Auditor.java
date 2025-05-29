package io.github.honhimw.ddd.jimmer.domain;

import jakarta.annotation.Nullable;
import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Embeddable;
import org.babyfish.jimmer.sql.Version;

import java.time.Instant;

/**
 * @author hon_him
 * @since 2025-03-20
 */

@Embeddable
public interface Auditor {

    @Nullable
    @Column(name = "created_at")
    Instant createdAt();
    @Nullable
    @Column(name = "updated_at")
    Instant updatedAt();
    @Nullable
    @Column(name = "created_by")
    String createdBy();
    @Nullable
    @Column(name = "updated_by")
    String updatedBy();

}
