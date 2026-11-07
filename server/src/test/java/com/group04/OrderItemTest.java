package com.group04;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
class OrderItemTest {

    @Test
    void constructor_rejectsNullDrink() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem(null, 1));
    }

    @ParameterizedTest
    @CsvSource({"0", "-1"})
    void constructor_rejectsNonPositiveQuantity(int quantity) {
        Coffee coffee = new Coffee("C01", "Ca phe", 25000, false, 0);

        assertThrows(IllegalArgumentException.class,
                () -> new OrderItem(coffee, quantity));
    }

    @Test
    void constructor_acceptsQuantityOne() {
        Coffee coffee = new Coffee("C01", "Ca phe", 25000, false, 0);

        OrderItem item = new OrderItem(coffee, 1);

        assertSame(coffee, item.getDrink());
        assertEquals(1, item.getQuantity());
    }

    @Test
    void subtotal_usesDrinkPriceAndQuantity() {
        Coffee coffee = new Coffee("C01", "Ca phe", 25000, false, 1);
        OrderItem item = new OrderItem(coffee, 2);

        // Giá một ly: 25.000 + 1 shot * 8.000 = 33.000
        assertEquals(66000, item.getSubtotal());
    }

    @Test
    void subtotal_worksWithMilkTea() {
        MilkTea tea = new MilkTea("M01", "Tra sua", 25000, 50, 2);
        OrderItem item = new OrderItem(tea, 3);

        // Giá một ly: 25.000 + 2 topping * 5.000 = 35.000
        assertEquals(105000, item.getSubtotal());
    }
}