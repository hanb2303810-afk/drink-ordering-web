package com.group04;

public class OrderService {

    private final IGenericRepository<Order, Integer> orderRepository;
    private final IGenericRepository<Drink, String> drinkRepository;
    private final OrderQueue orderQueue;

    public OrderService(
            IGenericRepository<Order, Integer> orderRepository,
            IGenericRepository<Drink, String> drinkRepository,
            OrderQueue orderQueue) {

        this.orderRepository = orderRepository;
        this.drinkRepository = drinkRepository;
        this.orderQueue = orderQueue;
    }

    /**
     * Pre-condition: customerName khong rong
     * Post-condition: tao Order moi voi status PENDING, luu vao repository
     */
    public Order createOrder(String customerName) {
        Order order = new Order(customerName);
        return orderRepository.add(order);
    }

    /**
     * Pre-condition: order va drink ton tai, quantity > 0
     * Post-condition: them mot OrderItem vao order
     * @throws EntityNotFoundException neu order hoac drink khong ton tai
     */
    public void addDrinkToOrder(int orderId, String drinkId, int quantity) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Khong tim thay don hang voi id: " + orderId
                        )
                );

        Drink drink = drinkRepository.findById(drinkId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Khong tim thay do uong voi id: " + drinkId
                        )
                );

        order.addItem(drink, quantity);
    }

    /**
     * Pre-condition: order ton tai va co it nhat mot OrderItem
     * Post-condition: order duoc dua vao OrderQueue cho Barista xu ly
     * @throws ValidationException neu don chua co mon nao
     */
    public void submitOrder(int orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Khong tim thay don hang voi id: " + orderId
                        )
                );

        if (order.getItems().isEmpty()) {
            throw new ValidationException(
                    "Don hang phai co it nhat mot mon truoc khi gui"
            );
        }

        orderQueue.placeOrder(order);
    }

    public double getOrderTotal(int orderId) {

        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Khong tim thay don hang voi id: " + orderId
                        )
                )
                .getTotal();
    }

    public OrderStatus getOrderStatus(int orderId) {

        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Khong tim thay don hang voi id: " + orderId
                        )
                )
                .getStatus();
    }
    public Order getOrder(int orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Khong tim thay don hang voi id: " + orderId));
    }
}