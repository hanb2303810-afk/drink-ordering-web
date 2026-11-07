package com.group04;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    void values_containsThreeStatuses() {
        assertEquals(3, OrderStatus.values().length);
    }

    @Test
    void valueOf_findsPendingStatus() {
        assertEquals(OrderStatus.PENDING, OrderStatus.valueOf("PENDING"));
    }

    @Test
    void pending_hasExpectedLabelAndString() {
        assertEquals("Dang cho", OrderStatus.PENDING.getLabel());
        assertEquals("Dang cho", OrderStatus.PENDING.toString());
    }

    @Test
    void preparing_hasExpectedLabelAndString() {
        assertEquals("Dang pha che", OrderStatus.PREPARING.getLabel());
        assertEquals("Dang pha che", OrderStatus.PREPARING.toString());
    }

    @Test
    void completed_hasExpectedLabelAndString() {
        assertEquals("Hoan thanh", OrderStatus.COMPLETED.getLabel());
        assertEquals("Hoan thanh", OrderStatus.COMPLETED.toString());
    }
}