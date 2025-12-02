package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ParkingFloor implements Serializable {
    @SerializedName("id")
    private long id;

    @SerializedName("floorNumber")
    private int floorNumber;

    @SerializedName("floorName")
    private String floorName;

    @SerializedName("floorTopLeftX")
    private double floorTopLeftX;

    @SerializedName("floorTopLeftY")
    private double floorTopLeftY;

    @SerializedName("floorWidth")
    private double floorWidth;

    @SerializedName("floorHeight")
    private double floorHeight;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("totalSubscriptionSpots")
    private int totalSubscriptionSpots;

    @SerializedName("availableSubscriptionSpots")
    private int availableSubscriptionSpots;

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public double getFloorTopLeftX() {
        return floorTopLeftX;
    }

    public void setFloorTopLeftX(double floorTopLeftX) {
        this.floorTopLeftX = floorTopLeftX;
    }

    public double getFloorTopLeftY() {
        return floorTopLeftY;
    }

    public void setFloorTopLeftY(double floorTopLeftY) {
        this.floorTopLeftY = floorTopLeftY;
    }

    public double getFloorWidth() {
        return floorWidth;
    }

    public void setFloorWidth(double floorWidth) {
        this.floorWidth = floorWidth;
    }

    public double getFloorHeight() {
        return floorHeight;
    }

    public void setFloorHeight(double floorHeight) {
        this.floorHeight = floorHeight;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getTotalSubscriptionSpots() {
        return totalSubscriptionSpots;
    }

    public void setTotalSubscriptionSpots(int totalSubscriptionSpots) {
        this.totalSubscriptionSpots = totalSubscriptionSpots;
    }

    public int getAvailableSubscriptionSpots() {
        return availableSubscriptionSpots;
    }

    public void setAvailableSubscriptionSpots(int availableSubscriptionSpots) {
        this.availableSubscriptionSpots = availableSubscriptionSpots;
    }
}

