package io.github.honhimw.spring;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import jakarta.annotation.Nonnull;

/**
 * 容器启动后按顺序执行多个Bean间的初始化工作, 如数据库内置数据设置是数据间依赖顺序
 * @author hon_him
 * @since 2023-04-21
 */

public interface BuildIn extends Ordered, Comparable<BuildIn> {

    void setup();

    @Override
    default int getOrder() {
        return 0;
    }

    @Override
    default int compareTo(@Nonnull BuildIn o) {
        int thisOrder = this.getOrder();
        int otherOrder = o.getOrder();
        if (this.getClass().isAnnotationPresent(Order.class)) {
            Order order = this.getClass().getAnnotation(Order.class);
            thisOrder = order.value();
        }
        if (o.getClass().isAnnotationPresent(Order.class)) {
            Order order = o.getClass().getAnnotation(Order.class);
            otherOrder = order.value();
        }
        return Integer.compare(thisOrder, otherOrder);
    }
}
