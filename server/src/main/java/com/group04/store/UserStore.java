package com.group04.store;

import com.group04.auth.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store tài khoản in-memory (đánh maps với InMemoryRepository của domain nhưng
 * Account không phải entity của đề tài nên tách riêng cho gọn nghiệp vụ).
 * Thread-safe bằng ConcurrentHashMap: nhiều request login/đổi role cùng lúc.
 */
@Component
public class UserStore {

    private final Map<String, UserAccount> accounts = new ConcurrentHashMap<>();

    /** @return tài khoản tạo mới hoặc cập nhật mật khẩu nếu đã tồn tại */
    public UserAccount upsert(String username, String password, Role role) {
        Objects.requireNonNull(username);
        return accounts.compute(username, (k, existing) -> {
            if (existing == null) {
                return new UserAccount(username, password, role);
            }
            existing.setPassword(password);
            existing.setRole(role);
            return existing;
        });
    }

    public boolean exists(String username) {
        return accounts.containsKey(username);
    }

    /** Đăng nhập: null nếu sai tài khoản/mật khẩu. */
    public UserAccount verify(String username, String password) {
        UserAccount account = accounts.get(username);
        if (account == null || !account.getPassword().equals(password)) {
            return null;
        }
        return account;
    }

    public UserAccount get(String username) {
        return accounts.get(username);
    }

    public List<UserAccount> getAll() {
        return List.copyOf(accounts.values());
    }

    /** Chỉ ADMIN mới được gọi ở tầng controller; store luôn chấp nhận thay đổi role. */
    public UserAccount changeRole(String username, Role newRole) {
        UserAccount account = accounts.get(username);
        if (account == null) {
            return null;
        }
        account.setRole(newRole);
        return account;
    }
}
