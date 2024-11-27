package io.github.honhimw.util.tool;

import ch.qos.logback.classic.net.SocketAppender;
import io.github.honhimw.util.IpUtils;
import lombok.Getter;
import lombok.Setter;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author hon_him
 * @since 2022-09-20
 */
@Setter
@Getter
@SuppressWarnings("unused")
public class LogbackIpPidSocketAppender extends SocketAppender {

    private static final AtomicBoolean SHIFT = new AtomicBoolean(false);

    private String contextName = "default";

    public LogbackIpPidSocketAppender() {
        super();
    }

    @Override
    public void start() {
        synchronized (SHIFT) {
            if (!SHIFT.get()) {
                try {
                    String ctxName = IpUtils.localIPv4() + "#" + getProcessID();
                    if (contextName != null) {
                        ctxName = contextName + "@" + ctxName;
                    }
                    this.getContext().setName(ctxName);
                } catch (Exception ignored) {
                }
                SHIFT.compareAndSet(false, true);
            }
        }
        super.start();
    }

    public static int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Integer.parseInt(runtimeMXBean.getName().split("@")[0]);
    }

}
