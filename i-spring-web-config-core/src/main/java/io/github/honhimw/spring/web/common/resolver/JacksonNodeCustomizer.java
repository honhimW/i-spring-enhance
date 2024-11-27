package io.github.honhimw.spring.web.common.resolver;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;


/**
 * @author hon_him
 * @since 2022-08-22
 */
@FunctionalInterface
public interface JacksonNodeCustomizer {

    /**
     * @param objectNode         data container
     * @param parameter          parameter in endpoint
     * @param httpServletRequest request
     */
    void customize(ObjectNode objectNode, MethodParameter parameter, HttpServletRequest httpServletRequest);

}
