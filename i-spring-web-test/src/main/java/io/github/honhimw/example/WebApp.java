package io.github.honhimw.example;

import io.github.honhimw.spring.cloud.dev.Config;
import io.github.honhimw.spring.cloud.dev.EnableDevLoadBalancer;
import io.github.honhimw.spring.cloud.dev.TestServer;
import io.github.honhimw.spring.web.annotation.EnableCsvConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author hon_him
 * @since 2023-05-10
 */

@EnableCsvConverter
@EnableCaching
@SpringBootApplication(
    exclude = {
//        ISpringExtendAutoConfiguration.class
    }
)
@EnableDevLoadBalancer({
    @Config(profile = "dev", servers = @TestServer(serviceId = "dung-eater", host = "127.0.0.1", port = 11451)),
    @Config(profile = "test", servers = @TestServer(serviceId = "dung-eater", host = "127.0.0.1", port = 11451)),
})
@EnableFeignClients
public class WebApp {

    public static void main(String[] args) {
        SpringApplication.run(WebApp.class, args);
    }

}
