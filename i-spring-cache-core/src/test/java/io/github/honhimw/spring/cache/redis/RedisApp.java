package io.github.honhimw.spring.cache.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * @author hon_him
 * @since 2023-06-26
 */

@RestController
@SpringBootApplication
public class RedisApp {

    public static void main(String[] args) {
        SpringApplication.run(RedisApp.class, args);
    }

    @GetMapping("/get/{uuid}")
    public String get(@PathVariable("uuid") String uuid) {
        return RedisUtils.get("ws:" + uuid);
    }

    @GetMapping("/set/{uuid}")
    public String set(@PathVariable("uuid") String uuid, @RequestParam("value") String value, @RequestParam(value = "ttl", required = false, defaultValue = "10") Long ttl) {
        RedisUtils.put("ws:" + uuid, value, Duration.ofSeconds(ttl));
        return "ok";
    }

    @GetMapping("/del/{uuid}")
    public String del(@PathVariable("uuid") String uuid) {
        RedisUtils.remove("ws:" + uuid);
        return "ok";
    }

}
