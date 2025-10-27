package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;

public class PaymentCancel {
    @SerializedName("message")
    private String message;

    @SerializedName("orderCode")
    private long orderCode;

    @SerializedName("reason")
    private String reason;

    public PaymentCancel() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(long orderCode) {
        this.orderCode = orderCode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

