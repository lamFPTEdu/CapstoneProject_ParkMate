package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class SubscriptionPackage implements Serializable {
    @SerializedName("id")
    private long id;

    @SerializedName("lotId")
    private long lotId;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("vehicleType")
    private String vehicleType;

    @SerializedName("durationType")
    private String durationType;

    @SerializedName("durationValue")
    private int durationValue;

    @SerializedName("price")
    private long price;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLotId() {
        return lotId;
    }

    public void setLotId(long lotId) {
        this.lotId = lotId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getDurationType() {
        return durationType;
    }

    public void setDurationType(String durationType) {
        this.durationType = durationType;
    }

    public int getDurationValue() {
        return durationValue;
    }

    public void setDurationValue(int durationValue) {
        this.durationValue = durationValue;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDurationTypeDisplay() {
        if (durationType == null) return "";
        switch (durationType) {
            case "MONTHLY":
                return durationValue + " Tháng";
            case "QUARTERLY":
                return durationValue + " Tháng"; // durationValue is already in months
            case "YEARLY":
                int years = durationValue / 12;
                return years + " Năm";
            default:
                return durationValue + " " + durationType;
        }
    }
}

