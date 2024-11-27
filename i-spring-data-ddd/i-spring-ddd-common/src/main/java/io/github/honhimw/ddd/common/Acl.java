package io.github.honhimw.ddd.common;

import jakarta.annotation.Nonnull;

import java.util.Collection;

/**
 * @author hon_him
 * @since 2023-09-12
 */

public interface Acl {

    boolean isRoot();

    @Nonnull
    Collection<Ace> getAces();


}
