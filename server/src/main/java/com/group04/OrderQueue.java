package com.group04;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Quan ly hang doi cac don hang.
 */
public class OrderQueue {

    /**
     * Hang doi luu cac don hang dang cho xu ly.
     */
    private BlockingQueue<Order> queue;

    /**
     * Dem so don hang da hoan thanh.
     */
    private AtomicInteger completedCount;

    /**
     * Khoi tao hang doi va bo dem so don hoan thanh.
     */
    public OrderQueue() {
        queue = new LinkedBlockingQueue<>();
        completedCount = new AtomicInteger(0);
    }

    /**
     * Dua mot don hang vao hang doi.
     *
     * @param order don hang can them vao hang doi
     */
    public void placeOrder(Order order) {
    if (order == null) {
        throw new IllegalArgumentException("Order khong duoc null");
    }

    try {
        queue.put(order);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException(
                "Bi ngat khi dua don hang vao queue", e);
    }
}

    /**
     * Lay mot don hang ra khoi hang doi.
     * Neu hang doi dang rong, phuong thuc se cho den khi co don hang.
     *
     * @return don hang duoc lay ra
     * @throws InterruptedException neu thread bi ngat trong luc cho
     */
    public Order takeOrder() throws InterruptedException {
        return queue.take();
    }

    /**
     * Tang so luong don hang da hoan thanh.
     */
    public void markCompleted() {
        completedCount.incrementAndGet();
    }

    /**
     * Lay so luong don hang da hoan thanh.
     *
     * @return so don hang da hoan thanh
     */
    public int getCompletedCount() {
        return completedCount.get();
    }
}
