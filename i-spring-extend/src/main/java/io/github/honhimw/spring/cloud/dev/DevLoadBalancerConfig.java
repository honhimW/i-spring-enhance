package io.github.honhimw.spring.cloud.dev;

import io.github.honhimw.util.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2022-06-10
 */

@Slf4j
@LoadBalancerClients(defaultConfiguration = DevLoadBalancerConfig.class)
class DevLoadBalancerConfig {

    @Bean
    public ReactorLoadBalancer<ServiceInstance> loadbalancer(
        ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
        Environment environment) {
        String clientName = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME, "");

        if (StringUtils.isNotBlank(clientName) && !DevLoadBalancerSelector.PROFILE_MAP.isEmpty()) {
            Map<String, ServiceInstance> instancesMap = new HashMap<>();
            Map<String, String> runtimePreferHost = new HashMap<>();

            DevLoadBalancerSelector.PROFILE_MAP.forEach((profile, testServerRecord) -> {
                String preferHost = testServerRecord.preferHost();
                if (Strings.CI.equals(preferHost, "#runtime")) {
                    preferHost = Objects.isNull(IpUtils.firstLocalIP()) ? IpUtils.localIPv4() : IpUtils.firstLocalIP();
                }
                final String _preferHost = preferHost;
                Map<String, ServiceInstance> instances = testServerRecord.instances();
                instances.forEach((serviceId, serviceInstance) -> {
                    if (Strings.CS.equals(clientName, serviceId)) {
                        instancesMap.putIfAbsent(clientName, serviceInstance);
                        runtimePreferHost.putIfAbsent(clientName, _preferHost);
                    }
                });
            });

            if (instancesMap.containsKey(clientName)) {
                ServiceInstance serviceInstance = instancesMap.get(clientName);
                String preferHost = runtimePreferHost.get(clientName);
                log.warn("Using dev load-balancer, preferHost: [{}], ServiceId: [{}].", preferHost, clientName);
                return new ReactiveDevLoadBalancer(serviceInstanceListSupplierProvider, clientName, preferHost, serviceInstance);
            }
        }
        return new RandomLoadBalancer(serviceInstanceListSupplierProvider, clientName);
    }

}
