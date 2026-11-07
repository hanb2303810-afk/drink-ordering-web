package com.group04.auth;

/**
 * Vai trò dùng để phân quyền API; khách vãng lai (guest)
 * không đăng nhập nên không mang vai trò nào.
 */
public enum Role {
    SERVER,
    BARISTA,
    ADMIN
}
