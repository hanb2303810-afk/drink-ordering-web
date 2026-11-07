package com.group04;


import java.util.List;

/**
 * Đại diện cho nhân viên pha chế.
 */
public class Barista implements Runnable {

    /**
     * Tên của nhân viên pha chế.
     */
    private String name;

    /**
     * Hàng đợi các đơn hàng cần pha chế.
     */
    private OrderQueue orderQueue;

    public String getName() {
        return name;
    }

    public OrderQueue getOrderQueue() {
        return orderQueue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrderQueue(OrderQueue orderQueue) {
        this.orderQueue = orderQueue;
    }

    /**
     * Khởi tạo một nhân viên pha chế.
     *
     * @param name tên nhân viên
     * @param queue hàng đợi đơn hàng
     */

    public Barista(String name, OrderQueue orderQueue) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ten barista khong duoc de trong");
        }
        if (orderQueue == null) {
            throw new IllegalArgumentException("OrderQueue khong duoc null");
        }
        this.name = name;
        this.orderQueue = orderQueue;
    }

    /**
     * Thực hiện công việc pha chế.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Lấy một đơn hàng từ hàng đợi.
                Order order = orderQueue.takeOrder();

                // Chuyển trạng thái đơn hàng sang đang pha chế.
                order.setStatus(OrderStatus.PREPARING);

                // Lấy danh sách các món trong đơn hàng.
                List<OrderItem> items = order.getItems();

                // Pha từng món trong đơn hàng.
                for (OrderItem item : items) {
                    Drink drink = item.getDrink();

                    // Chờ theo thời gian pha của món.
                    Thread.sleep(drink.getPrepTimeMs());
                }

                // Đánh dấu đơn hàng đã hoàn thành.
                order.setStatus(OrderStatus.COMPLETED);

                // Tăng số lượng đơn hàng đã hoàn thành.
                orderQueue.markCompleted();

            } catch (InterruptedException e) {
                // Khôi phục trạng thái interrupted của thread.
                Thread.currentThread().interrupt();

                // Dừng vòng lặp khi thread bị ngắt.
                break;
            }
        }
    }
}
