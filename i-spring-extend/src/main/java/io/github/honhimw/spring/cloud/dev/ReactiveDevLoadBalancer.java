package io.github.honhimw.spring.cloud.dev;

import io.github.honhimw.spring.util.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 开发环境下优先路由到本机IP, 避免多名开发人员的实例互相干扰
 * @author hon_him
 * @since 2022-06-10
 */

@Slf4j
public class ReactiveDevLoadBalancer extends RandomLoadBalancer {

    /**
     * 提供服务实例
     */
    protected ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    /**
     * 服务名, ${spring.application.name}
     */
    private final String serviceId;

    /**
     * 本机IP
     */
    private final String localIP;

    private final ServiceInstance defaultInstance;

    public ReactiveDevLoadBalancer(
        ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
        String serviceId,
        ServiceInstance defaultInstance
    ) {
        super(serviceInstanceListSupplierProvider, serviceId);
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.serviceId = serviceId;
        this.localIP = Objects.isNull(IpUtils.firstLocalIP()) ? IpUtils.localIPv4() : IpUtils.firstLocalIP();
        this.defaultInstance = defaultInstance;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider.getIfAvailable(
            NoopServiceInstanceListSupplier::new);
        return supplier.get(request).next().map(serviceInstances -> this.processInstanceResponse(supplier,
            serviceInstances));
    }

    private Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier,
        List<ServiceInstance> serviceInstances) {
        Response<ServiceInstance> serviceInstanceResponse = this.getInstanceResponse(serviceInstances);
        if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
            ((SelectedInstanceCallback) supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
        }
        return serviceInstanceResponse;
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            return Objects.nonNull(defaultInstance) ? new DefaultResponse(defaultInstance) : new EmptyResponse();
        } else {
            ServiceInstance instance =
                instances.stream().filter(serviceInstance -> StringUtils.equals(localIP,
                    serviceInstance.getHost())).findAny().orElse(null);
            if (Objects.isNull(instance)) {
                int index = ThreadLocalRandom.current().nextInt(instances.size());
                instance = instances.get(index);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("服务: {}, 实例: {}个, 本机IP: {}, 优先进入本机", serviceId, instances.size(), localIP);
                }
            }
            return new DefaultResponse(instance);
        }
    }
}
