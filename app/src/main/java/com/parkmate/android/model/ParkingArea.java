package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ParkingArea implements Serializable {
    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("vehicleType")
    private String vehicleType;

    @SerializedName("totalSpots")
    private int totalSpots;

    @SerializedName("areaTopLeftX")
    private double areaTopLeftX;

    @SerializedName("areaTopLeftY")
    private double areaTopLeftY;

    @SerializedName("areaWidth")
    private double areaWidth;

    @SerializedName("areaHeight")
    private double areaHeight;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("supportElectricVehicle")
    private boolean supportElectricVehicle;

    @SerializedName("areaType")
    private String areaType;

    @SerializedName("availableSubscriptionSpots")
    private int availableSubscriptionSpots;

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

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getTotalSpots() {
        return totalSpots;
    }

    public void setTotalSpots(int totalSpots) {
        this.totalSpots = totalSpots;
    }

    public double getAreaTopLeftX() {
        return areaTopLeftX;
    }

    public void setAreaTopLeftX(double areaTopLeftX) {
        this.areaTopLeftX = areaTopLeftX;
    }

    public double getAreaTopLeftY() {
        return areaTopLeftY;
    }

    public void setAreaTopLeftY(double areaTopLeftY) {
        this.areaTopLeftY = areaTopLeftY;
    }

    public double getAreaWidth() {
        return areaWidth;
    }

    public void setAreaWidth(double areaWidth) {
        this.areaWidth = areaWidth;
    }

    public double getAreaHeight() {
        return areaHeight;
    }

    public void setAreaHeight(double areaHeight) {
        this.areaHeight = areaHeight;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isSupportElectricVehicle() {
        return supportElectricVehicle;
    }

    public void setSupportElectricVehicle(boolean supportElectricVehicle) {
        this.supportElectricVehicle = supportElectricVehicle;
    }

    public String getAreaType() {
        return areaType;
    }

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }

    public int getAvailableSubscriptionSpots() {
        return availableSubscriptionSpots;
    }

    public void setAvailableSubscriptionSpots(int availableSubscriptionSpots) {
        this.availableSubscriptionSpots = availableSubscriptionSpots;
    }
}

