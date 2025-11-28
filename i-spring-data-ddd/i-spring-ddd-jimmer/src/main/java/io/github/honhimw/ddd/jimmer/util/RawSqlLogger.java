package io.github.honhimw.ddd.jimmer.util;

import org.babyfish.jimmer.sql.runtime.AbstractExecutorProxy;
import org.babyfish.jimmer.sql.runtime.DefaultExecutor;
import org.babyfish.jimmer.sql.runtime.Executor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author hon_him
 * @since 2025-02-27
 */

public class RawSqlLogger extends AbstractExecutorProxy {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger("jimmer.sql.raw");

    private final Logger log;

    public RawSqlLogger() {
        this(DefaultExecutor.INSTANCE, DEFAULT_LOGGER);
    }

    public RawSqlLogger(Executor e) {
        this(e, DEFAULT_LOGGER);
    }

    public RawSqlLogger(Logger log) {
        this(DefaultExecutor.INSTANCE, log);
    }

    public RawSqlLogger(Executor e, Logger log) {
        super(e);
        this.log = log != null ? log : DEFAULT_LOGGER;
    }

    @Override
    public <R> R execute(@NotNull Args<R> args) {
        simpleLog(args);
        return raw.execute(args);
    }

    @Override
    protected AbstractExecutorProxy recreate(Executor raw) {
        return new RawSqlLogger(raw, log);
    }

    @Override
    protected Batch createBatch(BatchContext raw) {
        return new Batch(raw) {};
    }

    protected void simpleLog(Args<?> args) {
        try {
            if (log.isInfoEnabled()) {
                log.info("{} - {}", "%-6s".formatted(args.purpose), args.sql);
            } else if (log.isDebugEnabled()) {
                log.debug("{} - {}; args: {}", "%-6s".formatted(args.purpose), args.sql, args.variables);
            } else if (log.isTraceEnabled()) {
                String sql = args.sql;
                List<Object> variables = args.variables;
                // replace sql '?' into variables
                StringBuilder sb = new StringBuilder();
                int idx = 0;
                for (char c : sql.toCharArray()) {
                    if (c == '?' && idx < variables.size()) {
                        sb.append(variables.get(idx++));
                    } else {
                        sb.append(c);
                    }
                }
                log.trace("{} - {}", "%-6s".formatted(args.purpose), sb);
            }
        } catch (Exception e) {
            log.error("Failed to log sql: {}", e.getMessage());
        }
    }

}
