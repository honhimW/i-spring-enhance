package io.github.honhimw.spring.data.jpa.util;

import org.springframework.data.jpa.domain.Specification;

/**
 * @author hon_him
 * @since 2023-11-08
 */

public class Specifications {

    public static <T> Specification<T> isTrue() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(criteriaBuilder.literal(true));
    }

    public static <T> Specification<T> isFalse() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(criteriaBuilder.literal(false));
    }

}
