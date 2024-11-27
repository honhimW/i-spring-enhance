package io.github.honhimw.ddd.common;

import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;

/**
 * @author hon_him
 * @since 2023-09-12
 */
public class EmptyAclImpl implements Acl {

    public static Acl INSTANCE = new EmptyAclImpl();

    private EmptyAclImpl() {
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Nonnull
    @Override
    public Collection<Ace> getAces() {
        return Collections.emptyList();
    }

}
