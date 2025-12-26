package com.jccv.tuprivadaapp.dto.events.payment;

import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentMarkedAsPaidEvent {

    private String eventId;
    private LocalDateTime occurredAt;
    private Long paymentId;
    private Long residentId;
    private Long userId;
    private String userEmail;
    private String userFirstName;
    private String chargeTitle;
    private String chargeDescription;
    private Double amount;

    public PaymentMarkedAsPaidEvent() {
    }

    public PaymentMarkedAsPaidEvent(Long paymentId,
                                    Long residentId,
                                    Long userId,
                                    String userEmail,
                                    String userFirstName,
                                    String chargeTitle,
                                    String chargeDescription,
                                    Double amount) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.paymentId = paymentId;
        this.residentId = residentId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userFirstName = userFirstName;
        this.chargeTitle = chargeTitle;
        this.chargeDescription = chargeDescription;
        this.amount = amount;
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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "PaymentMarkedAsPaidEvent{" +
                "eventId='" + eventId + '\'' +
                ", occurredAt=" + occurredAt +
                ", paymentId=" + paymentId +
                ", userId=" + userId +
                ", chargeTitle='" + chargeTitle + '\'' +
                ", amount=" + amount +
                '}';
    }
}
