package io.github.honhimw.example.web;

import io.github.honhimw.core.IResult;
import io.github.honhimw.example.model.RedisJavaSerial;
import io.github.honhimw.spring.cache.redis.RedisUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * @author hon_him
 * @since 2023-07-03
 */

@RestController
@RequestMapping("/redis")
public class RedisController {

    @RequestMapping("/get")
    public IResult<String> get(@RequestParam("key") String key) {

        String s = RedisUtils.get(key, String.class);
        return IResult.ok(s);
    }

    @RequestMapping("/put")
    public IResult<Boolean> put(@RequestParam("key") String key, @RequestParam("value") String value) {
        Boolean put = RedisUtils.put(key, value);
        return IResult.ok(put);
    }

    @RequestMapping("/putString")
    public IResult<Boolean> putString(@RequestParam("key") String key, @RequestParam("value") String value) {
        Boolean put = RedisUtils.putAsString(key, Map.of(key, value));
        return IResult.ok(put);
    }

    @RequestMapping("/getString")
    public IResult<Object> getString(@RequestParam("key") String key) {
        Object o = RedisUtils.getStringAs(key, Map.class);
        return IResult.ok(o);
    }

    @Autowired
    private SimpleService simpleService;

    @GetMapping("/cacheable")
    public IResult<Void> cacheable(@RequestParam("key") String key, @RequestParam("value") String value) {
        simpleService.put(key, value);
        return IResult.ok();
    }

    @GetMapping("/getFromCacheable")
    public IResult<String> getFromCacheable(@RequestParam("key") String key) {
        return IResult.ok(simpleService.get(key));
    }

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @SneakyThrows
    @RequestMapping("/putJavaSerialized")
    public IResult<Boolean> putJavaSerialized(@RequestParam("key") String key, @RequestParam("value") String value) {
        RedisJavaSerial _value = RedisJavaSerial.defaultObject();
        _value.setString(value);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(_value);
        oos.flush();
        RedisUtils.putBytes(key, out.toByteArray());
        return IResult.ok(true);
    }

    @RequestMapping("/getJavaSerialized")
    public IResult<RedisJavaSerial> getJavaSerialized(@RequestParam("key") String key) {
        RedisTemplate<String, RedisJavaSerial> redisTemplate = new RedisTemplate<>();
        JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
        RedisSerializer<?> keySerializer = RedisUtils.writeRedisTemplate().getKeySerializer();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setValueSerializer(jdkSerializationRedisSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jdkSerializationRedisSerializer);
        redisTemplate.afterPropertiesSet();

        return IResult.ok(redisTemplate.opsForValue().get(key));
    }

}
