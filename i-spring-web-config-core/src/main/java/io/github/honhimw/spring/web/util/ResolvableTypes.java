package io.github.honhimw.spring.web.util;

import org.springframework.core.ResolvableType;

import java.util.Collection;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-08-07
 */

public class ResolvableTypes {

    public static final ResolvableType COLLECTION_TYPE = ResolvableType.forType(Collection.class);

    public static final ResolvableType MAP_TYPE = ResolvableType.forType(Map.class);

    public static final ResolvableType STRING_TYPE = ResolvableType.forType(String.class);


}
