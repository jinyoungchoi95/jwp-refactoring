package kitchenpos.order.dao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import kitchenpos.menu.dao.JdbcTemplateMenuDao;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository implements OrderDao {

    private final JdbcTemplateOrderDao orderDao;
    private final JdbcTemplateOrderLineItemDao orderLineItemDao;
    private final JdbcTemplateMenuDao menuDao;

    public OrderRepository(final JdbcTemplateOrderDao orderDao, final JdbcTemplateOrderLineItemDao orderLineItemDao,
                           final JdbcTemplateMenuDao menuDao) {
        this.orderDao = orderDao;
        this.orderLineItemDao = orderLineItemDao;
        this.menuDao = menuDao;
    }

    @Override
    public Order save(final Order entity) {
        Order order = orderDao.save(entity);
        List<OrderLineItem> orderLineItems = entity.getOrderLineItems()
                .stream()
                .map(orderLineItem -> new OrderLineItem(orderLineItem.getName(), orderLineItem.getPrice(), orderLineItem.getQuantity()))
                .map(orderLineItem -> orderLineItemDao.save(orderLineItem, order.getId()))
                .collect(Collectors.toList());

        return new Order(
                order.getId(),
                order.getOrderTableId(),
                order.getOrderStatus(),
                order.getOrderedTime(),
                orderLineItems
        );
    }

    @Override
    public Optional<Order> findById(final Long id) {
        return orderDao.findById(id)
                .map(order -> new Order(
                        order.getId(),
                        order.getOrderTableId(),
                        order.getOrderStatus(),
                        order.getOrderedTime(),
                        orderLineItemDao.findAllByOrderId(order.getId())));
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = orderDao.findAll();

        return orders.stream()
                .map(order -> new Order(
                        order.getId(),
                        order.getOrderTableId(),
                        order.getOrderStatus(),
                        order.getOrderedTime(),
                        orderLineItemDao.findAllByOrderId(order.getId())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByOrderTableIdAndOrderStatusIn(final Long orderTableId, final List<String> orderStatuses) {
        return orderDao.existsByOrderTableIdAndOrderStatusIn(orderTableId, orderStatuses);
    }

    @Override
    public boolean existsByOrderTableIdInAndOrderStatusIn(final List<Long> orderTableIds,
                                                          final List<String> orderStatuses) {
        return orderDao.existsByOrderTableIdInAndOrderStatusIn(orderTableIds, orderStatuses);
    }
}
