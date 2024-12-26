package io.github.honhimw.test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.filter.TokenFilter;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.honhimw.core.IResult;
import io.github.honhimw.spring.cache.redis.RedisMessageEvent;
import io.github.honhimw.test.jacksonfilter.PointerFilteringGenerator;
import io.github.honhimw.util.JsonUtils;
import io.github.honhimw.util.ReactiveHttpUtils;
import jakarta.validation.*;
import jakarta.validation.bootstrap.GenericBootstrap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.validation.beanvalidation.LocaleContextMessageInterpolator;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hon_him
 * @since 2023-05-15
 */
@Slf4j
public class MainRunner {

    @Test
    @SneakyThrows
    void threadFactory() {
        ReactiveHttpUtils instance = ReactiveHttpUtils.getInstance();
        ReactiveHttpUtils.ReactiveHttpResult hello = instance.receiver(configurer -> configurer
            .patch()
            .url("http://127.0.0.1:11451/hello")
            .body(payload -> payload.raw(raw -> raw.text("hello")))
        );
        hello.response().block();
        ReactorClientHttpConnector reactorClientHttpConnector = new ReactorClientHttpConnector();
        Mono<ClientHttpResponse> connect = reactorClientHttpConnector.connect(HttpMethod.POST, URI.create("http://127.0.0.1:11451/hello"), clientHttpRequest -> {
            return clientHttpRequest.writeWith(Mono.just(clientHttpRequest.bufferFactory().wrap("hello".getBytes())));
        });
        ClientHttpResponse block = connect.block();
    }


    @Test
    @SneakyThrows
    void iResult() {
        IResult<Object> ok = IResult.ok();
        System.out.println(JsonUtils.toJson(ok));
    }

    @Test
    @SneakyThrows
    void validator() {
        GenericBootstrap genericBootstrap = Validation.byDefaultProvider();
        Configuration<?> configure = genericBootstrap.configure();
        MessageInterpolator defaultMessageInterpolator = configure.getDefaultMessageInterpolator();
        configure.messageInterpolator(new LocaleContextMessageInterpolator(defaultMessageInterpolator));
        ValidatorFactory validatorFactory = configure.buildValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        validatorFactory.close();

        Object arugment = List.of(
            new Person("honhim", 18, true, new Person("foo", 1, null, null)),
            new Person("hello", 18, null, null)
        );
        String[] excludesArgs = {"age"};
        Set<ConstraintViolation<Object>> validResult = validator.validate(arugment);
        if (StringUtils.isNoneBlank(excludesArgs)) {
            Set<String> ea = Arrays.stream(excludesArgs).collect(Collectors.toSet());
            validResult = validResult.stream().filter(cv -> !ea.contains(cv.getPropertyPath().toString())).collect(Collectors.toSet());
        }
        if (!validResult.isEmpty()) {
            throw new ConstraintViolationException(validResult);
        }
    }

    @Test
    @SneakyThrows
    void toml() {
        TomlMapper tomlMapper = new TomlMapper();
        tomlMapper.setSerializerFactory(JsonUtils.mapper().getSerializerFactory());

        Person person = new Person("honhim", 18, true, null);
        @Language("TOML")
        String tomlContent = """
            name = 'honhim'
            age = 18
            gender = true
            [[sub]]
            [[sub]]
            hello = 'world'
            foo = 'bar'
            [[sub]]
            hello = 'world2'
            foo = 'bar2'
            """;

        JsonNode jsonNode = tomlMapper.readTree(tomlContent);
        System.out.println(jsonNode.toPrettyString());

        System.out.println(tomlMapper.writeValueAsString(person));
    }

    @Test
    @SneakyThrows
    void yaml() {
        YAMLMapper yamlMapper = new YAMLMapper();
        yamlMapper.setSerializerFactory(JsonUtils.mapper().getSerializerFactory());

        Person person = new Person("honhim", 18, true, null);
        @Language("YAML")
        String yamlContent = """
            name: honhim
            age: 18
            gender: true
            sub:
              hello: world
              foo: bar
            """;

        JsonNode jsonNode = yamlMapper.readTree(yamlContent);
        System.out.println(jsonNode.toPrettyString());

        System.out.println(yamlMapper.writeValueAsString(person));
    }

    @Test
    @SneakyThrows
    void csv() {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.setSerializerFactory(JsonUtils.mapper().getSerializerFactory());
        CsvSchema columns = csvMapper.schemaFor(ObjectNode.class).withHeader();

        Person person = new Person("honhim", 18, true, null);
        String csvContent = """
            name,gender,age
            honhim,true,18
            tom,false,28
            """;

        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        try (MappingIterator<ObjectNode> iterator = csvMapper.reader().forType(ObjectNode.class).with(columns).readValues(csvContent)) {
            iterator.forEachRemaining(o -> {
                System.out.println(o);
            });
        }

        System.out.println(csvMapper.writer().with(csvMapper.schemaFor(Person.class).withHeader()).writeValueAsString(person));
        System.out.println(csvMapper.writer().with(csvMapper.schemaFor(Person.class).withHeader()).writeValueAsString(List.of(person, person)));

        CsvSchema build = CsvSchema.builder().addColumn("title").addColumn("id").build().withHeader();
        System.out.println(csvMapper.writer().with(build).writeValueAsString(List.of(
            Map.of("id", 1, "title", "foo"),
            Map.of("id", 2, "title", "bar")
        )));
    }

