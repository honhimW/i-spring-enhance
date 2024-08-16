package io.github.honhimw.spring;

import jakarta.annotation.Nonnull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.util.ClassUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * @author hon_him
 * @since 2023-05-24
 */
@SuppressWarnings({"unused", "unchecked"})
public class SpringBeanUtils implements ApplicationContextAware {

    private static ApplicationContext _context;

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

    public static boolean isWeb() {
        return isWebMvc() || isWebFlux();
    }

    public static boolean isWebMvc() {
        return matchContextType("org.springframework.web.context.WebApplicationContext");
    }

    public static boolean isWebFlux() {
        return matchContextType("org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext");
    }

    private static boolean matchContextType(String className) {
        boolean present = ClassUtils.isPresent(className, ClassUtils.getDefaultClassLoader());
        if (present) {
            try {
                Class<?> type = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
                return type.isAssignableFrom(_context.getClass());
            } catch (ClassNotFoundException ignored) {
            }
        }

        return false;
    }

}
