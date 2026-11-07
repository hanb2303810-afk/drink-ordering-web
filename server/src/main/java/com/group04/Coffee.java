package com.group04;

import java.util.Objects;

/**
 * Ca phe: ke thua Drink, tinh gia theo so shot them.
 */
public class Coffee extends Drink {

    private boolean iced;

    /**
     * So shot ca phe them
     */
    private int extraShots;

    public Coffee() {
        super();
    }

    /**
     * @param id
     * @param name
     * @param basePrice
     * @param iced
     * @param extraShots
     */
    public Coffee(String id, String name, double basePrice, boolean iced, int extraShots) {
        super(id, name, basePrice);
        this.iced = iced;
        setExtraShots(extraShots); // dung setter de tai su dung phan kiem tra
    }

    /**
     * @return true neu la ca phe da
     */
    public boolean isIced() {
        return iced;
    }

    /**
     * @param iced
     */
    public void setIced(boolean iced) {
        this.iced = iced;
    }

    /**
     * @return so shot them
     */
    public int getExtraShots() {
        return extraShots;
    }

    /**
     * @param extraShots
     */
    public void setExtraShots(int extraShots) {
        if (extraShots < 0) {
            throw new IllegalArgumentException("So shot them khong duoc am");
        }
        this.extraShots = extraShots;
    }

    /**
     * @return gia = giaCoBan + soShotThem * 8000
     */
    @Override
    public double calculatePrice() {
        return getBasePrice() + extraShots * 8000;
    }

    /**
     * @return mo ta buoc pha che
     */
    @Override
    public String prepare() {
        return "Xay ca phe, pha " + (1 + extraShots) + " shot"
                + (iced ? ", cho them da" : ", dung nong");
    }

    /**
     * @return thoi gian pha (ms)
     */
    @Override
    public int getPrepTimeMs() {
        return 2000;
    }

    // --- OVERRIDE EQUALS VÀ HASHCODE ---



}