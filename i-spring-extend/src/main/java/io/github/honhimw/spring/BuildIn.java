package io.github.honhimw.spring;

import jakarta.annotation.Nonnull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Do the initialization after container startup
 * 
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
