package io.github.honhimw.spring.web.common;

import com.fasterxml.jackson.core.JsonParser;
import io.github.honhimw.spring.web.common.wrapper.*;
import feign.Feign;
import jakarta.persistence.Entity;
import jakarta.servlet.ServletException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author hon_him
 * @since 2024-12-11
 */

@Configuration(proxyBeanMethods = false)
public class ExceptionWrapperConfiguration {

    @Bean
    @ConditionalOnClass(DataIntegrityViolationException.class)
    DataIntegrityExceptionWrapper dataIntegrityExceptionWrapper() {
        return new DataIntegrityExceptionWrapper();
    }

    @Bean
    ErrorExceptionWrapper errorExceptionWrapper() {
        return new ErrorExceptionWrapper();
    }

    @Bean
    @ConditionalOnClass(Feign.class)
    FeignExceptionWrapper feignExceptionWrapper() {
        return new FeignExceptionWrapper();
    }

    @Bean
    @ConditionalOnClass(Entity.class)
    JpaExceptionWrapper jpaExceptionWrapper() {
        return new JpaExceptionWrapper();
    }

    @Bean
    @ConditionalOnClass(JsonParser.class)
    JsonParserExceptionWrapper jsonParserExceptionWrapper() {
        return new JsonParserExceptionWrapper();
    }

    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnClass(MethodArgumentNotValidException.class)
    MethodArgumentNotValidExceptionWrapper methodArgumentNotValidExceptionWrapper() {
        return new MethodArgumentNotValidExceptionWrapper();
    }

    @Bean
    NPExceptionWrapper npExceptionWrapper() {
        return new NPExceptionWrapper();
    }

    @Bean
    PanicExceptionWrapper panicExceptionWrapper() {
        return new PanicExceptionWrapper();
    }

    @Bean
    @ConditionalOnClass(ResponseStatusException.class)
    ResponseStatusExceptionWrapper responseStatusExceptionWrapper() {
        return new ResponseStatusExceptionWrapper();
    }

    @Bean
    @ConditionalOnClass(ServletException.class)
    ServletExceptionWrapper servletExceptionWrapper() {
        return new ServletExceptionWrapper();
    }

    @Bean
    @ConditionalOnClass(DataAccessException.class)
    SpringDataExceptionWrapper springDataExceptionWrapper() {
        return new SpringDataExceptionWrapper();
    }

    @Bean
    SpringJsonParserExceptionWrapper springJsonParserExceptionWrapper() {
        return new SpringJsonParserExceptionWrapper();
    }

    @Bean
    @ConditionalOnClass(jakarta.validation.Validation.class)
    ValidationExceptionWrapper validationExceptionWrapper() {
        return new ValidationExceptionWrapper();
    }

    @Bean
    WrappedExceptionWrapper wrappedExceptionWrapper() {
        return new WrappedExceptionWrapper();
    }

}
