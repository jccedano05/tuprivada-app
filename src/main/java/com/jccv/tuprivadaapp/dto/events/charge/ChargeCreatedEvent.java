package com.jccv.tuprivadaapp.dto.events.charge;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ChargeCreatedEvent {

    private String eventId;
    private LocalDateTime occurredAt;
    private Long chargeId;
    private String chargeTitle;
    private String chargeDescription;
    private Double amount;
    private List<Long> residentIds;

    public ChargeCreatedEvent() {
    }

    public ChargeCreatedEvent(Long chargeId,
                              String chargeTitle,
                              String chargeDescription,
                              Double amount,
                              List<Long> residentIds) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.chargeId = chargeId;
        this.chargeTitle = chargeTitle;
        this.chargeDescription = chargeDescription;
        this.amount = amount;
        this.residentIds = residentIds;
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

    public Long getChargeId() {
        return chargeId;
    }

    public void setChargeId(Long chargeId) {
        this.chargeId = chargeId;
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

    public List<Long> getResidentIds() {
        return residentIds;
    }

    public void setResidentIds(List<Long> residentIds) {
        this.residentIds = residentIds;
    }

    @Override
    public String toString() {
        return "ChargeCreatedEvent{" +
                "eventId='" + eventId + '\'' +
                ", occurredAt=" + occurredAt +
                ", chargeId=" + chargeId +
                ", chargeTitle='" + chargeTitle + '\'' +
                ", amount=" + amount +
                ", residentIds=" + residentIds +
                '}';
    }
}
