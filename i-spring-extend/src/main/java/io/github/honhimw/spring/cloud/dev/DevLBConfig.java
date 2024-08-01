package io.github.honhimw.spring.cloud.dev;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author hon_him
 * @since 2022-06-10
 */

@Slf4j
@LoadBalancerClients(defaultConfiguration = DevLBConfig.class)
class DevLBConfig {

    @Bean
    public ReactorLoadBalancer<ServiceInstance> loadbalancer(
        ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
        Environment environment) {
        String clientName = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME, "");
        log.warn("==================={}开发环境自定义负载, 优先进入本地===================", clientName);
        return new ReactiveDevLoadBalancer(serviceInstanceListSupplierProvider, clientName, DevLBSelector.TEST_MAP.get(clientName));
    }

}
