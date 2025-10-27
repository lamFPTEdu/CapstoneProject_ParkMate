package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;

public class PaymentStatus {
    @SerializedName("orderCode")
    private long orderCode;

    @SerializedName("transactionStatus")
    private String transactionStatus;

    @SerializedName("amount")
    private long amount;

    public PaymentStatus() {
    }

    public long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(long orderCode) {
        this.orderCode = orderCode;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}

