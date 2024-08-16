package org.example;

import io.github.honhimw.spring.cloud.dev.Config;
import io.github.honhimw.spring.cloud.dev.EnableDevLoadBalancer;
import io.github.honhimw.spring.cloud.dev.TestServer;
import io.github.honhimw.spring.web.annotation.EnableCsvConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author hon_him
 * @since 2023-05-10
 */

@EnableCsvConverter
@EnableCaching
@SpringBootApplication
@EnableDevLoadBalancer({
    @Config(profile = "develop", servers = {
        @TestServer(serviceId = "geo-service", host = "geo-test.internal", port = 8080)
    }),
    @Config(profile = "test", servers = {
        @TestServer(serviceId = "geo-service", host = "geo-uat.cloud", port = 443, secure = true),
    }, preferHost = "geo-test.internal")
})
public class WebApp {

    public static void main(String[] args) {
        SpringApplication.run(WebApp.class, args);
    }

}
