package com.group04;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Test Suite")
class OrderServiceTest {

    @Mock
    private IGenericRepository<Order, Integer> orderRepository;

    @Mock
    private IGenericRepository<Drink, String> drinkRepository;

    @Mock
    private OrderQueue orderQueue;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                drinkRepository,
                orderQueue
        );
    }

    @Test
    @DisplayName("createOrder: tao don hang thanh cong")
    void createOrder_success() {
        when(orderRepository.add(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order created = orderService.createOrder("An");

        assertNotNull(created);
        assertEquals("An", created.getCustomerName());
        assertEquals(OrderStatus.PENDING, created.getStatus());

        ArgumentCaptor<Order> captor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository, times(1)).add(captor.capture());
        assertEquals("An", captor.getValue().getCustomerName());
    }

    @Test
    @DisplayName("addDrinkToOrder: khong tim thay don hang")
    void addDrinkToOrder_orderNotFound_throwsException() {
        when(orderRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> orderService.addDrinkToOrder(99, "C01", 1)
        );

        verifyNoInteractions(drinkRepository);
    }

    @Test
    @DisplayName("addDrinkToOrder: khong tim thay do uong")
    void addDrinkToOrder_drinkNotFound_throwsException() {
        Order order = new Order("An");

        when(orderRepository.findById(order.getOrderId()))
                .thenReturn(Optional.of(order));

        when(drinkRepository.findById("C99"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> orderService.addDrinkToOrder(
                        order.getOrderId(), "C99", 1
                )
        );
    }

    @Test
    @DisplayName("addDrinkToOrder: them do uong thanh cong")
    void addDrinkToOrder_success() {
        Order order = new Order("An");
        Drink drink = new Coffee(
                "C01", "Ca phe sua da", 25000, true, 1
        );

        when(orderRepository.findById(order.getOrderId()))
                .thenReturn(Optional.of(order));

        when(drinkRepository.findById("C01"))
                .thenReturn(Optional.of(drink));

        orderService.addDrinkToOrder(
                order.getOrderId(), "C01", 2
        );

        assertEquals(1, order.getItems().size());
        assertEquals(66000.0, order.getTotal(), 0.001);
    }

    @Test
    @DisplayName("addDrinkToOrder: so luong bang 0")
    void addDrinkToOrder_zeroQuantity_throwsException() {
        Order order = new Order("An");
        Drink drink = new Coffee(
                "C01", "Ca phe sua da", 25000, true, 1
        );

        when(orderRepository.findById(order.getOrderId()))
                .thenReturn(Optional.of(order));

        when(drinkRepository.findById("C01"))
                .thenReturn(Optional.of(drink));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.addDrinkToOrder(
                        order.getOrderId(), "C01", 0
                )
        );
    }

    @Test
    @DisplayName("submitOrder: don hang rong")
    void submitOrder_emptyOrder_throwsException() {
        Order order = new Order("An");

        when(orderRepository.findById(order.getOrderId()))
                .thenReturn(Optional.of(order));

        assertThrows(
                ValidationException.class,
                () -> orderService.submitOrder(order.getOrderId())
        );

        verify(orderQueue, never()).placeOrder(any());
    }

    @Test
    @DisplayName("submitOrder: gui don hang thanh cong")
    void submitOrder_success() {
        Order order = new Order("An");
        order.addItem(
                new Coffee("C01", "Ca phe sua da", 25000, true, 1),
                2
        );

        when(orderRepository.findById(order.getOrderId()))
                .thenReturn(Optional.of(order));

        orderService.submitOrder(order.getOrderId());

        verify(orderQueue, times(1)).placeOrder(order);
    }

    @Test
    @DisplayName("submitOrder: khong tim thay don hang")
    void submitOrder_orderNotFound_throwsException() {
        when(orderRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> orderService.submitOrder(99)
        );

        verify(orderQueue, never()).placeOrder(any());
    }

    @Test
    @DisplayName("getOrderTotal: tinh tong tien")
    void getOrderTotal_success() {
        Order order = new Order("An");
        order.addItem(
                new Coffee("C01", "Ca phe sua da", 25000, true, 1),
                2
        );

        when(orderRepository.findById(order.getOrderId()))
                .thenReturn(Optional.of(order));

        double total = orderService.getOrderTotal(order.getOrderId());

        assertEquals(66000.0, total, 0.001);
    }

    @Test
    @DisplayName("getOrderTotal: khong tim thay don hang")
    void getOrderTotal_notFound_throwsException() {
        when(orderRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> orderService.getOrderTotal(99)
        );
    }

    @Test
    @DisplayName("getOrderStatus: lay trang thai don hang")
    void getOrderStatus_success() {
        Order order = new Order("An");

        when(orderRepository.findById(order.getOrderId()))
                .thenReturn(Optional.of(order));

        assertEquals(
                OrderStatus.PENDING,
                orderService.getOrderStatus(order.getOrderId())
        );
    }

    @Test
    @DisplayName("getOrderStatus: khong tim thay don hang")
    void getOrderStatus_notFound_throwsException() {
        when(orderRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> orderService.getOrderStatus(99)
        );
    }

    @Test
    @DisplayName("getOrder: lay don hang thanh cong")
    void getOrder_success() {
        Order order = new Order("An");

        when(orderRepository.findById(order.getOrderId()))
                .thenReturn(Optional.of(order));

        Order result = orderService.getOrder(order.getOrderId());

        assertNotNull(result);
        assertEquals(order, result);

        verify(orderRepository, times(1))
                .findById(order.getOrderId());
    }

    @Test
    @DisplayName("getOrder: khong tim thay don hang")
    void getOrder_notFound_throwsException() {
        when(orderRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> orderService.getOrder(99)
        );
    }
}
