package io.github.honhimw.spring.cloud.dev;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.commons.util.SpringFactoryImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * @author hon_him
 * @since 2022-07-07
 */

@ConditionalOnDiscoveryEnabled
public class DevLoadBalancerSelector extends SpringFactoryImportSelector<EnableDevLoadBalancer> {

    static final Map<String, TestServerRecord> PROFILE_MAP = new ConcurrentHashMap<>();

    @Override
    protected boolean isEnabled() {
        return true;
    }

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        String[] imports =  super.selectImports(metadata);
        if (!isEnabled()) {
            return imports;
        }
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableDevLoadBalancer.class.getName());
        Objects.requireNonNull(attributes, "should not be null");
        String[] activeProfiles = getEnvironment().getActiveProfiles();
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(attributes);
        AnnotationAttributes[] values = annotationAttributes.getAnnotationArray("value");
        Map<String, TestServerRecord> profileMap = new HashMap<>();
        for (AnnotationAttributes value : values) {
            String profile = value.getString("profile");
            if (!ArrayUtils.contains(activeProfiles, profile)) {
                continue;
            }
            String preferHost = value.getString("preferHost");
            Map<String, ServiceInstance> instanceMap = new HashMap<>();
            TestServerRecord testServerRecord = new TestServerRecord(preferHost, instanceMap);
            profileMap.putIfAbsent(profile, testServerRecord);
            AnnotationAttributes[] servers = value.getAnnotationArray("servers");
            for (AnnotationAttributes server : servers) {
                String serviceId = server.getString("serviceId");
                String host = server.getString("host");
                int port = server.getNumber("port");
                boolean secure = server.getBoolean("secure");
                instanceMap.putIfAbsent(serviceId, new DefaultServiceInstance(UUID.randomUUID().toString(), serviceId, host, port, secure));
            }
        }

        if (profileMap.isEmpty() || !ArrayUtils.containsAny(activeProfiles, profileMap.keySet().toArray())) {
            return imports;
        }

        PROFILE_MAP.putAll(profileMap);
        return Stream.concat(Stream.of(DevLoadBalancerConfig.class.getName()), Arrays.stream(imports)).toArray(String[]::new);
    }

    @Override
    protected boolean hasDefaultFactory() {
        return true;
    }

}
