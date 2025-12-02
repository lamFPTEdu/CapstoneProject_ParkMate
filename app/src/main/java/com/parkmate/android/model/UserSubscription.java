package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class UserSubscription implements Serializable {
    @SerializedName("id")
    private long id;

    @SerializedName("userId")
    private long userId;

    @SerializedName("vehicleId")
    private long vehicleId;

    @SerializedName("vehicleType")
    private String vehicleType;

    @SerializedName("vehicleLicensePlate")
    private String vehicleLicensePlate;

    @SerializedName("subscriptionPackageId")
    private long subscriptionPackageId;

    @SerializedName("subscriptionPackageName")
    private String subscriptionPackageName;

    @SerializedName("parkingLotId")
    private long parkingLotId;

    @SerializedName("parkingLotName")
    private String parkingLotName;

    @SerializedName("assignedSpotId")
    private long assignedSpotId;

    @SerializedName("assignedSpotName")
    private String assignedSpotName;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    @SerializedName("autoRenew")
    private boolean autoRenew;

    @SerializedName("paidAmount")
    private double paidAmount;

    @SerializedName("paymentTransactionId")
    private String paymentTransactionId;

    @SerializedName("status")
    private String status;

    @SerializedName("cancelledAt")
    private String cancelledAt;

    @SerializedName("cancellationReason")
    private String cancellationReason;

    @SerializedName("refundAmount")
    private Double refundAmount;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("qrCode")
    private String qrCode;

    @SerializedName("daysRemaining")
    private int daysRemaining;

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleLicensePlate() {
        return vehicleLicensePlate;
    }

    public void setVehicleLicensePlate(String vehicleLicensePlate) {
        this.vehicleLicensePlate = vehicleLicensePlate;
    }

    public long getSubscriptionPackageId() {
        return subscriptionPackageId;
    }

    public void setSubscriptionPackageId(long subscriptionPackageId) {
        this.subscriptionPackageId = subscriptionPackageId;
    }

    public String getSubscriptionPackageName() {
        return subscriptionPackageName;
    }

    public void setSubscriptionPackageName(String subscriptionPackageName) {
        this.subscriptionPackageName = subscriptionPackageName;
    }

    public long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public String getParkingLotName() {
        return parkingLotName;
    }

    public void setParkingLotName(String parkingLotName) {
        this.parkingLotName = parkingLotName;
    }

    public long getAssignedSpotId() {
        return assignedSpotId;
    }

    public void setAssignedSpotId(long assignedSpotId) {
        this.assignedSpotId = assignedSpotId;
    }

    public String getAssignedSpotName() {
        return assignedSpotName;
    }

    public void setAssignedSpotName(String assignedSpotName) {
        this.assignedSpotName = assignedSpotName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(String cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
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

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public int getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(int daysRemaining) {
        this.daysRemaining = daysRemaining;
    }
}

