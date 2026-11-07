package com.group04.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * JWT "đơn giản" tự viết bằng HMAC-SHA256.
 * Không dùng thư viện jjwt để tránh thêm dependency - payload dạng JSON phẳng
 * {sub, role, exp} đủ phân quyền demo của nhóm.
 */
public final class JwtUtil {

    private final String secret;
    private final long expirationMs;

    public JwtUtil(String secret, long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    /** @return token có định dạng base64url(header).base64url(payload).base64url(hmac) */
    public String generate(String username, String role) {
        long exp = System.currentTimeMillis() + expirationMs;
        String header = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = b64("{\"sub\":\"" + escape(username) + "\",\"role\":\"" + escape(role) + "\",\"exp\":" + exp + "}");
        return header + "." + payload + "." + sign(header + "." + payload);
    }

    /**
     * Parser gọi trước: token hợp lệ + chưa hết hạn thì trả Claims, ngược lại null.
     * Không ném exception để interceptor chỉ cần if-null trả 401.
     */
    public Claims parse(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }
        String expected = sign(parts[0] + "." + parts[1]);
        if (!MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8))) {
            return null;
        }
        String payload = new String(Base64.getUrlDecoder()
                .decode(parts[1].replace('+', '-').replace('/', '_')),
                StandardCharsets.UTF_8);
        long exp = extractLong(payload, "\"exp\":");
        if (exp <= System.currentTimeMillis()) {
            return null;
        }
        String sub = extractString(payload, "sub");
        String role = extractString(payload, "role");
        if (sub == null || role == null) {
            return null;
        }
        return new Claims(sub, role);
    }

    public record Claims(String subject, String role) {
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return b64(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Khong ky duoc token", e);
        }
    }

    private static String b64(String s) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String b64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String extractString(String payload, String key) {
        int i = payload.indexOf("\"" + key + "\":\"");
        if (i < 0) {
            return null;
        }
        int start = i + key.length() + 4;
        StringBuilder sb = new StringBuilder();
        for (int j = start; j < payload.length(); j++) {
            char c = payload.charAt(j);
            if (c == '\\' && j + 1 < payload.length()) {
                sb.append(payload.charAt(++j));
            } else if (c == '"') {
                return sb.toString();
            } else {
                sb.append(c);
            }
        }
        return null;
    }

    private static long extractLong(String payload, String key) {
        int i = payload.indexOf(key);
        if (i < 0) {
            return 0;
        }
        int start = i + key.length();
        StringBuilder sb = new StringBuilder();
        for (int j = start; j < payload.length(); j++) {
            char c = payload.charAt(j);
            if (Character.isDigit(c)) {
                sb.append(c);
            } else {
                break;
            }
        }
        try {
            return Long.parseLong(sb.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
