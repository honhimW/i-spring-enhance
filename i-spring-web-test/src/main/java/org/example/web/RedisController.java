package org.example.web;

import io.github.honhimw.spring.Result;
import io.github.honhimw.spring.cache.redis.RedisUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * @author hon_him
 * @since 2023-07-03
 */

@RestController
@RequestMapping("/redis")
public class RedisController {

    @RequestMapping("/get")
    public Result<String> get(@RequestParam("key") String key) {

        String s = RedisUtils.get(key, String.class);
        return Result.ok(s);
    }

    @RequestMapping("/put")
    public Result<Boolean> put(@RequestParam("key") String key, @RequestParam("value") String value) {
        Boolean put = RedisUtils.put(key, value);
        return Result.ok(put);
    }

    @RequestMapping("/putString")
    public Result<Boolean> putString(@RequestParam("key") String key, @RequestParam("value") String value) {
        Boolean put = RedisUtils.putAsString(key, Map.of(key, value));
        return Result.ok(put);
    }

    @RequestMapping("/getString")
    public Result<Object> getString(@RequestParam("key") String key) {
        Object o = RedisUtils.getStringAs(key, Map.class);
        return Result.ok(o);
    }
    
}
