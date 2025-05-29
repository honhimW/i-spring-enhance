package io.github.honhimw.test.ddd;

import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author honhimW
 * @since 2025-05-27
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    private ApplicationContextRunner contextRunner;

    @BeforeAll
    void beforeAll() {
        contextRunner = new ApplicationContextRunner();
        List<Class<?>> classes = userConfig();
        if (CollectionUtils.isNotEmpty(classes)) {
            contextRunner = contextRunner.withUserConfiguration(classes.toArray(Class[]::new));
        }
        Map<String, String> properties = properties();
        if (MapUtils.isNotEmpty(properties)) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                contextRunner = contextRunner.withPropertyValues("%s=%s", entry.getKey(), entry.getValue());
            }
        }
    }

    protected Map<String, String> properties() {
        return Collections.emptyMap();
    }

    protected List<Class<?>> userConfig() {
        return Collections.emptyList();
    }

    @Test
    @SneakyThrows
    void doRun() {
        contextRunner.run(this::run);
    }

    protected abstract void run(AssertableApplicationContext context) throws Exception;

}
