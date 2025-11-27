package com.tiffin_sathi.dtos;

import com.tiffin_sathi.model.Order;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponseDTO {
    private Long orderId;
    private LocalDate deliveryDate;
    private Order.OrderStatus status;
    private String preferredDeliveryTime;
    private String deliveryAddress;
    private String specialInstructions;
    private String deliveryPersonId;
    private LocalDateTime actualDeliveryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private OrderCustomerDTO customer;
    private List<OrderMealDTO> orderMeals;

    // Constructor
    public OrderResponseDTO(Order order) {
        this.orderId = order.getOrderId();
        this.deliveryDate = order.getDeliveryDate();
        this.status = order.getStatus();
        this.preferredDeliveryTime = order.getPreferredDeliveryTime();
        this.deliveryAddress = order.getDeliveryAddress();
        this.specialInstructions = order.getSpecialInstructions();
        this.deliveryPersonId = order.getDeliveryPersonId();
        this.actualDeliveryTime = order.getActualDeliveryTime();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();

        // Customer info
        if (order.getSubscription() != null && order.getSubscription().getUser() != null) {
            this.customer = new OrderCustomerDTO(order.getSubscription().getUser());
        }

        // Order meals
        if (order.getOrderMeals() != null) {
            this.orderMeals = order.getOrderMeals().stream()
                    .map(OrderMealDTO::new)
                    .collect(Collectors.toList());
        }
    }

    // Getters and setters

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Order.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(Order.OrderStatus status) {
        this.status = status;
    }

    public String getPreferredDeliveryTime() {
        return preferredDeliveryTime;
    }

    public void setPreferredDeliveryTime(String preferredDeliveryTime) {
        this.preferredDeliveryTime = preferredDeliveryTime;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public String getDeliveryPersonId() {
        return deliveryPersonId;
    }

    public void setDeliveryPersonId(String deliveryPersonId) {
        this.deliveryPersonId = deliveryPersonId;
    }

    public LocalDateTime getActualDeliveryTime() {
        return actualDeliveryTime;
    }

    public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) {
        this.actualDeliveryTime = actualDeliveryTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OrderCustomerDTO getCustomer() {
        return customer;
    }

    public void setCustomer(OrderCustomerDTO customer) {
        this.customer = customer;
    }

    public List<OrderMealDTO> getOrderMeals() {
        return orderMeals;
    }

    public void setOrderMeals(List<OrderMealDTO> orderMeals) {
        this.orderMeals = orderMeals;
    }
}