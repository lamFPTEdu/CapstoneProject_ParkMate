package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Vehicle implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private Long id;

    @SerializedName("licensePlate")
    private String licensePlate;

    @SerializedName("vehicleType")
    private String vehicleType;

    @SerializedName("vehicleBrand")
    private String brand;

    @SerializedName("vehicleModel")
    private String model;

    @SerializedName("vehicleColor")
    private String color;

    @SerializedName("active")
    private boolean isActive;

    @SerializedName("isElectric")
    private boolean isElectric;

    @SerializedName("isDefault")
    private boolean isDefault;

    @SerializedName("vehiclePhotoUrl")
    private String vehiclePhotoUrl;

    @SerializedName("hasSubscriptionInThisParkingLot")
    private boolean hasSubscriptionInThisParkingLot;

    @SerializedName("inReservation")
    private boolean inReservation;

    public Vehicle() {
    }

    public Vehicle(String id, String licensePlate, String vehicleType, String brand, String color, boolean isDefault) {
        this.id = id != null ? Long.parseLong(id) : null;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.brand = brand;
        this.color = color;
        this.isDefault = isDefault;
        this.isActive = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isElectric() {
        return isElectric;
    }

    public void setElectric(boolean electric) {
        isElectric = electric;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getVehiclePhotoUrl() {
        return vehiclePhotoUrl;
    }

    public void setVehiclePhotoUrl(String vehiclePhotoUrl) {
        this.vehiclePhotoUrl = vehiclePhotoUrl;
    }

    public boolean isHasSubscriptionInThisParkingLot() {
        return hasSubscriptionInThisParkingLot;
    }

    public void setHasSubscriptionInThisParkingLot(boolean hasSubscriptionInThisParkingLot) {
        this.hasSubscriptionInThisParkingLot = hasSubscriptionInThisParkingLot;
    }

    public boolean isInReservation() {
        return inReservation;
    }

    public void setInReservation(boolean inReservation) {
        this.inReservation = inReservation;
    }
}
