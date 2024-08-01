package org.example.web;

import io.github.honhimw.spring.Result;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

/**
 * @author hon_him
 * @since 2023-05-10
 */

@RestController
@RequestMapping("/simple")
public class SimpleController {

    @Autowired
    private MessageSource messageSource;

    @RequestMapping("/hello")
    public Result<String> hello() {
        return Result.okWithMsg("{hello}");
    }

    @RequestMapping("/throw")
    public Result<Void> error() {
        throw new RuntimeException("runtime ex.");
    }


    @Data
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entity implements Serializable {
        @NotBlank
        private String id;
    }


}
