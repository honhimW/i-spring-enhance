package io.github.honhimw.spring.annotation.resolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author hon_him
 * @since 2022-06-06
 */

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormDataParam {

    /**
     * exclude validate specific parameters in object path: "id", "entity.xxx",...
     * <p>
     * <pre>{@code
     * {
     *     id: "1",              // "id"
     *     entity: {             // "entity"
     *         hello: "world",   // "entity.hello"
     *         property: "any"   // "entity.property"
     *     },
     *     filePart: <FilePart>  // "filePart"
     * }
     * }</pre>
     */
    String[] excludesValidate() default {};

}
