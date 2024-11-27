package org.example.web;

import io.github.honhimw.core.IResult;
import io.github.honhimw.spring.annotation.resolver.TextParam;
import io.github.honhimw.spring.web.common.resolver.annotation.CsvField;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * @author hon_him
 * @since 2024-06-17
 */

@RestController
@RequestMapping("/textParam")
public class TextParamAnnotationController {

    @RequestMapping("/test")
    public IResult<String> test(@TextParam TypeWithParameter<String, TypeWithParameter<Integer, Boolean>> name) {
        return Optional.ofNullable(name)
            .map(TypeWithParameter::getR)
            .map(TypeWithParameter::getT)
            .map(String::valueOf)
            .map(IResult::ok)
            .orElse(IResult.err());
    }

    @RequestMapping("/csvRaw")
    public IResult<?> csvRaw(@TextParam @CsvField("r") TypeWithParameter<String, List<TypeWithParameter<String, Double>>> simpleType) {
        return IResult.ok(simpleType);
    }

    @Getter
    @Setter
    public static class TypeWithParameter<T, R> implements Serializable {

        private T t;

        private R r;

    }

}
