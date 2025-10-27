package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;
import com.parkmate.android.model.Reservation;

/**
 * Response cho API tạo reservation
 */
public class ReservationResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Reservation data;

    @SerializedName("error")
    private String error;

    @SerializedName("timestamp")
    private String timestamp;

    // Getters & Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Reservation getData() {
        return data;
    }

    public void setData(Reservation data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

