package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;

public class HoldReservationResponse {
    @SerializedName("holdId")
    private String holdId;

    @SerializedName("parkingLotId")
    private Long parkingLotId;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("reservedFrom")
    private String reservedFrom;

    @SerializedName("assumedStayMinute")
    private int assumedStayMinute;

    @SerializedName("expiresAt")
    private String expiresAt;

    @SerializedName("message")
    private String message;

    // Getters and Setters
    public String getHoldId() {
        return holdId;
    }

    public void setHoldId(String holdId) {
        this.holdId = holdId;
    }

    public Long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(Long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getReservedFrom() {
        return reservedFrom;
    }

    public void setReservedFrom(String reservedFrom) {
        this.reservedFrom = reservedFrom;
    }

    public int getAssumedStayMinute() {
        return assumedStayMinute;
    }

    public void setAssumedStayMinute(int assumedStayMinute) {
        this.assumedStayMinute = assumedStayMinute;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

