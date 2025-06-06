package io.github.honhimw.ddd.jimmer.support;

import org.babyfish.jimmer.meta.TypedProp;
import org.babyfish.jimmer.sql.ast.Expression;
import org.babyfish.jimmer.sql.ast.query.Order;
import org.babyfish.jimmer.sql.ast.table.Props;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hon_him
 * @since 2025-02-26
 */

public class SpringOrders {

    private static final TypedProp.Scalar<?, ?>[] EMPTY_PROPS = new TypedProp.Scalar<?, ?>[0];

    private static final Order[] EMPTY_ORDERS = new Order[0];

    private SpringOrders() {}

    public static Order[] toOrders(Props table, Sort sort) {
        if (sort == null || sort.isEmpty()) {
            return EMPTY_ORDERS;
        }
        List<Order> astOrders = new ArrayList<>();
        for (Sort.Order order : sort) {
            Expression<?> expr = Order.orderedExpression(table, order.getProperty());
            Order astOrder = order.isDescending() ? expr.desc() : expr.asc();
            astOrder = switch (order.getNullHandling()) {
                case NULLS_FIRST -> astOrder.nullsFirst();
                case NULLS_LAST -> astOrder.nullsLast();
                default -> astOrder;
            };
            astOrders.add(astOrder);
        }
        return astOrders.toArray(EMPTY_ORDERS);
    }
}
