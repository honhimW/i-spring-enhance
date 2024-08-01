# Spring-Boot-Enhancement configuration

## Web configuration for MVC/Webflux

### Http delegating log

Http raw data logging by delegating the very IOStream of Request/Response. Only work on the data is read/written(e.g. HttpBody haven't be used by the server, the server may not read the data from the IOStream thus the data will not be logged).

- MVC: io.github.honhimw.spring.web.mvc.MvcHttpLogFilter
- Webflux: io.github.honhimw.spring.web.reactive.ReactiveHttpLogHandler

### Exception handling

```java
/**
 * @see io.github.honhimw.spring.web.common.ExceptionWrapper
 * Declare a bean that can be scan
 */
@Component
public class SomeExceptionWrapper implements ExceptionWrapper {
    @Override
    public boolean support(@Nonnull Throwable e) {
        return e instanceof A_Exception
            || e instanceof B_Exception
            || (StringUtils.startsWith(e.getClass().getPackage().getName(), "org.example.exce"))
            ;
    }
    @Nonnull
    @Override
    public Object wrap(@Nonnull Throwable e) {
        Result<Void> result = Result.empty();
        result.code("500");
        result.msg(e.getMessage());
        return result;
    }
}
```

### Json response properties fetcher

Fetch only selected properties, make the result message more simple.

```http request
POST http://localhost:80/hello/world/page
FETCHER-ONLY-INCLUDE: /code;/data/*/content;/msg;/data/1/id
```
```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "content": "hello"
    },
    {
      "id": 1,
      "content": "world"
    }
  ]
}
```

### TextParam/FormDataParam resolver

- TextParam: Aggregate those text-type param(e.g. RquestParameter/form-data-url-encoded/Json/pathVariable) to an single entity.
- FormDataParam: form-data/multipart is included.

### Validation

- PhoneNumber: CN phoneNumber format validation.

---

## Cache configuration

### TTLCache

An interface that extends Map<K, V> defines a way to set a separate Time-To-Live for each key(common redis usage).

### RefreshableCache

An interface that defines cache data  that refreshable by specific-event.

### Redis extendsion

- RedisUtils/R2edisUtils: Provide a Generic supported usage(Multi-RedisTemplate);
- RedisKeySpaceEvent: support cluster-mode redisEvent;
- RedisTTLCache: An implementation of TTLCache using RedisTemplate;

### Memory

- InMemoryTTLCache: An implementation of TTLCache using ConcurrentHashMap and ScheduledExecutorService;