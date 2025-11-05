package com.parkmate.android.model.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request body để tạo reservation mới
 */
public class ReservationRequest {

    @SerializedName("ownedByMe")
    private boolean ownedByMe;

    @SerializedName("vehicleId")
    private Long vehicleId;

    @SerializedName("parkingLotId")
    private Long parkingLotId;

    @SerializedName("initialFee")
    private long initialFee;

    @SerializedName("reservedFrom")
    private String reservedFrom; // ISO 8601: "2024-07-01T10:00:00"

    @SerializedName("assumedStayMinute")
    private int assumedStayMinute; // Thời gian dự kiến ở (phút)

    @SerializedName("holdId")
    private String holdId; // Hold ID từ API hold reservation

    // Constructor
    public ReservationRequest() {
        this.ownedByMe = true; // Luôn true vì đây là reservation của user hiện tại
    }

    public ReservationRequest(Long vehicleId, Long parkingLotId, long initialFee,
                              String reservedFrom, int assumedStayMinute, String holdId) {
        this.ownedByMe = true;
        this.vehicleId = vehicleId;
        this.parkingLotId = parkingLotId;
        this.initialFee = initialFee;
        this.reservedFrom = reservedFrom;
        this.assumedStayMinute = assumedStayMinute;
        this.holdId = holdId;
    }

    // Getters & Setters
    public boolean isOwnedByMe() {
        return ownedByMe;
    }

    public void setOwnedByMe(boolean ownedByMe) {
        this.ownedByMe = ownedByMe;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(Long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public long getInitialFee() {
        return initialFee;
    }

    public void setInitialFee(long initialFee) {
        this.initialFee = initialFee;
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

    public String getHoldId() {
        return holdId;
    }

    public void setHoldId(String holdId) {
        this.holdId = holdId;
    }
}
