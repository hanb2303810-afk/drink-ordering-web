package com.group04;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DrinkTest {

    @Test
    void setBasePrice_acceptsZero() {
        Coffee coffee = new Coffee("C01", "Ca phe", 25000, false, 0);

        coffee.setBasePrice(0);

        assertEquals(0, coffee.getBasePrice());
        assertEquals(0, coffee.calculatePrice());
    }

    @Test
    void setBasePrice_rejectsNegativeValue() {
        Coffee coffee = new Coffee("C01", "Ca phe", 25000, false, 0);

        assertThrows(IllegalArgumentException.class,
                () -> coffee.setBasePrice(-1));
    }

    @Test
    void constructor_rejectsNullId() {
        assertThrows(IllegalArgumentException.class,
                () -> new Coffee(null, "Ca phe", 25000, false, 0));
    }

    @Test
    void constructor_rejectsBlankId() {
        assertThrows(IllegalArgumentException.class,
                () -> new Coffee("   ", "Ca phe", 25000, false, 0));
    }

    @Test
    void constructor_rejectsNullName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Coffee("C01", null, 25000, false, 0));
    }

    @Test
    void constructor_rejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Coffee("C01", "   ", 25000, false, 0));
    }

    @Test
    void constructor_rejectsNegativeBasePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new Coffee("C01", "Ca phe", -1, false, 0));
    }

    @Test
    void equals_returnsTrueForSameObject() {
        Coffee coffee = new Coffee("C01", "Ca phe", 25000, false, 0);

        assertEquals(coffee, coffee);
    }

    @Test
    void equals_returnsTrueForSameClassAndId() {
        Coffee first = new Coffee("C01", "Ca phe sua", 25000, false, 0);
        Coffee second = new Coffee("C01", "Ca phe den", 30000, true, 1);

        assertEquals(first, second);
    }

    @Test
    void equals_returnsFalseForDifferentId() {
        Coffee first = new Coffee("C01", "Ca phe", 25000, false, 0);
        Coffee second = new Coffee("C02", "Ca phe", 25000, false, 0);

        assertNotEquals(first, second);
    }

    @Test
    void equals_returnsFalseForNull() {
        Coffee coffee = new Coffee("C01", "Ca phe", 25000, false, 0);

        assertNotEquals(null, coffee);
    }

    @Test
    void equals_returnsFalseForDifferentDrinkClass() {
        Coffee coffee = new Coffee("D01", "Do uong", 25000, false, 0);
        MilkTea milkTea = new MilkTea("D01", "Do uong", 25000, 50, 0);

        assertNotEquals(coffee, milkTea);
    }

    @Test
    void hashCode_matchesForEqualDrinks() {
        Coffee first = new Coffee("C01", "Ca phe sua", 25000, false, 0);
        Coffee second = new Coffee("C01", "Ca phe den", 30000, true, 1);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void toString_containsNameAndId() {
        Coffee coffee = new Coffee("C01", "Ca phe", 25000, false, 0);

        assertTrue(coffee.toString().contains("Ca phe"));
        assertTrue(coffee.toString().contains("C01"));
    }
}