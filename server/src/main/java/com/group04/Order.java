package com.group04;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Don hang cua mot khach, chua danh sach OrderItem.
 * orderId duoc sinh tu dong va an toan khi nhieu luong cung tao don.
 */
public class Order {

    /**
     * Bo dem chung, dung sinh orderId tu dong tang, an toan da luong.
     */
    private static final AtomicInteger idCounter = new AtomicInteger(0);

    /**
     * Ma don, tu dong sinh, khong trung nhau giua cac don
     */
    private int orderId;

    /**
     * Ten khach hang dat don
     */
    private String customerName;

    /**
     * Trang thai hien tai cua don (PENDING, PREPARING, COMPLETED)
     */
    private OrderStatus status;

    /**
     * Thoi diem tao don
     */
    private LocalDateTime createdAt;

    /**
     * Danh sach cac dong trong don
     */
    private List<OrderItem> items;

    /**
     * @param customerName ten khach hang dat don
     */
    public Order(String customerName) {
        this.orderId = idCounter.incrementAndGet();
        this.customerName = customerName;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.items = new ArrayList<>();
    }

    /**
     * Them mot dong do uong vao don.
     *
     * @param drink    do uong can dat
     * @param quantity so luong
     */
    public void addItem(Drink drink, int quantity) {
        items.add(new OrderItem(drink, quantity));
    }

    /**
     * @return danh sach cac dong trong don
     */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * @return tong tien cua don = tong thanh tien tung dong
     */
    public double getTotal() {
        double total = 0;
        for (OrderItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    /**
     * Doi trang thai don. Danh dau synchronized vi nhieu luong
     * (barista, luong hien thi) co the cung doc/ghi tren mot don.
     *
     * @param status trang thai moi
     */
    public synchronized void setStatus(OrderStatus status) {
        this.status = status;
    }

    /**
     * @return trang thai hien tai cua don
     */
    public synchronized OrderStatus getStatus() {
        return status;
    }

    /**
     * @return ma don
     */
    public int getOrderId() {
        return orderId;
    }

    /**
     * @return ten khach hang dat don
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * @return thoi diem tao don
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        return this.orderId == ((Order) o).orderId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(orderId);
    }


}


