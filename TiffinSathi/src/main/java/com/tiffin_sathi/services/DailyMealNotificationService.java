package com.tiffin_sathi.services;

import com.tiffin_sathi.model.Subscription;
import com.tiffin_sathi.repository.OrderRepository;
import com.tiffin_sathi.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DailyMealNotificationService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OrderRepository orderRepository;

    @Scheduled(cron = "0 0 7 * * ?") // Run every day at 7 AM
    public void sendDailyMealNotifications() {
        try {
            LocalDate today = LocalDate.now();
            List<Subscription> activeSubscriptions = subscriptionRepository.findActiveSubscriptionsOnDate(today);

            for (Subscription subscription : activeSubscriptions) {
                sendDailyMealEmail(subscription, today);
            }
        } catch (Exception e) {
            System.err.println("Error sending daily meal notifications: " + e.getMessage());
        }
    }

    private void sendDailyMealEmail(Subscription subscription, LocalDate today) {
        try {
            String subject = "Today's Meal - " + today;
            String message = String.format(
                    "Dear %s,\n\n" +
                            "Here's your meal delivery for today:\n\n" +
                            "Subscription: %s\n" +
                            "Delivery Time: %s\n" +
                            "Delivery Address: %s\n\n" +
                            "Your meals today:\n%s\n\n" +
                            "Thank you for choosing TiffinSathi!",
                    subscription.getUser().getUserName(),
                    subscription.getMealPackage().getName(),
                    subscription.getPreferredDeliveryTime(),
                    subscription.getDeliveryAddress(),
                    getTodaysMealsDescription(subscription, today)
            );

            emailService.sendEmail(subscription.getUser().getEmail(), subject, message);
        } catch (Exception e) {
            System.err.println("Failed to send daily meal email: " + e.getMessage());
        }
    }

    private String getTodaysMealsDescription(Subscription subscription, LocalDate today) {
        // Get today's meals from subscription schedule
        String dayOfWeek = today.getDayOfWeek().toString();

        return subscription.getSubscriptionDays().stream()
                .filter(day -> day.getDayOfWeek().equals(dayOfWeek) && day.getIsEnabled())
                .flatMap(day -> day.getSubscriptionDayMeals().stream())
                .map(meal -> String.format("- %s x%d",
                        meal.getMealSet().getName(),
                        meal.getQuantity()))
                .collect(Collectors.joining("\n"));
    }
}
