package com.group04;

import java.util.Objects;
/**
 * Tra sua: ke thua Drink, tinh gia theo so topping.
 */
public class MilkTea extends Drink {

    /**
     * Phan tram duong (0 - 100)
     */
    private int sugarLevel;

    /**
     * So topping them
     */
    private int toppingCount;

    /**
     * Default constructor
     */
    public MilkTea() {
        super();
    }

    /**
     * @param id
     * @param name
     * @param basePrice
     * @param sugarLevel
     * @param toppingCount
     */
    public MilkTea(String id, String name, double basePrice, int sugarLevel, int toppingCount) {
        super(id, name, basePrice);
        setSugarLevel(sugarLevel);     // dung setter de tai su dung phan kiem tra
        setToppingCount(toppingCount);
    }

    /**
     * @return phan tram duong
     */
    public int getSugarLevel() {
        return sugarLevel;
    }

    /**
     * @param sugarLevel
     */
    public void setSugarLevel(int sugarLevel) {
        if (sugarLevel < 0 || sugarLevel > 100) {
            throw new IllegalArgumentException("Luong duong phai tu 0 den 100");
        }
        this.sugarLevel = sugarLevel;
    }

    /**
     * @return so topping
     */
    public int getToppingCount() {
        return toppingCount;
    }

    /**
     * @param toppingCount
     */
    public void setToppingCount(int toppingCount) {
        if (toppingCount < 0) {
            throw new IllegalArgumentException("So topping khong duoc am");
        }
        this.toppingCount = toppingCount;
    }

    /**
     * @return gia = giaCoBan + soTopping * 5000
     */
    @Override
    public double calculatePrice() {
        return getBasePrice() + toppingCount * 5000;
    }

    /**
     * @return mo ta buoc pha che
     */
    @Override
    public String prepare() {
        return "Pha tra sua " + sugarLevel + "% duong, them " + toppingCount + " topping";
    }

    /**
     * @return thoi gian pha (ms) = 3000 + 500 * soTopping
     */
    @Override
    public int getPrepTimeMs() {
        return 3000 + 500 * toppingCount;
    }
}