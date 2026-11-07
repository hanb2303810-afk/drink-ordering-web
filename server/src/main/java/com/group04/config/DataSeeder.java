package com.group04.config;

import com.group04.Coffee;
import com.group04.Drink;
import com.group04.DrinkService;
import com.group04.MilkTea;
import com.group04.auth.Role;
import com.group04.store.UserAccount;
import com.group04.store.UserStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * Nạp dữ liệu mẫu: menu và tài khoản phân quyền.
 * Idempotent: nếu menu đã có thì bỏ qua, tài khoản admin luôn cập nhật mật khẩu theo config
 * để lúc đổi biến môi trường mật khẩu admin không bị "kẹt" dữ liệu cũ.
 */
@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    public CommandLineRunner seedData(
            DrinkService drinkService,
            UserStore userStore,
            @Value("${app.auth.admin-user:admin}") String adminUser,
            @Value("${app.auth.admin-password:admin123}") String adminPassword) {
        return args -> {
            if (drinkService.getAllDrinks().isEmpty()) {
                List<Drink> menu = List.of(
                        new Coffee("CF01", "Cà phê sữa đá", 25000, true, 0),
                        new Coffee("CF02", "Bạc xỉu", 27000, true, 1),
                        new Coffee("CF03", "Espresso", 22000, false, 0),
                        new MilkTea("MT01", "Trà sữa trân châu", 32000, 70, 1),
                        new MilkTea("MT02", "Trà sữa matcha", 35000, 50, 2),
                        new MilkTea("MT03", "Trà sữa caramel", 38000, 100, 1)
                );
                menu.forEach(drinkService::createDrink);
                log.info("Seed menu: {} món", menu.size());
            } else {
                log.info("Menu đã có sẵn, bỏ qua seed");
            }

            // Tài khoản demo cho từng vai trò; admin luôn đồng bộ với cấu hình mới nhất.
            userStore.upsert(adminUser, adminPassword, Role.ADMIN);
            if (!userStore.exists("phucvu1")) {
                userStore.upsert("phucvu1", "pass123", Role.SERVER);
                userStore.upsert("phucvu2", "pass123", Role.SERVER);
            }
            if (!userStore.exists("barista1")) {
                userStore.upsert("barista1", "pass123", Role.BARISTA);
                userStore.upsert("barista2", "pass123", Role.BARISTA);
            }
        };
    }
}
