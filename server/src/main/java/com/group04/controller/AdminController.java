package com.group04.controller;

import com.group04.Coffee;
import com.group04.Drink;
import com.group04.DrinkService;
import com.group04.IGenericRepository;
import com.group04.MilkTea;
import com.group04.Order;
import com.group04.OrderQueue;
import com.group04.OrderStatus;
import com.group04.auth.AuthInterceptor;
import com.group04.auth.Role;
import com.group04.config.ConfigBeans.BaristaPool;
import com.group04.dto.DtoMapper;
import com.group04.dto.DtoMapper.ChangeRoleRequest;
import com.group04.dto.DtoMapper.DrinkDto;
import com.group04.dto.DtoMapper.DrinkRequest;
import com.group04.dto.DtoMapper.DrinkSalesDto;
import com.group04.dto.DtoMapper.OrderDto;
import com.group04.dto.DtoMapper.StatsDto;
import com.group04.dto.DtoMapper.UserDto;
import com.group04.store.UserAccount;
import com.group04.store.UserStore;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Toàn bộ route dưới đây đã được AuthInterceptor chặn: phải đăng nhập và role=ADMIN
 * (chặn ở interceptor nên controller không cần kiểm tra lại quyền).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DrinkService drinkService;
    private final IGenericRepository<Order, Integer> orderRepository;
    private final UserStore userStore;
    private final BaristaPool baristaPool;

    public AdminController(DrinkService drinkService,
                           IGenericRepository<Order, Integer> orderRepository,
                           UserStore userStore,
                           BaristaPool baristaPool) {
        this.drinkService = drinkService;
        this.orderRepository = orderRepository;
        this.userStore = userStore;
        this.baristaPool = baristaPool;
    }

    // ---------- Quản lý menu ----------

    @GetMapping("/drinks")
    public List<DrinkDto> drinks() {
        return DtoMapper.toDrinkDtos(drinkService.getAllDrinks());
    }

    @PostMapping("/drinks")
    public ResponseEntity<?> createDrink(@RequestBody DrinkRequest request) {
        try {
            Drink drink = fromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(DtoMapper.toDrinkDto(drinkService.createDrink(drink)));
        } catch (IllegalArgumentException | com.group04.DuplicateKeyException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/drinks/{id}")
    public ResponseEntity<?> updateDrink(@PathVariable String id, @RequestBody DrinkRequest request) {
        try {
            Drink updated = fromRequest(request);
            if (!id.equals(updated.getId())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Id trong đường dẫn và nội dung không khớp"));
            }
            return ResponseEntity.ok(DtoMapper.toDrinkDto(drinkService.updateDrink(updated)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/drinks/{id}")
    public ResponseEntity<?> deleteDrink(@PathVariable String id) {
        drinkService.deleteDrink(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Quản lý tài khoản ----------

    @GetMapping("/users")
    public List<UserDto> users() {
        return userStore.getAll().stream().map(DtoMapper::toUserDto).toList();
    }

    /** Gán vai trò mới cho tài khoản; không cho đổi role của admin đang đăng nhập để không tự khóa. */
    @PutMapping("/users/{username}/role")
    public ResponseEntity<?> changeRole(@PathVariable String username,
                                        @RequestBody ChangeRoleRequest request,
                                        HttpServletRequest http) {
        if (request == null || request.role() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu vai trò mới"));
        }
        Role newRole;
        try {
            newRole = Role.valueOf(request.role());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Vai trò không hợp lệ: " + request.role()));
        }
        String currentAdmin = (String) http.getAttribute(AuthInterceptor.ATTR_USERNAME);
        if (username.equals(currentAdmin)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Không được tự đổi vai trò của chính mình"));
        }
        UserAccount account = userStore.changeRole(username, newRole);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy tài khoản: " + username));
        }
        return ResponseEntity.ok(DtoMapper.toUserDto(account));
    }

    // ---------- Đơn hàng + thống kê ----------

    @GetMapping("/orders")
    public List<OrderDto> orders() {
        return DtoMapper.toOrderDtos(orderRepository.findAll());
    }

    @GetMapping("/stats")
    public StatsDto stats() {
        List<Order> all = orderRepository.findAll();
        long pending = all.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long preparing = all.stream().filter(o -> o.getStatus() == OrderStatus.PREPARING).count();
        long completed = all.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        double revenue = all.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .mapToDouble(Order::getTotal)
                .sum();

        Map<String, long[]> soldByDrink = new HashMap<>();
        Map<String, String> nameByDrink = new HashMap<>();
        Map<String, Double> revenueByDrink = new HashMap<>();
        for (Order order : all) {
            order.getItems().forEach(item -> {
                long[] counter = soldByDrink.computeIfAbsent(
                        item.getDrink().getId(), k -> new long[]{0});
                counter[0] += item.getQuantity();
                nameByDrink.put(item.getDrink().getId(), item.getDrink().getName());
                revenueByDrink.merge(item.getDrink().getId(),
                        item.getSubtotal(), Double::sum);
            });
        }
        List<DrinkSalesDto> topDrinks = new ArrayList<>();
        soldByDrink.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]))
                .limit(5)
                .forEach(e -> topDrinks.add(new DrinkSalesDto(
                        e.getKey(), nameByDrink.get(e.getKey()),
                        e.getValue()[0], revenueByDrink.get(e.getKey()))));

        return new StatsDto(all.size(), pending, preparing, completed, revenue,
                baristaPool.getRunningCount(), topDrinks);
    }

    private static Drink fromRequest(DrinkRequest request) {
        if (request == null || request.id() == null || request.name() == null) {
            throw new IllegalArgumentException("Thiếu id hoặc tên đồ uống");
        }
        String type = request.type() == null ? "COFFEE" : request.type();
        if ("MILK_TEA".equals(type)) {
            return new MilkTea(request.id().trim(), request.name().trim(), request.basePrice(),
                    request.sugarLevel() == null ? 70 : request.sugarLevel(),
                    request.toppingCount() == null ? 0 : request.toppingCount());
        }
        if ("COFFEE".equals(type)) {
            return new Coffee(request.id().trim(), request.name().trim(), request.basePrice(),
                    request.iced() != null && request.iced(),
                    request.extraShots() == null ? 0 : request.extraShots());
        }
        throw new IllegalArgumentException("Loại đồ uống không hợp lệ: " + type);
    }
}
