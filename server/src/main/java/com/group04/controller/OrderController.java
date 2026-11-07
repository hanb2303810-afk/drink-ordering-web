package com.group04.controller;

import com.group04.IGenericRepository;
import com.group04.Order;
import com.group04.OrderService;
import com.group04.dto.DtoMapper;
import com.group04.dto.DtoMapper.CreateOrderRequest;
import com.group04.dto.DtoMapper.OrderDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Đặt nước: một request duy nhất tạo đơn + thêm món + đẩy vào OrderQueue.
 * Sau khi submit, các thread Barista (domain) tự lấy đơn xử lý song song -
 * client theo dõi tình trạng qua GET /api/stream/orders (SSE).
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final IGenericRepository<Order, Integer> orderRepository;

    public OrderController(OrderService orderService,
                           IGenericRepository<Order, Integer> orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    /** Danh sách toàn bộ đơn - trang theo dõi (khách/phục vụ/barista xem chung). */
    @GetMapping
    public List<OrderDto> list() {
        return DtoMapper.toOrderDtos(orderRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable int id) {
        Order order = orderService.getOrder(id);
        return ResponseEntity.ok(DtoMapper.toOrderDto(order));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateOrderRequest request) {
        if (request == null || request.customerName() == null || request.customerName().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Vui lòng nhập tên khách"));
        }
        if (request.items() == null || request.items().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Đơn phải có ít nhất một món"));
        }
        Order order;
        try {
            order = orderService.createOrder(request.customerName().trim());
            for (DtoMapper.ItemRequest item : request.items()) {
                if (item.drinkId() == null || item.quantity() <= 0) {
                    throw new IllegalArgumentException("Mỗi dòng phải chọn món và số lượng lớn hơn 0");
                }
                orderService.addDrinkToOrder(order.getOrderId(), item.drinkId(), item.quantity());
            }
            orderService.submitOrder(order.getOrderId());
        } catch (IllegalArgumentException | com.group04.ValidationException
                 | com.group04.EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.status(201).body(DtoMapper.toOrderDto(order));
    }
}
