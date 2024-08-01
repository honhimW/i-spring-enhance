package io.github.honhimw.spring.data.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * Example:
 *
 * &#064;AclDataDomain("xxx")
 * public class XxxDO {
 *     &#064;Id
 *     private String id;
 * }
 * </pre>
 * @author hon_him
 * @since 2023-04-21
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AclDataDomain {

    /**
     * @return must not be blank
     */
    String value();

    /**
     * @return if enable acl read-capability
     */
    boolean read() default true;

    /**
     * @return if enable acl write-capability
     */
    boolean write() default true;

    /**
     * @return if enable acl write-capability
     */
    boolean execute() default true;

    /**
     * @return default mod when there are not acl is set
     */
    ResourceMod defaultMod() default ResourceMod.RWX;

}
