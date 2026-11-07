package com.group04;

import java.util.*;

/**
 * Chuong trinh chay thu he Thong quan ly do uong.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        // ===== 1. Test tao do uong va tinh gia =====
        System.out.println("=== 1. Do uong ===");
        Drink latte = new Coffee("C01", "Ca phe sua da", 25000, true, 1);
        Drink black = new Coffee("C02", "Ca phe den nong", 20000, false, 0);
        Drink tea = new MilkTea("T01", "Tra sua tran chau", 30000, 70, 2);
        Drink tea2 = new MilkTea("T02", "Tra sua matcha", 35000, 50, 1);

        for (Drink d : List.of(latte, black, tea, tea2)) {
            System.out.println(d + " | " + d.prepare() + " | " + d.getPrepTimeMs() + "ms");
        }

        // ===== 2. Test kiem tra du lieu (phai bao loi) =====
        System.out.println("\n=== 2. Kiem tra du lieu khong hop le ===");
        try {
            new Coffee("C03", "Loi", -1000, false, 0);
        } catch (IllegalArgumentException e) {
            System.out.println("Bat duoc loi: " + e.getMessage());
        }
        try {
            new MilkTea("T03", "Loi", 30000, 150, 0);
        } catch (IllegalArgumentException e) {
            System.out.println("Bat duoc loi: " + e.getMessage());
        }
        try {
            new Order("Khach").addItem(latte, 0);
        } catch (IllegalArgumentException e) {
            System.out.println("Bat duoc loi: " + e.getMessage());
        }

        // ===== 3. Tao don hang =====
        System.out.println("\n=== 3. Don hang ===");
        Order o1 = new Order("An");
        o1.addItem(latte, 2);
        o1.addItem(tea, 1);

        Order o2 = new Order("Binh");
        o2.addItem(black, 1);

        Order o3 = new Order("Chi");
        o3.addItem(tea2, 2);
        o3.addItem(latte, 1);

        List<Order> orders = List.of(o1, o2, o3);
        for (Order o : orders) {
            System.out.println("Don #" + o.getOrderId() + " - " + o.getCustomerName()
                    + " - tong: " + String.format("%,.0f", o.getTotal()) + "d"
                    + " - " + o.getStatus());
        }

        // ===== 4. Chay barista da luong =====
        System.out.println("\n=== 4. Pha che ===");
        OrderQueue queue = new OrderQueue();

        Thread b1 = new Thread(new Barista("Barista 1", queue), "Barista-1");
        Thread b2 = new Thread(new Barista("Barista 2", queue), "Barista-2");
        b1.start();
        b2.start();

        for (Order o : orders) {
            queue.placeOrder(o);
        }

        // Cho den khi tat ca don hoan thanh, in trang thai moi 1 giay
        while (queue.getCompletedCount() < orders.size()) {
            StringBuilder sb = new StringBuilder("Trang thai: ");
            for (Order o : orders) {
                sb.append("#").append(o.getOrderId()).append("=").append(o.getStatus()).append("  ");
            }
            System.out.println(sb);
            Thread.sleep(1000);
        }

        // ===== 5. Ket qua =====
        System.out.println("\n=== 5. Ket qua ===");
        for (Order o : orders) {
            System.out.println("Don #" + o.getOrderId() + " (" + o.getCustomerName() + "): " + o.getStatus());
        }
        System.out.println("So don hoan thanh: " + queue.getCompletedCount() + "/" + orders.size());

        // Dung barista (takeOrder dang block nen can interrupt)
        b1.interrupt();
        b2.interrupt();
        b1.join();
        b2.join();
        System.out.println("Da dung tat ca barista.");
    }
}