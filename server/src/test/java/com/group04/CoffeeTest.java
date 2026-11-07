package com.group04;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CoffeeTest {

    @Test
    void defaultConstructor_createsObject() {
        Coffee coffee = new Coffee();

        assertNotNull(coffee);
        assertFalse(coffee.isIced());
        assertEquals(0, coffee.getExtraShots());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 25000",
        "1, 33000",
        "3, 49000"
    })
    void calculatePrice_addsEightThousandPerShot(
            int shots, double expectedPrice) {
        Coffee coffee = new Coffee("C01", "Ca phe", 25000, true, shots);

        assertEquals(expectedPrice, coffee.calculatePrice());
    }

    @Test
    void constructor_rejectsNegativeShots() {
        assertThrows(IllegalArgumentException.class,
                () -> new Coffee("C01", "Ca phe", 25000, false, -1));
    }

    @Test
    void setExtraShots_rejectsNegativeValue() {
        Coffee coffee = new Coffee("C01", "Ca phe", 25000, false, 0);

        assertThrows(IllegalArgumentException.class,
                () -> coffee.setExtraShots(-1));
    }

    @ParameterizedTest
    @CsvSource({"0", "1", "3"})
    void setExtraShots_acceptsNonNegativeValues(int shots) {
        Coffee coffee = new Coffee("C01", "Ca phe", 25000, false, 0);

        coffee.setExtraShots(shots);

        assertEquals(shots, coffee.getExtraShots());
    }

    @Test
    void setIced_updatesIcedState() {
        Coffee coffee = new Coffee("C01", "Ca phe", 25000, false, 0);

        coffee.setIced(true);

        assertTrue(coffee.isIced());
    }

    @Test
    void prepare_icedCoffeeMentionsIce() {
        Coffee coffee = new Coffee("C01", "Ca phe", 25000, true, 1);

        assertEquals("Xay ca phe, pha 2 shot, cho them da", coffee.prepare());
    }

    @Test
    void prepare_hotCoffeeMentionsHotServing() {
        Coffee coffee = new Coffee("C01", "Ca phe", 25000, false, 0);

        assertEquals("Xay ca phe, pha 1 shot, dung nong", coffee.prepare());
    }

    @Test
    void getPrepTimeMs_returnsTwoSeconds() {
        Coffee coffee = new Coffee("C01", "Ca phe", 25000, false, 0);

        assertEquals(2000, coffee.getPrepTimeMs());
    }
}