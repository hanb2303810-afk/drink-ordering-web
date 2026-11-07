package com.group04.web;

import com.group04.IGenericRepository;
import com.group04.InMemoryRepository;
import com.group04.Order;
import com.group04.realtime.RealtimeService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm logic dựng snapshot và nguyên tắc "không thay đổi thì không phát SSE". */
class RealtimeServiceTest {

    @Test
    void snapshotKhacChoi() throws Exception {
        IGenericRepository<Order, Integer> orders = new InMemoryRepository<>(Order::getOrderId);
        // Jackson của Spring đăng ký sẵn module java.time; ObjectMapper tự tạo phải findAndRegisterModules
        RealtimeService service = new RealtimeService(orders,
                new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules(), 10);

        assertTrue(service.buildSnapshot().contains("[]"));
        assertEquals(0, service.getEmitterCount());

        // Lần đầu so với snapshot rỗng ban đầu -> coi như "có thay đổi" (logic mong đợi)
        assertTrue(service.pollOnce());
        // Bảng rỗng tiếp -> snapshot giữ nguyên -> không phát lại
        assertFalse(service.pollOnce());

        orders.add(new Order("Kh"));
        assertTrue(service.pollOnce());  // có đơn mới -> phát 1 lần
        assertFalse(service.pollOnce()); // đổi không thêm -> câm gửi lại
    }
}
