package io.github.honhimw.spring.cloud.dev;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Better load balancer under development.
 * <ul>
 *     <li>Avoid developers to effect each other by using the same host instance as prefer.</li>
 *     <li>Use the test server for default service instance, so that developers don't have to run the depended services themselves.</li>
 * </ul>
 *
 * @author hon_him
 * @since 2022-07-07
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(DevLoadBalancerSelector.class)
public @interface EnableDevLoadBalancer {

    /**
     * Load balancer configuration depending on profile
     *
     * @return multiple configurations
     */
    Config[] value();

}
