package io.github.honhimw.spring.web.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hon_him
 * @since 2023-05-10
 */

public abstract class AbstractFallbackHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private boolean printStacktrace = true;

    protected void log(Throwable throwable) {
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

}
