package com.group04.store;

import com.group04.auth.Role;

/**
 * Tài khoản dùng chung cho nhân viên phục vụ / barista / quản trị.
 * Password lưu plain-text chỉ để phục vụ demo học phần; chạy thật phải hash
 * (BCrypt) và lấy từ biến môi trường.
 */
public class UserAccount {

    private final String username;
    private String password;
    private Role role;

    public UserAccount(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
