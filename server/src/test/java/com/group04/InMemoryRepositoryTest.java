package com.group04;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryRepository Test Suite")
class InMemoryRepositoryTest {

    private InMemoryRepository<Drink, String> repo;
    private Coffee latte;
    private MilkTea tea;

    @BeforeEach
    void setUp() {
        repo = new InMemoryRepository<>(Drink::getId);
        latte = new Coffee("C01", "Ca phe sua da", 25000, true, 1);
        tea = new MilkTea("T01", "Tra sua", 30000, 70, 2);
    }

    // ===== Constructor =====

    @Test
    @DisplayName("constructor: idExtractor null -> NullPointerException")
    void constructor_nullExtractor_throwsException() {
        assertThrows(NullPointerException.class, () -> new InMemoryRepository<Drink, String>(null));
    }

    // ===== add =====

    @Test
    @DisplayName("add: them moi thanh cong")
    void add_newEntity_success() {
        Drink result = repo.add(latte);

        assertSame(latte, result);
        assertEquals(1, repo.count());
        assertTrue(repo.existsById("C01"));
    }

    @Test
    @DisplayName("add: trung id -> DuplicateKeyException")
    void add_duplicateId_throwsException() {
        repo.add(latte);

        assertThrows(DuplicateKeyException.class, () -> repo.add(latte));
        assertEquals(1, repo.count());
    }

    @Test
    @DisplayName("add: entity null -> NullPointerException")
    void add_null_throwsException() {
        assertThrows(NullPointerException.class, () -> repo.add(null));
    }

    @Test
    @DisplayName("add: entity co id null -> ValidationException")
    void add_nullId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> repo.add(new Coffee()));
        assertEquals(0, repo.count());
    }

    // ===== update =====

    @Test
    @DisplayName("update: cap nhat thanh cong")
    void update_existing_success() {
        repo.add(latte);
        Coffee updated = new Coffee("C01", "Ca phe moi", 30000, false, 0);

        Drink result = repo.update(updated);

        assertSame(updated, result);
        assertSame(updated, repo.findById("C01").get());
        assertEquals(1, repo.count());
    }

    @Test
    @DisplayName("update: id chua co -> EntityNotFoundException")
    void update_missing_throwsException() {
        assertThrows(EntityNotFoundException.class, () -> repo.update(latte));
    }

    @Test
    @DisplayName("update: entity null -> NullPointerException")
    void update_null_throwsException() {
        assertThrows(NullPointerException.class, () -> repo.update(null));
    }

    // ===== findById =====

    @Test
    @DisplayName("findById: tim thay")
    void findById_found_returnsEntity() {
        repo.add(latte);

        Optional<Drink> result = repo.findById("C01");

        assertTrue(result.isPresent());
        assertSame(latte, result.get());
    }

    @Test
    @DisplayName("findById: khong thay -> Optional rong")
    void findById_notFound_returnsEmpty() {
        assertTrue(repo.findById("C99").isEmpty());
    }

    @Test
    @DisplayName("findById: id null -> NullPointerException")
    void findById_nullId_throwsException() {
        assertThrows(NullPointerException.class, () -> repo.findById(null));
    }

    // ===== findAllByIds =====

    @Test
    @DisplayName("findAllByIds: bo qua id khong co")
    void findAllByIds_skipsMissingIds() {
        repo.add(latte);
        repo.add(tea);

        List<Drink> result = repo.findAllByIds(List.of("C01", "X99", "T01"));

        assertEquals(2, result.size());
        assertSame(latte, result.get(0));
        assertSame(tea, result.get(1));
    }

    @Test
    @DisplayName("findAllByIds: danh sach id rong -> danh sach rong")
    void findAllByIds_emptyIds_returnsEmpty() {
        repo.add(latte);

        assertTrue(repo.findAllByIds(List.of()).isEmpty());
    }

    @Test
    @DisplayName("findAllByIds: ids null -> NullPointerException")
    void findAllByIds_nullIds_throwsException() {
        assertThrows(NullPointerException.class, () -> repo.findAllByIds(null));
    }

    @Test
    @DisplayName("findAllByIds: chua id null -> NullPointerException")
    void findAllByIds_containsNullId_throwsException() {
        List<String> ids = java.util.Arrays.asList("C01", null);

        assertThrows(NullPointerException.class, () -> repo.findAllByIds(ids));
    }

    // ===== findAll =====

    @Test
    @DisplayName("findAll: dung thu tu them vao")
    void findAll_keepsInsertionOrder() {
        repo.add(tea);
        repo.add(latte);

        List<Drink> result = repo.findAll();

        assertEquals(2, result.size());
        assertSame(tea, result.get(0));
        assertSame(latte, result.get(1));
    }

    @Test
    @DisplayName("findAll: kho rong -> danh sach rong")
    void findAll_empty_returnsEmptyList() {
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    @DisplayName("findAll: danh sach tra ve khong sua duoc")
    void findAll_returnsUnmodifiableList() {
        repo.add(latte);

        assertThrows(UnsupportedOperationException.class, () -> repo.findAll().add(tea));
    }

    // ===== addAll =====

    @Test
    @DisplayName("addAll: them nhieu phan tu thanh cong")
    void addAll_valid_addsAll() {
        repo.addAll(List.of(latte, tea));

        assertEquals(2, repo.count());
        assertTrue(repo.existsById("C01"));
        assertTrue(repo.existsById("T01"));
    }

    @Test
    @DisplayName("addAll: co phan tu trung -> khong them gi (tinh toan ven)")
    void addAll_duplicateInStorage_addsNothing() {
        repo.add(latte);

        assertThrows(DuplicateKeyException.class, () -> repo.addAll(List.of(tea, latte)));

        assertFalse(repo.existsById("T01"));
        assertEquals(1, repo.count());
    }

    @Test
    @DisplayName("addAll: trung ngay trong lo -> khong them gi")
    void addAll_duplicateInsideBatch_addsNothing() {
        assertThrows(DuplicateKeyException.class, () -> repo.addAll(List.of(latte, latte)));

        assertEquals(0, repo.count());
    }

    @Test
    @DisplayName("addAll: danh sach null -> NullPointerException")
    void addAll_null_throwsException() {
        assertThrows(NullPointerException.class, () -> repo.addAll(null));
    }

    // ===== deleteById / existsById / count =====

    @Test
    @DisplayName("deleteById: xoa thanh cong")
    void deleteById_existing_removes() {
        repo.add(latte);

        repo.deleteById("C01");

        assertFalse(repo.existsById("C01"));
        assertEquals(0, repo.count());
    }

    @Test
    @DisplayName("deleteById: id chua co -> EntityNotFoundException")
    void deleteById_missing_throwsException() {
        assertThrows(EntityNotFoundException.class, () -> repo.deleteById("C99"));
    }

    @Test
    @DisplayName("deleteById: id null -> NullPointerException")
    void deleteById_nullId_throwsException() {
        assertThrows(NullPointerException.class, () -> repo.deleteById(null));
    }

    @Test
    @DisplayName("existsById: co / khong / null")
    void existsById_cases() {
        repo.add(latte);

        assertTrue(repo.existsById("C01"));
        assertFalse(repo.existsById("C99"));
        assertThrows(NullPointerException.class, () -> repo.existsById(null));
    }

    @Test
    @DisplayName("count: kho rong bang 0, tang theo so phan tu")
    void count_tracksSize() {
        assertEquals(0, repo.count());

        repo.add(latte);
        repo.add(tea);

        assertEquals(2, repo.count());
    }
}
