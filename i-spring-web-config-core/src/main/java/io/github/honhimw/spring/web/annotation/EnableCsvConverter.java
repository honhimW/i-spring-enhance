package io.github.honhimw.spring.web.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author hon_him
 * @since 2024-08-09
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(CsvConverterConfiguration.class)
public @interface EnableCsvConverter {
}
