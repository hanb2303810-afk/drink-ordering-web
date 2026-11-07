package com.group04;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderQueue Test Suite")
class OrderQueueTest {

    private OrderQueue queue;

    @BeforeEach
    void setUp() {
        queue = new OrderQueue();
    }

    @Test
    @DisplayName("placeOrder roi takeOrder -> lay lai dung don")
    void placeOrder_thenTakeOrder_returnsSameOrder() throws InterruptedException {
        Order order = new Order("An");

        queue.placeOrder(order);

        assertSame(order, queue.takeOrder());
    }

    @Test
    @DisplayName("Hang doi FIFO: don vao truoc ra truoc")
    void takeOrder_twoOrders_followsFifoOrder() throws InterruptedException {
        Order first = new Order("An");
        Order second = new Order("Binh");

        queue.placeOrder(first);
        queue.placeOrder(second);

        assertSame(first, queue.takeOrder());
        assertSame(second, queue.takeOrder());
    }

    @Test
    @DisplayName("Bo dem don hoan thanh: ban dau 0, moi lan markCompleted tang 1")
    void markCompleted_incrementsCounter() {
        assertEquals(0, queue.getCompletedCount());

        queue.markCompleted();
        queue.markCompleted();

        assertEquals(2, queue.getCompletedCount());
    }

    @Test
    @DisplayName("takeOrder tren hang doi rong bi chan; bi ngat thi nem InterruptedException")
    void takeOrder_emptyQueue_blocksUntilInterrupted() throws InterruptedException {
        // ARRANGE: luong phu goi takeOrder tren hang doi rong
        AtomicBoolean gotInterrupted = new AtomicBoolean(false);
        Thread taker = new Thread(() -> {
            try {
                queue.takeOrder();
            } catch (InterruptedException e) {
                gotInterrupted.set(true);
            }
        });
        taker.setDaemon(true);
        taker.start();

        // ACT: cho den khi luong phu thuc su dang cho (bi chan), roi ngat no
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            while (taker.getState() != Thread.State.WAITING) {
                Thread.sleep(5);
            }
        });
        assertTrue(taker.isAlive());
        taker.interrupt();
        taker.join(1000);

        // ASSERT
        assertTrue(gotInterrupted.get());
        assertFalse(taker.isAlive());
    }

    @Test
    @DisplayName("Luong dang cho se nhan duoc don khi don duoc dat vao sau")
    void takeOrder_orderPlacedLater_waitingThreadReceivesIt() throws Exception {
        Order order = new Order("An");
        Order[] received = new Order[1];
        Thread taker = new Thread(() -> {
            try {
                received[0] = queue.takeOrder();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        taker.setDaemon(true);
        taker.start();

        queue.placeOrder(order);
        taker.join(2000);

        assertSame(order, received[0]);
    }
}
