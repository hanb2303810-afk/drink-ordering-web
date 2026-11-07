package com.group04;

public enum OrderStatus {

    /** Don vua tao, dang cho barista nhan */
    PENDING("Dang cho"),

    /** Barista dang pha che */
    PREPARING("Dang pha che"),

    /** Da pha xong */
    COMPLETED("Hoan thanh");

    /**
     * Ten hien thi cua trang thai
     */
    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    /**
     * @return ten hien thi cua trang thai
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}