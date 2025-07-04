package io.github.honhimw.ddl.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author honhimW
 * @since 2025-07-02
 */
@Target({})
@Retention(RUNTIME)
public @interface Check {

    String name() default "";

    String constraint();

}
