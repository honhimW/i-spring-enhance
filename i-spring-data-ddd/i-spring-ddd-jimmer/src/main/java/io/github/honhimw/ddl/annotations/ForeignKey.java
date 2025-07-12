package io.github.honhimw.ddl.annotations;

import io.github.honhimw.ddl.ConstraintNamingStrategy;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author honhimW
 * @since 2025-07-09
 */
@Target({})
@Retention(RUNTIME)
public @interface ForeignKey {

    String name() default "";

    String definition() default "";

    OnDeleteAction action() default OnDeleteAction.NONE;

    Class<? extends ConstraintNamingStrategy> naming() default ConstraintNamingStrategy.class;

}
