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
     * @param objectNode         实体构造前的ObjectNode, 编辑该对象中增删改查节点树即可编辑最终生成的实体类数据
     * @param parameter          Controller入口参数中被注解标记的参数的引用
     * @param httpServletRequest 请求对象, 在调用该方法前输入流已经读取完成, 因此不能再次读取, 但可以获取请求的上下文
     */
    void customize(ObjectNode objectNode, MethodParameter parameter, HttpServletRequest httpServletRequest);

}
