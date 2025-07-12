package io.github.honhimw.spring.cloud.dev;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Under development. Avoid multiple developers affecting each other by using the same host instance as preferred
 *
 * @author hon_him
 * @since 2022-06-10
 */

@Slf4j
public class ReactiveDevLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private static final ServiceInstanceListSupplier NOOP_SERVICE_INSTANCE_LIST_SUPPLIER = new NoopServiceInstanceListSupplier();

    protected ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    private final String serviceId;

    private final String preferHost;

    private final ServiceInstance defaultInstance;

    public ReactiveDevLoadBalancer(
        ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
        String serviceId,
        String preferHost,
        ServiceInstance defaultInstance
    ) {
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.serviceId = serviceId;
        this.preferHost = preferHost;
        this.defaultInstance = defaultInstance;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider.getIfAvailable(() -> NOOP_SERVICE_INSTANCE_LIST_SUPPLIER);
        return supplier.get(request).next().map(serviceInstances -> this.processInstanceResponse(supplier,
            serviceInstances));
    }

    private Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier,
                                                              List<ServiceInstance> serviceInstances) {
        Response<ServiceInstance> serviceInstanceResponse = this.getInstanceResponse(serviceInstances);
        if (supplier instanceof SelectedInstanceCallback selectedInstanceCallback && serviceInstanceResponse.hasServer()) {
            selectedInstanceCallback.selectedServiceInstance(serviceInstanceResponse.getServer());
        }
        return serviceInstanceResponse;
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            return Objects.nonNull(defaultInstance) ? new DefaultResponse(defaultInstance) : new EmptyResponse();
        } else {
            ServiceInstance instance =
                instances.stream().filter(serviceInstance -> Strings.CS.equals(preferHost,
                    serviceInstance.getHost())).findAny().orElse(null);
            if (Objects.isNull(instance)) {
                int index = ThreadLocalRandom.current().nextInt(instances.size());
                instance = instances.get(index);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Matched Local Instance. ServiceId: [{}], Instances: [{}], Prefer: [{}].", serviceId, instances.size(), preferHost);
                }
            }
            return new DefaultResponse(instance);
        }
    }
}
