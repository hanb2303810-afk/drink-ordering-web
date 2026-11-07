package com.group04;

import java.util.Objects;

/**
 * Lop truu tuong cho mot loai do uong. Cac lop con (Coffee, MilkTea)
 * phai tu cai dat calculatePrice(), prepare() va getPrepTimeMs().
 */
public abstract class Drink implements Preparable {

    /**
     * Ma do uong
     */
    private String id;

    /**
     * Ten do uong
     */
    private String name;

    /**
     * Gia co ban (chua tinh them shot/topping)
     */
    private double basePrice;

    /**
     * Default constructor
     */
    public Drink() {
    }

    /**
     * @param id        ma do uong, khong duoc rong
     * @param name      ten do uong, khong duoc rong
     * @param basePrice gia co ban, khong duoc am
     */
    public Drink(String id, String name, double basePrice) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Ma do uong khong duoc de trong");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ten do uong khong duoc de trong");
        }
        this.id = id;
        this.name = name;
        setBasePrice(basePrice); // dung setter de tai su dung phan kiem tra
    }

    /**
     * @return gia cuoi cung cua do uong (da tinh phan them)
     */
    public abstract double calculatePrice();

    /**
     * @return ma do uong
     */
    public String getId() {
        return id;
    }

    /**
     * @return ten do uong
     */
    public String getName() {
        return name;
    }

    /**
     * @return gia co ban
     */
    public double getBasePrice() {
        return basePrice;
    }

    /**
     * @param basePrice gia co ban moi, khong duoc am
     */
    public void setBasePrice(double basePrice) {
        if (basePrice < 0) {
            throw new IllegalArgumentException("Gia co ban khong duoc am");
        }
        this.basePrice = basePrice;
    }

    // prepare() va getPrepTimeMs() duoc ke thua tu Preparable,
    // lop con bat buoc phai cai dat.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Drink drink = (Drink) o;
        return Objects.equals(id, drink.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + id + ") - " + String.format("%,.0f", calculatePrice()) + "d";
    }

}