    @Test
    public void file() {
        File file = new File("E:\\temp");
        String[] list = file.list((dir, name) -> {
            return true;
        });
        for (String s : list) {
            System.out.println(s);
        }
    }

    @Test
    public void redisEvent() {
        RedisMessageEvent redisMessageEvent = new RedisMessageEvent("__keyevent@0__:lrem", "i:spring:a");
        if (redisMessageEvent.fromKeyEvent()) {
            System.out.println(redisMessageEvent.getDatabase());
            System.out.println(redisMessageEvent.getCommand());
        }
    }

    @Test
    public void readString() throws Exception {
        Object o = JsonUtils.fromJson(
            """
                {
                "hello":"world"
                }
                """
            , Object.class
        );
        System.out.println(o);
    }

    @Test
    public void jacksonFilter() throws Exception {
        ObjectMapper mapper = JsonUtils.mapper();
        SimpleFilterProvider fp = new SimpleFilterProvider();
        SimpleBeanPropertyFilter defaultFilter = SimpleBeanPropertyFilter.serializeAllExcept();
        fp.addFilter("default", defaultFilter);
        fp.setDefaultFilter(defaultFilter);
        mapper.setFilterProvider(fp);
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            public Object findFilterId(Annotated a) {
                return Optional.of(a)
                    .map(super::findFilterId)
                    .orElse("specific");
            }

        });

        Entity entity = get();
        ObjectWriter objectWriter = mapper.writerFor(Entity.class);
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        objectWriter = objectWriter.with(filterProvider);
        String s = objectWriter.writeValueAsString(entity);
        System.out.println(s);

    }

    @Test
    public void jsonPointer() throws Exception {
        JsonPointer jsonPointer = JsonPointer.compile("/root/hello/0/world");
        JsonPointer jsonPointer1 = jsonPointer.matchProperty("root");
        System.out.println(jsonPointer1);
    }

    @Test
    public void tokenFilter() throws Exception {
        Entity entity = get();
        ObjectMapper mapper = JsonUtils.mapper();
        SegmentedStringWriter w = new SegmentedStringWriter(mapper.getFactory()._getBufferRecycler());
        JsonGenerator generator = mapper.createGenerator(w);
        PointerFilteringGenerator delegate = new PointerFilteringGenerator(
            generator,
            new IFilter("/id", "/gender", "/sub/title", "/ss/0/title", "/ss/*/firstName"),
//            new JsonPointerBasedFilter(JsonPointer.compile("/ss/title")),
//            TokenFilter.INCLUDE_ALL,
            TokenFilter.Inclusion.INCLUDE_NON_NULL,
            true);
        mapper.writeValue(delegate, entity);
        System.out.println(w.getAndClear());
    }

    public static class IFilter extends TokenFilter {

        public static final String ANY_INDEX = "*";

        private final Set<JsonPointer> _includes = new HashSet<>();

        public IFilter(String... includes) {
            Set<String> set = Set.of(includes);
            set.stream()
                .map(JsonPointer::compile)
                .forEach(_includes::add);
        }

        public IFilter(Set<JsonPointer> includes) {
            _includes.addAll(includes);
        }

        @Override
        public TokenFilter includeElement(int index) {
            Set<JsonPointer> next = new HashSet<>();
            for (JsonPointer pointer : _includes) {
                if (StringUtils.equals(pointer.getMatchingProperty(), ANY_INDEX)) {
                    next.add(pointer.matchProperty(ANY_INDEX));
                } else {
                    JsonPointer _next = pointer.matchElement(index);
                    if (Objects.nonNull(_next)) {
                        next.add(_next);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(next)) {
                boolean b = next.stream().anyMatch(JsonPointer::matches);
                if (b) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return new IFilter(next);
            } else {
                return new IFilter();
            }
        }

        @Override
        public TokenFilter includeProperty(String name) {
            System.out.println("name = " + name);
            Set<JsonPointer> set = _includes.stream()
                .map(jsonPointer -> jsonPointer.matchProperty(name))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(set)) {
                boolean b = set.stream().anyMatch(JsonPointer::matches);
                if (b) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return new IFilter(set);
            } else {
                return null;
            }
        }

        @Override
        public TokenFilter filterStartObject() {
            if (CollectionUtils.isEmpty(_includes)) {
                return null;
            }
            return super.filterStartObject();
        }

    }

    private Entity get() {
        Entity entity = new Entity();
        entity.setId("1");
        entity.setAge(18);
        entity.setGender(false);
        Entity.SubEntity sub = new Entity.SubEntity();
        sub.setTitle("hello");
        sub.setFirstName("world");
        entity.setSub(sub);
        entity.setSs(new Entity.SubEntity[]{sub, sub});
        return entity;
    }

    public static class Entity implements Serializable {

        private String id;
        private Integer age;
        private Boolean gender;
        private SubEntity sub;
        private SubEntity[] ss;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public Boolean getGender() {
            return gender;
        }

        public void setGender(Boolean gender) {
            this.gender = gender;
        }

        public SubEntity getSub() {
            return sub;
        }

        public void setSub(SubEntity sub) {
            this.sub = sub;
        }

        public SubEntity[] getSs() {
            return ss;
        }

        public void setSs(SubEntity[] ss) {
            this.ss = ss;
        }

        public static class SubEntity implements Serializable {
            private String title;
            private String firstName;

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getFirstName() {
                return firstName;
            }

            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }
        }

    }

}
