package io.github.honhimw.ddd.common;

import org.jspecify.annotations.NonNull;

import java.util.Collection;

/**
 * @author hon_him
 * @since 2023-09-12
 */

public interface Acl {

    boolean isRoot();

    @NonNull
    Collection<Ace> getAces();


}
