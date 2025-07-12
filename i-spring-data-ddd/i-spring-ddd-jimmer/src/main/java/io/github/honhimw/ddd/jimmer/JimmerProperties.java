package io.github.honhimw.ddd.jimmer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.babyfish.jimmer.sql.EnumType;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.event.TriggerType;
import org.babyfish.jimmer.sql.runtime.DatabaseValidationMode;
import org.babyfish.jimmer.sql.runtime.IdOnlyTargetCheckingLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DatabaseDriver;

import java.time.Duration;
import java.util.Collection;

/**
 * @author hon_him
 * @since 2025-01-13
 */

@Getter
@Setter
@ConfigurationProperties(prefix = JimmerProperties.PREFIX)
public class JimmerProperties {

    public final static String PREFIX = "spring.jimmer";

    /**
     * Database dialect
     */
    private Class<? extends Dialect> dialectClass = null;

    /**
     * Postgre, Oracle, MySql, SqlServer, H2, SQLite
     */
    private DatabaseDriver dialect = null;

    private Log log = new Log();

    private boolean jacksonModuleEnabled = false;

    private DatabaseValidation databaseValidation = new DatabaseValidation();

    private TriggerType triggerType = TriggerType.TRANSACTION_ONLY;

    private boolean defaultDissociationActionCheckable = true;

    private DatabaseNamingStrategy databaseNamingStrategy = DatabaseNamingStrategy.LOWER_CASE;

    private IdOnlyTargetCheckingLevel idOnlyTargetCheckingLevel = IdOnlyTargetCheckingLevel.NONE;

    private Duration transactionCacheOperatorFixedDelay = Duration.ofSeconds(5);

    private EnumType.Strategy defaultEnumStrategy = EnumType.Strategy.NAME;

    private int defaultBatchSize = JSqlClient.Builder.DEFAULT_BATCH_SIZE;

    private int defaultListBatchSize = JSqlClient.Builder.DEFAULT_LIST_BATCH_SIZE;

    private boolean inListPaddingEnabled = false;

    private boolean expandedInListPaddingEnabled = false;

    private int offsetOptimizingThreshold = Integer.MAX_VALUE;

    private boolean isForeignKeyEnabledByDefault = false;

    private int maxCommandJoinCount = 2;

    private boolean targetTransferable = false;

    private boolean explicitBatchEnabled = false;

    private boolean dumbBatchAcceptable = false;

    private Collection<String> executorContextPrefixes = null;

    private String microServiceName = "";

    private ErrorTranslator errorTranslator = new ErrorTranslator();

    private DDLAuto ddlAuto = DDLAuto.NONE;

    @Getter
    @Setter
    @ToString
    public static class DatabaseValidation {

        private DatabaseValidationMode mode = DatabaseValidationMode.NONE;

    }

    @Getter
    @Setter
    @ToString
    public static class ErrorTranslator {

        private boolean enabled = false;

        private int httpStatus = 500;

        private boolean debugInfoSupported = false;

        private int debugInfoMaxStackTraceCount = Integer.MAX_VALUE;

    }

    @Getter
    @Setter
    public static class Log {
        private boolean enable = false;
        private boolean pretty = false;
        private String name = null;
        private Kind kind = Kind.DEFAULT;
        private boolean cacheAbandonedEnabled = false;
        private boolean inlineSqlVariables = false;

        public enum Kind {
            /**
             * Jimmer provided
             */
            DEFAULT,
            /**
             * Simple implementation
             */
            SIMPLE,
        }
    }

    public enum DatabaseNamingStrategy {
        /**
         * Lower case with underscore
         */
        LOWER_CASE,
        /**
         * Upper case with underscore
         */
        UPPER_CASE,
    }

    public enum DDLAuto {
        /**
         * Create the schema.
         */
        CREATE,
        /**
         * Create and then destroy the schema at the end of the session.
         */
        CREATE_DROP,
        /**
         * Drop the schema at the end of the session.
         */
        DROP,
        /**
         * Disable DDL handling.
         */
        NONE,
    }
    
}
