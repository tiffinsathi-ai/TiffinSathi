package com.tiffin_sathi.services;

import com.tiffin_sathi.dtos.*;
import com.tiffin_sathi.model.*;
import com.tiffin_sathi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private MealPackageRepository mealPackageRepository;

    @Autowired
    private MealSetRepository mealSetRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PricingService pricingService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public SubscriptionResponseDTO createSubscription(SubscriptionRequestDTO request, String currentUserEmail) {
        try {
            // 1. Get the currently authenticated user
            User user = userService.getUserByEmail(currentUserEmail)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + currentUserEmail));

            // 2. Validate meal package
            MealPackage mealPackage = mealPackageRepository.findById(request.getPackageId())
                    .orElseThrow(() -> new RuntimeException("Meal package not found: " + request.getPackageId()));

            // 3. Validate start date
            if (request.getStartDate() == null) {
                throw new RuntimeException("Start date is required");
            }
            LocalDate earliestAllowedDate = LocalDate.now().plusDays(2);

            if (request.getStartDate().isBefore(earliestAllowedDate)) {
                throw new RuntimeException("Start date must be at least 2 days from today.");
            }

            // 4. Calculate pricing
            PricingService.PricingResult pricing = pricingService.calculatePricing(request, mealPackage);

            // 5. Create subscription
            Subscription subscription = new Subscription();
            subscription.setSubscriptionId(generateSubscriptionId());
            subscription.setUser(user);
            subscription.setMealPackage(mealPackage);
            subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);

            // Use the provided start date
            subscription.setStartDate(request.getStartDate());
            subscription.setEndDate(request.getStartDate().plusDays(mealPackage.getDurationDays()));

            // Set pricing
            subscription.setTotalAmount(pricing.getGrandTotal());
            subscription.setSubtotalAmount(pricing.getSubtotal());
            subscription.setDeliveryFee(pricing.getDeliveryFee());
            subscription.setTaxAmount(pricing.getTax());
            subscription.setDiscountAmount(pricing.getDiscount());
            subscription.setDiscountCode(request.getDiscountCode());

            // Set delivery details
            subscription.setDeliveryAddress(request.getDeliveryAddress());
            subscription.setLandmark(request.getLandmark());
            subscription.setPreferredDeliveryTime(request.getPreferredDeliveryTime());
            subscription.setDietaryNotes(request.getDietaryNotes());
            subscription.setSpecialInstructions(request.getSpecialInstructions());
            subscription.setIncludePackaging(request.getIncludePackaging() != null ? request.getIncludePackaging() : true);
            subscription.setIncludeCutlery(request.getIncludeCutlery() != null ? request.getIncludeCutlery() : false);

            // 6. Create subscription days and meals
            createSubscriptionDays(subscription, request.getSchedule(), mealPackage);

            // 7. Save subscription
            Subscription savedSubscription = subscriptionRepository.save(subscription);

            // 8. Create payment record
            Payment payment = paymentService.createPayment(savedSubscription, request.getPaymentMethod(), pricing.getGrandTotal());

            // 9. Generate orders for the subscription period
            generateSubscriptionOrders(savedSubscription);

            // 10. Send email notifications
            sendSubscriptionEmails(savedSubscription);

            System.out.println("Subscription created successfully: " + savedSubscription.getSubscriptionId());
            System.out.println("Payment created: " + payment.getPaymentId());

            return convertToResponseDTO(savedSubscription);

        } catch (Exception e) {
            System.err.println("Error creating subscription: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create subscription: " + e.getMessage());
        }
    }

    private void sendSubscriptionEmails(Subscription subscription) {
        try {
            // Send email to user
            String userSubject = "Subscription Confirmed - TiffinSathi";
            String userMessage = String.format(
                    "Dear %s,\n\n" +
                            "Your subscription has been confirmed and is now ACTIVE!\n\n" +
                            "Subscription ID: %s\n" +
                            "Package: %s\n" +
                            "Start Date: %s\n" +
                            "End Date: %s\n" +
                            "Total Amount: Rs. %.2f\n" +
                            "Delivery Address: %s\n" +
                            "Preferred Time: %s\n\n" +
                            "Thank you for choosing TiffinSathi!",
                    subscription.getUser().getUserName(),
                    subscription.getSubscriptionId(),
                    subscription.getMealPackage().getName(),
                    subscription.getStartDate(),
                    subscription.getEndDate(),
                    subscription.getTotalAmount(),
                    subscription.getDeliveryAddress(),
                    subscription.getPreferredDeliveryTime()
            );

            emailService.sendEmail(
                    subscription.getUser().getEmail(),
                    userSubject,
                    userMessage
            );

            // Send email to vendor
            String vendorSubject = "New Active Subscription - TiffinSathi";
            String vendorMessage = String.format(
                    "New ACTIVE subscription received!\n\n" +
                            "Subscription ID: %s\n" +
                            "Customer: %s\n" +
                            "Package: %s\n" +
                            "Start Date: %s\n" +
                            "End Date: %s\n" +
                            "Delivery Address: %s\n" +
                            "Contact: %s\n" +
                            "Total Amount: Rs. %.2f",
                    subscription.getSubscriptionId(),
                    subscription.getUser().getUserName(),
                    subscription.getMealPackage().getName(),
                    subscription.getStartDate(),
                    subscription.getEndDate(),
                    subscription.getDeliveryAddress(),
                    subscription.getUser().getPhoneNumber(),
                    subscription.getTotalAmount()
            );

            emailService.sendEmail(
                    subscription.getMealPackage().getVendor().getBusinessEmail(),
                    vendorSubject,
                    vendorMessage
            );

        } catch (Exception e) {
            System.err.println("Failed to send subscription emails: " + e.getMessage());
        }
    }

    private void createSubscriptionDays(Subscription subscription, List<SubscriptionDayDTO> schedule, MealPackage mealPackage) {
        if (schedule == null || schedule.isEmpty()) {
            throw new RuntimeException("Subscription schedule is required");
        }

        for (SubscriptionDayDTO dayDTO : schedule) {
            SubscriptionDay subscriptionDay = new SubscriptionDay();
            subscriptionDay.setSubscription(subscription);
            subscriptionDay.setDayOfWeek(dayDTO.getDayOfWeek());
            subscriptionDay.setIsEnabled(dayDTO.getEnabled());

            if (dayDTO.getEnabled() && dayDTO.getMeals() != null) {
                List<SubscriptionDayMeal> dayMeals = new ArrayList<>();
                for (SubscriptionDayMealDTO mealDTO : dayDTO.getMeals()) {
                    MealSet mealSet = mealSetRepository.findById(mealDTO.getSetId())
                            .orElseThrow(() -> new RuntimeException("Meal set not found: " + mealDTO.getSetId()));

                    SubscriptionDayMeal dayMeal = new SubscriptionDayMeal();
                    dayMeal.setSubscriptionDay(subscriptionDay);
                    dayMeal.setMealSet(mealSet);
                    dayMeal.setQuantity(mealDTO.getQuantity());
                    dayMeal.setUnitPrice(pricingService.calculateMealSetPrice(mealSet, mealPackage.getPricePerSet()));

                    dayMeals.add(dayMeal);
                }
                subscriptionDay.setSubscriptionDayMeals(dayMeals);
            }
            subscription.getSubscriptionDays().add(subscriptionDay);
        }
    }

    private void generateSubscriptionOrders(Subscription subscription) {
        LocalDate currentDate = subscription.getStartDate();
        LocalDate endDate = subscription.getEndDate();

        List<Order> orders = new ArrayList<>();

        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            String dayName = dayOfWeek.name();

            // Check if this day is enabled in the subscription
            boolean isDayEnabled = subscription.getSubscriptionDays().stream()
                    .anyMatch(day -> day.getDayOfWeek().equals(dayName) && day.getIsEnabled());

            if (isDayEnabled) {
                Order order = createOrderFromSubscription(subscription, currentDate);
                orders.add(order);
            }

            currentDate = currentDate.plusDays(1);
        }

        if (!orders.isEmpty()) {
            orderRepository.saveAll(orders);
            System.out.println("Generated " + orders.size() + " orders for subscription: " + subscription.getSubscriptionId());
        }
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
        Optional<SubscriptionDay> subscriptionDayOpt = subscription.getSubscriptionDays().stream()
                .filter(day -> day.getDayOfWeek().equals(dayName) && day.getIsEnabled())
                .findFirst();

        if (subscriptionDayOpt.isPresent()) {
            SubscriptionDay subscriptionDay = subscriptionDayOpt.get();
            List<OrderMeal> orderMeals = new ArrayList<>();
            for (SubscriptionDayMeal subscriptionDayMeal : subscriptionDay.getSubscriptionDayMeals()) {
                OrderMeal orderMeal = new OrderMeal();
                orderMeal.setOrder(order);
                orderMeal.setMealSet(subscriptionDayMeal.getMealSet());
                orderMeal.setQuantity(subscriptionDayMeal.getQuantity());
                orderMeals.add(orderMeal);
            }
            order.setOrderMeals(orderMeals);
        }

        return order;
    }

    public List<SubscriptionResponseDTO> getUserSubscriptions(String email) {
        try {
            System.out.println("Fetching subscriptions for user: " + email);
            List<Subscription> subscriptions = subscriptionRepository.findByUserEmail(email);
            System.out.println("Found " + subscriptions.size() + " subscriptions for user: " + email);

            List<SubscriptionResponseDTO> result = subscriptions.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

            System.out.println("Converted to " + result.size() + " DTOs");
            return result;
        } catch (Exception e) {
            System.err.println("Error in getUserSubscriptions: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<SubscriptionResponseDTO> getUserSubscriptionsByPhone(String phoneNumber) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserPhoneNumber(phoneNumber);
        return subscriptions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<SubscriptionResponseDTO> getSubscriptionById(String subscriptionId) {
        return subscriptionRepository.findByIdWithUser(subscriptionId)
                .map(this::convertToResponseDTO);
    }

    @Transactional
    public SubscriptionResponseDTO updateSubscriptionStatus(String subscriptionId, Subscription.SubscriptionStatus status) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));

        Subscription.SubscriptionStatus oldStatus = subscription.getStatus();
        subscription.setStatus(status);
        Subscription updated = subscriptionRepository.save(subscription);

        // If status changed to PAUSED or CANCELLED, cancel future orders
        if ((status == Subscription.SubscriptionStatus.PAUSED || status == Subscription.SubscriptionStatus.CANCELLED)
                && oldStatus == Subscription.SubscriptionStatus.ACTIVE) {
            cancelFutureOrders(subscriptionId);
        }

        // If status changed from PAUSED to ACTIVE, regenerate orders
        if (status == Subscription.SubscriptionStatus.ACTIVE && oldStatus == Subscription.SubscriptionStatus.PAUSED) {
            regenerateFutureOrders(subscription);
        }

        return convertToResponseDTO(updated);
    }

    @Transactional
    public SubscriptionResponseDTO pauseSubscription(String subscriptionId, String userEmail) {
        Subscription subscription = subscriptionRepository.findByIdWithUser(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Verify user owns this subscription
        if (!subscription.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("You are not authorized to modify this subscription");
        }

        if (subscription.getStatus() != Subscription.SubscriptionStatus.ACTIVE) {
            throw new RuntimeException("Only active subscriptions can be paused");
        }

        subscription.setStatus(Subscription.SubscriptionStatus.PAUSED);
        Subscription updated = subscriptionRepository.save(subscription);

        // Cancel future orders
        cancelFutureOrders(subscriptionId);

        return convertToResponseDTO(updated);
    }

    @Transactional
    public SubscriptionResponseDTO resumeSubscription(String subscriptionId, String userEmail) {
        Subscription subscription = subscriptionRepository.findByIdWithUser(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Verify user owns this subscription
        if (!subscription.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("You are not authorized to modify this subscription");
        }

        if (subscription.getStatus() != Subscription.SubscriptionStatus.PAUSED) {
            throw new RuntimeException("Only paused subscriptions can be resumed");
        }

        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        Subscription updated = subscriptionRepository.save(subscription);

        // Regenerate future orders
        regenerateFutureOrders(subscription);

        return convertToResponseDTO(updated);
    }

    @Transactional
    public void cancelSubscription(String subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));

        // Only allow cancellation if subscription hasn't started or is active/paused
        if (subscription.getStartDate().isAfter(LocalDate.now()) ||
                subscription.getStatus() == Subscription.SubscriptionStatus.ACTIVE ||
                subscription.getStatus() == Subscription.SubscriptionStatus.PAUSED) {

            subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(subscription);

            // Cancel all future orders
            cancelFutureOrders(subscriptionId);

            // Refund logic would go here if payment was made
            paymentService.handleCancellationRefund(subscriptionId);
        } else {
            throw new RuntimeException("Cannot cancel subscription that has already ended");
        }
    }

    private void cancelFutureOrders(String subscriptionId) {
        LocalDate today = LocalDate.now();
        List<Order> futureOrders = orderRepository.findByDeliveryDateGreaterThanEqual(today).stream()
                .filter(order -> order.getSubscription().getSubscriptionId().equals(subscriptionId))
                .filter(order -> order.getStatus() == Order.OrderStatus.PENDING || order.getStatus() == Order.OrderStatus.CONFIRMED)
                .collect(Collectors.toList());

        for (Order order : futureOrders) {
            order.setStatus(Order.OrderStatus.CANCELLED);
        }

        if (!futureOrders.isEmpty()) {
            orderRepository.saveAll(futureOrders);
            System.out.println("Cancelled " + futureOrders.size() + " future orders for subscription: " + subscriptionId);
        }
    }

    private void regenerateFutureOrders(Subscription subscription) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.isAfter(subscription.getStartDate()) ? today : subscription.getStartDate();
        LocalDate endDate = subscription.getEndDate();

        // Cancel existing future orders first
        cancelFutureOrders(subscription.getSubscriptionId());

        // Generate new orders from today/start date to end date
        LocalDate currentDate = startDate;
        List<Order> newOrders = new ArrayList<>();

        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            String dayName = dayOfWeek.name();

            // Check if this day is enabled in the subscription
            boolean isDayEnabled = subscription.getSubscriptionDays().stream()
                    .anyMatch(day -> day.getDayOfWeek().equals(dayName) && day.getIsEnabled());

            if (isDayEnabled) {
                Order order = createOrderFromSubscription(subscription, currentDate);
                newOrders.add(order);
            }

            currentDate = currentDate.plusDays(1);
        }

        if (!newOrders.isEmpty()) {
            orderRepository.saveAll(newOrders);
            System.out.println("Regenerated " + newOrders.size() + " orders for subscription: " + subscription.getSubscriptionId());
        }
    }

    // FIXED: Get ALL subscriptions for vendor (including ALL statuses)
    public List<SubscriptionResponseDTO> getSubscriptionsByVendorEmail(String vendorEmail) {
        try {
            System.out.println("Fetching ALL subscriptions for vendor: " + vendorEmail);

            Vendor vendor = vendorRepository.findByBusinessEmail(vendorEmail)
                    .orElseThrow(() -> new RuntimeException("Vendor not found with email: " + vendorEmail));

            List<Subscription> subscriptions = subscriptionRepository.findByMealPackageVendorVendorId(vendor.getVendorId());

            System.out.println("Found " + subscriptions.size() + " total subscriptions for vendor: " + vendorEmail);

            // Log the status distribution for debugging
            Map<Subscription.SubscriptionStatus, Long> statusCount = subscriptions.stream()
                    .collect(Collectors.groupingBy(Subscription::getStatus, Collectors.counting()));
            System.out.println("Subscription status distribution: " + statusCount);

            return subscriptions.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error getting vendor subscriptions: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Get only ACTIVE subscriptions for vendor
    public List<SubscriptionResponseDTO> getActiveSubscriptionsByVendorEmail(String vendorEmail) {
        try {
            Vendor vendor = vendorRepository.findByBusinessEmail(vendorEmail)
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));

            List<Subscription> subscriptions = subscriptionRepository
                    .findByMealPackageVendorVendorIdAndStatus(vendor.getVendorId(), Subscription.SubscriptionStatus.ACTIVE);

            return subscriptions.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching active vendor subscriptions: " + e.getMessage());
        }
    }

    // Get subscriptions by specific status for vendor
    public List<SubscriptionResponseDTO> getSubscriptionsByVendorEmailAndStatus(String vendorEmail, Subscription.SubscriptionStatus status) {
        try {
            Vendor vendor = vendorRepository.findByBusinessEmail(vendorEmail)
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));

            List<Subscription> subscriptions = subscriptionRepository
                    .findByMealPackageVendorVendorIdAndStatus(vendor.getVendorId(), status);

            return subscriptions.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching vendor subscriptions by status: " + e.getMessage());
        }
    }

    private SubscriptionResponseDTO convertToResponseDTO(Subscription subscription) {
        try {
            SubscriptionResponseDTO dto = new SubscriptionResponseDTO();
            dto.setSubscriptionId(subscription.getSubscriptionId());
            dto.setStatus(subscription.getStatus().name());
            dto.setStartDate(subscription.getStartDate());
            dto.setEndDate(subscription.getEndDate());
            dto.setTotalAmount(subscription.getTotalAmount());
            dto.setSubtotalAmount(subscription.getSubtotalAmount());
            dto.setDeliveryFee(subscription.getDeliveryFee());
            dto.setTaxAmount(subscription.getTaxAmount());
            dto.setDiscountAmount(subscription.getDiscountAmount());
            dto.setDeliveryAddress(subscription.getDeliveryAddress());
            dto.setLandmark(subscription.getLandmark());
            dto.setPreferredDeliveryTime(subscription.getPreferredDeliveryTime());
            dto.setDietaryNotes(subscription.getDietaryNotes());
            dto.setSpecialInstructions(subscription.getSpecialInstructions());
            dto.setIncludePackaging(subscription.getIncludePackaging());
            dto.setIncludeCutlery(subscription.getIncludeCutlery());
            dto.setCreatedAt(subscription.getCreatedAt());

            // Customer information
            if (subscription.getUser() != null) {
                OrderCustomerDTO customer = new OrderCustomerDTO();
                customer.setUserId(String.valueOf(subscription.getUser().getId()));
                customer.setUserName(subscription.getUser().getUserName());
                customer.setEmail(subscription.getUser().getEmail());
                customer.setPhoneNumber(subscription.getUser().getPhoneNumber());
                dto.setCustomer(customer);
            }

            // Payment information if exists
            if (subscription.getPayment() != null) {
                PaymentResponseDTO paymentDTO = new PaymentResponseDTO();
                paymentDTO.setPaymentId(subscription.getPayment().getPaymentId());
                paymentDTO.setPaymentMethod(subscription.getPayment().getPaymentMethod().name());
                paymentDTO.setPaymentStatus(subscription.getPayment().getPaymentStatus().name());
                paymentDTO.setAmount(subscription.getPayment().getAmount());
                paymentDTO.setTransactionId(subscription.getPayment().getTransactionId());
                paymentDTO.setPaidAt(subscription.getPayment().getPaidAt());
                dto.setPayment(paymentDTO);
            }

            // Convert subscription days
            if (subscription.getSubscriptionDays() != null) {
                List<SubscriptionDayResponseDTO> dayDTOs = new ArrayList<>();
                for (SubscriptionDay day : subscription.getSubscriptionDays()) {
                    SubscriptionDayResponseDTO dayDTO = new SubscriptionDayResponseDTO();
                    dayDTO.setDayOfWeek(day.getDayOfWeek());
                    dayDTO.setEnabled(day.getIsEnabled());

                    if (day.getSubscriptionDayMeals() != null) {
                        List<SubscriptionDayMealResponseDTO> mealDTOs = new ArrayList<>();
                        for (SubscriptionDayMeal meal : day.getSubscriptionDayMeals()) {
                            SubscriptionDayMealResponseDTO mealDTO = new SubscriptionDayMealResponseDTO();
                            mealDTO.setSetId(meal.getMealSet().getSetId());
                            mealDTO.setMealSetName(meal.getMealSet().getName());
                            mealDTO.setMealSetType(meal.getMealSet().getType().name());
                            mealDTO.setQuantity(meal.getQuantity());
                            mealDTO.setUnitPrice(meal.getUnitPrice());
                            mealDTOs.add(mealDTO);
                        }
                        dayDTO.setMeals(mealDTOs);
                    }
                    dayDTOs.add(dayDTO);
                }
                dto.setSchedule(dayDTOs);
            }

            return dto;
        } catch (Exception e) {
            System.err.println("Error converting subscription to DTO: " + e.getMessage());
            e.printStackTrace();
            // Return at least basic info
            SubscriptionResponseDTO dto = new SubscriptionResponseDTO();
            dto.setSubscriptionId(subscription.getSubscriptionId());
            dto.setStatus(subscription.getStatus().name());
            dto.setStartDate(subscription.getStartDate());
            dto.setEndDate(subscription.getEndDate());
            dto.setTotalAmount(subscription.getTotalAmount());
            return dto;
        }
    }

    private String generateSubscriptionId() {
        return "SUB" + LocalDate.now().getYear() +
                String.format("%02d", LocalDate.now().getMonthValue()) +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Method to get subscriptions by status for admin purposes
    public List<SubscriptionResponseDTO> getSubscriptionsByStatus(Subscription.SubscriptionStatus status) {
        List<Subscription> subscriptions = subscriptionRepository.findByStatus(status);
        return subscriptions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Method to complete subscriptions that have reached end date
    @Transactional
    public void completeExpiredSubscriptions() {
        LocalDate today = LocalDate.now();
        List<Subscription> expiredSubscriptions = subscriptionRepository.findAll().stream()
                .filter(sub -> sub.getEndDate().isBefore(today))
                .filter(sub -> sub.getStatus() == Subscription.SubscriptionStatus.ACTIVE)
                .collect(Collectors.toList());

        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus(Subscription.SubscriptionStatus.COMPLETED);
        }

        if (!expiredSubscriptions.isEmpty()) {
            subscriptionRepository.saveAll(expiredSubscriptions);
            System.out.println("Completed " + expiredSubscriptions.size() + " expired subscriptions");
        }
    }
}