package io.github.honhimw.example.web;

import io.github.honhimw.core.IResult;
import io.github.honhimw.example.domain.jpa.PersonServiceImpl;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author hon_him
 * @since 2024-11-25
 */

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController extends PersonServiceImpl<Map<String, Map<String, Object>>> {

    @RequestMapping("/any")
    public IResult<Void> any(@TextParam Map<String, String> foo) {
        log.info(foo.toString());
        return IResult.ok();
    }

    @RequestMapping("/date")
    public IResult<Void> date(@TextParam IResult<Date> params) {
        log.info(params.toString());
        return IResult.ok();
    }

    public record Date(LocalDateTime at) {}


}
