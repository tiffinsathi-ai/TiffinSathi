package com.tiffin_sathi.services;

import com.tiffin_sathi.dtos.*;
import com.tiffin_sathi.model.*;
import com.tiffin_sathi.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubscriptionEditService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private MealPackageRepository mealPackageRepository;

    @Autowired
    private MealSetRepository mealSetRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PricingService pricingService;

    @Autowired
    private SubscriptionEditHistoryRepository editHistoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final double DELIVERY_FEE_PER_DAY = 25.0;
    private static final double TAX_RATE = 0.13;

    // Add missing method: completeEditAfterPayment
    @Transactional
    public EditSubscriptionResponseDTO completeEditAfterPayment(String paymentId) {
        try {
            System.out.println("Completing edit after payment for payment ID: " + paymentId);

            // Find payment
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

            // Verify payment is completed
            if (payment.getPaymentStatus() != Payment.PaymentStatus.COMPLETED) {
                throw new RuntimeException("Payment is not completed yet");
            }

            // Find edit history by payment ID
            List<SubscriptionEditHistory> editHistories = editHistoryRepository.findByPaymentId(paymentId);

            if (editHistories.isEmpty()) {
                throw new RuntimeException("No edit history found for payment: " + paymentId);
            }

            SubscriptionEditHistory editHistory = editHistories.get(0);
            Subscription subscription = editHistory.getSubscription();

            // Update edit history status
            editHistory.setStatus("COMPLETED");
            editHistory.setCompletedAt(LocalDateTime.now());
            editHistoryRepository.save(editHistory);

            System.out.println("Updated edit history status to COMPLETED for: " + editHistory.getEditHistoryId());

            // Apply the edit to subscription
            applySubscriptionEditFromHistory(editHistory, subscription);

            // Send notification
            sendEditCompletionNotification(subscription, editHistory);

            // Prepare response
            EditSubscriptionResponseDTO response = new EditSubscriptionResponseDTO();
            response.setSubscriptionId(subscription.getSubscriptionId());
            response.setStatus(subscription.getStatus().name());
            response.setEditStatus("COMPLETED");
            response.setEditedAt(LocalDateTime.now());
            response.setEditHistoryId(editHistory.getEditHistoryId());

            // Set refund amount if applicable
            if (editHistory.getRefundAmount() != null && editHistory.getRefundAmount() > 0) {
                response.setRefundAmount(editHistory.getRefundAmount());
                response.setMessage("Subscription updated successfully! A refund of Rs. " +
                        String.format("%.2f", editHistory.getRefundAmount()) +
                        " will be processed to your original payment method within 5-7 business days. " +
                        "Please contact the vendor if you have any questions. " +
                        "Vendor: " + subscription.getMealPackage().getVendor().getBusinessName() +
                        ", Phone: " + subscription.getMealPackage().getVendor().getPhone());
            } else if (editHistory.getAdditionalAmount() != null && editHistory.getAdditionalAmount() > 0) {
                response.setAdditionalPayment(editHistory.getAdditionalAmount());
                response.setMessage("Subscription updated successfully with additional payment of Rs. " +
                        String.format("%.2f", editHistory.getAdditionalAmount()));
            } else {
                response.setMessage("Subscription updated successfully with no additional payment.");
            }

            // Set vendor contact details
            if (subscription.getMealPackage() != null && subscription.getMealPackage().getVendor() != null) {
                Vendor vendor = subscription.getMealPackage().getVendor();
                response.setVendorPhone(vendor.getPhone());
                response.setVendorName(vendor.getBusinessName());
            }

            return response;

        } catch (Exception e) {
            System.err.println("Error completing edit after payment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to complete edit after payment: " + e.getMessage());
        }
    }

    private void applySubscriptionEditFromHistory(SubscriptionEditHistory editHistory, Subscription subscription) {
        try {
            System.out.println("Applying edit to subscription: " + subscription.getSubscriptionId());

            // Parse new schedule from JSON
            List<SubscriptionDayDTO> newSchedule = parseScheduleFromJson(editHistory.getNewSchedule());

            if (newSchedule == null || newSchedule.isEmpty()) {
                throw new RuntimeException("No valid schedule found in edit history");
            }

            // Clear existing subscription days
            subscription.getSubscriptionDays().clear();

            // Create new subscription days
            createNewSubscriptionDaysFromDTO(subscription, newSchedule);

            // Update subscription total amount
            updateSubscriptionTotalAmountFromHistory(subscription, editHistory);

            // Save updated subscription
            subscriptionRepository.save(subscription);

            // Regenerate orders
            regenerateOrdersForEditFromHistory(subscription, editHistory);

            System.out.println("Successfully applied edit to subscription: " + subscription.getSubscriptionId());

        } catch (Exception e) {
            System.err.println("Error applying edit from history: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to apply subscription edit: " + e.getMessage());
        }
    }

    private List<SubscriptionDayDTO> parseScheduleFromJson(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<List<SubscriptionDayDTO>>() {});
        } catch (Exception e) {
            System.err.println("Error parsing schedule JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void createNewSubscriptionDaysFromDTO(Subscription subscription, List<SubscriptionDayDTO> newSchedule) {
        if (newSchedule == null || newSchedule.isEmpty()) {
            throw new RuntimeException("New schedule is required");
        }

        for (SubscriptionDayDTO dayDTO : newSchedule) {
            SubscriptionDay subscriptionDay = new SubscriptionDay();
            subscriptionDay.setSubscription(subscription);
            subscriptionDay.setDayOfWeek(dayDTO.getDayOfWeek());
            subscriptionDay.setIsEnabled(dayDTO.getEnabled());

            if (dayDTO.getEnabled() && dayDTO.getMeals() != null && !dayDTO.getMeals().isEmpty()) {
                List<SubscriptionDayMeal> dayMeals = new ArrayList<>();
                for (SubscriptionDayMealDTO mealDTO : dayDTO.getMeals()) {
                    MealSet mealSet = mealSetRepository.findById(mealDTO.getSetId())
                            .orElseThrow(() -> new RuntimeException("Meal set not found: " + mealDTO.getSetId()));

                    SubscriptionDayMeal dayMeal = new SubscriptionDayMeal();
                    dayMeal.setSubscriptionDay(subscriptionDay);
                    dayMeal.setMealSet(mealSet);
                    dayMeal.setQuantity(mealDTO.getQuantity());
                    dayMeal.setUnitPrice(pricingService.calculateMealSetPrice(mealSet,
                            subscription.getMealPackage().getPricePerSet()));

                    dayMeals.add(dayMeal);
                }
                subscriptionDay.setSubscriptionDayMeals(dayMeals);
            }
            subscription.getSubscriptionDays().add(subscriptionDay);
        }
    }

    private void updateSubscriptionTotalAmountFromHistory(Subscription subscription, SubscriptionEditHistory editHistory) {
        try {
            if (editHistory.getAdditionalAmount() != null && editHistory.getAdditionalAmount() > 0) {
                // Update total amount with additional payment
                subscription.setTotalAmount(subscription.getTotalAmount() + editHistory.getAdditionalAmount());
                System.out.println("Updated subscription amount by: " + editHistory.getAdditionalAmount() +
                        " New total: " + subscription.getTotalAmount());
            }
        } catch (Exception e) {
            System.err.println("Error updating subscription amount: " + e.getMessage());
        }
    }

    private void regenerateOrdersForEditFromHistory(Subscription subscription, SubscriptionEditHistory editHistory) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.isAfter(subscription.getStartDate()) ? today : subscription.getStartDate();

            // Delete future orders
            deleteFutureOrdersForSubscription(subscription.getSubscriptionId());

            // Regenerate orders from today/start date
            generateOrdersForEditPeriod(subscription, startDate);

        } catch (Exception e) {
            System.err.println("Error regenerating orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteFutureOrdersForSubscription(String subscriptionId) {
        try {
            LocalDate today = LocalDate.now();

            // Find future orders using the repository method
            List<Order> futureOrders = orderRepository.findBySubscriptionSubscriptionId(subscriptionId).stream()
                    .filter(order -> !order.getDeliveryDate().isBefore(today))
                    .filter(order -> order.getStatus() == Order.OrderStatus.PENDING ||
                            order.getStatus() == Order.OrderStatus.CONFIRMED)
                    .collect(Collectors.toList());

            if (!futureOrders.isEmpty()) {
                orderRepository.deleteAll(futureOrders);
                System.out.println("Deleted " + futureOrders.size() + " future orders for subscription: " + subscriptionId);
            }
        } catch (Exception e) {
            System.err.println("Error deleting future orders: " + e.getMessage());
        }
    }

    private void generateOrdersForEditPeriod(Subscription subscription, LocalDate startDate) {
        try {
            LocalDate endDate = subscription.getEndDate();
            LocalDate currentDate = startDate;
            List<Order> newOrders = new ArrayList<>();

            while (!currentDate.isAfter(endDate)) {
                DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
                String dayName = dayOfWeek.name();

                // Check if this day is enabled in subscription
                boolean isDayEnabled = subscription.getSubscriptionDays().stream()
                        .anyMatch(day -> day.getDayOfWeek().equals(dayName) && day.getIsEnabled());

                if (isDayEnabled) {
                    Order order = createOrderForEdit(subscription, currentDate);
                    if (order != null) {
                        newOrders.add(order);
                    }
                }

                currentDate = currentDate.plusDays(1);
            }

            if (!newOrders.isEmpty()) {
                orderRepository.saveAll(newOrders);
                System.out.println("Generated " + newOrders.size() + " new orders for edited subscription: " +
                        subscription.getSubscriptionId());
            }

        } catch (Exception e) {
            System.err.println("Error generating orders for edit period: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Order createOrderForEdit(Subscription subscription, LocalDate deliveryDate) {
        try {
            Order order = new Order();
            order.setSubscription(subscription);
            order.setDeliveryDate(deliveryDate);
            order.setStatus(Order.OrderStatus.PENDING);
            order.setPreferredDeliveryTime(subscription.getPreferredDeliveryTime());
            order.setDeliveryAddress(subscription.getDeliveryAddress());
            order.setSpecialInstructions(subscription.getSpecialInstructions());

            // Generate order meals based on subscription days
            String dayName = deliveryDate.getDayOfWeek().name();
            Optional<SubscriptionDay> subscriptionDayOpt = subscription.getSubscriptionDays().stream()
                    .filter(day -> day.getDayOfWeek().equals(dayName) && day.getIsEnabled())
                    .findFirst();

            if (subscriptionDayOpt.isPresent()) {
                SubscriptionDay subscriptionDay = subscriptionDayOpt.get();

                if (subscriptionDay.getSubscriptionDayMeals() != null &&
                        !subscriptionDay.getSubscriptionDayMeals().isEmpty()) {

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
            }

            return null;

        } catch (Exception e) {
            System.err.println("Error creating order for edit: " + e.getMessage());
            return null;
        }
    }

    private void sendEditCompletionNotification(Subscription subscription, SubscriptionEditHistory editHistory) {
        try {
            String userSubject = "Subscription Update Completed - TiffinSathi";

            String userMessage;
            if (editHistory.getRefundAmount() != null && editHistory.getRefundAmount() > 0) {
                userMessage = String.format(
                        "Dear %s,\n\n" +
                                "Your subscription has been updated successfully!\n\n" +
                                "Subscription ID: %s\n" +
                                "Package: %s\n" +
                                "Refund Amount: Rs. %.2f\n" +
                                "Reason for Change: %s\n\n" +
                                "Important: A refund of Rs. %.2f will be processed to your original payment method within 5-7 business days.\n\n" +
                                "If you have any questions, please contact:\n" +
                                "Vendor: %s\n" +
                                "Phone: %s\n" +
                                "Email: %s\n\n" +
                                "Thank you for choosing TiffinSathi!",
                        subscription.getUser().getUserName(),
                        subscription.getSubscriptionId(),
                        subscription.getMealPackage().getName(),
                        editHistory.getRefundAmount(),
                        editHistory.getEditReason(),
                        editHistory.getRefundAmount(),
                        subscription.getMealPackage().getVendor().getBusinessName(),
                        subscription.getMealPackage().getVendor().getPhone(),
                        subscription.getMealPackage().getVendor().getBusinessEmail()
                );
            } else if (editHistory.getAdditionalAmount() != null && editHistory.getAdditionalAmount() > 0) {
                userMessage = String.format(
                        "Dear %s,\n\n" +
                                "Your subscription has been updated successfully!\n\n" +
                                "Subscription ID: %s\n" +
                                "Package: %s\n" +
                                "Additional Payment: Rs. %.2f\n" +
                                "Reason for Change: %s\n\n" +
                                "Your new schedule is now active.\n\n" +
                                "Thank you for choosing TiffinSathi!",
                        subscription.getUser().getUserName(),
                        subscription.getSubscriptionId(),
                        subscription.getMealPackage().getName(),
                        editHistory.getAdditionalAmount(),
                        editHistory.getEditReason()
                );
            } else {
                userMessage = String.format(
                        "Dear %s,\n\n" +
                                "Your subscription has been updated successfully!\n\n" +
                                "Subscription ID: %s\n" +
                                "Package: %s\n" +
                                "Reason for Change: %s\n\n" +
                                "Your new schedule is now active.\n\n" +
                                "Thank you for choosing TiffinSathi!",
                        subscription.getUser().getUserName(),
                        subscription.getSubscriptionId(),
                        subscription.getMealPackage().getName(),
                        editHistory.getEditReason()
                );
            }

            emailService.sendEmail(subscription.getUser().getEmail(), userSubject, userMessage);

        } catch (Exception e) {
            System.err.println("Failed to send edit completion notification: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public EditSubscriptionResponseDTO calculateEditPrice(EditSubscriptionRequestDTO request, String userEmail) {
        try {
            // Get existing subscription
            Subscription subscription = subscriptionRepository.findByIdWithUser(request.getSubscriptionId())
                    .orElseThrow(() -> new RuntimeException("Subscription not found: " + request.getSubscriptionId()));

            // Verify user owns this subscription
            if (!subscription.getUser().getEmail().equals(userEmail)) {
                throw new RuntimeException("You are not authorized to edit this subscription");
            }

            // Check if subscription can be edited
            if (!canSubscriptionBeEdited(subscription)) {
                throw new RuntimeException("This subscription cannot be edited. Status: " + subscription.getStatus());
            }

            LocalDate today = LocalDate.now();
            LocalDate startEditDate = today.isAfter(subscription.getStartDate()) ? today : subscription.getStartDate();
            LocalDate endDate = subscription.getEndDate();

            // Calculate remaining days
            long remainingDays = java.time.temporal.ChronoUnit.DAYS.between(startEditDate, endDate) + 1;
            if (remainingDays <= 0) {
                throw new RuntimeException("Subscription has already ended or has no remaining days");
            }

            // Calculate remaining weeks (ceil division)
            int remainingWeeks = (int) Math.ceil(remainingDays / 7.0);

            // Calculate old cost for remaining period
            double oldCostForRemaining = calculateOldCostForRemaining(subscription, startEditDate, endDate);

            // Calculate new cost for remaining period
            double newCostForRemaining = calculateNewCostForRemaining(subscription, request.getNewSchedule(),
                    startEditDate, endDate);

            System.out.println("Price Calculation Debug:");
            System.out.println("Remaining Days: " + remainingDays);
            System.out.println("Remaining Weeks: " + remainingWeeks);
            System.out.println("Old Cost: " + oldCostForRemaining);
            System.out.println("New Cost: " + newCostForRemaining);

            // Calculate difference (negative means refund, positive means additional payment)
            double priceDifference = newCostForRemaining - oldCostForRemaining;

            // Prepare response
            EditSubscriptionResponseDTO response = new EditSubscriptionResponseDTO();
            response.setSubscriptionId(subscription.getSubscriptionId());

            // Set additional payment or refund based on difference
            if (priceDifference > 0) {
                response.setAdditionalPayment(priceDifference);
                response.setRefundAmount(0.0);
                response.setEditStatus("PENDING_PAYMENT");
                response.setMessage("Additional payment of Rs. " + String.format("%.2f", priceDifference) +
                        " required for the changes.");
            } else if (priceDifference < 0) {
                response.setAdditionalPayment(0.0);
                response.setRefundAmount(Math.abs(priceDifference));
                response.setEditStatus("REFUND_REQUIRED");
                response.setMessage("You will receive a refund of Rs. " + String.format("%.2f", Math.abs(priceDifference)) +
                        " for the changes. The refund will be processed within 5-7 business days.");
            } else {
                response.setAdditionalPayment(0.0);
                response.setRefundAmount(0.0);
                response.setEditStatus("PROCESSED");
                response.setMessage("No price difference. Changes can be applied immediately.");
            }

            // Set cost breakdown for UI
            response.setOldCost(oldCostForRemaining);
            response.setNewCost(newCostForRemaining);
            response.setEditedAt(LocalDateTime.now());

            return response;

        } catch (Exception e) {
            System.err.println("Error calculating edit price: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to calculate edit price: " + e.getMessage());
        }
    }

    @Transactional
    public EditSubscriptionResponseDTO applySubscriptionEdit(EditSubscriptionRequestDTO request, String userEmail) {
        try {
            // Get existing subscription
            Subscription oldSubscription = subscriptionRepository.findByIdWithUser(request.getSubscriptionId())
                    .orElseThrow(() -> new RuntimeException("Subscription not found: " + request.getSubscriptionId()));

            // Verify user owns this subscription
            if (!oldSubscription.getUser().getEmail().equals(userEmail)) {
                throw new RuntimeException("You are not authorized to edit this subscription");
            }

            // Check if subscription can be edited
            if (!canSubscriptionBeEdited(oldSubscription)) {
                throw new RuntimeException("This subscription cannot be edited. Status: " + oldSubscription.getStatus());
            }

            LocalDate today = LocalDate.now();
            LocalDate startEditDate = today.isAfter(oldSubscription.getStartDate()) ? today : oldSubscription.getStartDate();

            // Calculate price difference
            EditSubscriptionResponseDTO priceCalculation = calculateEditPrice(request, userEmail);

            // Create edit history record
            SubscriptionEditHistory editHistory = createEditHistory(oldSubscription, request, priceCalculation);

            // If no additional payment required (price difference <= 0), update immediately
            if (priceCalculation.getAdditionalPayment() == null || priceCalculation.getAdditionalPayment() <= 0) {
                // Update subscription with changes
                Subscription updatedSubscription = updateSubscriptionWithChanges(oldSubscription, request, startEditDate);

                // Update edit history
                editHistory.setStatus("COMPLETED");
                editHistory.setCompletedAt(LocalDateTime.now());
                editHistoryRepository.save(editHistory);

                // Send refund notification if applicable
                if (priceCalculation.getRefundAmount() != null && priceCalculation.getRefundAmount() > 0) {
                    sendRefundNotification(updatedSubscription, priceCalculation.getRefundAmount(), request.getEditReason());
                }

                // Send completion email
                sendEditCompletionNotification(updatedSubscription, editHistory);

                // Prepare response
                EditSubscriptionResponseDTO response = new EditSubscriptionResponseDTO();
                response.setSubscriptionId(updatedSubscription.getSubscriptionId());
                response.setStatus(updatedSubscription.getStatus().name());
                response.setRefundAmount(priceCalculation.getRefundAmount());
                response.setEditStatus("COMPLETED");

                if (priceCalculation.getRefundAmount() != null && priceCalculation.getRefundAmount() > 0) {
                    response.setMessage("Subscription updated successfully! A refund of Rs. " +
                            String.format("%.2f", priceCalculation.getRefundAmount()) +
                            " will be processed to your original payment method within 5-7 business days. " +
                            "Please contact the vendor if you have any questions. " +
                            "Vendor: " + updatedSubscription.getMealPackage().getVendor().getBusinessName() +
                            ", Phone: " + updatedSubscription.getMealPackage().getVendor().getPhone());
                } else {
                    response.setMessage("Subscription updated successfully with no additional payment.");
                }

                response.setEditedAt(LocalDateTime.now());
                response.setEditHistoryId(editHistory.getEditHistoryId());

                // Set vendor contact details
                if (updatedSubscription.getMealPackage() != null && updatedSubscription.getMealPackage().getVendor() != null) {
                    Vendor vendor = updatedSubscription.getMealPackage().getVendor();
                    response.setVendorPhone(vendor.getPhone());
                    response.setVendorName(vendor.getBusinessName());
                }

                return response;
            }

            // If additional payment required, mark as pending payment
            editHistory.setStatus("PENDING_PAYMENT");
            editHistory.setAdditionalAmount(priceCalculation.getAdditionalPayment());
            editHistory = editHistoryRepository.save(editHistory);

            // Create payment record for the edit
            Payment payment = paymentService.createEditPayment(oldSubscription,
                    request.getPaymentMethod() != null ? request.getPaymentMethod() : "ESEWA",
                    priceCalculation.getAdditionalPayment(),
                    request.getEditReason());

            // Link payment to edit history
            editHistory.setPaymentId(payment.getPaymentId());
            editHistoryRepository.save(editHistory);

            // Prepare response
            EditSubscriptionResponseDTO response = new EditSubscriptionResponseDTO();
            response.setSubscriptionId(oldSubscription.getSubscriptionId());
            response.setStatus(oldSubscription.getStatus().name());
            response.setAdditionalPayment(priceCalculation.getAdditionalPayment());
            response.setEditStatus("PENDING_PAYMENT");
            response.setMessage("Please complete the payment of Rs. " +
                    String.format("%.2f", priceCalculation.getAdditionalPayment()) + " to apply changes.");
            response.setEditedAt(LocalDateTime.now());
            response.setEditHistoryId(editHistory.getEditHistoryId());
            response.setPaymentId(payment.getPaymentId());

            return response;

        } catch (Exception e) {
            System.err.println("Error applying subscription edit: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to apply subscription edit: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentInitiationResponse processEditPayment(String subscriptionId, String userEmail,
                                                        String paymentMethod, Double amount) {
        try {
            System.out.println("Processing edit payment for subscription: " + subscriptionId +
                    ", user: " + userEmail + ", amount: " + amount);

            // Get subscription
            Subscription subscription = subscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));

            // Verify user
            if (!subscription.getUser().getEmail().equals(userEmail)) {
                throw new RuntimeException("You are not authorized to process this payment");
            }

            // Find pending edit history
            List<SubscriptionEditHistory> pendingEditHistories = editHistoryRepository
                    .findBySubscriptionSubscriptionIdAndStatus(subscriptionId, "PENDING_PAYMENT");

            if (pendingEditHistories.isEmpty()) {
                // If no pending edit history, check for initiated ones
                pendingEditHistories = editHistoryRepository
                        .findBySubscriptionSubscriptionIdAndStatus(subscriptionId, "INITIATED");
            }

            if (pendingEditHistories.isEmpty()) {
                throw new RuntimeException("No pending edit found for subscription: " + subscriptionId);
            }

            SubscriptionEditHistory editHistory = pendingEditHistories.get(0);

            // Verify amount matches
            if (editHistory.getAdditionalAmount() != null &&
                    !editHistory.getAdditionalAmount().equals(amount)) {
                System.out.println("Warning: Payment amount " + amount +
                        " doesn't match edit history amount " + editHistory.getAdditionalAmount());
                // Update edit history amount
                editHistory.setAdditionalAmount(amount);
                editHistoryRepository.save(editHistory);
            }

            // Check for existing edit payment
            List<Payment> pendingEditPayments = paymentRepository.findEditPaymentBySubscriptionAndStatus(
                    subscriptionId, Payment.PaymentStatus.PENDING);

            Payment payment;
            if (!pendingEditPayments.isEmpty()) {
                // Use existing pending edit payment
                payment = pendingEditPayments.get(0);
                payment.setPaymentMethod(Payment.PaymentMethod.valueOf(paymentMethod.toUpperCase()));
                payment.setAmount(amount);
                payment = paymentRepository.save(payment);
                System.out.println("Using existing edit payment: " + payment.getPaymentId());
            } else {
                // Create new edit payment
                payment = paymentService.createEditPayment(subscription, paymentMethod, amount,
                        "SUBSCRIPTION_EDIT_PAYMENT");
                System.out.println("Created new edit payment: " + payment.getPaymentId());
            }

            // Link edit history to payment
            editHistory.setPaymentId(payment.getPaymentId());
            editHistoryRepository.save(editHistory);

            // Initiate payment
            return paymentService.initiateOnlinePaymentForEdit(
                    subscriptionId,
                    paymentMethod,
                    amount,
                    payment.getPaymentId()
            );

        } catch (Exception e) {
            System.err.println("Error processing edit payment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process edit payment: " + e.getMessage());
        }
    }

    public List<SubscriptionEditHistory> getEditHistory(String subscriptionId, String userEmail) {
        try {
            // Verify subscription exists and user has access
            Subscription subscription = subscriptionRepository.findByIdWithUser(subscriptionId)
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));

            if (!subscription.getUser().getEmail().equals(userEmail) &&
                    !subscription.getMealPackage().getVendor().getBusinessEmail().equals(userEmail)) {
                throw new RuntimeException("You are not authorized to view this history");
            }

            return editHistoryRepository.findBySubscriptionSubscriptionIdOrderByCreatedAtDesc(subscriptionId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get edit history: " + e.getMessage());
        }
    }

    private boolean canSubscriptionBeEdited(Subscription subscription) {
        // Only active subscriptions can be edited
        return subscription.getStatus() == Subscription.SubscriptionStatus.ACTIVE;
    }

    private double calculateOldCostForRemaining(Subscription subscription, LocalDate startDate, LocalDate endDate) {
        double totalCost = 0.0;
        LocalDate current = startDate;

        // Get price per set from subscription
        double pricePerSet = subscription.getMealPackage().getPricePerSet();

        while (!current.isAfter(endDate)) {
            String dayOfWeek = current.getDayOfWeek().name();

            // Find subscription day for this day
            Optional<SubscriptionDay> subscriptionDay = subscription.getSubscriptionDays().stream()
                    .filter(day -> day.getDayOfWeek().equals(dayOfWeek) && day.getIsEnabled())
                    .findFirst();

            if (subscriptionDay.isPresent()) {
                for (SubscriptionDayMeal meal : subscriptionDay.get().getSubscriptionDayMeals()) {
                    totalCost += meal.getUnitPrice() * meal.getQuantity();
                }
            }

            current = current.plusDays(1);
        }

        return totalCost;
    }

    private double calculateNewCostForRemaining(Subscription subscription, List<SubscriptionDayDTO> newSchedule,
                                                LocalDate startDate, LocalDate endDate) {
        if (newSchedule == null || newSchedule.isEmpty()) {
            return 0.0;
        }

        double totalCost = 0.0;
        LocalDate current = startDate;
        double basePricePerSet = subscription.getMealPackage().getPricePerSet();

        // Create a map for quick lookup of meal sets
        Map<String, Double> mealSetPriceCache = new HashMap<>();

        while (!current.isAfter(endDate)) {
            String dayOfWeek = current.getDayOfWeek().name();

            // Find the day in new schedule
            Optional<SubscriptionDayDTO> dayDTO = newSchedule.stream()
                    .filter(d -> d.getDayOfWeek().equals(dayOfWeek))
                    .findFirst();

            if (dayDTO.isPresent() && dayDTO.get().getEnabled() && dayDTO.get().getMeals() != null) {
                for (SubscriptionDayMealDTO mealDTO : dayDTO.get().getMeals()) {
                    // Get or calculate meal set price
                    Double unitPrice = mealSetPriceCache.get(mealDTO.getSetId());
                    if (unitPrice == null) {
                        MealSet mealSet = mealSetRepository.findById(mealDTO.getSetId())
                                .orElseThrow(() -> new RuntimeException("Meal set not found: " + mealDTO.getSetId()));
                        unitPrice = pricingService.calculateMealSetPrice(mealSet, basePricePerSet);
                        mealSetPriceCache.put(mealDTO.getSetId(), unitPrice);
                    }
                    totalCost += unitPrice * mealDTO.getQuantity();
                }
            }

            current = current.plusDays(1);
        }

        return totalCost;
    }

    private SubscriptionEditHistory createEditHistory(Subscription subscription,
                                                      EditSubscriptionRequestDTO request,
                                                      EditSubscriptionResponseDTO priceCalculation) {
        SubscriptionEditHistory history = new SubscriptionEditHistory();
        history.setEditHistoryId(generateEditHistoryId());
        history.setSubscription(subscription);
        history.setEditReason(request.getEditReason());
        history.setOldSchedule(convertSubscriptionDaysToJson(subscription.getSubscriptionDays()));
        history.setNewSchedule(convertScheduleDTOToJson(request.getNewSchedule()));
        history.setAdditionalAmount(priceCalculation.getAdditionalPayment());
        history.setRefundAmount(priceCalculation.getRefundAmount());
        history.setStatus("INITIATED");
        history.setCreatedAt(LocalDateTime.now());

        return editHistoryRepository.save(history);
    }

    private Subscription updateSubscriptionWithChanges(Subscription subscription,
                                                       EditSubscriptionRequestDTO request,
                                                       LocalDate startEditDate) {
        try {
            // Delete existing subscription days
            subscription.getSubscriptionDays().clear();

            // Create new subscription days from the new schedule
            createNewSubscriptionDays(subscription, request.getNewSchedule());

            // Update subscription total amount
            updateSubscriptionAmount(subscription, request, startEditDate);

            // Save updated subscription
            Subscription updatedSubscription = subscriptionRepository.save(subscription);

            // Delete and regenerate orders
            deleteFutureOrders(subscription.getSubscriptionId());
            regenerateOrdersFromEdit(updatedSubscription, startEditDate);

            return updatedSubscription;

        } catch (Exception e) {
            System.err.println("Error updating subscription: " + e.getMessage());
            throw new RuntimeException("Failed to update subscription: " + e.getMessage());
        }
    }

    private void createNewSubscriptionDays(Subscription subscription, List<SubscriptionDayDTO> newSchedule) {
        if (newSchedule == null || newSchedule.isEmpty()) {
            throw new RuntimeException("New schedule is required");
        }

        for (SubscriptionDayDTO dayDTO : newSchedule) {
            SubscriptionDay subscriptionDay = new SubscriptionDay();
            subscriptionDay.setSubscription(subscription);
            subscriptionDay.setDayOfWeek(dayDTO.getDayOfWeek());
            subscriptionDay.setIsEnabled(dayDTO.getEnabled());

            if (dayDTO.getEnabled() && dayDTO.getMeals() != null && !dayDTO.getMeals().isEmpty()) {
                List<SubscriptionDayMeal> dayMeals = new ArrayList<>();
                for (SubscriptionDayMealDTO mealDTO : dayDTO.getMeals()) {
                    MealSet mealSet = mealSetRepository.findById(mealDTO.getSetId())
                            .orElseThrow(() -> new RuntimeException("Meal set not found: " + mealDTO.getSetId()));

                    SubscriptionDayMeal dayMeal = new SubscriptionDayMeal();
                    dayMeal.setSubscriptionDay(subscriptionDay);
                    dayMeal.setMealSet(mealSet);
                    dayMeal.setQuantity(mealDTO.getQuantity());
                    dayMeal.setUnitPrice(pricingService.calculateMealSetPrice(mealSet,
                            subscription.getMealPackage().getPricePerSet()));

                    dayMeals.add(dayMeal);
                }
                subscriptionDay.setSubscriptionDayMeals(dayMeals);
            }
            subscription.getSubscriptionDays().add(subscriptionDay);
        }
    }

    private void deleteFutureOrders(String subscriptionId) {
        LocalDate today = LocalDate.now();
        List<Order> futureOrders = orderRepository.findBySubscriptionSubscriptionId(subscriptionId).stream()
                .filter(order -> !order.getDeliveryDate().isBefore(today))
                .filter(order -> order.getStatus() == Order.OrderStatus.PENDING ||
                        order.getStatus() == Order.OrderStatus.CONFIRMED)
                .collect(Collectors.toList());

        if (!futureOrders.isEmpty()) {
            orderRepository.deleteAll(futureOrders);
            System.out.println("Deleted " + futureOrders.size() + " future orders for subscription: " + subscriptionId);
        }
    }

    private void regenerateOrdersFromEdit(Subscription subscription, LocalDate startDate) {
        LocalDate endDate = subscription.getEndDate();
        LocalDate currentDate = startDate;

        List<Order> newOrders = new ArrayList<>();

        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            String dayName = dayOfWeek.name();

            // Check if this day is enabled in the new schedule
            boolean isDayEnabled = subscription.getSubscriptionDays().stream()
                    .anyMatch(day -> day.getDayOfWeek().equals(dayName) && day.getIsEnabled());

            if (isDayEnabled) {
                Order order = createOrderFromSubscription(subscription, currentDate);
                if (order != null) {
                    newOrders.add(order);
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        if (!newOrders.isEmpty()) {
            orderRepository.saveAll(newOrders);
            System.out.println("Regenerated " + newOrders.size() + " orders after edit for subscription: " +
                    subscription.getSubscriptionId());
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

        String dayName = deliveryDate.getDayOfWeek().name();
        Optional<SubscriptionDay> subscriptionDayOpt = subscription.getSubscriptionDays().stream()
                .filter(day -> day.getDayOfWeek().equals(dayName) && day.getIsEnabled())
                .findFirst();

        if (subscriptionDayOpt.isPresent()) {
            SubscriptionDay subscriptionDay = subscriptionDayOpt.get();

            if (subscriptionDay.getSubscriptionDayMeals() == null ||
                    subscriptionDay.getSubscriptionDayMeals().isEmpty()) {
                return null;
            }

            List<OrderMeal> orderMeals = new ArrayList<>();
            for (SubscriptionDayMeal subscriptionDayMeal : subscriptionDay.getSubscriptionDayMeals()) {
                OrderMeal orderMeal = new OrderMeal();
                orderMeal.setOrder(order);
                orderMeal.setMealSet(subscriptionDayMeal.getMealSet());
                orderMeal.setQuantity(subscriptionDayMeal.getQuantity());
                orderMeals.add(orderMeal);
            }
            order.setOrderMeals(orderMeals);
        } else {
            return null;
        }

        return order;
    }

    private void updateSubscriptionAmount(Subscription subscription, EditSubscriptionRequestDTO request, LocalDate startEditDate) {
        // Recalculate total amount based on new schedule
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.isAfter(subscription.getStartDate()) ? today : subscription.getStartDate();
        LocalDate endDate = subscription.getEndDate();

        // Calculate new cost for remaining period
        double newCostForRemaining = calculateNewCostForRemaining(subscription,
                request.getNewSchedule(), startDate, endDate);

        // Calculate cost for past days (unchanged) from original subscription
        double pastDaysCost = 0.0;
        LocalDate pastStart = subscription.getStartDate();
        LocalDate pastEnd = startDate.minusDays(1);

        LocalDate current = pastStart;
        while (!current.isAfter(pastEnd)) {
            String dayOfWeek = current.getDayOfWeek().name();
            Optional<SubscriptionDay> oldDay = subscription.getSubscriptionDays().stream()
                    .filter(day -> day.getDayOfWeek().equals(dayOfWeek) && day.getIsEnabled())
                    .findFirst();

            if (oldDay.isPresent()) {
                for (SubscriptionDayMeal meal : oldDay.get().getSubscriptionDayMeals()) {
                    pastDaysCost += meal.getUnitPrice() * meal.getQuantity();
                }
            }
            current = current.plusDays(1);
        }

        subscription.setTotalAmount(pastDaysCost + newCostForRemaining);
    }

    private void sendRefundNotification(Subscription subscription, Double refundAmount, String reason) {
        try {
            String userSubject = "Subscription Updated - Refund Required";
            String userMessage = String.format(
                    "Dear %s,\n\n" +
                            "Your subscription has been updated successfully!\n\n" +
                            "Subscription ID: %s\n" +
                            "Package: %s\n" +
                            "Refund Amount: Rs. %.2f\n" +
                            "Reason for Change: %s\n\n" +
                            "Important: A refund of Rs. %.2f will be processed to your original payment method within 5-7 business days.\n\n" +
                            "If you have any questions, please contact:\n" +
                            "Vendor: %s\n" +
                            "Email: %s\n" +
                            "Phone: %s\n\n" +
                            "Thank you for choosing TiffinSathi!",
                    subscription.getUser().getUserName(),
                    subscription.getSubscriptionId(),
                    subscription.getMealPackage().getName(),
                    refundAmount,
                    reason,
                    refundAmount,
                    subscription.getMealPackage().getVendor().getBusinessName(),
                    subscription.getMealPackage().getVendor().getBusinessEmail(),
                    subscription.getMealPackage().getVendor().getPhone()
            );

            emailService.sendEmail(subscription.getUser().getEmail(), userSubject, userMessage);

            // Also notify vendor
            String vendorSubject = "Subscription Edit - Refund Required: " + subscription.getSubscriptionId();
            String vendorMessage = String.format(
                    "Customer %s has edited their subscription.\n\n" +
                            "Subscription ID: %s\n" +
                            "Customer: %s\n" +
                            "Customer Phone: %s\n" +
                            "Refund Amount: Rs. %.2f\n" +
                            "Reason: %s\n\n" +
                            "Please process the refund to the customer's original payment method within 5-7 business days.",
                    subscription.getUser().getUserName(),
                    subscription.getSubscriptionId(),
                    subscription.getUser().getUserName(),
                    subscription.getUser().getPhoneNumber(),
                    refundAmount,
                    reason
            );

            emailService.sendEmail(
                    subscription.getMealPackage().getVendor().getBusinessEmail(),
                    vendorSubject,
                    vendorMessage
            );

        } catch (Exception e) {
            System.err.println("Failed to send refund notification: " + e.getMessage());
        }
    }

    private String convertSubscriptionDaysToJson(List<SubscriptionDay> subscriptionDays) {
        try {
            return objectMapper.writeValueAsString(subscriptionDays);
        } catch (Exception e) {
            System.err.println("Error converting subscription days to JSON: " + e.getMessage());
            return "[]";
        }
    }

    private String convertScheduleDTOToJson(List<SubscriptionDayDTO> schedule) {
        try {
            return objectMapper.writeValueAsString(schedule);
        } catch (Exception e) {
            System.err.println("Error converting schedule to JSON: " + e.getMessage());
            return "[]";
        }
    }

    private String generateEditHistoryId() {
        return "EDIT" + LocalDateTime.now().getYear() +
                String.format("%02d", LocalDateTime.now().getMonthValue()) +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}