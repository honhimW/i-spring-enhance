# Spring-Boot-Enhancement configuration

## Web configuration for MVC/Webflux

### Http delegating log

Http raw data logging by delegating the very IOStream of Request/Response. Only work on the data is read/written(e.g.
HttpBody haven't be used by the server, the server may not read the data from the IOStream thus the data will not be
logged).

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

Or fetch only non excluded properties.

```http request
POST http://localhost:80/hello/world/page
FETCH-NON-EXCLUDE: /code;/data/*;
```

```json
{
  "msg": "success",
  "data": [
  ]
}
```

### TextParam/FormDataParam resolver

- TextParam: Aggregate those text-type param(e.g. RquestParameter/form-data-url-encoded/Json/pathVariable) to an single
  entity.
- FormDataParam: `multipart/form-data` is included.

> JacksonNodeReactiveCustomizer:
> * CsvJacksonNodeReactiveCustomizer(with `@CsvField`, application/csv)
> * YamlJacksonNodeReactiveCustomizer(application/yaml)
>
> JacksonNodeCustomizer
> * CsvJacksonNodeCustomizer(with `@CsvField`, application/csv)
> * YamlJacksonNodeCustomizer(application/yaml)

### PartParam/FileReturn resolver and result handler

- PartParam: accept a named part of `multipart/form-data` part, currently support CSV resolve.
- FileReturn: return handler that set the `Content-Disposition` header with a default filename, and specific a file content charset(bom if unicode).

> Json response properties fetcher also supported.

**Usage**
```java
@Controller
@EnableCsvConverter     // Enable CSV Converter Features
@SpringBootApplication
public class WebApp {
  public static void main(String[] args) { SpringApplication.run(WebApp.class, args); }

  @RequestMapping(value = "/concat", produces = {"text/csv"})
  @FileReturn(value = "csvTran.csv", encoding = FileReturn.Encoding.UTF_8_BOM)
  public List<Entity> conCSV(@PartParam("file") List<Entity> list, @PartParam("file2") List<Entity> list2) {
//    DispositionHelper.attachment("ct.csv"); you may override the filename by setting 'Content-Disposition' manually
    List<Entity> concat = new ArrayList<>();
    concat.addAll(list);
    concat.addAll(list2);
    return concat;
  }
}
```

### Validation

- PhoneNumber: CN phoneNumber format validation.

---

## Cache configuration

### TTLCache

An interface that extends Map<K, V> defines a way to set a separate Time-To-Live for each key(common redis usage).

### RefreshableCache

An interface that defines cache data that refreshable by specific-event.

### Redis extendsion

- RedisUtils/R2edisUtils: Provide a Generic supported usage(Multi-RedisTemplate);
- RedisKeySpaceEvent: support cluster-mode redisEvent;
- RedisTTLCache: An implementation of TTLCache using RedisTemplate;

### Memory

- InMemoryTTLCache: An implementation of TTLCache using ConcurrentHashMap and ScheduledExecutorService;

## Reactor Thread Local Accessor HttpHandler/WebHandler

Inject context parameters into thread local in reactive(project reactor)

```groovy
// required
implementation 'io.micrometer:context-propagation:1.1.1'
```

- AbstractThreadLocalHttpHandler
- AbstractThreadLocalWebFilter

## Spring Cloud Configuration

### Development Load Balancer

A Better load balancer under development.
- Avoid developers affecting each other by using the same host instance as preferred.
- Use the test server for the default service instance, so that developers don’t have to run the dependent services themselves.

```java
@SpringBootApplication
@EnableDevLoadBalancer({
        @Config(profile = "develop", servers = {
                @TestServer(serviceId = "geo-service", host = "geo-test.internal", port = 8080)
        }),
        @Config(profile = "test", servers = {
                @TestServer(serviceId = "geo-service", host = "geo-uat.cloud", port = 443, secure = true),
        }, preferHost = "geo-test.internal")
})
@EnableFeignClients
public class WebApp {
  public static void main(String[] args) { SpringApplication.run(WebApp.class, args); }
}
```

## Data

### Common

- Access control list: Acl/Ace/ResourceMod etc.
- Data operation event

### Jpa implementation

#### Event

**Let Your Entity Extends AbstractAR**, and override DomainEntity#eventBuilder
```java
@Setter
@Getter
@Entity
@Table(value = "table_name")
public class YourEntity extends AbstractAR<YourEntity, String> {

    @Id
    private String id;

    @Override
    public Function<DaoAction, ? extends DomainEvent<YourEntity, String>> eventBuilder() {
         return daoAction -> new YourEntityEvent(daoAction).id(getId()).entity(this);
    }
}
```

**Subscribe Entity Event with Spring Event**
```java
@Component
public class YourListener extends AbstractListener {
    @EventListener
    public void appEvent(YourEvent event) {
         afterInsert(event)
             .ifPresent(e -> {
                 String id = e.getId();
                 YourEntity entity = e.getEntity();
                 // do something
             });
    }
}
```

#### Acl

