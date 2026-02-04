// SubscriptionEditHistory.java
package com.tiffin_sathi.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_edit_history")
public class SubscriptionEditHistory {

    @Id
    @Column(name = "edit_history_id", length = 50)
    private String editHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "edit_reason", columnDefinition = "TEXT")
    private String editReason;

    @Lob
    @Column(name = "old_schedule", columnDefinition = "LONGTEXT")
    private String oldSchedule;

    @Lob
    @Column(name = "new_schedule", columnDefinition = "LONGTEXT")
    private String newSchedule;

    @Column(name = "additional_amount")
    private Double additionalAmount = 0.0;

    @Column(name = "refund_amount")
    private Double refundAmount = 0.0;

    @Column(name = "payment_id", length = 50)
    private String paymentId;

    @Column(name = "status", length = 50)
    private String status; // INITIATED, PENDING_PAYMENT, COMPLETED, COMPLETED_WITH_REFUND, CANCELLED

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public SubscriptionEditHistory() {}

    // Getters and Setters
    public String getEditHistoryId() { return editHistoryId; }
    public void setEditHistoryId(String editHistoryId) { this.editHistoryId = editHistoryId; }

    public Subscription getSubscription() { return subscription; }
    public void setSubscription(Subscription subscription) { this.subscription = subscription; }

    public String getEditReason() { return editReason; }
    public void setEditReason(String editReason) { this.editReason = editReason; }

    public String getOldSchedule() { return oldSchedule; }
    public void setOldSchedule(String oldSchedule) { this.oldSchedule = oldSchedule; }

    public String getNewSchedule() { return newSchedule; }
    public void setNewSchedule(String newSchedule) { this.newSchedule = newSchedule; }

    public Double getAdditionalAmount() { return additionalAmount; }
    public void setAdditionalAmount(Double additionalAmount) { this.additionalAmount = additionalAmount; }

    public Double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Double refundAmount) { this.refundAmount = refundAmount; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}