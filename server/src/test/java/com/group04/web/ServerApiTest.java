package com.group04.web;

import com.group04.dto.DtoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

/**
 * Test luồng REST của server là Spring Boot thật:
 * seed -> login theo vai trò -> đặt đơn -> chờ barista xử lý -> admin quản menu/tài khoản.
 * MockMvc để không phải mở port thật.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ServerApiTest {

    @Autowired
    MockMvc mvc;

    private static final com.fasterxml.jackson.databind.ObjectMapper JSON =
            new com.fasterxml.jackson.databind.ObjectMapper();

    /** Login thành công và lấy token theo tài khoản seed. */
    private String login(String user, String pass) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").exists())
                .andReturn().getResponse().getContentAsString();
        return JSON.readTree(body).get("token").asText();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void loginSaiMatKhauTra401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"sai\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void loginThieuThongTinTra400() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void menuPublicVaKhachXemDuoc() throws Exception {
        mvc.perform(get("/api/drinks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void taoDonKhongMonTra400() throws Exception {
        String waiter = login("phucvu1", "pass123");
        mvc.perform(post("/api/orders")
                        .header("Authorization", bearer(waiter))
                        .contentType("application/json")
                        .content("{\"customerName\":\"Khách test\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void luongDatDonVaXuLySongSongHoatDong() throws Exception {
        String waiter = login("phucvu1", "pass123");

        String body = mvc.perform(post("/api/orders")
                        .header("Authorization", bearer(waiter))
                        .contentType("application/json")
                        .content("{\"customerName\":\"Hán TEST\",\"items\":[{\"drinkId\":\"CF03\",\"quantity\":1}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andReturn().getResponse().getContentAsString();

        int orderId = JSON.readTree(body).get("orderId").asInt();

        // Barista là thread nền nên có thể đã nhận đơn (PREPARING) lúc response về -
        // chỉ chờ COMPLETED; không sleep cứng mà poll theo thời hạn tối đa 5 giây.
        long deadline = System.currentTimeMillis() + 5_000;
        String status = null;
        while (System.currentTimeMillis() < deadline) {
            String current = mvc.perform(get("/api/orders/" + orderId)
                            .header("Authorization", bearer(waiter)))
                    .andReturn().getResponse().getContentAsString();
            status = JSON.readTree(current).get("status").asText();
            if ("COMPLETED".equals(status)) {
                break;
            }
            Thread.sleep(100);
        }
        org.junit.jupiter.api.Assertions.assertEquals("COMPLETED", status);
    }

    @Test
    void baristaKhongDuocQuanTriAdminVaDoiVaiTro() throws Exception {
        String barista = login("barista1", "pass123");
        mvc.perform(get("/api/admin/users").header("Authorization", bearer(barista)))
                .andExpect(status().isForbidden());

        String admin = login("admin", "admin123");
        mvc.perform(get("/api/admin/users").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());

        // Trao barista1 quyền phuc vụ rồi trả lại BARISTA, kiểm tra API đổi vai trò
        mvc.perform(put("/api/admin/users/barista1/role")
                        .header("Authorization", bearer(admin))
                        .contentType("application/json")
                        .content("{\"role\":\"SERVER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SERVER"));

        mvc.perform(put("/api/admin/users/barista1/role")
                        .header("Authorization", bearer(admin))
                        .contentType("application/json")
                        .content("{\"role\":\"BARISTA\"}"))
                .andExpect(status().isOk());

        // role không tồn tại và tự đổi chính mình
        mvc.perform(put("/api/admin/users/barista1/role")
                        .header("Authorization", bearer(admin))
                        .contentType("application/json")
                        .content("{\"role\":\"SIMP\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/admin/users/admin/role")
                        .header("Authorization", bearer(admin))
                        .contentType("application/json")
                        .content("{\"role\":\"SERVER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminQuanTriMenuVaThongKe() throws Exception {
        String admin = login("admin", "admin123");

        mvc.perform(post("/api/admin/drinks")
                        .header("Authorization", bearer(admin))
                        .contentType("application/json")
                        .content("{\"id\":\"T01\",\"name\":\"Trà đào\",\"basePrice\":30000,\"type\":\"MILK_TEA\",\"sugarLevel\":50,\"toppingCount\":0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(30000.0));

        // Tạo trùng id -> 400 (controller bắt DuplicateKeyException thành badRequest)
        mvc.perform(post("/api/admin/drinks")
                        .header("Authorization", bearer(admin))
                        .contentType("application/json")
                        .content("{\"id\":\"T01\",\"name\":\"Trà đào 2\",\"basePrice\":30000,\"type\":\"MILK_TEA\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(put("/api/admin/drinks/T01")
                        .header("Authorization", bearer(admin))
                        .contentType("application/json")
                        .content("{\"id\":\"T01\",\"name\":\"Trà đào sửa\",\"basePrice\":31000,\"type\":\"COFFEE\",\"iced\":true,\"extraShots\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Trà đào sửa"));

        mvc.perform(delete("/api/admin/drinks/T01")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/admin/stats").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").exists());
    }

    @Test
    void deleteMonLacThuTra404() throws Exception {
        String admin = login("admin", "admin123");
        mvc.perform(delete("/api/admin/drinks/NOT_EXIST")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void doiVaiTroKhongTonTaiTra404() throws Exception {
        String admin = login("admin", "admin123");
        mvc.perform(put("/api/admin/users/khongtonai/role")
                        .header("Authorization", bearer(admin))
                        .contentType("application/json")
                        .content("{\"role\":\"SERVER\"}"))
                .andExpect(status().isNotFound());
    }

    /** /api/drinks chỉ đọc public; POST/PUT/DELETE phải đăng nhập và đủ ADMIN. */
    @Test
    void menuChiVietDoPublicGhiPhaiAdmin() throws Exception {
        // 401: chưa đăng nhập
        mvc.perform(post("/api/drinks")
                        .contentType("application/json")
                        .content("{\"id\":\"X\",\"name\":\"N\",\"basePrice\":1}"))
                .andExpect(status().isUnauthorized());

        String barista = login("barista1", "pass123");
        // 403: đã đăng nhập nhưng không phải ADMIN
        mvc.perform(post("/api/drinks")
                        .header("Authorization", bearer(barista))
                        .contentType("application/json")
                        .content("{\"id\":\"X\",\"name\":\"N\",\"basePrice\":1}"))
                .andExpect(status().isForbidden());

        String admin = login("admin", "admin123");
        mvc.perform(post("/api/drinks")
                        .header("Authorization", bearer(admin))
                        .contentType("application/json")
                        .content("{\"id\":\"DC01\",\"name\":\"Đá chanh\",\"basePrice\":15000,\"type\":\"COFFEE\",\"iced\":true,\"extraShots\":0}"))
                .andExpect(status().isCreated());

        // id url khác body -> 400
        mvc.perform(put("/api/drinks/DC01")
                        .header("Authorization", bearer(admin))
                        .contentType("application/json")
                        .content("{\"id\":\"DC02\",\"name\":\"Đá chanh\",\"basePrice\":15000,\"type\":\"COFFEE\",\"iced\":true,\"extraShots\":0}"))
                .andExpect(status().isBadRequest());

        // type lạ -> 400
        mvc.perform(post("/api/drinks")
                        .header("Authorization", bearer(admin))
                        .contentType("application/json")
                        .content("{\"id\":\"DC99\",\"name\":\"Lạ\",\"basePrice\":1,\"type\":\"NOK\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/drinks/DC01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Đá chanh"));

        mvc.perform(delete("/api/drinks/DC01")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
    }

    @Test
    void streamDaNhanDonSnapshot() throws Exception {
        String waiter = login("phucvu2", "pass123");
        mvc.perform(post("/api/orders")
                        .header("Authorization", bearer(waiter))
                        .contentType("application/json")
                        .content("{\"customerName\":\"StreamTest\",\"items\":[{\"drinkId\":\"MT01\",\"quantity\":2}]}"))
                .andExpect(status().isCreated());

        // SSE là async: MockMvc xác nhận đã bắt đầu stream là đủ cho endpoint đăng ký emitter.
        mvc.perform(get("/api/stream/orders"))
                .andExpect(request().asyncStarted());
    }
}
