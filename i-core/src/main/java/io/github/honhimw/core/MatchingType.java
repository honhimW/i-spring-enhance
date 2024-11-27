package io.github.honhimw.core;

/**
 * @author hon_him
 * @since 2024-11-18
 */
public enum MatchingType {

    EQUAL,
    NOT_EQUAL,
    IN,

    NULL,
    NOT_NULL,

    /**
     * StringMatching
     */
    STARTING,
    ENDING,
    CONTAINING,

    /**
     * NumberMatching
     */

    GT,
    GE,
    LT,
    LE,

}
