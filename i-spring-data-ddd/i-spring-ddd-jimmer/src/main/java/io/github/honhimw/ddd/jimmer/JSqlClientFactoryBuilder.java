package io.github.honhimw.ddd.jimmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.ddd.jimmer.support.DialectDetector;
import io.github.honhimw.ddd.jimmer.support.SpringConnectionManager;
import io.github.honhimw.ddd.jimmer.support.SpringMetaStringResolver;
import org.babyfish.jimmer.sql.DraftInterceptor;
import org.babyfish.jimmer.sql.DraftPreProcessor;
import org.babyfish.jimmer.sql.cache.CacheAbandonedCallback;
import org.babyfish.jimmer.sql.cache.CacheFactory;
import org.babyfish.jimmer.sql.cache.CacheOperator;
import org.babyfish.jimmer.sql.di.AopProxyProvider;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.filter.Filter;
import org.babyfish.jimmer.sql.meta.DatabaseNamingStrategy;
import org.babyfish.jimmer.sql.meta.MetaStringResolver;
import org.babyfish.jimmer.sql.meta.UserIdGenerator;
import org.babyfish.jimmer.sql.runtime.*;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Objects;

/**
 * @author hon_him
 * @since 2025-03-06
 */

public class JSqlClientFactoryBuilder {

    private final JimmerProperties properties;

    private DataSource dataSource;

    private UserIdGenerator<?> userIdGenerator;

    private AopProxyProvider aopProxyProvider;

    private MetaStringResolver metaStringResolver;

    private Dialect dialect;

    private DialectDetector dialectDetector;

    private Executor executor;

    private ConnectionManager connectionManager;

    private SqlFormatter sqlFormatter;

    private ObjectMapper objectMapper;

    private EntityManager entityManager;

    private DatabaseNamingStrategy databaseNamingStrategy;

    private CacheFactory cacheFactory;

    private CacheOperator cacheOperator;

    private MicroServiceExchange microServiceExchange;

    private Collection<CacheAbandonedCallback> callbacks;

    private Collection<ScalarProvider<?, ?>> providers;

    private Collection<DraftPreProcessor<?>> processors;

    private Collection<DraftInterceptor<?, ?>> interceptors;

    private Collection<ExceptionTranslator<?>> exceptionTranslators;

    private Collection<Filter<?>> filters;

    private Collection<Customizer> customizers;

    private Collection<Initializer> initializers;

    public JSqlClientFactoryBuilder(JimmerProperties properties) {
        this.properties = properties;
    }

    public JSqlClientFactoryBuilder dataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder userIdGeneratorProvider(ObjectProvider<UserIdGenerator<?>> provider) {
        this.userIdGenerator = provider.getIfAvailable();
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder aopProxyProvider(ObjectProvider<AopProxyProvider> provider) {
        this.aopProxyProvider = provider.getIfAvailable(() -> AopUtils::getTargetClass);
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder metaStringResolver(ObjectProvider<MetaStringResolver> provider, ConfigurableBeanFactory beanFactory) {
        this.metaStringResolver = provider.getIfAvailable(() -> new SpringMetaStringResolver(new EmbeddedValueResolver(beanFactory)));
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder dialectDetector(ObjectProvider<DialectDetector> provider) {
        this.dialectDetector = provider.getIfAvailable(() -> DialectDetector.INSTANCE);
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder executor(ObjectProvider<Executor> provider) {
        this.executor = provider.getIfAvailable(() -> DefaultExecutor.INSTANCE);
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder connectionManager(ObjectProvider<ConnectionManager> provider) {
        this.connectionManager = provider.getIfAvailable();
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder dialect(ObjectProvider<Dialect> provider) {
        this.dialect = provider.getIfAvailable();
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder sqlFormatter(ObjectProvider<SqlFormatter> provider) {
        this.sqlFormatter = provider.getIfAvailable(() -> {
            if (properties.getLog().isPretty()) {
                if (properties.getLog().isInlineSqlVariables()) {
                    return SqlFormatter.INLINE_PRETTY;
                } else {
                    return SqlFormatter.PRETTY;
                }
            } else {
                return SqlFormatter.SIMPLE;
            }
        });
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder objectMapper(ObjectProvider<ObjectMapper> provider) {
        this.objectMapper = provider.getIfAvailable(ObjectMapper::new);
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder entityManager(ObjectProvider<EntityManager> provider) {
        this.entityManager = provider.getIfAvailable();
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder databaseNamingStrategy(ObjectProvider<DatabaseNamingStrategy> provider) {
        this.databaseNamingStrategy = provider.getIfAvailable();
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder cacheFactory(ObjectProvider<CacheFactory> provider) {
        this.cacheFactory = provider.getIfAvailable();
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder cacheOperator(ObjectProvider<CacheOperator> provider) {
        this.cacheOperator = provider.getIfAvailable();
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder microServiceExchange(ObjectProvider<MicroServiceExchange> provider) {
        this.microServiceExchange = provider.getIfAvailable();
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder callbacks(ObjectProvider<CacheAbandonedCallback> provider) {
        this.callbacks = provider.orderedStream().toList();
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder providers(ObjectProvider<ScalarProvider<?,?>> provider) {
        this.providers = provider.orderedStream().toList();
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder processors(ObjectProvider<DraftPreProcessor<?>> provider) {
        this.processors = provider.orderedStream().toList();
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder interceptors(ObjectProvider<DraftInterceptor<?,?>> provider) {
        this.interceptors = provider.orderedStream().toList();
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder exceptionTranslators(ObjectProvider<ExceptionTranslator<?>> exceptionTranslators) {
        this.exceptionTranslators = exceptionTranslators.orderedStream().toList();
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder filters(ObjectProvider<Filter<?>> provider) {
        this.filters = provider.orderedStream().toList();
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder customizers(ObjectProvider<Customizer> provider) {
        this.customizers = provider.orderedStream().toList();
        return this;
    }

    @Autowired
    public JSqlClientFactoryBuilder initializers(ObjectProvider<Initializer> provider) {
        this.initializers = provider.orderedStream().toList();
        return this;
    }
    
    public JSqlClientFactoryBean build() {
        return new JSqlClientFactoryBean(
            properties,
            dataSource,
            aopProxyProvider,
            metaStringResolver,
            dialectDetector,
            executor,
            sqlFormatter,
            objectMapper,
            Objects.requireNonNullElseGet(connectionManager, () -> new SpringConnectionManager(dataSource)),
            entityManager,
            databaseNamingStrategy,
            dialect,
            userIdGenerator,
            cacheFactory,
            cacheOperator,
            microServiceExchange,
            callbacks,
            providers,
            processors,
            interceptors,
            exceptionTranslators,
            filters,
            customizers,
            initializers
        ) {};
    }
    
}
