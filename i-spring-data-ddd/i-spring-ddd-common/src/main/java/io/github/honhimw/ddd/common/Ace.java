package io.github.honhimw.ddd.common;

import io.github.honhimw.core.MatchingType;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Access-Control-Entry
 * Entry of the Access-Control-List
 * ACL is composed of any number of Aces.
 *
 * @author hon_him
 * @since 2023-09-12
 */

public interface Ace {

    @Nonnull
    String getDataDomain();

    @Nonnull
    String getTargetValue();

    @Nonnull
    ResourceMod getMod();

    @Nullable
    String getName();

    @Nullable
    String getValue();

    @Nullable
    MatchingType getMatchingType();

}
