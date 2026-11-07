package com.group04;

import java.util.*;

/**
 * Mot dong trong don hang: mot loai do uong va so luong duoc dat.
 */
public class OrderItem {

    /**
     * Do uong cua dong nay
     */
    private Drink drink;

    /**
     * So luong dat
     */
    private int quantity;

    /**
     * @param drink    do uong duoc dat
     * @param quantity so luong, phai lon hon 0
     */
    public OrderItem(Drink drink, int quantity) {
        if (drink == null) {
            throw new IllegalArgumentException("Do uong khong duoc de trong");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("So luong phai lon hon 0");
        }
        this.drink = drink;
        this.quantity = quantity;
    }

    /**
     * @return do uong cua dong nay
     */
    public Drink getDrink() {
        return drink;
    }

    /**
     * @return so luong dat
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @return thanh tien = gia mot don vi * so luong
     */
    public double getSubtotal() {
        return drink.calculatePrice() * quantity;
    }

}
