package com.tiffin_sathi.services;

import com.tiffin_sathi.config.VendorContext;
import com.tiffin_sathi.model.*;
import com.tiffin_sathi.repository.DeliveryPartnerRepository;
import com.tiffin_sathi.repository.OrderRepository;
import com.tiffin_sathi.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private VendorContext vendorContext;

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Transactional
    public List<Order> getTodaysOrders() {
        LocalDate today = LocalDate.now();
        List<Order> existingOrders = orderRepository.findByDeliveryDate(today);

        if (existingOrders.isEmpty()) {
            return generateOrdersForDate(today);
        }

        return existingOrders;
    }

    public List<Order> getOrdersByDate(LocalDate date) {
        return orderRepository.findByDeliveryDate(date);
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersByDeliveryPerson(String deliveryPersonId) {
        return orderRepository.findByDeliveryPersonId(deliveryPersonId);
    }

    // UPDATED: Filter out DELIVERED, CANCELLED, COMPLETED, and ARRIVED orders from upcoming
    public List<Order> getVendorUpcomingOrders() {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                throw new RuntimeException("Vendor not found");
            }

            LocalDate tomorrow = LocalDate.now().plusDays(1);
            LocalDate nextWeek = tomorrow.plusDays(7);

            // Get orders for the next 7 days and filter by vendor and status
            List<Order> orders = orderRepository.findByDeliveryDateGreaterThanEqual(tomorrow);
            return orders.stream()
                    .filter(order -> {
                        // Check if the order's meal package belongs to the current vendor
                        try {
                            MealPackage mealPackage = order.getSubscription().getMealPackage();
                            return mealPackage != null &&
                                    mealPackage.getVendor() != null &&
                                    mealPackage.getVendor().getVendorId().equals(vendorId) &&
                                    !order.getDeliveryDate().isAfter(nextWeek) &&
                                    // EXCLUDE these statuses from upcoming orders
                                    order.getStatus() != Order.OrderStatus.DELIVERED &&
                                    order.getStatus() != Order.OrderStatus.CANCELLED &&
                                    order.getStatus() != Order.OrderStatus.PAUSED &&
                                    order.getStatus() != Order.OrderStatus.COMPLETED &&
                                    order.getStatus() != Order.OrderStatus.ARRIVED;
                        } catch (Exception e) {
                            return false; // Skip orders that can't be verified
                        }
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error fetching vendor upcoming orders: " + e.getMessage());
        }
    }

    public List<Order> getVendorOrdersForDate(LocalDate date) {
        try {
            Long vendorId = vendorContext.getCurrentVendorId();
            if (vendorId == null) {
                throw new RuntimeException("Vendor not found");
            }

            // Get orders for the specific date and filter by vendor
            List<Order> orders = orderRepository.findByDeliveryDate(date);
            return orders.stream()
                    .filter(order -> {
                        // Check if the order's meal package belongs to the current vendor
                        try {
                            MealPackage mealPackage = order.getSubscription().getMealPackage();
                            return mealPackage != null &&
                                    mealPackage.getVendor() != null &&
                                    mealPackage.getVendor().getVendorId().equals(vendorId);
                        } catch (Exception e) {
                            return false; // Skip orders that can't be verified
                        }
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error fetching vendor orders: " + e.getMessage());
        }
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Validate status transition
        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);

        if (status == Order.OrderStatus.DELIVERED || status == Order.OrderStatus.COMPLETED) {
            order.setActualDeliveryTime(java.time.LocalDateTime.now());
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status, String deliveryPersonId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Validate status transition
        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);

        if (deliveryPersonId != null) {
            order.setDeliveryPersonId(deliveryPersonId);
        }

        if (status == Order.OrderStatus.DELIVERED || status == Order.OrderStatus.COMPLETED) {
            order.setActualDeliveryTime(java.time.LocalDateTime.now());
        }

        return orderRepository.save(order);
    }

    // Validate status transitions to ensure logical flow
    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        // Define valid transitions
        Map<Order.OrderStatus, List<Order.OrderStatus>> validTransitions = new HashMap<>();

        validTransitions.put(Order.OrderStatus.PENDING, Arrays.asList(
                Order.OrderStatus.CONFIRMED,
                Order.OrderStatus.PREPARING,
                Order.OrderStatus.CANCELLED
        ));



        validTransitions.put(Order.OrderStatus.CONFIRMED, Arrays.asList(
                Order.OrderStatus.PREPARING,
                Order.OrderStatus.CANCELLED
        ));

        validTransitions.put(Order.OrderStatus.PREPARING, Arrays.asList(
                Order.OrderStatus.READY_FOR_DELIVERY,
                Order.OrderStatus.CANCELLED
        ));

        validTransitions.put(Order.OrderStatus.READY_FOR_DELIVERY, Arrays.asList(
                Order.OrderStatus.ASSIGNED,
                Order.OrderStatus.CANCELLED
        ));

        validTransitions.put(Order.OrderStatus.ASSIGNED, Arrays.asList(
                Order.OrderStatus.PICKED_UP,
                Order.OrderStatus.CANCELLED
        ));

        validTransitions.put(Order.OrderStatus.PICKED_UP, Arrays.asList(
                Order.OrderStatus.OUT_FOR_DELIVERY,
                Order.OrderStatus.CANCELLED
        ));

        validTransitions.put(Order.OrderStatus.OUT_FOR_DELIVERY, Arrays.asList(
                Order.OrderStatus.ARRIVED,
                Order.OrderStatus.CANCELLED
        ));

        validTransitions.put(Order.OrderStatus.ARRIVED, Arrays.asList(
                Order.OrderStatus.DELIVERED,
                Order.OrderStatus.CANCELLED
        ));

        validTransitions.put(Order.OrderStatus.DELIVERED, Arrays.asList(
                Order.OrderStatus.COMPLETED
        ));

        validTransitions.put(Order.OrderStatus.PAUSED, Arrays.asList(
                Order.OrderStatus.CONFIRMED,
                Order.OrderStatus.CANCELLED
        ));

        // Special transitions that are always allowed
        List<Order.OrderStatus> alwaysAllowed = Arrays.asList(
                Order.OrderStatus.CANCELLED,
                Order.OrderStatus.FAILED
        );

        // Check if transition is valid
        if (alwaysAllowed.contains(newStatus)) {
            return; // Always allowed to cancel or mark as failed
        }

        if (validTransitions.containsKey(currentStatus)) {
            if (!validTransitions.get(currentStatus).contains(newStatus)) {
                throw new RuntimeException("Invalid status transition from " + currentStatus + " to " + newStatus);
            }
        } else {
            // For statuses not in the map (like COMPLETED), no further transitions are allowed
            throw new RuntimeException("Cannot transition from " + currentStatus + " to " + newStatus);
        }
    }

    @Transactional
    public Order assignDeliveryPerson(Long orderId, String deliveryPersonId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findById(Long.parseLong(deliveryPersonId))
                .orElseThrow(() -> new RuntimeException("Delivery partner not found"));

        // Check if delivery partner is available
        if (deliveryPartner.getAvailabilityStatus() != DeliveryPartner.AvailabilityStatus.AVAILABLE) {
            throw new RuntimeException("Delivery partner is not available for assignment");
        }

        order.setDeliveryPersonId(deliveryPersonId);
        order.setStatus(Order.OrderStatus.OUT_FOR_DELIVERY);

        return orderRepository.save(order);
    }

    // Generate orders for a specific date - UPDATED to only consider ACTIVE subscriptions
    private List<Order> generateOrdersForDate(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String dayName = dayOfWeek.name();

        // CHANGED: Only find ACTIVE subscriptions for this date
        List<Subscription> activeSubscriptions = subscriptionRepository
                .findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        Subscription.SubscriptionStatus.ACTIVE, date, date);

        List<Order> orders = new ArrayList<>();

        for (Subscription subscription : activeSubscriptions) {
            // Check if subscription has this day enabled
            boolean isDayEnabled = subscription.getSubscriptionDays().stream()
                    .anyMatch(day -> day.getDayOfWeek().equals(dayName) && day.getIsEnabled());

            if (isDayEnabled) {
                Order order = createOrderFromSubscription(subscription, date);
                orders.add(order);
            }
        }

        return orderRepository.saveAll(orders);
    }

    private Order createOrderFromSubscription(Subscription subscription, LocalDate deliveryDate) {
        Order order = new Order();
        order.setSubscription(subscription);
        order.setDeliveryDate(deliveryDate);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPreferredDeliveryTime(subscription.getPreferredDeliveryTime());
        order.setDeliveryAddress(subscription.getDeliveryAddress());
        order.setSpecialInstructions(subscription.getSpecialInstructions());

        // Get meals for this specific day
        String dayName = deliveryDate.getDayOfWeek().name();
        SubscriptionDay subscriptionDay = subscription.getSubscriptionDays().stream()
                .filter(day -> day.getDayOfWeek().equals(dayName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Subscription day not found for: " + dayName));

        List<OrderMeal> orderMeals = new ArrayList<>();
        for (SubscriptionDayMeal subscriptionDayMeal : subscriptionDay.getSubscriptionDayMeals()) {
            OrderMeal orderMeal = new OrderMeal();
            orderMeal.setOrder(order);
            orderMeal.setMealSet(subscriptionDayMeal.getMealSet());
            orderMeal.setQuantity(subscriptionDayMeal.getQuantity());
            orderMeals.add(orderMeal);
        }

        order.setOrderMeals(orderMeals);
        return order;
    }

    // Scheduled job to generate next day's orders at 6 PM daily
    @Scheduled(cron = "0 0 18 * * ?")
    @Transactional
    public void generateNextDayOrders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Order> orders = generateOrdersForDate(tomorrow);
        System.out.println("Generated " + orders.size() + " orders for date: " + tomorrow);
    }

    // NEW: Method to mark delivered orders as completed (can be scheduled)
    @Scheduled(cron = "0 0 23 * * ?") // Run daily at 11 PM
    @Transactional
    public void markDeliveredOrdersAsCompleted() {
        LocalDate today = LocalDate.now();
        List<Order> deliveredOrders = orderRepository.findByDeliveryDateAndStatus(today, Order.OrderStatus.DELIVERED);

        for (Order order : deliveredOrders) {
            order.setStatus(Order.OrderStatus.COMPLETED);
        }

        if (!deliveredOrders.isEmpty()) {
            orderRepository.saveAll(deliveredOrders);
            System.out.println("Marked " + deliveredOrders.size() + " delivered orders as COMPLETED");
        }
    }

    // NEW: Method to mark arrived orders as delivered if they've been in ARRIVED status for too long
    @Scheduled(cron = "0 */30 * * * ?") // Run every 30 minutes
    @Transactional
    public void autoMarkArrivedAsDelivered() {
        LocalDate today = LocalDate.now();
        List<Order> arrivedOrders = orderRepository.findByDeliveryDateAndStatus(today, Order.OrderStatus.ARRIVED);

        // Mark orders that have been in ARRIVED status for more than 1 hour as DELIVERED
        // Note: This is a simplified implementation. In production, you'd track when status was changed to ARRIVED

        if (!arrivedOrders.isEmpty()) {
            for (Order order : arrivedOrders) {
                order.setStatus(Order.OrderStatus.DELIVERED);
                order.setActualDeliveryTime(java.time.LocalDateTime.now());
            }
            orderRepository.saveAll(arrivedOrders);
            System.out.println("Auto-marked " + arrivedOrders.size() + " arrived orders as DELIVERED");
        }
    }

    // NEW METHODS FOR DELIVERY PARTNERS

    public List<Order> getDeliveryPartnerOrders(String deliveryPartnerEmail) {
        try {
            // Get delivery partner by email
            DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(deliveryPartnerEmail)
                    .orElseThrow(() -> new RuntimeException("Delivery partner not found with email: " + deliveryPartnerEmail));

            // Get orders assigned to this delivery partner
            List<Order> orders = orderRepository.findByDeliveryPersonId(deliveryPartner.getPartnerId().toString());

            return orders.stream()
                    .filter(order -> {
                        // Only include orders that are active for delivery (not delivered, cancelled, or completed)
                        return order.getStatus() != Order.OrderStatus.DELIVERED &&
                                order.getStatus() != Order.OrderStatus.CANCELLED &&
                                order.getStatus() != Order.OrderStatus.COMPLETED;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error fetching delivery partner orders: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Order> getDeliveryPartnerCompletedOrders(String deliveryPartnerEmail) {
        try {
            DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(deliveryPartnerEmail)
                    .orElseThrow(() -> new RuntimeException("Delivery partner not found with email: " + deliveryPartnerEmail));

            List<Order> orders = orderRepository.findByDeliveryPersonId(deliveryPartner.getPartnerId().toString());

            return orders.stream()
                    .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED ||
                            order.getStatus() == Order.OrderStatus.COMPLETED)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error fetching delivery partner completed orders: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Order> getDeliveryPartnerTodaysOrders(String deliveryPartnerEmail) {
        try {
            DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(deliveryPartnerEmail)
                    .orElseThrow(() -> new RuntimeException("Delivery partner not found with email: " + deliveryPartnerEmail));

            LocalDate today = LocalDate.now();
            List<Order> orders = orderRepository.findByDeliveryDateAndDeliveryPersonId(today, deliveryPartner.getPartnerId().toString());

            return orders.stream()
                    .filter(order -> order.getStatus() != Order.OrderStatus.DELIVERED &&
                            order.getStatus() != Order.OrderStatus.COMPLETED)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error fetching delivery partner today's orders: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Order> getAllDeliveryPartnerOrders(String deliveryPartnerEmail) {
        try {
            DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(deliveryPartnerEmail)
                    .orElseThrow(() -> new RuntimeException("Delivery partner not found with email: " + deliveryPartnerEmail));

            return orderRepository.findByDeliveryPersonId(deliveryPartner.getPartnerId().toString());

        } catch (Exception e) {
            System.err.println("Error fetching all delivery partner orders: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // NEW: Get orders that are currently in delivery process (for delivery dashboard)
    public List<Order> getDeliveryPartnerActiveDeliveries(String deliveryPartnerEmail) {
        try {
            DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(deliveryPartnerEmail)
                    .orElseThrow(() -> new RuntimeException("Delivery partner not found with email: " + deliveryPartnerEmail));

            LocalDate today = LocalDate.now();
            List<Order> orders = orderRepository.findByDeliveryDateAndDeliveryPersonId(today, deliveryPartner.getPartnerId().toString());

            return orders.stream()
                    .filter(order -> {
                        // Include orders that are in delivery process
                        return order.getStatus() == Order.OrderStatus.OUT_FOR_DELIVERY ||
                                order.getStatus() == Order.OrderStatus.ASSIGNED ||
                                order.getStatus() == Order.OrderStatus.PICKED_UP ||
                                order.getStatus() == Order.OrderStatus.ARRIVED;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error fetching delivery partner active deliveries: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // NEW: Update order to ON_THE_WAY status
    @Transactional
    public Order markOrderAsOytForDelivery(Long orderId, String deliveryPartnerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Verify the delivery partner is assigned to this order
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(deliveryPartnerEmail)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found"));

        if (!order.getDeliveryPersonId().equals(deliveryPartner.getPartnerId().toString())) {
            throw new RuntimeException("Delivery partner is not assigned to this order");
        }

        // Validate current status
        if (order.getStatus() != Order.OrderStatus.PICKED_UP) {
            throw new RuntimeException("Order must be in PICKED_UP status to mark as OUT_FOR_DELIVERY");
        }

        order.setStatus(Order.OrderStatus.OUT_FOR_DELIVERY);
        return orderRepository.save(order);
    }

    // NEW: Update order to ARRIVED status
    @Transactional
    public Order markOrderAsArrived(Long orderId, String deliveryPartnerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Verify the delivery partner is assigned to this order
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(deliveryPartnerEmail)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found"));

        if (!order.getDeliveryPersonId().equals(deliveryPartner.getPartnerId().toString())) {
            throw new RuntimeException("Delivery partner is not assigned to this order");
        }

        // Validate current status
        if (order.getStatus() != Order.OrderStatus.OUT_FOR_DELIVERY) {
            throw new RuntimeException("Order must be in OUT_FOR_DELIVERY status to mark as ARRIVED");
        }

        order.setStatus(Order.OrderStatus.ARRIVED);
        return orderRepository.save(order);
    }

    // NEW: Update order to DELIVERED status (for delivery partners)
    @Transactional
    public Order markOrderAsDelivered(Long orderId, String deliveryPartnerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Verify the delivery partner is assigned to this order
        DeliveryPartner deliveryPartner = deliveryPartnerRepository.findByEmail(deliveryPartnerEmail)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found"));

        if (!order.getDeliveryPersonId().equals(deliveryPartner.getPartnerId().toString())) {
            throw new RuntimeException("Delivery partner is not assigned to this order");
        }

        // Validate current status
        if (order.getStatus() != Order.OrderStatus.ARRIVED && order.getStatus() != Order.OrderStatus.OUT_FOR_DELIVERY) {
            throw new RuntimeException("Order must be in ARRIVED or OUT_FOR_DELIVERY status to mark as DELIVERED");
        }

        order.setStatus(Order.OrderStatus.DELIVERED);
        order.setActualDeliveryTime(java.time.LocalDateTime.now());
        return orderRepository.save(order);
    }
}