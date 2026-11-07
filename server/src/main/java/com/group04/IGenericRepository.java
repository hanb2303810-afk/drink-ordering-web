package com.group04;

import java.util.List;
import java.util.Optional;

public interface IGenericRepository<T, ID> {

    // Thêm mới. Báo DuplicateKeyException nếu ID đã tồn tại.
    T add(T entity);

    // Cập nhật. Báo EntityNotFoundException nếu ID chưa có.
    T update(T entity);

    // Tìm một entity.
    Optional<T> findById(ID id);

    // Tìm nhiều entity theo danh sách ID.
    List<T> findAllByIds(Iterable<? extends ID> ids);

    // Lấy tất cả entity.
    List<T> findAll();

    // Thêm nhiều entity cùng lúc.
    void addAll(Iterable<? extends T> entities);

    // Xóa theo ID. Báo EntityNotFoundException nếu ID chưa có.
    void deleteById(ID id);

    boolean existsById(ID id);

    long count();
}