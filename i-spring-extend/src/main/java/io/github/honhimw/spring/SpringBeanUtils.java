package io.github.honhimw.spring;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.util.ClassUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author hon_him
 * @since 2023-05-24
 */
@SuppressWarnings({"unused", "unchecked"})
public class SpringBeanUtils implements ApplicationContextAware {

    private static ApplicationContext _context;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        SpringBeanUtils._context = context;
    }

    @NonNull
    public static ApplicationContext getApplicationContext() {
        if (_context == null) {
            throw new IllegalStateException("Spring context is not available, either this is not a Spring application or the context has not yet been initialized.");
        }
        return _context;
    }

    /**
     * Get bean from applicationContext by beanId
     *
     * @param beanId unique id in context
     * @see ApplicationContext#getBean(String)
     */
    @Nullable
    public static <T> T getBean(String beanId) {
        return (T) Optional.ofNullable(_context)
            .filter(context -> context.containsBean(beanId))
            .map(context -> context.getBean(beanId))
            .orElse(null);
    }

    /**
     * Get bean from applicationContext by bean type
     *
     * @param clazz unique bean of type
     * @param <T>   bean type
     * @return bean
     * @see ApplicationContext#getBean(Class)
     */
    @Nullable
    public static <T> T getBean(Class<T> clazz) {
        return Optional.ofNullable(_context)
            .map(context -> context.getBeanProvider(clazz))
            .map(ObjectProvider::getIfAvailable)
            .orElse(null);
    }

    /**
     * Get bean provider from applicationContext by bean type
     *
     * @param clazz bean type
     * @param <T>   bean type
     * @return bean provider
     */
    @NonNull
    public static <T> ObjectProvider<T> getBeanProvider(Class<T> clazz) {
        if (_context == null) {
            return EmptyObjectProvider.getInstance();
        }
        return _context.getBeanProvider(clazz);
    }

    /**
     * Inject properties into the given object
     *
     * @param bean to be autowired
     * @see AutowireCapableBeanFactory#autowireBean(Object)
     */
    public static void autowireBean(Object bean) {
        Objects.requireNonNull(bean);
        Optional.ofNullable(_context)
            .map(ApplicationContext::getAutowireCapableBeanFactory)
            .ifPresent(factory -> factory.autowireBean(bean));
    }

    public static String getProperty(String key) {
        return getProperty(key, () -> null);
    }

    public static String getProperty(String key, Supplier<String> defaultValue) {
        return Optional.ofNullable(_context)
            .map(EnvironmentCapable::getEnvironment)
            .map(environment -> environment.getProperty(key))
            .orElseGet(defaultValue);
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
