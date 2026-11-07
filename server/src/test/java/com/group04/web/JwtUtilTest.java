package com.group04.web;

import com.group04.auth.JwtUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtUtilTest {

    private static final JwtUtil JWT = new JwtUtil("test-secret", 60_000);

    @Test
    void sinhVaParSeLaiDuongVong() {
        String token = JWT.generate("phucvu1", "SERVER");
        JwtUtil.Claims claims = JWT.parse(token);
        assertEquals("phucvu1", claims.subject());
        assertEquals("SERVER", claims.role());
    }

    /** Token bị sửa ký tự sau khi ký phải bị từ chối. */
    @Test
    void tokenDatChuKyBatTuChoi() {
        String token = JWT.generate("phucvu1", "SERVER");
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertNull(JWT.parse(tampered));
    }

    @Test
    void tokenHetHanHoacLoaiBich() {
        JwtUtil expired = new JwtUtil("test-secret", -1);
        assertNull(JWT.parse(expired.generate("a", "ADMIN")));

        assertNull(JWT.parse(null));
        assertNull(JWT.parse(""));
        assertNull(JWT.parse("khong.phai.jwt"));
        assertNull(JWT.parse(JWT.generate("a", "ADMIN") + "them"));
    }
}
