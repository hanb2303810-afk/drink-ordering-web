package com.group04;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class OrderProcessingService implements AutoCloseable {

    private final OrderQueue orderQueue;
    private final int workerCount;
    private final ExecutorService executor;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public OrderProcessingService(OrderQueue orderQueue, int workerCount) {
        if (orderQueue == null) {
            throw new IllegalArgumentException("OrderQueue khong duoc null");
        }
        if (workerCount <= 0) {
            throw new IllegalArgumentException("workerCount phai lon hon 0");
        }

        this.orderQueue = orderQueue;
        this.workerCount = workerCount;
        this.executor = Executors.newFixedThreadPool(workerCount);
    }

    public synchronized void start() {
        if (closed.get()) {
            throw new IllegalStateException("Service da dong, khong the start lai");
        }
        if (!started.compareAndSet(false, true)) {
            return; // start lặp lại không tạo thêm worker
        }

        for (int i = 1; i <= workerCount; i++) {
            executor.submit(new Barista("Barista " + i, orderQueue));
        }
    }

    @Override
    public synchronized void close() {
        if (!closed.compareAndSet(false, true)) {
            return; // close lặp lại an toàn
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}