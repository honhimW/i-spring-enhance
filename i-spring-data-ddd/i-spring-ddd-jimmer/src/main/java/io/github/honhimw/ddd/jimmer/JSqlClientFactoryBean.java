package io.github.honhimw.ddd.jimmer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.honhimw.ddd.jimmer.support.DialectDetector;
import io.github.honhimw.ddd.jimmer.util.RawSqlLogger;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.babyfish.jimmer.sql.DraftInterceptor;
import org.babyfish.jimmer.sql.DraftPreProcessor;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.cache.CacheAbandonedCallback;
import org.babyfish.jimmer.sql.cache.CacheFactory;
import org.babyfish.jimmer.sql.cache.CacheOperator;
import org.babyfish.jimmer.sql.di.AopProxyProvider;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.event.Triggers;
import org.babyfish.jimmer.sql.filter.Filter;
import org.babyfish.jimmer.sql.meta.DatabaseNamingStrategy;
import org.babyfish.jimmer.sql.meta.MetaStringResolver;
import org.babyfish.jimmer.sql.meta.UserIdGenerator;
import org.babyfish.jimmer.sql.runtime.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Collection;

/**
 * @author hon_him
 * @since 2025-03-05
 */

public class JSqlClientFactoryBean implements FactoryBean<JSqlClient>, InitializingBean, ApplicationEventPublisherAware, SmartInitializingSingleton {

    protected final JimmerProperties properties;

    protected final JSqlClient.Builder builder;

    protected final DataSource dataSource;

    protected final AopProxyProvider aopProxyProvider;

    protected final MetaStringResolver metaStringResolver;

    protected final DialectDetector dialectDetector;

    protected final Executor executor;

    protected final SqlFormatter sqlFormatter;

    protected final ObjectMapper objectMapper;

    protected final ConnectionManager connectionManager;

    @Nullable
    protected final EntityManager entityManager;

    @Nullable
    protected final DatabaseNamingStrategy databaseNamingStrategy;

    @Nullable
    protected final Dialect dialect;

    @Nullable
    protected final UserIdGenerator<?> userIdGenerator;

    @Nullable
    protected final CacheFactory cacheFactory;

    @Nullable
    protected final CacheOperator cacheOperator;

    @Nullable
    protected final MicroServiceExchange microServiceExchange;

    protected final Collection<CacheAbandonedCallback> callbacks;

    protected final Collection<ScalarProvider<?, ?>> providers;

    protected final Collection<DraftPreProcessor<?>> processors;

    protected final Collection<DraftInterceptor<?, ?>> interceptors;

    protected final Collection<ExceptionTranslator<?>> exceptionTranslators;

    protected final Collection<Filter<?>> filters;

    protected final Collection<Customizer> customizers;

    protected final Collection<Initializer> initializers;

    private JSqlClient jSqlClient;

    private ApplicationEventPublisher publisher;

    public JSqlClientFactoryBean(
        JimmerProperties properties,
        DataSource dataSource,
        AopProxyProvider aopProxyProvider,
        MetaStringResolver metaStringResolver,
        DialectDetector dialectDetector,
        Executor executor,
        SqlFormatter sqlFormatter,
        ObjectMapper objectMapper,
        ConnectionManager connectionManager,
        @Nullable EntityManager entityManager,
        @Nullable DatabaseNamingStrategy databaseNamingStrategy,
        @Nullable Dialect dialect,
        @Nullable UserIdGenerator<?> userIdGenerator,
        @Nullable CacheFactory cacheFactory,
        @Nullable CacheOperator cacheOperator,
        @Nullable MicroServiceExchange microServiceExchange,
        Collection<CacheAbandonedCallback> callbacks,
        Collection<ScalarProvider<?, ?>> providers,
        Collection<DraftPreProcessor<?>> processors,
        Collection<DraftInterceptor<?, ?>> interceptors,
        Collection<ExceptionTranslator<?>> exceptionTranslators,
        Collection<Filter<?>> filters,
        Collection<Customizer> customizers,
        Collection<Initializer> initializers
    ) {
        this.properties = properties;
        this.dataSource = dataSource;
        this.aopProxyProvider = aopProxyProvider;
        this.metaStringResolver = metaStringResolver;
        this.dialectDetector = dialectDetector;
        this.executor = executor;
        this.sqlFormatter = sqlFormatter;
        this.objectMapper = objectMapper;
        this.connectionManager = connectionManager;
        this.entityManager = entityManager;
        this.databaseNamingStrategy = databaseNamingStrategy;
        this.dialect = dialect;
        this.userIdGenerator = userIdGenerator;
        this.cacheFactory = cacheFactory;
        this.cacheOperator = cacheOperator;
        this.microServiceExchange = microServiceExchange;
        this.callbacks = callbacks;
        this.providers = providers;
        this.processors = processors;
        this.interceptors = interceptors;
        this.exceptionTranslators = exceptionTranslators;
        this.filters = filters;
        this.customizers = customizers;
        this.initializers = initializers;
        this.builder = JSqlClient.newBuilder();
    }

    @Override
    public JSqlClient getObject() throws Exception {
        return jSqlClient;
    }

    @Override
    public Class<?> getObjectType() {
        return jSqlClient != null ? jSqlClient.getClass() : JSqlClient.class;
    }

