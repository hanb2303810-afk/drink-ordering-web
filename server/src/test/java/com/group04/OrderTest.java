package com.group04;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Test Suite")
class OrderTest {

    private Order order;
    private Coffee latte;   // gia 25000 + 1 shot * 8000 = 33000
    private MilkTea tea;    // gia 30000 + 2 topping * 5000 = 40000

    @BeforeEach
    void setUp() {
        // ARRANGE dung chung
        order = new Order("An");
        latte = new Coffee("C01", "Ca phe sua da", 25000, true, 1);
        tea = new MilkTea("T01", "Tra sua tran chau", 30000, 70, 2);
    }

    // ---------------- Khoi tao ----------------

    @Test
    @DisplayName("Don moi tao: PENDING, chua co mon, tong tien = 0")
    void constructor_newOrder_hasPendingStatusAndNoItems() {
        assertEquals("An", order.getCustomerName());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertTrue(order.getItems().isEmpty());
        assertEquals(0, order.getTotal(), 0.001);
        assertNotNull(order.getCreatedAt());
    }

    // ---------------- Them mon va tinh tien ----------------

    @Test
    @DisplayName("Tong tien = tong thanh tien cua cac dong")
    void addItem_multipleItems_totalIsSumOfSubtotals() {
        // ACT
        order.addItem(latte, 2);   // 2 * 33000 = 66000
        order.addItem(tea, 1);     // 1 * 40000 = 40000

        // ASSERT
        assertEquals(2, order.getItems().size());
        assertEquals(106000, order.getTotal(), 0.001);
    }

    @Test
    @DisplayName("So luong khong hop le -> IllegalArgumentException, don khong doi")
    void addItem_invalidQuantity_throwsExceptionAndKeepsOrderUnchanged() {
        assertThrows(IllegalArgumentException.class, () -> order.addItem(latte, 0));
        assertThrows(IllegalArgumentException.class, () -> order.addItem(latte, -1));

        assertTrue(order.getItems().isEmpty());
    }

    @Test
    @DisplayName("Do uong null -> IllegalArgumentException")
    void addItem_nullDrink_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> order.addItem(null, 1));
    }

    @Test
    @DisplayName("getItems() tra ve danh sach khong sua duoc")
    void getItems_returnsUnmodifiableList() {
        order.addItem(latte, 1);

        assertThrows(UnsupportedOperationException.class, () -> order.getItems().add(null));
        assertThrows(UnsupportedOperationException.class, () -> order.getItems().clear());
    }

    // ---------------- Trang thai ----------------

    @ParameterizedTest(name = "setStatus({0})")
    @EnumSource(OrderStatus.class)
    @DisplayName("setStatus: getStatus tra ve dung trang thai vua dat")
    void setStatus_anyStatus_isReturnedByGetStatus(OrderStatus status) {
        order.setStatus(status);

        assertEquals(status, order.getStatus());
    }

    // ---------------- Ma don ----------------

    @Test
    @DisplayName("Ma don tang dan (so sanh tuong doi vi idCounter la static)")
    void orderId_nextOrder_isPreviousIdPlusOne() {
        Order next = new Order("Binh");

        assertEquals(order.getOrderId() + 1, next.getOrderId());
    }

    @Test
    @DisplayName("200 don tao tu 8 luong -> ma don khong bi trung")
    void orderId_createdFromManyThreads_areUnique() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        Set<Integer> ids = ConcurrentHashMap.newKeySet();
        try {
            List<Future<?>> tasks = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                tasks.add(pool.submit(() -> ids.add(new Order("Khach").getOrderId())));
            }
            for (Future<?> task : tasks) {
                task.get();   // cho tung luong xong
            }
        } finally {
            pool.shutdown();
        }

        assertEquals(200, ids.size());
    }

    // ---------------- equals / hashCode ----------------

    @Test
    @DisplayName("equals: chinh no, don khac, null, khac kieu")
    void equals_variousCases() {
        Order other = new Order("Binh");

        assertTrue(order.equals(order));          // cung doi tuong
        assertNotEquals(order, other);            // khac ma don
        assertFalse(order.equals(null));          // null
        assertFalse(order.equals("khong phai Order")); // khac kieu
    }

    @Test
    @DisplayName("hashCode dua tren ma don")
    void hashCode_isBasedOnOrderId() {
        assertEquals(Integer.hashCode(order.getOrderId()), order.hashCode());
        assertEquals(order.hashCode(), order.hashCode());
    }
}
