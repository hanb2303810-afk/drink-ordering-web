package com.group04.web;

import com.group04.Coffee;
import com.group04.Order;
import com.group04.OrderProcessingService;
import com.group04.OrderQueue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test bổ sung cho OrderProcessingService - lớp domain của nhóm chưa có test trong repo cũ
 * (đây là connector giữa OrderQueue và ExecutorService, thay cho thread thủ công).
 * Chờ hoàn thành theo thời hạn, không sleep cứng (quy tắc FIRST của nhóm).
 */
class OrderProcessingServiceTest {

    @Test
    void constructorTuChoiThamSoSai() {
        OrderQueue queue = new OrderQueue();
        assertThrows(IllegalArgumentException.class, () -> new OrderProcessingService(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new OrderProcessingService(queue, 0));
        assertThrows(IllegalArgumentException.class, () -> new OrderProcessingService(queue, -2));
    }

    @Test
    void startSauKhiDongBatLoiVaIdemPotent() {
        OrderProcessingService service = new OrderProcessingService(new OrderQueue(), 1);
        service.start();
        service.start(); // gọi lại không sinh thêm worker
        service.close();
        service.close(); // close lại an toàn
        assertThrows(IllegalStateException.class, service::start);
    }

    @Test
    void xuLyMotDonKetThucHoanThanh() throws Exception {
        OrderQueue queue = new OrderQueue();
        OrderProcessingService service = new OrderProcessingService(queue, 2);

        Order order = new Order("Khach xu ly song song");
        order.addItem(new Coffee("CF_UT", "Espresso test", 20000, true, 0), 1);
        queue.placeOrder(order);

        service.start();
        long deadline = System.currentTimeMillis() + 5_000;
        while (System.currentTimeMillis() < deadline && queue.getCompletedCount() == 0) {
            Thread.sleep(50);
        }
        service.close();

        assertEquals(1, queue.getCompletedCount(), "Đơn phải được barista xử lý xong");
        assertEquals(com.group04.OrderStatus.COMPLETED, order.getStatus());
        assertTrue(order.getOrderId() > 0);
    }
}
