package com.tiffin_sathi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "order_meals")
public class OrderMeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_meal_id")
    private Long orderMealId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    @JsonIgnore
    private MealSet mealSet;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Constructors
    public OrderMeal() {}

    public OrderMeal(Order order, MealSet mealSet, Integer quantity) {
        this.order = order;
        this.mealSet = mealSet;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getOrderMealId() { return orderMealId; }
    public void setOrderMealId(Long orderMealId) { this.orderMealId = orderMealId; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public MealSet getMealSet() { return mealSet; }
    public void setMealSet(MealSet mealSet) { this.mealSet = mealSet; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}