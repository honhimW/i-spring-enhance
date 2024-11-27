package io.github.honhimw.ddd.jpa.acl;

import jakarta.annotation.Nullable;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author hon_him
 * @since 2023-12-28
 */

public interface AclExecutor<T> {

    @Nullable
    <S extends T> Specification<S> read();

    void write() throws UnsupportedOperationException;

}