**Create an AclExecutor**
```java
public class CustomAclExecutorImpl<T> extends AbstractAclExecutor<T> {

    public PhalanxAclExecutorImpl(
        @Nonnull ResourceMod defaultMod,
        @Nonnull JpaEntityInformation<T, ?> ei,
        @Nonnull EntityManager em, @Nonnull String dataDomain) {
        super(defaultMod, ei, em, dataDomain);
    }

    @Override
    protected boolean guard() {
        if (SudoSupports.isSudo()) {
            log.warn("Sudo mode, executed without ACL guard.");
            return false;
        }
        return SecurityUtils.getAuthorizedUser().isPresent();
    }

    @Override
    protected boolean isRoot() {
        return SecurityUtils.getAuthorizedUser()
            .map(AuthorizedUser::isRoot)
            .orElse(false);
    }

    @Nonnull
    @Override
    protected Map<String, Object> getAttributes() {
        return SecurityUtils.getAuthorizedUser()
            .map(AuthorizedUser::getAttributes)
            .orElseGet(HashMap::new);
    }

    @Nonnull
    @Override
    protected List<? extends Ace> getAcl() {
        return SecurityUtils.getAuthorizedUser()
            .map(AuthorizedUser::getAcl)
            .orElseGet(ArrayList::new);
    }
}
```

**Register AclExecutor**

```java
@Component
public class CustomAclProvider implements AclProvider {
    @Nonnull
    @Override
    public <T> AclExecutor<T> getExecutor(
        JpaEntityInformation<T, ?> jpaEntityInformation,
        EntityManager entityManager, String dataDomain,
        ResourceMod resourceMod) {
        return new CustomAclExecutorImpl<>(resourceMod, jpaEntityInformation, entityManager, dataDomain);
    }
}
```

**Enable Repositories**
```java
@Configuration
@EnableJpaRepositories(basePackages = "repositories.package.to.scan", repositoryFactoryBeanClass = AclJpaRepositoryFactoryBean.class)
public class CustomConfiguration {}
```

**Naming Domain**
```java
@AclDataDomain(DatabaseObjectClass.TABLE_NAME)
@Entity
@Table(name = DatabaseObjectClass.TABLE_NAME)
@Generated(value = DatabaseObjectClass.TABLE_NAME)
public class DatabaseObjectClass<ID> extends AbstractAggregateRoot<ID> {
    @Id
    private ID id;
}
```

#### Query

> Suppose you have the following data object relationship structure as follows:
```json lines
{
  id: 1,
  name: {
    first_name: "John",
    last_name: "Doe"
  },
  parent: {
    id: 2,
    name: {
      first_name: "xxx",
      last_name: "xxx"
    }
  }
}
```

**Pattern Matching**, limited to Match as Equal, but easier to use
```java
// select * from person where first_name = 'xxx' and parent_id = '2' limit 10;
public void patternMatching() {
  IPageRequest<Person> iPageRequest = IPageRequest.of(1, 10);
  Person condition = new Person();
  Name name = new Name();
  name.setFirstName("xxx");
  condition.setName(name);
  Person parent = new Person();
  parent.setId("2");
  condition.setParent(parent);
  iPageRequest.setCondition(condition);
  Page<Person> paging = PageUtils.paging(personRepository, iPageRequest);
  PageInfoVO<Person> personPageInfoVO = PageUtils.pageInfoVO(paging, person -> person);
}
```

**Query Builder**, flexible
```java
// select * from person where first_name like '%xxx%' and parent_id is null limit 10;
public void queryBuilder() {
  IPageRequest<Person> iPageRequest = IPageRequest.of(1, 10);
  List<ConditionColumn> conditions = new ArrayList<>();
  conditions.add(ConditionColumn.of("name.firstName", "xxx", MatchingType.CONTAINING));
  conditions.add(ConditionColumn.of("parent.id", "2", MatchingType.NULL));
  iPageRequest.setConditions(conditions);
  Page<Person> paging = PageUtils.paging(personRepository, iPageRequest);
  PageInfoVO<Person> personPageInfoVO = PageUtils.pageInfoVO(paging, person -> person);
}
```

**Ordering**
```java
// select * from person order by id desc limit 10;
public void ordering() {
  IPageRequest<Person> iPageRequest = IPageRequest.of(1, 10);
  List<OrderColumn> orders = new ArrayList<>();
  orders.add(OrderColumn.of("id", true));
  iPageRequest.setOrders(orders);
  Page<Person> paging = PageUtils.paging(personRepository, iPageRequest);
  PageInfoVO<Person> personPageInfoVO = PageUtils.pageInfoVO(paging, person -> person);
}
```

**Example**
```java
@PostMapping("/list")
public IResult<PageInfoVO<PersonVO>> list(@TextParam IPageRequest<Person> iPageRequest) {
  Page<Person> paging = PageUtils.paging(personRepository, iPageRequest);
  PageInfoVO<PersonVO> personPageInfoVO = PageUtils.pageInfoVO(paging, person -> person.toVO());
  return IResult.ok(personPageInfoVO);
}
```

```http request
POST /list
Host: localhost:8080
Content-Type: application/json

{
  "page": 1,
  "pageSize": 10,
  "condition": {
    "name": {
      "firstName": "xxx"
    },
    "parent": {
      "id": "2"
    }
  },
  "orders": [
    {
      "column": "id",
      "desc": true
    }
  ]
}
```
