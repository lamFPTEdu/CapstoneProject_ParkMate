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

    @SerializedName("vehicleLicensePlate")
    private String vehicleLicensePlate;

    @SerializedName("vehicleType")
    private String vehicleType;

    @SerializedName("parkingLotId")
    private String parkingLotId;

    @SerializedName("parkingLotName")
    private String parkingLotName;

    @SerializedName("initialFee")
    private Integer initialFee;

    @SerializedName("totalFee")
    private Integer totalFee;

    @SerializedName("refundPolicy")
    private RefundPolicy refundPolicy;

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

    public String getVehicleLicensePlate() {
        return vehicleLicensePlate;
    }

    public void setVehicleLicensePlate(String vehicleLicensePlate) {
        this.vehicleLicensePlate = vehicleLicensePlate;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
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

    public Integer getInitialFee() {
        return initialFee;
    }

    public void setInitialFee(Integer initialFee) {
        this.initialFee = initialFee;
    }

    public Integer getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Integer totalFee) {
        this.totalFee = totalFee;
    }

    public RefundPolicy getRefundPolicy() {
        return refundPolicy;
    }

    public void setRefundPolicy(RefundPolicy refundPolicy) {
        this.refundPolicy = refundPolicy;
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
        int fee = 0;
        if (totalFee != null && totalFee > 0) {
            fee = totalFee;
        } else if (initialFee != null) {
            fee = initialFee;
        }
        return String.format("%,dđ", fee);
    }

    public String getStatusDisplayName() {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "Chờ vào bãi";
            case "ACTIVE": return "Đang đỗ xe";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELLED": return "Đã hủy";
            case "EXPIRED": return "Hết hạn";
            default: return status;
        }
    }

    public int getStatusColor() {
        if (status == null) return android.R.color.darker_gray;
        switch (status) {
            case "PENDING": return android.R.color.holo_orange_dark;
            case "ACTIVE": return android.R.color.holo_green_dark;
            case "COMPLETED": return android.R.color.holo_blue_dark;
            case "CANCELLED": return android.R.color.holo_red_dark;
            case "EXPIRED": return android.R.color.darker_gray;
            default: return android.R.color.darker_gray;
        }
    }
}

