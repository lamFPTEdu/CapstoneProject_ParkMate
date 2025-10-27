package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Model cho Reservation (Đặt chỗ)
 */
public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private Long id;

    @SerializedName("userId")
    private String userId;

    @SerializedName("vehicleId")
    private String vehicleId;

    @SerializedName("parkingLotId")
    private String parkingLotId;

    @SerializedName("parkingLotName")
    private String parkingLotName;

    @SerializedName("spotId")
    private String spotId;

    @SerializedName("spotName")
    private String spotName;

    @SerializedName("initialFee")
    private int initialFee;

    @SerializedName("totalFee")
    private int totalFee;

    @SerializedName("reservationFee")
    private int reservationFee;

    @SerializedName("reservedFrom")
    private String reservedFrom; // "2024-07-01 10:00:00"

    @SerializedName("reservedUntil")
    private String reservedUntil; // "2024-07-01 11:00:00"

    @SerializedName("status")
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED

    @SerializedName("qrCode")
    private String qrCode; // Base64 string

    @SerializedName("isUsed")
    private boolean isUsed;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Constructor
    public Reservation() {
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(String parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public String getParkingLotName() {
        return parkingLotName;
    }

    public void setParkingLotName(String parkingLotName) {
        this.parkingLotName = parkingLotName;
    }

    public String getSpotId() {
        return spotId;
    }

    public void setSpotId(String spotId) {
        this.spotId = spotId;
    }

    public String getSpotName() {
        return spotName;
    }

    public void setSpotName(String spotName) {
        this.spotName = spotName;
    }

    public int getInitialFee() {
        return initialFee;
    }

    public void setInitialFee(int initialFee) {
        this.initialFee = initialFee;
    }

    public int getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(int totalFee) {
        this.totalFee = totalFee;
    }

    public int getReservationFee() {
        return reservationFee;
    }

    public void setReservationFee(int reservationFee) {
        this.reservationFee = reservationFee;
    }

    public String getReservedUntil() {
        return reservedUntil;
    }

    public void setReservedUntil(String reservedUntil) {
        this.reservedUntil = reservedUntil;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public String getReservedFrom() {
        return reservedFrom;
    }

    public void setReservedFrom(String reservedFrom) {
        this.reservedFrom = reservedFrom;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
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

    // Helper methods
    public String getFormattedFee() {
        return String.format("%,dđ", reservationFee);
    }

    public String getStatusDisplayName() {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "Chờ xác nhận";
            case "CONFIRMED": return "Đã xác nhận";
            case "CANCELLED": return "Đã hủy";
            case "COMPLETED": return "Hoàn thành";
            default: return status;
        }
    }

    public int getStatusColor() {
        if (status == null) return android.R.color.darker_gray;
        switch (status) {
            case "PENDING": return android.R.color.holo_orange_dark;
            case "CONFIRMED": return android.R.color.holo_green_dark;
            case "CANCELLED": return android.R.color.holo_red_dark;
            case "COMPLETED": return android.R.color.holo_blue_dark;
            default: return android.R.color.darker_gray;
        }
    }
}

