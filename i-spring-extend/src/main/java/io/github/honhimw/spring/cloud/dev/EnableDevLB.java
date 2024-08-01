package io.github.honhimw.spring.cloud.dev;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author hon_him
 * @since 2022-07-07
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(DevLBSelector.class)
public @interface EnableDevLB {

    String activeProfile();

    /**
     * 测试环境IP, 开发环境下如果本地未启动且没有可以访问的服务, 会尝试调用该IP下的接口
     * 格式：demo-provider@10.37.1.132:8090
     */
    String[] testIP() default {};

}
