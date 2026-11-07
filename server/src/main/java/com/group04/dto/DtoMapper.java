package com.group04.dto;

import com.group04.Coffee;
import com.group04.Drink;
import com.group04.MilkTea;
import com.group04.Order;
import com.group04.OrderItem;
import com.group04.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Toàn bộ DTO là record - mapping tĩnh duy nhất từ domain ra JSON.
 * Giữ class riêng thay vì decorator để JaCoCo đo được và client đồng bộ types dễ.
 */
public final class DtoMapper {

    private DtoMapper() {
    }

    public record DrinkDto(String id, String name, double basePrice, double price,
                           String type, Boolean iced, Integer extraShots,
                           Integer sugarLevel, Integer toppingCount, int prepTimeMs) {
    }

    public record DrinkRequest(String id, String name, double basePrice, String type,
                               Boolean iced, Integer extraShots,
                               Integer sugarLevel, Integer toppingCount) {
    }

    public record OrderItemDto(String drinkId, String drinkName, int quantity,
                               double unitPrice, double subtotal) {
    }

    public record OrderDto(int orderId, String customerName, OrderStatus status,
                           LocalDateTime createdAt, double total, List<OrderItemDto> items) {
    }

    public record CreateOrderRequest(String customerName, List<ItemRequest> items) {
    }

    public record ItemRequest(String drinkId, int quantity) {
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(String token, String username, String role) {
    }

    public record UserDto(String username, String role) {
    }

    public record ChangeRoleRequest(String role) {
    }

    public record StatsDto(long totalOrders, long pendingOrders, long preparingOrders,
                           long completedOrders, double revenue, int completedCount,
                           List<DrinkSalesDto> topDrinks) {
    }

    public record DrinkSalesDto(String drinkId, String drinkName, long quantity, double revenue) {
    }

    public static DrinkDto toDrinkDto(Drink drink) {
        Boolean iced = null;
        Integer extraShots = null;
        Integer sugarLevel = null;
        Integer toppingCount = null;
        String type;
        if (drink instanceof Coffee coffee) {
            type = "COFFEE";
            iced = coffee.isIced();
            extraShots = coffee.getExtraShots();
        } else {
            type = "MILK_TEA";
            MilkTea milkTea = (MilkTea) drink;
            sugarLevel = milkTea.getSugarLevel();
            toppingCount = milkTea.getToppingCount();
        }
        return new DrinkDto(drink.getId(), drink.getName(), drink.getBasePrice(),
                drink.calculatePrice(), type, iced, extraShots,
                sugarLevel, toppingCount, drink.getPrepTimeMs());
    }

    public static List<DrinkDto> toDrinkDtos(List<Drink> drinks) {
        return drinks.stream().map(DtoMapper::toDrinkDto).toList();
    }

    public static OrderDto toOrderDto(Order order) {
        List<OrderItemDto> items = order.getItems().stream()
                .map(DtoMapper::toOrderItemDto)
                .toList();
        return new OrderDto(order.getOrderId(), order.getCustomerName(),
                order.getStatus(), order.getCreatedAt(), order.getTotal(), items);
    }

    public static List<OrderDto> toOrderDtos(List<Order> orders) {
        return orders.stream().map(DtoMapper::toOrderDto).toList();
    }

    private static OrderItemDto toOrderItemDto(OrderItem item) {
        return new OrderItemDto(item.getDrink().getId(), item.getDrink().getName(),
                item.getQuantity(), item.getDrink().calculatePrice(), item.getSubtotal());
    }

    public static UserDto toUserDto(com.group04.store.UserAccount account) {
        return new UserDto(account.getUsername(), account.getRole().name());
    }
}
