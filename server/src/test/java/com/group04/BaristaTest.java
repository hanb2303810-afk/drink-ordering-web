package com.group04;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Barista Test Suite")
class BaristaTest {

    private OrderQueue queue;

    @BeforeEach
    void setUp() {
        queue = new OrderQueue();
    }

    // ---------------- Ham ho tro ----------------

    /** Do uong chi mat prepTimeMs de test chay nhanh (nguyen tac FIRST: Fast). */
    private static Drink drinkWithPrepTime(int prepTimeMs) {
        return new Coffee("T01", "Do uong test", 1000, false, 0) {
            @Override
            public int getPrepTimeMs() {
                return prepTimeMs;
            }
        };
    }

    private static Order orderOf(String customer, Drink drink) {
        Order order = new Order(customer);
        order.addItem(drink, 1);
        return order;
    }

    private static Thread startWorker(Barista barista) {
        Thread worker = new Thread(barista);
        worker.setDaemon(true);   // neu test loi, luong khong giu JVM song
        worker.start();
        return worker;
    }

    /** Cho toi da 2 giay den khi dieu kien dung (khong dung sleep co dinh). */
    private static void waitUntil(BooleanSupplier condition) {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            while (!condition.getAsBoolean()) {
                Thread.sleep(5);
            }
        });
    }

    // ---------------- Constructor, getter, setter ----------------

    @Test
    @DisplayName("Ten null -> IllegalArgumentException")
    void constructor_nullName_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Barista(null, queue));

        assertEquals("Ten barista khong duoc de trong", ex.getMessage());
    }

    @Test
    @DisplayName("Ten chi co dau cach -> IllegalArgumentException")
    void constructor_blankName_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Barista("   ", queue));

        assertEquals("Ten barista khong duoc de trong", ex.getMessage());
    }

    @Test
    @DisplayName("OrderQueue null -> IllegalArgumentException")
    void constructor_nullQueue_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Barista("Barista 1", null));

        assertEquals("OrderQueue khong duoc null", ex.getMessage());
    }

    @Test
    @DisplayName("Constructor hop le: getter tra ve dung gia tri")
    void constructor_validArguments_storesValues() {
        Barista barista = new Barista("Barista 1", queue);

        assertEquals("Barista 1", barista.getName());
        assertSame(queue, barista.getOrderQueue());
    }

    @Test
    @DisplayName("Setter cap nhat ten va hang doi")
    void setters_updateValues() {
        Barista barista = new Barista("Barista 1", queue);
        OrderQueue otherQueue = new OrderQueue();

        barista.setName("Barista 2");
        barista.setOrderQueue(otherQueue);

        assertEquals("Barista 2", barista.getName());
        assertSame(otherQueue, barista.getOrderQueue());
    }

    // ---------------- run() va da luong ----------------

    @Test
    @DisplayName("Pha xong mot don -> COMPLETED va tang bo dem")
    void run_processesOrder_marksCompleted() throws InterruptedException {
        // ARRANGE
        Order order = orderOf("An", drinkWithPrepTime(10));
        queue.placeOrder(order);

        // ACT
        Thread worker = startWorker(new Barista("Barista 1", queue));
        waitUntil(() -> queue.getCompletedCount() >= 1);

        // ASSERT
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals(1, queue.getCompletedCount());

        worker.interrupt();
        worker.join(1000);
        assertFalse(worker.isAlive());
    }

    @Test
    @DisplayName("Mot barista pha lan luot nhieu don")
    void run_processesSeveralOrdersInSequence() throws InterruptedException {
        Order first = orderOf("An", drinkWithPrepTime(10));
        Order second = orderOf("Binh", drinkWithPrepTime(10));
        queue.placeOrder(first);
        queue.placeOrder(second);

        Thread worker = startWorker(new Barista("Barista 1", queue));
        waitUntil(() -> queue.getCompletedCount() >= 2);

        assertEquals(OrderStatus.COMPLETED, first.getStatus());
        assertEquals(OrderStatus.COMPLETED, second.getStatus());
        assertEquals(2, queue.getCompletedCount());

        worker.interrupt();
        worker.join(1000);
    }

    @Test
    @DisplayName("Luong da bi ngat tu truoc -> run() thoat ngay, khong pha gi")
    void run_threadAlreadyInterrupted_exitsImmediately() {
        Barista barista = new Barista("Barista 1", queue);

        Thread.currentThread().interrupt();
        barista.run();

        assertEquals(0, queue.getCompletedCount());
        // Don co interrupt de khong anh huong cac test khac
        assertTrue(Thread.interrupted());
    }

    @Test
    @DisplayName("Bi ngat luc cho don -> luong ket thuc")
    void run_interruptedWhileWaitingForOrder_threadStops() throws InterruptedException {
        Thread worker = startWorker(new Barista("Barista 1", queue));
        waitUntil(() -> worker.getState() == Thread.State.WAITING);

        worker.interrupt();
        worker.join(1000);

        assertFalse(worker.isAlive());
        assertEquals(0, queue.getCompletedCount());
    }

    @Test
    @DisplayName("Bi ngat luc dang pha -> dung giua chung, don khong COMPLETED")
    void run_interruptedDuringPreparation_orderNotCompleted() throws InterruptedException {
        Order order = orderOf("An", drinkWithPrepTime(5000));
        queue.placeOrder(order);

        Thread worker = startWorker(new Barista("Barista 1", queue));
        waitUntil(() -> order.getStatus() == OrderStatus.PREPARING);

        worker.interrupt();
        worker.join(1000);

        assertFalse(worker.isAlive());
        assertEquals(OrderStatus.PREPARING, order.getStatus());
        assertEquals(0, queue.getCompletedCount());
    }

    @Test
    @DisplayName("Hai barista cung xu ly 4 don, moi don chi duoc pha mot lan")
    void run_twoBaristas_processAllOrders() throws InterruptedException {
        Order[] orders = new Order[4];
        for (int i = 0; i < orders.length; i++) {
            orders[i] = orderOf("Khach " + i, drinkWithPrepTime(10));
            queue.placeOrder(orders[i]);
        }

        Thread w1 = startWorker(new Barista("Barista 1", queue));
        Thread w2 = startWorker(new Barista("Barista 2", queue));
        waitUntil(() -> queue.getCompletedCount() >= 4);

        for (Order order : orders) {
            assertEquals(OrderStatus.COMPLETED, order.getStatus());
        }
        assertEquals(4, queue.getCompletedCount());

        w1.interrupt();
        w2.interrupt();
        w1.join(1000);
        w2.join(1000);
    }
}
