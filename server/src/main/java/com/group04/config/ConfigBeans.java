package com.group04.config;

import com.group04.Barista;
import com.group04.Drink;
import com.group04.DrinkService;
import com.group04.IGenericRepository;
import com.group04.InMemoryRepository;
import com.group04.Order;
import com.group04.OrderQueue;
import com.group04.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Khai báo các bean domain (tái sử dụng nguyên vẹn từ console) là singleton của server.
 * Barista chạy ngay khi server khởi động: mỗi barista một thread lấy đơn từ OrderQueue,
 * đúng kịch bản "nhiều đơn song song" của đề tài.
 */
@Configuration
public class ConfigBeans {

    private static final Logger log = LoggerFactory.getLogger(ConfigBeans.class);

    @Bean
    public OrderQueue orderQueue() {
        return new OrderQueue();
    }

    @Bean
    public IGenericRepository<Order, Integer> orderRepository() {
        return new InMemoryRepository<>(Order::getOrderId);
    }

    @Bean
    public IGenericRepository<Drink, String> drinkRepository() {
        return new InMemoryRepository<>(Drink::getId);
    }

    @Bean
    public DrinkService drinkService(IGenericRepository<Drink, String> drinkRepository) {
        return new DrinkService(drinkRepository);
    }

    @Bean
    public OrderService orderService(
            IGenericRepository<Order, Integer> orderRepository,
            IGenericRepository<Drink, String> drinkRepository,
            OrderQueue orderQueue) {
        return new OrderService(orderRepository, drinkRepository, orderQueue);
    }

    /**
     * Khởi chạy N thread Barista dùng lại đúng run() của domain (không sửa domain).
     * Ràng buộc: nếu thread bị ngắt khi đơn đang PREPARING, phần domain vốn không
     * khôi phục được tình trạng đó - đơn in-memory nên chấp nhận, restart mất.
     */
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public BaristaPool baristaPool(OrderQueue orderQueue,
                                   @Value("${app.baristas:2}") int baristas) {
        List<Barista> baristaList = new ArrayList<>();
        for (int i = 1; i <= baristas; i++) {
            baristaList.add(new Barista("Barista-" + i, orderQueue));
        }
        return new BaristaPool(baristaList);
    }

    /**
     * Quản lý vòng đời các thread Barista: start/shutdown gắn với lifecycle Spring.
     */
    public static class BaristaPool {

        private final List<Barista> baristas;
        private final List<Thread> threads = new ArrayList<>();
        private final AtomicBoolean running = new AtomicBoolean(false);

        public BaristaPool(List<Barista> baristas) {
            this.baristas = baristas;
        }

        public List<Barista> getBaristas() {
            return baristas;
        }

        public void start() {
            // Idempotent để restart context test không sinh thread bội.
            if (running.compareAndSet(false, true)) {
                for (Barista barista : baristas) {
                    Thread thread = new Thread(barista, barista.getName());
                    thread.setDaemon(true);
                    thread.start();
                    threads.add(thread);
                }
                log.info("Khởi động {} barista xử lý đơn song song", threads.size());
            }
        }

        public int getRunningCount() {
            return (int) threads.stream().filter(Thread::isAlive).count();
        }

        public void shutdown() {
            if (running.compareAndSet(true, false)) {
                for (Thread thread : threads) {
                    thread.interrupt();
                }
                threads.clear();
            }
        }
    }
}
