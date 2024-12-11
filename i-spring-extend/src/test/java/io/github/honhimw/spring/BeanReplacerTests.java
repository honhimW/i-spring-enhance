package io.github.honhimw.spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author hon_him
 * @since 2023-11-27
 */

@SpringBootConfiguration
@Configuration
@SpringBootTest
public class BeanReplacerTests {

    @Bean
    BuildIn echoFooBuildIn() {
        return () -> System.out.println("foo");
    }

    @Bean
    AbstractBeanReplacer<BarBuildIn> simpleBeanReplacer() {
        return new AbstractBeanReplacer<>("echoFooBuildIn") {
        };
    }

    @Bean
    ApplicationListener<ApplicationStartedEvent> stopListener(List<BuildIn> buildIns) {
        return event -> buildIns.forEach(BuildIn::setup);
    }

    @Test
    void doNothing() throws Exception {
    }

    public static class BarBuildIn implements BuildIn {
        @Override
        public void setup() {
            System.out.println("bar");
        }
    }

}
