package io.github.honhimw.spring.cloud.dev;

/**
 * @author hon_him
 * @since 2024-08-09
 */
public @interface TestServer {

    /**
     * The name of the service to be discovered.
     *
     * @return the service id
     */
    String serviceId();

    /**
     * Service instance host.
     *
     * @return the host
     */
    String host();

    /**
     * Service instance port.
     *
     * @return the port
     */
    int port();

    /**
     * Whether the connection is secure.
     *
     * @return the secure
     */
    boolean secure() default false;
}
