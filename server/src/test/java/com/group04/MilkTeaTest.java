package com.group04;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class MilkTeaTest {

    @Test
    void defaultConstructor_createsObject() {
        MilkTea tea = new MilkTea();

        assertNotNull(tea);
        assertEquals(0, tea.getSugarLevel());
        assertEquals(0, tea.getToppingCount());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 25000",
        "1, 30000",
        "3, 40000"
    })
    void calculatePrice_addsFiveThousandPerTopping(
            int toppings, double expectedPrice) {
        MilkTea tea = new MilkTea("M01", "Tra sua", 25000, 50, toppings);

        assertEquals(expectedPrice, tea.calculatePrice());
    }

    @ParameterizedTest
    @CsvSource({"0", "50", "100"})
    void constructor_acceptsValidSugarLevels(int sugarLevel) {
        MilkTea tea = new MilkTea("M01", "Tra sua", 25000, sugarLevel, 0);

        assertEquals(sugarLevel, tea.getSugarLevel());
    }

    @ParameterizedTest
    @CsvSource({"-1", "101"})
    void constructor_rejectsSugarOutsideRange(int sugarLevel) {
        assertThrows(IllegalArgumentException.class,
                () -> new MilkTea("M01", "Tra sua", 25000, sugarLevel, 0));
    }

    @ParameterizedTest
    @CsvSource({"-1", "101"})
    void setSugarLevel_rejectsSugarOutsideRange(int sugarLevel) {
        MilkTea tea = new MilkTea("M01", "Tra sua", 25000, 50, 0);

        assertThrows(IllegalArgumentException.class,
                () -> tea.setSugarLevel(sugarLevel));
    }

    @ParameterizedTest
    @CsvSource({"0", "1", "100"})
    void setSugarLevel_acceptsBoundaryAndMiddleValues(int sugarLevel) {
        MilkTea tea = new MilkTea("M01", "Tra sua", 25000, 50, 0);

        tea.setSugarLevel(sugarLevel);

        assertEquals(sugarLevel, tea.getSugarLevel());
    }

    @Test
    void constructor_rejectsNegativeToppings() {
        assertThrows(IllegalArgumentException.class,
                () -> new MilkTea("M01", "Tra sua", 25000, 50, -1));
    }

    @Test
    void setToppingCount_rejectsNegativeValue() {
        MilkTea tea = new MilkTea("M01", "Tra sua", 25000, 50, 1);

        assertThrows(IllegalArgumentException.class,
                () -> tea.setToppingCount(-1));
    }

    @ParameterizedTest
    @CsvSource({"0", "1", "100"})
    void setToppingCount_acceptsNonNegativeValues(int toppings) {
        MilkTea tea = new MilkTea("M01", "Tra sua", 25000, 50, 0);

        tea.setToppingCount(toppings);

        assertEquals(toppings, tea.getToppingCount());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 3000",
        "1, 3500",
        "2, 4000"
    })
    void getPrepTimeMs_addsHalfSecondPerTopping(
            int toppings, int expectedTime) {
        MilkTea tea = new MilkTea("M01", "Tra sua", 25000, 50, toppings);

        assertEquals(expectedTime, tea.getPrepTimeMs());
    }

    @Test
    void prepare_includesSugarAndToppingCounts() {
        MilkTea tea = new MilkTea("M01", "Tra sua", 25000, 50, 2);

        assertEquals("Pha tra sua 50% duong, them 2 topping", tea.prepare());
    }
}