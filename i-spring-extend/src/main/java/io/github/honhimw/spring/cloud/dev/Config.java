package io.github.honhimw.spring.cloud.dev;

/**
 * @author hon_him
 * @since 2024-08-09
 */
public @interface Config {

    /**
     * active profile, single
     *
     * @return ${spring.profiles.active}
     */
    String profile();

    /**
     * Test Server information. Try to use as default instance when no instance is available.
     * Useful for development. Don't have to run the depended services.
     *
     * @return array of {@link TestServer}
     */
    TestServer[] servers() default {};

    /**
     * By default, the value is '#runtime' which means the runtime environment host.
     * If the application is deployed in an embedded container, the value may be set manually to the host machine IP address.
     * Also, if there are multiple IP addresses, you may want to set the host manually.
     *
     * @return If there are multiple instances, use the one with the same host.
     */
    String preferHost() default "#runtime";
}
