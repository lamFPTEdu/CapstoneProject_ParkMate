package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ParkingSpot implements Serializable {
    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("spotTopLeftX")
    private double spotTopLeftX;

    @SerializedName("spotTopLeftY")
    private double spotTopLeftY;

    @SerializedName("spotWidth")
    private double spotWidth;

    @SerializedName("spotHeight")
    private double spotHeight;

    @SerializedName("status")
    private String status;

    @SerializedName("blockReason")
    private String blockReason;

    @SerializedName("hasSession")
    private boolean hasSession;

    @SerializedName("isAvailableForSubscription")
    private boolean isAvailableForSubscription;

    @SerializedName("subscriptionUnavailabilityReason")
    private String subscriptionUnavailabilityReason;

    // Transient field - không serialize, chỉ dùng để filter
    private transient Long areaId;

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSpotTopLeftX() {
        return spotTopLeftX;
    }

    public void setSpotTopLeftX(double spotTopLeftX) {
        this.spotTopLeftX = spotTopLeftX;
    }

    public double getSpotTopLeftY() {
        return spotTopLeftY;
    }

    public void setSpotTopLeftY(double spotTopLeftY) {
        this.spotTopLeftY = spotTopLeftY;
    }

    public double getSpotWidth() {
        return spotWidth;
    }

    public void setSpotWidth(double spotWidth) {
        this.spotWidth = spotWidth;
    }

    public double getSpotHeight() {
        return spotHeight;
    }

    public void setSpotHeight(double spotHeight) {
        this.spotHeight = spotHeight;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }

    public boolean isHasSession() {
        return hasSession;
    }

    public void setHasSession(boolean hasSession) {
        this.hasSession = hasSession;
    }

    public boolean isAvailableForSubscription() {
        return isAvailableForSubscription;
    }

    public void setAvailableForSubscription(boolean availableForSubscription) {
        isAvailableForSubscription = availableForSubscription;
    }

    public String getSubscriptionUnavailabilityReason() {
        return subscriptionUnavailabilityReason;
    }

    public void setSubscriptionUnavailabilityReason(String subscriptionUnavailabilityReason) {
        this.subscriptionUnavailabilityReason = subscriptionUnavailabilityReason;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }
}

