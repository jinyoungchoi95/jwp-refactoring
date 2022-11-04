package kitchenpos.table.application;

import static kitchenpos.order.domain.OrderStatus.COOKING;
import static kitchenpos.order.domain.OrderStatus.MEAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import kitchenpos.SpringServiceTest;
import kitchenpos.order.application.OrderChangeNumberOfGuestsRequest;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.TableGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TableServiceTest {

    @Nested
    class create_메소드는 {

        @Nested
        class 생성할_주문테이블을_입력받는_경우 extends SpringServiceTest {

            private final OrderTableCreateRequest request = new OrderTableCreateRequest(0, true);

            @Test
            void 주문테이블을_저장하고_반환한다() {
                OrderTable actual = tableService.create(request);

                assertThat(actual).isNotNull();
            }
        }
    }

    @Nested
    class list_메소드는 {

        @Nested
        class 요청이_들어오는_경우 extends SpringServiceTest {

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
        class 존재하지않는_주문테이블_id가_입력된_경우 extends SpringServiceTest {

            private final long NOT_FOUND_ID = -1L;
            private final OrderTableEmptyChangeRequest request = new OrderTableEmptyChangeRequest(true);

            @Test
            void 예외가_발생한다() {
                assertThatThrownBy(() -> tableService.changeEmpty(NOT_FOUND_ID, request))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }

        @Nested
        class 단체지정된_주문테이블이_입력된_경우 extends SpringServiceTest {

            private final Long orderTableId = 1L;
            private final OrderTableEmptyChangeRequest request = new OrderTableEmptyChangeRequest(false);

            @BeforeEach
            void setUp() {
                Long tableGroupId = tableGroupDao.save(new TableGroup(LocalDateTime.now(),
                                Arrays.asList(
                                        new OrderTable(0, true),
                                        new OrderTable(0, true)
                                )))
                        .getId();
                orderTableDao.save(new OrderTable(orderTableId, 0, true), tableGroupId);
            }

            @Test
            void 예외가_발생한다() {
                assertThatThrownBy(() -> tableService.changeEmpty(orderTableId, request))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("주문 테이블이 비어있지 않거나 이미 단체지정되어있습니다.");
            }
        }

        @Nested
        class 주문테이블에_조리상태의_주문이_있는_경우 extends SpringServiceTest {

            private final Long orderTableId = 1L;
            private final OrderTableEmptyChangeRequest request = new OrderTableEmptyChangeRequest(false);

            @BeforeEach
            void setUp() {
                orderDao.save(new Order(orderTableId, COOKING.name(), LocalDateTime.now(),
                        Arrays.asList(new OrderLineItem(1L, "후라이드", BigDecimal.ONE, 1))));
            }

            @Test
            void 예외가_발생한다() {
                assertThatThrownBy(() -> tableService.changeEmpty(orderTableId, request))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("조리 혹은 식사중인 테이블 상태를 변화할 수 없습니다.");
            }
        }

        @Nested
        class 주문테이블에_식사상태의_주문이_있는_경우 extends SpringServiceTest {

            private final Long orderTableId = 1L;
            private final OrderTableEmptyChangeRequest request = new OrderTableEmptyChangeRequest(false);

            @BeforeEach
            void setUp() {
                orderDao.save(new Order(orderTableId, MEAL.name(), LocalDateTime.now(),
                        Arrays.asList(new OrderLineItem(1L, "후라이드", BigDecimal.ONE, 1))));
            }

            @Test
            void 예외가_발생한다() {
                assertThatThrownBy(() -> tableService.changeEmpty(orderTableId, request))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("조리 혹은 식사중인 테이블 상태를 변화할 수 없습니다.");
            }
        }

        @Nested
        class 주문테이블_상태를_정상적으로_변환가능한_경우 extends SpringServiceTest {

            private final Long orderTableId = 1L;
            private final OrderTableEmptyChangeRequest request = new OrderTableEmptyChangeRequest(false);

            @Test
            void 변경된_주문테이블이_반환된다() {
                OrderTable actual = tableService.changeEmpty(orderTableId, request);

                assertThat(actual.isEmpty()).isFalse();
            }
        }
    }

    @Nested
    class changeNumberOfGuests_메소드는 {

        @Nested
        class 변경할_손님의_수가_0미만인_경우 extends SpringServiceTest {

            private final Long orderTableId = 1L;
            private final OrderChangeNumberOfGuestsRequest request = new OrderChangeNumberOfGuestsRequest(-1);

            @Test
            void 예외가_발생한다() {
                assertThatThrownBy(() -> tableService.changeNumberOfGuests(orderTableId, request))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("손님의 수는 0미만이 될 수 없습니다.");
            }
        }

        @Nested
        class 존재하지않는_주문테이블을_입력한_경우 extends SpringServiceTest {

            private final Long NOT_FOUND_ORDER_TABLE_ID = 0L;
            private final OrderChangeNumberOfGuestsRequest request = new OrderChangeNumberOfGuestsRequest(2);

            @Test
            void 예외가_발생한다() {
                assertThatThrownBy(() -> tableService.changeNumberOfGuests(NOT_FOUND_ORDER_TABLE_ID, request))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }

        @Nested
        class 주문테이블이_비어있는_경우 extends SpringServiceTest {

            private final Long orderTableId = 1L;
            private final OrderChangeNumberOfGuestsRequest request = new OrderChangeNumberOfGuestsRequest(2);
            @Test
            void 예외가_발생한다() {
                assertThatThrownBy(() -> tableService.changeNumberOfGuests(orderTableId, request))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("빈 테이블은 손님 수를 수정할 수 없습니다.");
            }
        }

        @Nested
        class 정상적으로_손님_수를_변경가능한_경우 extends SpringServiceTest {

            private static final int CHANGE_NUMBER_OF_GUESTS = 2;

            private final Long orderTableId = 1L;
            private final OrderChangeNumberOfGuestsRequest request = new OrderChangeNumberOfGuestsRequest(CHANGE_NUMBER_OF_GUESTS);

            @BeforeEach
            void setUp() {
                orderTableDao.save(new OrderTable(1L, 0, false));
            }

            @Test
            void 손님_수를_변경하고_주문테이블을_반환한다() {
                OrderTable actual = tableService.changeNumberOfGuests(orderTableId, request);
                assertThat(actual.getNumberOfGuests()).isEqualTo(CHANGE_NUMBER_OF_GUESTS);
            }
        }
    }
}
