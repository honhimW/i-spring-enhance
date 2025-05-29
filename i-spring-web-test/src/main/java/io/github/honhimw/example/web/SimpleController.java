package io.github.honhimw.example.web;

import io.github.honhimw.core.IResult;
import io.github.honhimw.example.feign.DungEater;
import io.github.honhimw.spring.annotation.resolver.FileReturn;
import io.github.honhimw.spring.annotation.resolver.PartParam;
import io.github.honhimw.spring.web.util.DispositionHelper;
import io.github.honhimw.spring.web.util.HttpLogHelper;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author hon_him
 * @since 2023-05-10
 */

@Slf4j
@RestController
@RequestMapping("/simple")
public class SimpleController {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private DungEater dungEater;

    @GetMapping("/mvc/log")
    public IResult<String> mvcLog() {
        HttpLogHelper.Mvc.log((logger, httpLog) -> {
            logger.warn("before super log");
            httpLog.log();
            logger.warn("after super log");
        });
        return IResult.ok("log");
    }

    @GetMapping("/log2")
    public IResult<String> log2() {
        return IResult.ok();
    }

    @GetMapping("/log")
    public Mono<IResult<String>> log() {
        return HttpLogHelper.Webflux.log((logger, httpLog) -> {
            logger.warn("before super log");
            httpLog.log();
            logger.warn("after super log");
        }).then(Mono.just(IResult.ok("log")));
    }

    @RequestMapping("/hello")
    public IResult<String> hello() {
        return IResult.okWithMsg("{hello}");
    }

    @RequestMapping("/throw")
    public IResult<Void> error() {
        throw new RuntimeException("runtime ex.");
    }

    @RequestMapping("/feign")
    public String feign(@RequestParam("content") String content) {
        return dungEater.eat(content);
    }

    @RequestMapping("/csv")
    public IResult<List<Entity>> list(@PartParam("file") List<Entity> list) {
        return IResult.ok(list);
    }

    @RequestMapping("/csv-reactive")
    public Mono<IResult<List<Entity>>> listReactive(@PartParam("file") List<Entity> list) {
        return Mono.just(IResult.ok(list));
    }

    @RequestMapping("/csv-reactive2")
    public Mono<IResult<List<Entity>>> listReactive2(@PartParam("file") Mono<List<Entity>> list) {
        return list.map(IResult::ok);
    }

    @RequestMapping("/csv-reactive3")
    public Mono<IResult<List<Entity>>> listReactive3(@PartParam("file") Flux<List<Entity>> list) {
        return list.next().map(IResult::ok);
    }

    @GetMapping(value = "/getCsv", produces = "text/csv")
    public ResponseEntity<String> getCsv() {
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=test.csv")
            .body("""
                "id","name"
                "1","name1"
                "2","name2"
                "3","name3"
                """);
    }

    @GetMapping(value = "/getEntities", produces = "text/csv")
    public List<Entity> getEntities() {
        return List.of(new Entity("1"), new Entity("2"), new Entity("3"));
    }

    @RequestMapping(value = "/csv-transform", produces = {"application/json", "text/csv"})
    @FileReturn("csvTran.csv")
    public List<Entity> csvTransform(@PartParam("file") List<Entity> list) {
        DispositionHelper.attachment("ct.csv");
        return list;
    }

    @RequestMapping(value = "/multi-csv-transform", produces = {"application/json", "text/csv"})
    @FileReturn(value = "csvTran.csv", encoding = FileReturn.Encoding.UTF_8_BOM)
    public List<Entity> multiCsvTransform(@PartParam("file") List<Entity> list, @PartParam("file2") List<Entity> list2) {
        DispositionHelper.attachment("ct.csv");
        List<Entity> concat = new ArrayList<>();
        concat.addAll(list);
        concat.addAll(list2);
        return concat;
    }

    @GetMapping("/getEntity")
    public Entity getEntity(Entity entity) {
        return entity;
    }

    @Controller
    @RequestMapping("/simple2")
    public static class Simple2Controller {
        @RequestMapping(value = "/csv-transform", produces = "text/csv")
        @FileReturn("csvTran.csv")
        public List<Entity> csvTransform(@PartParam("file") List<Entity> list) {
            return list;
        }

        @RequestMapping(value = "/csv-transform-reactive", produces = "text/csv")
        @FileReturn("csvTran.csv")
        public Mono<List<Entity>> csvTransformReactive(@PartParam("file") Mono<List<Entity>> list) {
            return list;
        }

        @GetMapping(value = "/getEntities", produces = "text/csv")
        @FileReturn(value = "csvTran.csv", encoding = FileReturn.Encoding.UTF_8_BOM)
        public List<Entity> getEntities() {
            return List.of(new Entity("1"), new Entity("2"), new Entity("你好"), new Entity("哈喽", "CSV", Map.of("a", "b")));
        }

        @GetMapping("/getEntity")
        public Entity getEntity(Entity entity) {
            return entity;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entity implements Serializable {
        public Entity(String id) {
            this.id = id;
        }

        @NotBlank
        private String id;
        private String name;
        private Map<String, String> attributes;
    }


}
