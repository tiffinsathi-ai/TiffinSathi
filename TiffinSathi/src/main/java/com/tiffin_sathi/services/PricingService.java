package com.tiffin_sathi.services;

import com.tiffin_sathi.dtos.SubscriptionRequestDTO;
import com.tiffin_sathi.model.MealPackage;
import com.tiffin_sathi.model.MealSet;
import com.tiffin_sathi.repository.MealSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class PricingService {

    @Autowired
    private MealSetRepository mealSetRepository;

    public PricingResult calculatePricing(SubscriptionRequestDTO request, MealPackage mealPackage) {
        double subtotal = 0.0;
        int deliveryDaysPerWeek = 0;
        int totalMealsPerWeek = 0;

        // Calculate weekly costs
        for (var day : request.getSchedule()) {
            if (day.getEnabled()) {
                deliveryDaysPerWeek++;
                for (var meal : day.getMeals()) {
                    double mealPrice = calculateMealSetPrice(meal.getSetId(), mealPackage.getPricePerSet());
                    subtotal += mealPrice * meal.getQuantity();
                    totalMealsPerWeek += meal.getQuantity();
                }
            }
        }

        // Calculate for entire duration
        int durationWeeks = mealPackage.getDurationDays() / 7;
        subtotal = subtotal * durationWeeks;

        // Delivery fee: Rs 25 per delivery day
        double deliveryFee = 25.0 * deliveryDaysPerWeek * durationWeeks;

        // Apply discount
        double discount = calculateDiscount(subtotal + deliveryFee, request.getDiscountCode());

        // Calculate tax (13% VAT on taxable amount)
        double taxableAmount = subtotal + deliveryFee - discount;
        double tax = taxableAmount * 0.13;

        double grandTotal = subtotal + deliveryFee + tax - discount;

        return new PricingResult(subtotal, deliveryFee, tax, discount, grandTotal);
    }

    public double calculateMealSetPrice(String setId, double planBasePrice) {
        Optional<MealSet> mealSetOpt = mealSetRepository.findById(setId);
        if (mealSetOpt.isPresent()) {
            MealSet mealSet = mealSetOpt.get();
            // In a real scenario, you might have more complex pricing logic
            // For now, using base price adjustment
            return planBasePrice; // Simplified for now
        }
        return planBasePrice;
    }

    public double calculateMealSetPrice(MealSet mealSet, double planBasePrice) {
        return planBasePrice; // Simplified for now
    }

    private double calculateDiscount(double amount, String discountCode) {
        if (discountCode == null || discountCode.trim().isEmpty()) {
            return 0.0;
        }

        switch (discountCode.toUpperCase()) {
            case "SAVE10":
                return amount * 0.10;
            case "WELCOME15":
                return amount * 0.15;
            case "FIRSTORDER":
                return Math.min(amount, 100.0); // Free delivery up to Rs 100
            case "TIFFIN5":
                return Math.min(amount, 50.0);
            default:
                return 0.0;
        }
    }

    public static class PricingResult {
        private final double subtotal;
        private final double deliveryFee;
        private final double tax;
        private final double discount;
        private final double grandTotal;

        public PricingResult(double subtotal, double deliveryFee, double tax, double discount, double grandTotal) {
            this.subtotal = subtotal;
            this.deliveryFee = deliveryFee;
            this.tax = tax;
            this.discount = discount;
            this.grandTotal = grandTotal;
        }

        // Getters
        public double getSubtotal() { return subtotal; }
        public double getDeliveryFee() { return deliveryFee; }
        public double getTax() { return tax; }
        public double getDiscount() { return discount; }
        public double getGrandTotal() { return grandTotal; }
    }
}