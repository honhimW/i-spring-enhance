package io.github.honhimw.spring.web.util;

import io.github.honhimw.spring.SpringBeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.env.Environment;

import java.util.Collections;
import java.util.Map;

/**
 * @author hon_him
 * @since 2023-11-17
 */

@SuppressWarnings("unused")
public class LoggingSystemUtils {

    public static LoggingSystem loggingSystem() {
        return LoggingSystem.get(LoggingSystem.class.getClassLoader());
    }

    public static void setLevel(String logger, LogLevel level) {
        LoggingSystem loggingSystem = loggingSystem();
        if (StringUtils.equalsIgnoreCase(logger, LoggingSystem.ROOT_LOGGER_NAME)) {
            logger = null;
        }
        loggingSystem.setLogLevel(logger, level);
    }

    public static void setLevel(String logger, String level) {
        setLevel(logger, resolveLevel(level));
    }

    public static void setLevel(String logger, Level level) {
        setLevel(logger, level.toString());
    }

    public static Level getLevel(String logger) {
        Logger _logger = LoggerFactory.getLogger(logger);
        if (_logger.isTraceEnabled()) {
            return Level.TRACE;
        } else if (_logger.isDebugEnabled()) {
            return Level.DEBUG;
        } else if (_logger.isInfoEnabled()) {
            return Level.INFO;
        } else if (_logger.isWarnEnabled()) {
            return Level.WARN;
        } else if (_logger.isErrorEnabled()) {
            return Level.ERROR;
        } else {
            return null;
        }
    }

    public static LogLevel getLogLevel(String logger) {
        Logger _logger = LoggerFactory.getLogger(logger);
        if (_logger.isTraceEnabled()) {
            return LogLevel.TRACE;
        } else if (_logger.isDebugEnabled()) {
            return LogLevel.DEBUG;
        } else if (_logger.isInfoEnabled()) {
            return LogLevel.INFO;
        } else if (_logger.isWarnEnabled()) {
            return LogLevel.WARN;
        } else if (_logger.isErrorEnabled()) {
            return LogLevel.ERROR;
        } else {
            return LogLevel.OFF;
        }
    }

    public static void reset() {
        Environment environment = SpringBeanUtils.getApplicationContext().getEnvironment();
        Bindable<Map<String, String>> STRING_STRING_MAP = Bindable.mapOf(String.class, String.class);
        Map<String, String> levels = Binder.get(environment).bind("logging.level", STRING_STRING_MAP)
            .orElseGet(Collections::emptyMap);
        for (Map.Entry<String, String> entry : levels.entrySet()) {
            String level = environment.resolvePlaceholders(entry.getValue());
            setLevel(entry.getKey(), level);
        }
    }

    public static void resetLevel(String logger) {
        Environment environment = SpringBeanUtils.getApplicationContext().getEnvironment();
        String level = environment.getProperty("logging.level." + logger);
        if (StringUtils.isNotBlank(level)) {
            setLevel(logger, level);
        }
    }

    public static LogLevel resolveLevel(String level) {
        return LogLevel.valueOf(StringUtils.toRootUpperCase(level));
    }

}
