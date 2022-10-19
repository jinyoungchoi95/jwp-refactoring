package kitchenpos.application;

import static kitchenpos.domain.OrderStatus.COOKING;
import static kitchenpos.domain.OrderStatus.MEAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.dao.TableGroupDao;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TableServiceTest {

    @Autowired
    private TableService tableService;

    @Autowired
    private TableGroupDao tableGroupDao;

    @Autowired
    private OrderTableDao orderTableDao;

    @Autowired
    private OrderDao orderDao;

    @Nested
    class create_메소드는 {

        @Nested
        class 생성할_주문테이블을_입력받는_경우 {

            private final OrderTable orderTable = new OrderTable(0, true);

            @Test
            void 주문테이블을_저장하고_반환한다() {
                OrderTable actual = tableService.create(orderTable);

                assertThat(actual).isNotNull();
            }
        }
    }

    @Nested
    class list_메소드는 {

        @Nested
        class 요청이_들어오는_경우 {

            @Test
            void 주문테이블목록을_반환한다() {
                List<OrderTable> actual = tableService.list();

                assertThat(actual).hasSize(8);
            }
        }
    }

    @Nested
    class changeEmpty_메소드는 {

        @Nested
        class 존재하지않는_주문테이블_id가_입력된_경우 {

            private final long NOT_FOUND_ID = 0L;
            private final OrderTable orderTable = new OrderTable(0, true);

            @Test
            void 예외가_발생한다() {
                assertThatThrownBy(() -> tableService.changeEmpty(NOT_FOUND_ID, orderTable))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }

        @Nested
        class 단체지정된_주문테이블이_입력된_경우 {

            private final Long orderTableId = 1L;
            private final OrderTable changeOrderTable = new OrderTable(0, false);

            @BeforeEach
            void setUp() {
                Long tableGroupId = tableGroupDao.save(new TableGroup(LocalDateTime.now(), new ArrayList<>()))
                        .getId();
                orderTableDao.save(new OrderTable(orderTableId, tableGroupId, 0, true));
            }

            @Test
            void 예외가_발생한다() {
                assertThatThrownBy(() -> tableService.changeEmpty(orderTableId, changeOrderTable))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("단체 지정된 테이블 상태를 변화할 수 없습니다.");
            }
        }

        @Nested
        class 주문테이블에_조리상태의_주문이_있는_경우 {

            private final Long orderTableId = 1L;
            private final OrderTable changeOrderTable = new OrderTable(0, false);

            @BeforeEach
            void setUp() {
                orderDao.save(new Order(orderTableId, COOKING.name(), LocalDateTime.now(), new ArrayList<>()));
            }

            @Test
            void 예외가_발생한다() {
                assertThatThrownBy(() -> tableService.changeEmpty(orderTableId, changeOrderTable))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("조리 혹은 식사중인 테이블 상태를 변화할 수 업습니다.");
            }
        }

        @Nested
        class 주문테이블에_식사상태의_주문이_있는_경우 {

            private final Long orderTableId = 1L;
            private final OrderTable changeOrderTable = new OrderTable(0, false);

            @BeforeEach
            void setUp() {
                orderDao.save(new Order(orderTableId, MEAL.name(), LocalDateTime.now(), new ArrayList<>()));
            }

            @Test
            void 예외가_발생한다() {
                assertThatThrownBy(() -> tableService.changeEmpty(orderTableId, changeOrderTable))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("조리 혹은 식사중인 테이블 상태를 변화할 수 업습니다.");
            }
        }

        @Nested
        class 주문테이블_상태를_정상적으로_변환가능한_경우 {

            private final Long orderTableId = 1L;
            private final OrderTable changeOrderTable = new OrderTable(0, false);

            @Test
            void 변경된_주문테이블이_반환된다() {
                OrderTable actual = tableService.changeEmpty(orderTableId, changeOrderTable);

                assertThat(actual.isEmpty()).isFalse();
            }
        }
    }

    @Nested
    class changeNumberOfGuests_메소드는 {

        @Nested
        class 변경할_손님의_수가_0미만인_경우 {

            private final Long orderTableId = 1L;
            private final OrderTable changeOrderTable = new OrderTable(-1, true);

            @Test
            void 예외가_발생한다() {
                assertThatThrownBy(() -> tableService.changeNumberOfGuests(orderTableId, changeOrderTable))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("손님의 수는 0미만이 될 수 없습니다.");
            }
        }

        @Nested
        class 존재하지않는_주문테이블을_입력한_경우 {

            private final Long NOT_FOUND_ORDER_TABLE_ID = 0L;
            private final OrderTable changeOrderTable = new OrderTable(0, true);

            @Test
            void 예외가_발생한다() {
                assertThatThrownBy(() -> tableService.changeNumberOfGuests(NOT_FOUND_ORDER_TABLE_ID, changeOrderTable))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }
}
