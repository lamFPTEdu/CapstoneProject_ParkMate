package com.parkmate.android.model.request;

import com.google.gson.annotations.SerializedName;

public class HoldReservationRequest {
    @SerializedName("parkingLotId")
    private Long parkingLotId;

    @SerializedName("vehicleId")
    private Long vehicleId;

    @SerializedName("reservedFrom")
    private String reservedFrom;

    @SerializedName("assumedStayMinute")
    private int assumedStayMinute;

    public HoldReservationRequest(Long parkingLotId, Long vehicleId, String reservedFrom, int assumedStayMinute) {
        this.parkingLotId = parkingLotId;
        this.vehicleId = vehicleId;
        this.reservedFrom = reservedFrom;
        this.assumedStayMinute = assumedStayMinute;
    }

    // Getters and Setters
    public Long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(Long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
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
}

