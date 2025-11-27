package com.tiffin_sathi.repository;

import com.tiffin_sathi.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderMeals om LEFT JOIN FETCH om.mealSet WHERE o.deliveryDate = :deliveryDate")
    List<Order> findByDeliveryDate(@Param("deliveryDate") LocalDate deliveryDate);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderMeals om LEFT JOIN FETCH om.mealSet WHERE o.status = :status")
    List<Order> findByStatus(@Param("status") Order.OrderStatus status);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderMeals om LEFT JOIN FETCH om.mealSet WHERE o.deliveryDate = :date AND o.deliveryPersonId = :deliveryPersonId")
    List<Order> findByDeliveryPersonIdAndDeliveryDate(
            @Param("deliveryPersonId") String deliveryPersonId,
            @Param("date") LocalDate date);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderMeals om LEFT JOIN FETCH om.mealSet LEFT JOIN FETCH o.subscription s WHERE o.deliveryDate = :date")
    List<Order> findByDeliveryDateWithSubscription(@Param("date") LocalDate date);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderMeals om LEFT JOIN FETCH om.mealSet WHERE o.deliveryPersonId = :deliveryPersonId")
    List<Order> findByDeliveryPersonId(@Param("deliveryPersonId") String deliveryPersonId);

    @Query("SELECT o FROM Order o WHERE o.deliveryDate >= :deliveryDate")
    List<Order> findByDeliveryDateGreaterThanEqual(@Param("deliveryDate") LocalDate deliveryDate);

    List<Order> findByDeliveryDateAndStatus(LocalDate deliveryDate, Order.OrderStatus status);

    List<Order> findByDeliveryDateAndDeliveryPersonId(LocalDate deliveryDate, String deliveryPersonId);

}