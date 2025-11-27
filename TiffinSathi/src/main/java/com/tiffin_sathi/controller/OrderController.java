package com.tiffin_sathi.controller;

import com.tiffin_sathi.dtos.OrderResponseDTO;
import com.tiffin_sathi.model.Order;
import com.tiffin_sathi.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/today")
    public ResponseEntity<List<OrderResponseDTO>> getTodaysOrders() {
        try {
            List<Order> orders = orderService.getTodaysOrders();
            List<OrderResponseDTO> orderDTOs = orders.stream()
                    .map(OrderResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<Order> orders = orderService.getOrdersByDate(date);
            List<OrderResponseDTO> orderDTOs = orders.stream()
                    .map(OrderResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<OrderResponseDTO>> getVendorOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            LocalDate targetDate = date != null ? date : LocalDate.now();
            List<Order> orders = orderService.getVendorOrdersForDate(targetDate);
            List<OrderResponseDTO> orderDTOs = orders.stream()
                    .map(OrderResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ADD THIS NEW ENDPOINT FOR UPCOMING ORDERS
    @GetMapping("/vendor/upcoming")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<OrderResponseDTO>> getVendorUpcomingOrders() {
        try {
            List<Order> orders = orderService.getVendorUpcomingOrders();
            List<OrderResponseDTO> orderDTOs = orders.stream()
                    .map(OrderResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Order.OrderStatus status,
            @RequestParam(required = false) String deliveryPersonId) {
        try {
            Order order;
            if (deliveryPersonId != null) {
                order = orderService.updateOrderStatus(orderId, status, deliveryPersonId);
            } else {
                order = orderService.updateOrderStatus(orderId, status);
            }
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{orderId}/assign-delivery")
    public ResponseEntity<Order> assignDeliveryPerson(
            @PathVariable Long orderId,
            @RequestParam String deliveryPersonId) {
        try {
            Order order = orderService.assignDeliveryPerson(orderId, deliveryPersonId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/delivery/{deliveryPersonId}")
    public ResponseEntity<List<Order>> getDeliveryPersonOrders(
            @PathVariable String deliveryPersonId) {
        List<Order> orders = orderService.getOrdersByDeliveryPerson(deliveryPersonId);
        return ResponseEntity.ok(orders);
    }

    // NEW ENDPOINTS FOR DELIVERY PARTNERS

    @GetMapping("/delivery/my-orders")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<List<OrderResponseDTO>> getDeliveryPartnerOrders(Authentication authentication) {
        try {
            String email = authentication.getName();
            List<Order> orders = orderService.getDeliveryPartnerOrders(email);
            List<OrderResponseDTO> orderDTOs = orders.stream()
                    .map(OrderResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            System.err.println("Error in getDeliveryPartnerOrders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/delivery/completed")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<List<OrderResponseDTO>> getDeliveryPartnerCompletedOrders(Authentication authentication) {
        try {
            String email = authentication.getName();
            List<Order> orders = orderService.getDeliveryPartnerCompletedOrders(email);
            List<OrderResponseDTO> orderDTOs = orders.stream()
                    .map(OrderResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            System.err.println("Error in getDeliveryPartnerCompletedOrders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/delivery/today")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<List<OrderResponseDTO>> getDeliveryPartnerTodaysOrders(Authentication authentication) {
        try {
            String email = authentication.getName();
            List<Order> orders = orderService.getDeliveryPartnerTodaysOrders(email);
            List<OrderResponseDTO> orderDTOs = orders.stream()
                    .map(OrderResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            System.err.println("Error in getDeliveryPartnerTodaysOrders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/delivery/all")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<List<OrderResponseDTO>> getAllDeliveryPartnerOrders(Authentication authentication) {
        try {
            String email = authentication.getName();
            List<Order> orders = orderService.getAllDeliveryPartnerOrders(email);
            List<OrderResponseDTO> orderDTOs = orders.stream()
                    .map(OrderResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            System.err.println("Error in getAllDeliveryPartnerOrders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}