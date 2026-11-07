package com.group04.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor chặn Authorization trước khi vào controller.
 * Trả 401 (chưa đăng nhập/token sai) hoặc 403 (sai vai trò), body dạng JSON nhỏ
 * cho client hiển thị thông báo tiếng Việt.
 */
public class AuthInterceptor implements HandlerInterceptor {

    /** Tên key trong request attribute: controller đọc nếu cần username. */
    public static final String ATTR_USERNAME = "authUsername";
    public static final String ATTR_ROLE = "authRole";

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // Preflight của CORS không mang token - luôn cho qua để browser không bị chặn trước.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // GET menu là public để khách vãng lai xem được bảng đồ uống;
        // đặt ở interceptor (method-aware) thay vì excludePathPatterns để
        // POST/PUT/DELETE /api/drinks vẫn bị chặn.
        if ("GET".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().startsWith("/api/drinks")) {
            return true;
        }

        String header = request.getHeader("Authorization");
        JwtUtil.Claims claims = jwtUtil.parse(
                header != null && header.startsWith("Bearer ") ? header.substring(7) : null);

        if (claims == null) {
            deny(response, 401, "Vui lòng đăng nhập để dùng tính năng này");
            return false;
        }

        Role role;
        try {
            role = Role.valueOf(claims.role());
        } catch (IllegalArgumentException e) {
            deny(response, 401, "Token không hợp lệ");
            return false;
        }

        request.setAttribute(ATTR_USERNAME, claims.subject());
        request.setAttribute(ATTR_ROLE, role);

        // Khu vực /admin: yêu cầu Admin
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/admin") && role != Role.ADMIN) {
            deny(response, 403, "Chỉ quản trị mới được dùng tính năng này");
            return false;
        }

        return true;
    }

    private static void deny(HttpServletResponse response, int status, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
