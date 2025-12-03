package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class RefundPolicy implements Serializable {
    @SerializedName("refundWindowMinutes")
    private Integer refundWindowMinutes;

    @SerializedName("refundRate")
    private Double refundRate;

    public RefundPolicy() {
    }

    public RefundPolicy(Integer refundWindowMinutes, Double refundRate) {
        this.refundWindowMinutes = refundWindowMinutes;
        this.refundRate = refundRate;
    }

    // Getters and Setters
    public Integer getRefundWindowMinutes() {
        // Default 30 minutes if null
        return refundWindowMinutes != null ? refundWindowMinutes : 30;
    }

    public void setRefundWindowMinutes(Integer refundWindowMinutes) {
        this.refundWindowMinutes = refundWindowMinutes;
    }

    public Double getRefundRate() {
        return refundRate;
    }

    public void setRefundRate(Double refundRate) {
        this.refundRate = refundRate;
    }
}

