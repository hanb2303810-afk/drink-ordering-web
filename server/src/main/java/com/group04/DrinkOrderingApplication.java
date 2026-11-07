package com.group04;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Web hóa đề tài CT240 nhóm 04 "Hệ thống quản lý và đặt nước tại quán".
 * Domain (Drink/Order/OrderQueue/Barista...) được tái sử dụng nguyên vẹn từ repo console,
 * phần web chỉ bọc REST + SSE quanh domain, không sửa domain.
 */
@SpringBootApplication
public class DrinkOrderingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DrinkOrderingApplication.class, args);
    }
}
