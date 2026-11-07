package com.group04.realtime;

import com.group04.Order;
import com.group04.dto.DtoMapper;
import com.group04.IGenericRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Cầu nối giữa domain đã chạy đa luồng (OrderQueue + Barista) và tầng web.
 * Trạng thái đơn được thread Barista đổi ngầm nên cách đơn giản và an toàn:
 * một luồng poll dựng snapshot JSON của toàn bộ đơn rồi gửi SSE khi khác lần trước.
 * Poll interval 300ms đủ "thời gian thực" cho demo pha chế tính theo giây.
 */
public class RealtimeService {

    private static final Logger log = LoggerFactory.getLogger(RealtimeService.class);

    private final IGenericRepository<Order, Integer> orderRepository;
    private final ObjectMapper objectMapper;
    private final long pollIntervalMs;

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final AtomicReference<String> lastSnapshot = new AtomicReference<>("");
    private Thread poller;
    private volatile boolean running;

    public RealtimeService(IGenericRepository<Order, Integer> orderRepository,
                           ObjectMapper objectMapper,
                           long pollIntervalMs) {
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
        this.pollIntervalMs = pollIntervalMs;
    }

    /** Kết nối mới: đăng ký emitter và đẩy ngay snapshot hiện tại (khách không phải chờ đợt poll). */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("orders").data(lastSnapshot.get()));
        } catch (Exception ignored) {
            // Gửi snapshot rỗng lỗi thì bỏ qua - đợt poll kế tiếp vẫn đẩy lại đủ.
        }
        return emitter;
    }

    public int getEmitterCount() {
        return emitters.size();
    }

    /** Sinh snapshot JSON mới; hàm đọc được để unit test logic dựng JSON. */
    public String buildSnapshot() throws Exception {
        List<Order> orders = orderRepository.findAll();
        return objectMapper.writeValueAsString(DtoMapper.toOrderDtos(orders));
    }

    /** So snapshot cũ: nếu thay đổi (status đổi do barista) -> đẩy SSE cho mọi client; @return khác lần trước. */
    public boolean pollOnce() throws Exception {
        String snapshot = buildSnapshot();
        if (snapshot.equals(lastSnapshot.get())) {
            return false;
        }
        lastSnapshot.set(snapshot);
        broadcast(snapshot);
        return true;
    }

    private void broadcast(String data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("orders").data(data));
            } catch (Exception e) {
                // client đã đóng tab -> gỡ emitter để không dồn kết nối chết
                emitters.remove(emitter);
            }
        }
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        poller = new Thread(() -> {
            while (running) {
                try {
                    pollOnce();
                } catch (Exception e) {
                    log.debug("Bỏ qua lỗi poll realtime: {}", e.getMessage());
                }
                try {
                    Thread.sleep(pollIntervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "realtime-poller");
        poller.setDaemon(true);
        poller.start();
    }

    public void shutdown() {
        running = false;
        if (poller != null) {
            poller.interrupt();
        }
    }
}
