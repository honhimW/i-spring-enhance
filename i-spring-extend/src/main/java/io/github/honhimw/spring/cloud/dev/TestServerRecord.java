package io.github.honhimw.spring.cloud.dev;

import org.springframework.cloud.client.ServiceInstance;

import java.util.Map;

/**
 * @author hon_him
 * @since 2024-08-09
 */

public record TestServerRecord(String preferHost, Map<String, ServiceInstance> instances) {
}
