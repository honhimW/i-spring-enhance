package io.github.honhimw.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.EnvironmentCapable;

import jakarta.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

/**
 * @author hon_him
 * @since 2023-05-24
 */
@SuppressWarnings({"unused", "unchecked"})
public class SpringBeanUtils implements ApplicationContextAware {

    /**
     * 当前IOC
     */
    private static ApplicationContext _context;

    /**
     * 设置当前上下文环境，此方法由spring自动装配
     */
    @Override
    public void setApplicationContext(@Nonnull ApplicationContext context) throws BeansException {
        SpringBeanUtils._context = context;
    }

    public static ApplicationContext getApplicationContext() {
        return _context;
    }

    /**
     * Get bean from applicationContext by beanId
     * @see ApplicationContext#getBean(String)
     * @param beanId unique id in context
     */
    public static <T> T getBean(String beanId) {
        return (T) Optional.ofNullable(_context)
            .map(context -> context.getBean(beanId))
            .orElse(null);
    }

    /**
     * Get bean from applicationContext by bean type
     * @see ApplicationContext#getBean(Class)
     * @param clazz unique bean of type
     */
    public static <T> T getBean(Class<T> clazz) {
        return Optional.ofNullable(_context)
            .map(context -> context.getBean(clazz))
            .orElse(null);
    }

    /**
     * Inject properties into the given object
     * @see AutowireCapableBeanFactory#autowireBean(Object)
     * @param bean to be autowired
     */
    public static void autowireBean(Object bean) {
        Objects.requireNonNull(bean);
        Optional.ofNullable(_context)
            .map(ApplicationContext::getAutowireCapableBeanFactory)
            .ifPresent(factory -> factory.autowireBean(bean));
    }

    public static String getProperty(String key) {
        return Optional.ofNullable(_context)
            .map(EnvironmentCapable::getEnvironment)
            .map(environment -> environment.getProperty(key))
            .orElse(null);
    }

}
