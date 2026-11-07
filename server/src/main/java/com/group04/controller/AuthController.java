package com.group04.controller;

import com.group04.auth.JwtUtil;
import com.group04.auth.Role;
import com.group04.dto.DtoMapper;
import com.group04.dto.DtoMapper.LoginRequest;
import com.group04.dto.DtoMapper.LoginResponse;
import com.group04.store.UserAccount;
import com.group04.store.UserStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Đăng nhập: so tài khoản với UserStore, trả JWT nhẹ để client gắn vào header.
 * Không có API đăng ký - tài khoản do admin quản lý (đề tài chỉ có nhân sự quán).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserStore userStore;
    private final JwtUtil jwtUtil;

    public AuthController(UserStore userStore, JwtUtil jwtUtil) {
        this.userStore = userStore;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request == null || request.username() == null || request.password() == null) {
            return badRequest("Thiếu tên đăng nhập hoặc mật khẩu");
        }
        UserAccount account = userStore.verify(request.username().trim(), request.password());
        if (account == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Sai tên đăng nhập hoặc mật khẩu"));
        }
        String token = jwtUtil.generate(account.getUsername(), account.getRole().name());
        return ResponseEntity.ok(new LoginResponse(token, account.getUsername(), account.getRole().name()));
    }

    private static ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
