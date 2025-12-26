package com.jccv.tuprivadaapp.dto.events.payment;

import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentCompletedEvent {

    private String eventId;
    private LocalDateTime occurredAt;
    private Long paymentId;
    private Long residentId;
    private Long userId;
    private String userEmail;
    private String userFirstName;
    private Double amount;
    private String currency;
    private String chargeTitle;
    private String chargeDescription;

    public PaymentCompletedEvent() {
    }

    public PaymentCompletedEvent(Long paymentId,
                                 Long residentId,
                                 Long userId,
                                 String userEmail,
                                 String userFirstName,
                                 Double amount,
                                 String currency,
                                 String chargeTitle,
                                 String chargeDescription) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.paymentId = paymentId;
        this.residentId = residentId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userFirstName = userFirstName;
        this.amount = amount;
        this.currency = currency;
        this.chargeTitle = chargeTitle;
        this.chargeDescription = chargeDescription;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getResidentId() {
        return residentId;
    }

    public void setResidentId(Long residentId) {
        this.residentId = residentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getChargeTitle() {
        return chargeTitle;
    }

    public void setChargeTitle(String chargeTitle) {
        this.chargeTitle = chargeTitle;
    }

    public String getChargeDescription() {
        return chargeDescription;
    }

    public void setChargeDescription(String chargeDescription) {
        this.chargeDescription = chargeDescription;
    }

    @Override
    public String toString() {
        return "PaymentCompletedEvent{" +
                "eventId='" + eventId + '\'' +
                ", occurredAt=" + occurredAt +
                ", paymentId=" + paymentId +
                ", residentId=" + residentId +
                ", userId=" + userId +
                ", userEmail='" + userEmail + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", chargeTitle='" + chargeTitle + '\'' +
                '}';
    }
}
