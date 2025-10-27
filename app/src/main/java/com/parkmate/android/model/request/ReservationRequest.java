package com.parkmate.android.model.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request body để tạo reservation
 */
public class ReservationRequest {

    @SerializedName("vehicleId")
    private Long vehicleId;

    @SerializedName("parkingLotId")
    private Long parkingLotId;

    @SerializedName("spotId")
    private Long spotId;

    @SerializedName("reservationFee")
    private int reservationFee;

    @SerializedName("reservedFrom")
    private String reservedFrom; // ISO 8601: "2025-10-22T11:07:50"

    @SerializedName("reservedUntil")
    private String reservedUntil; // ISO 8601: "2025-11-20T11:00:00"

    @SerializedName("ownedByMe")
    private boolean ownedByMe;

    // Constructor
    public ReservationRequest() {
    }

    public ReservationRequest(Long vehicleId, Long parkingLotId, Long spotId,
                              int reservationFee, String reservedFrom, String reservedUntil) {
        this.vehicleId = vehicleId;
        this.parkingLotId = parkingLotId;
        this.spotId = spotId;
        this.reservationFee = reservationFee;
        this.reservedFrom = reservedFrom;
        this.reservedUntil = reservedUntil;
        this.ownedByMe = true; // Luôn set true vì đây là reservation của user hiện tại
    }

    // Getters & Setters
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

    public Long getSpotId() {
        return spotId;
    }

    public void setSpotId(Long spotId) {
        this.spotId = spotId;
    }

    public int getReservationFee() {
        return reservationFee;
    }

    public void setReservationFee(int reservationFee) {
        this.reservationFee = reservationFee;
    }

    public String getReservedFrom() {
        return reservedFrom;
    }

    public void setReservedFrom(String reservedFrom) {
        this.reservedFrom = reservedFrom;
    }

    public String getReservedUntil() {
        return reservedUntil;
    }

    public void setReservedUntil(String reservedUntil) {
        this.reservedUntil = reservedUntil;
    }

    public boolean isOwnedByMe() {
        return ownedByMe;
    }

    public void setOwnedByMe(boolean ownedByMe) {
        this.ownedByMe = ownedByMe;
    }
}
