package io.github.honhimw.ddd.jimmer.acl;

import io.github.honhimw.ddd.jimmer.domain.Specification;
import org.jspecify.annotations.Nullable;

/**
 * @author honhimW
 * @since 2025-05-29
 */

public interface AclExecutor {

    Specification.@Nullable Query read();

    void write();

    Specification.@Nullable Delete delete();

}
