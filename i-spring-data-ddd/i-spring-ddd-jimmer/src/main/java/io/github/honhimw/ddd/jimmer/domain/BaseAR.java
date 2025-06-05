package io.github.honhimw.ddd.jimmer.domain;

import org.babyfish.jimmer.sql.MappedSuperclass;

/**
 * @author honhimW
 * @since 2025-06-04
 */

@MappedSuperclass
public interface BaseAR extends SoftDeleteAR, AuditAR {
}
