package io.github.honhimw.spring.web.common;

import io.github.honhimw.util.tool.ErrorStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hon_him
 * @since 2023-05-10
 */

@SuppressWarnings("all")
public abstract class AbstractFallbackHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final ExceptionWrappers exceptionWrappers;

    protected final ExceptionWrapper.MessageFormatter messageFormatter;

    public AbstractFallbackHandler(ExceptionWrappers exceptionWrappers, ExceptionWrapper.MessageFormatter messageFormatter) {
        this.exceptionWrappers = exceptionWrappers;
        this.messageFormatter = messageFormatter;
    }

    private boolean printStacktrace = true;

    protected int maxStackTraceDepth = 20;

    protected void log(Throwable throwable) {
        ErrorStack errorStack = new ErrorStack(throwable, this.maxStackTraceDepth);
        log.warn("Debug Stack: {}", errorStack);
        if (printStacktrace()) {
            log.warn(throwable.toString(), throwable);
        } else {
            log.warn(throwable.toString());
        }
    }

    protected boolean printStacktrace() {
        return printStacktrace;
    }

    public void enablePrintStacktrace() {
        this.printStacktrace = true;
    }

    public void disablePrintStacktrace() {
        this.printStacktrace = false;
    }

    public void setPrintStacktrace(boolean print) {
        this.printStacktrace = print;
    }

    protected Object handle(ExceptionWrapper wrapper, Throwable e, int status) {
        if (wrapper instanceof ExceptionWrapper.MessageExceptionWrapper messageExceptionWrapper) {
            return messageFormatter.format(status, messageExceptionWrapper.wrap(e));
        }
        return wrapper.wrap(e);
    }

}
