package io.github.honhimw.ddl.annotations;

import io.github.honhimw.ddl.ConstraintNamingStrategy;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author honhimW
 * @since 2025-07-02
 */
@Target({})
@Retention(RUNTIME)
public @interface Unique {

    String name() default "";

    /**
     * (Required) An array of the column names that make up the constraint.
     */
    String[] columns();

    Kind kind() default Kind.PATH;

    Class<? extends ConstraintNamingStrategy> naming() default ConstraintNamingStrategy.class;

}
