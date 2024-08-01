package io.github.honhimw.spring.cloud.dev;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.commons.util.SpringFactoryImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author hon_him
 * @since 2022-07-07
 */

public class DevLBSelector extends SpringFactoryImportSelector<EnableDevLB> {

    static final Map<String, ServiceInstance> TEST_MAP = new ConcurrentHashMap<>();

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
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableDevLB.class.getName());
        Objects.requireNonNull(attributes, "should not be null");
        String[] activeProfiles = getEnvironment().getActiveProfiles();
        String activeProfile = (String) attributes.get("activeProfile");
        if (!ArrayUtils.contains(activeProfiles, activeProfile)) {
            return imports;
        }
        String[] testIP = (String[]) attributes.get("testIP");
        if (ArrayUtils.isNotEmpty(testIP)) {
            Map<String, ServiceInstance> testMap = Arrays.stream(testIP).filter(StringUtils::isNotBlank)
                .map(tip -> tip.split("@")).collect(Collectors.toMap(strings -> strings[0], strings -> {
                    String[] ipPort = strings[1].split(":");
                    return new DefaultServiceInstance(UUID.randomUUID().toString(), strings[0],
                        ipPort[0], Integer.parseInt(ipPort[1]), false);
                }));
            TEST_MAP.putAll(testMap);
        }
        return Arrays.asList(DevLBConfig.class.getName(), imports).toArray(String[]::new);
    }

    @Override
    protected boolean hasDefaultFactory() {
        return true;
    }

}
