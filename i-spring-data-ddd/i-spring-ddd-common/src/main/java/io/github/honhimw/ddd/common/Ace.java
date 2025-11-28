package io.github.honhimw.ddd.common;

import io.github.honhimw.core.MatchingType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Access-Control-Entry
 * Entry of the Access-Control-List
 * ACL is composed of any number of Aces.
 *
 * @author hon_him
 * @since 2023-09-12
 */

public interface Ace {

    @NonNull
    String getDataDomain();

    @NonNull
    String getTargetValue();

    @NonNull
    ResourceMod getMod();

    @Nullable
    String getName();

    @Nullable
    String getValue();

    @Nullable
    MatchingType getMatchingType();

}
