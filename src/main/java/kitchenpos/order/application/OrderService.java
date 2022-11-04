package kitchenpos.order.application;

import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.menu.dao.MenuDao;
import kitchenpos.menu.domain.Menu;
import kitchenpos.order.application.OrderCreateRequest.OrderLineItemCreateRequest;
import kitchenpos.order.dao.OrderDao;
import kitchenpos.order.dao.OrderLineItemDao;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.table.dao.OrderTableDao;
import kitchenpos.table.domain.OrderTable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final MenuDao menuDao;
    private final OrderDao orderDao;
    private final OrderLineItemDao orderLineItemDao;
    private final OrderTableDao orderTableDao;

    public OrderService(
            final MenuDao menuDao,
            final OrderDao orderDao,
            final OrderLineItemDao orderLineItemDao,
            final OrderTableDao orderTableDao
    ) {
        this.menuDao = menuDao;
        this.orderDao = orderDao;
        this.orderLineItemDao = orderLineItemDao;
        this.orderTableDao = orderTableDao;
    }

    @Transactional
    public Order create(final OrderCreateRequest request) {
        Order order = new Order(
                parseOrderTableId(request.getOrderTableId()),
                request.getOrderLineItems()
                        .stream()
                        .map(this::mapToOrderLineItem)
                        .collect(Collectors.toList()));
        order.validateMenuSize(menuDao.countByIdIn(request.getOrderLineItems()
                .stream()
                .map(OrderLineItemCreateRequest::getMenuId)
                .collect(Collectors.toList())));
        return orderDao.save(order);
    }

    private Long parseOrderTableId(final Long orderTableId) {
        final OrderTable orderTable = orderTableDao.findById(orderTableId)
                .orElseThrow(IllegalArgumentException::new);
        if (orderTable.isEmpty()) {
            throw new IllegalArgumentException("주문테이블이 비어있습니다.");
        }
        return orderTable.getId();
    }

    public List<Order> list() {
        return orderDao.findAll();
    }

    @Transactional
    public Order changeOrderStatus(final Long orderId, final OrderStatusChangeRequest request) {
        final Order savedOrder = orderDao.findById(orderId)
                .orElseThrow(IllegalArgumentException::new);

        savedOrder.changeOrderStatus(OrderStatus.valueOf(request.getOrderStatus()));
        return orderDao.save(savedOrder);
    }

    private OrderLineItem mapToOrderLineItem(final OrderLineItemCreateRequest request) {
        Menu menu = menuDao.findById(request.getMenuId())
                .orElseThrow(() -> new IllegalArgumentException("실제 메뉴가 아닙니다."));
        return new OrderLineItem(menu.getName(), menu.getPrice(), request.getQuantity());
    }
}
