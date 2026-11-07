package com.group04.controller;

import com.group04.Coffee;
import com.group04.Drink;
import com.group04.DrinkService;
import com.group04.MilkTea;
import com.group04.dto.DtoMapper;
import com.group04.dto.DtoMapper.DrinkDto;
import com.group04.dto.DtoMapper.DrinkRequest;
import com.group04.auth.AuthInterceptor;
import com.group04.auth.Role;
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

import java.util.List;

/**
 * Menu đồ uống. GET public cho mọi người; POST/PUT/DELETE đọc vai trò
 * từ request attribute do AuthInterceptor đặt - chỉ ADMIN được thao tác.
 */
@RestController
@RequestMapping("/api/drinks")
public class DrinkController {

    private final DrinkService drinkService;

    public DrinkController(DrinkService drinkService) {
        this.drinkService = drinkService;
    }

    @GetMapping
    public List<DrinkDto> list() {
        return DtoMapper.toDrinkDtos(drinkService.getAllDrinks());
    }

    @GetMapping("/{id}")
    public DrinkDto get(@PathVariable String id) {
        return DtoMapper.toDrinkDto(drinkService.getDrink(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DrinkRequest request, HttpServletRequest http) {
        ResponseEntity<?> forbidden = requireAdmin(http);
        if (forbidden != null) {
            return forbidden;
        }
        try {
            Drink drink = fromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(DtoMapper.toDrinkDto(drinkService.createDrink(drink)));
        } catch (IllegalArgumentException | com.group04.DuplicateKeyException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id,
                                    @RequestBody DrinkRequest request,
                                    HttpServletRequest http) {
        ResponseEntity<?> forbidden = requireAdmin(http);
        if (forbidden != null) {
            return forbidden;
        }
        try {
            Drink updated = fromRequest(request);
            if (!id.equals(updated.getId())) {
                return ResponseEntity.badRequest()
                        .body(java.util.Map.of("error", "Id trong đường dẫn và nội dung không khớp"));
            }
            return ResponseEntity.ok(DtoMapper.toDrinkDto(drinkService.updateDrink(updated)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, HttpServletRequest http) {
        ResponseEntity<?> forbidden = requireAdmin(http);
        if (forbidden != null) {
            return forbidden;
        }
        drinkService.deleteDrink(id);
        return ResponseEntity.noContent().build();
    }

    /** @return null nếu đủ quyền ADMIN; ngược lại 401/403 trả thẳng về client. */
    private static ResponseEntity<?> requireAdmin(HttpServletRequest http) {
        Object role = http.getAttribute(AuthInterceptor.ATTR_ROLE);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("error", "Vui lòng đăng nhập để dùng tính năng này"));
        }
        if (role != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of("error", "Chỉ quản trị mới được dùng tính năng này"));
        }
        return null;
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
