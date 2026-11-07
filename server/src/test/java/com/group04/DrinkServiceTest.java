package com.group04;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DrinkService Test Suite")
class DrinkServiceTest {

    @Mock
    private IGenericRepository<Drink, String> drinkRepository;

    private DrinkService drinkService;

    private Coffee latte;

    @BeforeEach
    void setUp() {
        // Tao Service that, su dung Repository gia lap
        drinkService = new DrinkService(drinkRepository);

        // Tao du lieu do uong mau dung chung
        latte = new Coffee("C01", "Ca phe sua da", 25000, true, 1);
    }

    @Test
    @DisplayName("createDrink: them do uong moi thanh cong")
    void createDrink_newDrink_success() {
        when(drinkRepository.existsById("C01")).thenReturn(false);
        when(drinkRepository.add(latte)).thenReturn(latte);

        Drink result = drinkService.createDrink(latte);

        assertSame(latte, result);
        verify(drinkRepository, times(1)).add(latte);
    }

    @Test
    @DisplayName("createDrink: ID da ton tai")
    void createDrink_duplicateId_throwsException() {
        when(drinkRepository.existsById("C01")).thenReturn(true);

        assertThrows(
                DuplicateKeyException.class,
                () -> drinkService.createDrink(latte)
        );

        verify(drinkRepository, never()).add(any());
    }

    @Test
    @DisplayName("getDrink: tim thay do uong")
    void getDrink_found_returnsDrink() {
        when(drinkRepository.findById("C01"))
                .thenReturn(Optional.of(latte));

        Drink result = drinkService.getDrink("C01");

        assertSame(latte, result);
    }

    @Test
    @DisplayName("getDrink: khong tim thay do uong")
    void getDrink_notFound_throwsException() {
        when(drinkRepository.findById("C99"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> drinkService.getDrink("C99")
        );

        assertTrue(exception.getMessage().contains("C99"));
    }

    @Test
    @DisplayName("getAllDrinks: tra ve danh sach do uong")
    void getAllDrinks_returnsDrinkList() {
        List<Drink> drinks = List.of(
                latte,
                new Coffee("C02", "Ca phe den", 20000, false, 0)
        );

        when(drinkRepository.findAll()).thenReturn(drinks);

        List<Drink> result = drinkService.getAllDrinks();

        assertEquals(2, result.size());
        assertSame(drinks, result);
    }

    @Test
    @DisplayName("deleteDrink: xoa do uong thanh cong")
    void deleteDrink_success() {
        drinkService.deleteDrink("C01");

        verify(drinkRepository, times(1)).deleteById("C01");
    }

    @Test
    @DisplayName("deleteDrink: Repository nem loi")
    void deleteDrink_notFound_throwsException() {
        doThrow(new EntityNotFoundException("Khong tim thay C99"))
                .when(drinkRepository).deleteById("C99");

        assertThrows(
                EntityNotFoundException.class,
                () -> drinkService.deleteDrink("C99")
        );
    }

    @Test
    @DisplayName("updateDrink: do uong null")
    void updateDrink_null_throwsValidationException() {
        assertThrows(
                ValidationException.class,
                () -> drinkService.updateDrink(null)
        );

        verifyNoInteractions(drinkRepository);
    }

    @Test
    @DisplayName("updateDrink: cap nhat do uong thanh cong")
    void updateDrink_validDrink_success() {
        when(drinkRepository.update(latte)).thenReturn(latte);

        drinkService.updateDrink(latte);

        verify(drinkRepository, times(1)).update(latte);
    }
}

