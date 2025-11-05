package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Model cho parking session (lịch sử đỗ xe)
 */
public class ParkingSession implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("licensePlate")
    private String licensePlate;

    @SerializedName("entryTime")
    private String entryTime;

    @SerializedName("exitTime")
    private String exitTime;

    @SerializedName("durationMinute")
    private Integer durationMinute;

    @SerializedName("totalAmount")
    private Long totalAmount;

    @SerializedName("status")
    private String status; // ACTIVE, COMPLETED

    @SerializedName("syncStatus")
    private String syncStatus;

    @SerializedName("syncedFromLocal")
    private String syncedFromLocal;

    @SerializedName("note")
    private String note;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("lotId")
    private Long lotId;

    @SerializedName("cardUUID")
    private String cardUUID;

    @SerializedName("pricingRuleId")
    private Long pricingRuleId;

    @SerializedName("referenceId")
    private String referenceId;

    @SerializedName("referenceType")
    private String referenceType; // WALK_IN, RESERVATION, SUBSCRIPTION

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }

    public String getExitTime() {
        return exitTime;
    }

    public void setExitTime(String exitTime) {
        this.exitTime = exitTime;
    }

    public Integer getDurationMinute() {
        return durationMinute;
    }

    public void setDurationMinute(Integer durationMinute) {
        this.durationMinute = durationMinute;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getSyncedFromLocal() {
        return syncedFromLocal;
    }

    public void setSyncedFromLocal(String syncedFromLocal) {
        this.syncedFromLocal = syncedFromLocal;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Long getLotId() {
        return lotId;
    }

    public void setLotId(Long lotId) {
        this.lotId = lotId;
    }

    public String getCardUUID() {
        return cardUUID;
    }

    public void setCardUUID(String cardUUID) {
        this.cardUUID = cardUUID;
    }

    public Long getPricingRuleId() {
        return pricingRuleId;
    }

    public void setPricingRuleId(Long pricingRuleId) {
        this.pricingRuleId = pricingRuleId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }
}

