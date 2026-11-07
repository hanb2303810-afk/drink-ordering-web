package com.group04;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class InMemoryRepository<T, ID>
        implements IGenericRepository<T, ID> {

    private final Map<ID, T> storage = new LinkedHashMap<>();
    private final Function<T, ID> idExtractor;

    public InMemoryRepository(Function<T, ID> idExtractor) {
        this.idExtractor = Objects.requireNonNull(
                idExtractor, "idExtractor không được null");
    }

    private ID getEntityId(T entity) {
        Objects.requireNonNull(entity, "Entity không được null");

        ID id = idExtractor.apply(entity);
        if (id == null) {
            throw new ValidationException("ID của entity không được null");
        }
        return id;
    }

    private void validateId(ID id) {
        Objects.requireNonNull(id, "ID không được null");
    }

    @Override
    public synchronized T add(T entity) {
        ID id = getEntityId(entity);

        if (storage.containsKey(id)) {
            throw new DuplicateKeyException("ID đã tồn tại: " + id);
        }

        storage.put(id, entity);
        return entity;
    }

    @Override
    public synchronized T update(T entity) {
        ID id = getEntityId(entity);

        if (!storage.containsKey(id)) {
            throw new EntityNotFoundException("Không tìm thấy entity với ID: " + id);
        }

        storage.put(id, entity);
        return entity;
    }

    @Override
    public synchronized Optional<T> findById(ID id) {
        validateId(id);
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public synchronized List<T> findAllByIds(Iterable<? extends ID> ids) {
        Objects.requireNonNull(ids, "Danh sách ID không được null");

        List<T> result = new ArrayList<>();
        for (ID id : ids) {
            validateId(id);
            T entity = storage.get(id);

            // Bỏ qua ID không tìm thấy; quy tắc này cần ghi trong JavaDoc.
            if (entity != null) {
                result.add(entity);
            }
        }
        return List.copyOf(result);
    }

    @Override
    public synchronized List<T> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public synchronized void addAll(Iterable<? extends T> entities) {
        Objects.requireNonNull(entities, "Danh sách entity không được null");

        Map<ID, T> pending = new LinkedHashMap<>();

        // Kiểm tra toàn bộ trước khi ghi để tránh thêm dở dang nếu có ID trùng.
        for (T entity : entities) {
            ID id = getEntityId(entity);

            if (storage.containsKey(id) || pending.containsKey(id)) {
                throw new DuplicateKeyException("ID đã tồn tại: " + id);
            }

            pending.put(id, entity);
        }

        storage.putAll(pending);
    }

    @Override
    public synchronized void deleteById(ID id) {
        validateId(id);

        if (storage.remove(id) == null) {
            throw new EntityNotFoundException("Không tìm thấy entity với ID: " + id);
        }
    }

    @Override
    public synchronized boolean existsById(ID id) {
        validateId(id);
        return storage.containsKey(id);
    }

    @Override
    public synchronized long count() {
        return storage.size();
    }
}