    @Override
    public void setApplicationEventPublisher(@Nonnull ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        builder
            .setAopProxyProvider(aopProxyProvider)
            .setMetaStringResolver(metaStringResolver)
            .setSqlFormatter(sqlFormatter)
            .setConnectionManager(connectionManager)
            .setEntityManager(entityManager)

            .setDatabaseValidationMode(properties.getDatabaseValidation().getMode())
            .setDefaultSerializedTypeObjectMapper(objectMapper)
            .setCacheFactory(cacheFactory)
            .setCacheOperator(cacheOperator)
            .addCacheAbandonedCallbacks(callbacks)

            .setTriggerType(properties.getTriggerType())
            .setDefaultDissociateActionCheckable(properties.isDefaultDissociationActionCheckable())
            .setIdOnlyTargetCheckingLevel(properties.getIdOnlyTargetCheckingLevel())
            .setDefaultEnumStrategy(properties.getDefaultEnumStrategy())
            .setDefaultBatchSize(properties.getDefaultBatchSize())
            .setDefaultListBatchSize(properties.getDefaultListBatchSize())
            .setInListPaddingEnabled(properties.isInListPaddingEnabled())
            .setExpandedInListPaddingEnabled(properties.isExpandedInListPaddingEnabled())
            .setOffsetOptimizingThreshold(properties.getOffsetOptimizingThreshold())
            .setForeignKeyEnabledByDefault(properties.isForeignKeyEnabledByDefault())
            .setMaxCommandJoinCount(properties.getMaxCommandJoinCount())
            .setTargetTransferable(properties.isTargetTransferable())
            .setExplicitBatchEnabled(properties.isExplicitBatchEnabled())
            .setDumbBatchAcceptable(properties.isDumbBatchAcceptable())
            .setExecutorContextPrefixes(properties.getExecutorContextPrefixes())
        ;

        configureLogging();
        configureDialect();
        configureNamingStrategy();
        configureExtensions();

        if (userIdGenerator != null) {
            builder.setIdGenerator(userIdGenerator);
        }
        if (microServiceExchange != null) {
            builder.setMicroServiceName(properties.getMicroServiceName());
            builder.setMicroServiceExchange(microServiceExchange);
        }
        this.jSqlClient = doBuild(builder);
    }

    protected JSqlClient doBuild(JSqlClient.Builder builder) {
        return builder.build();
    }

    @Override
    public void afterSingletonsInstantiated() {
    }

    protected void configureLogging() {
        JimmerProperties.Log log = properties.getLog();

        if (log.isEnable()) {
            String name = log.getName();
            Logger logger = null;
            if (StringUtils.hasText(name)) {
                logger = LoggerFactory.getLogger(name);
            }
            JimmerProperties.Log.Kind kind = log.getKind();
            builder.setExecutor(switch (kind) {
                case DEFAULT -> Executor.log(executor, logger);
                case SIMPLE -> new RawSqlLogger(executor, logger);
            });
            if (log.isCacheAbandonedEnabled()) {
                builder.addCacheAbandonedCallback(CacheAbandonedCallback.log());
            }
        }
    }

    protected void configureDialect() {
        if (dialect != null) {
            builder.setDialect(dialect);
        } else {
            Dialect dialect;
            Class<? extends Dialect> dialectClass = properties.getDialectClass();
            if (dialectClass != null) {
                try {
                    dialect = dialectClass.getConstructor().newInstance();
                } catch (Exception e) {
                    throw new IllegalArgumentException("unknown dialect: %s".formatted(dialectClass.getName()));
                }
            } else if (properties.getDialect() != null) {
                dialect = DialectDetector.getDialectFromDriver(properties.getDialect());
            } else {
                dialect = connectionManager.execute(dialectDetector::detectDialect);
            }
            builder.setDialect(dialect);
        }
    }

    protected void configureNamingStrategy() {
        if (databaseNamingStrategy != null) {
            builder.setDatabaseNamingStrategy(databaseNamingStrategy);
        } else {
            switch (properties.getDatabaseNamingStrategy()) {
                case LOWER_CASE -> builder.setDatabaseNamingStrategy(DefaultDatabaseNamingStrategy.LOWER_CASE);
                case UPPER_CASE -> builder.setDatabaseNamingStrategy(DefaultDatabaseNamingStrategy.UPPER_CASE);
            }
        }
    }

    protected void configureExtensions() {
        providers.forEach(builder::addScalarProvider);
        builder
            .addDraftPreProcessors(processors)
            .addDraftInterceptors(interceptors)
            .addExceptionTranslators(exceptionTranslators)
            .addFilters(filters)
            .addCustomizers(customizers)
            .addInitializers(initializers)
            .addInitializers(new SpringEventInitializer(publisher))
        ;
    }

    protected static class SpringEventInitializer implements Initializer {

        private final ApplicationEventPublisher publisher;

        private SpringEventInitializer(ApplicationEventPublisher publisher) {
            this.publisher = publisher;
        }

        @Override
        public void initialize(JSqlClient sqlClient) throws Exception {
            Triggers[] triggersArr = switch (((JSqlClientImplementor) sqlClient).getTriggerType()) {
                case TRANSACTION_ONLY -> new Triggers[]{sqlClient.getTriggers(true)};
                case BINLOG_ONLY -> new Triggers[]{sqlClient.getTriggers()};
                case BOTH -> new Triggers[]{sqlClient.getTriggers(), sqlClient.getTriggers(true)};
            };
            for (Triggers triggers : triggersArr) {
                triggers.addEntityListener(publisher::publishEvent);
                triggers.addAssociationListener(publisher::publishEvent);
            }
        }
    }

}
