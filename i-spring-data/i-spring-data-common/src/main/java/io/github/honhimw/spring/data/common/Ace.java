package io.github.honhimw.spring.data.common;

import io.github.honhimw.spring.model.IPageRequest;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Access control Entry(访问控制项), 即访问控制表对应的单个条目项.
 * 访问控制表即是由多个访问控制项组成的集合
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
    IPageRequest.MatchingType getMatchingType();

}
