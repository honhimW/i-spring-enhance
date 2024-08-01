package io.github.honhimw.spring.web.common.resolver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inject csv data into Parameter Target Field
 * @author hon_him
 * @since 2024-07-18
 */

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CsvField {

    String value();

}
