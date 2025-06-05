package io.github.honhimw.ddd.jimmer.acl;

import io.github.honhimw.ddd.jimmer.domain.Specification;
import jakarta.annotation.Nullable;

/**
 * @author honhimW
 * @since 2025-05-29
 */

public interface AclExecutor {

    @Nullable
    Specification.Query read();

    void write();

    @Nullable
    Specification.Delete delete();

}
