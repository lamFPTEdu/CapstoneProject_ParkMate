package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("userId")
    private long userId;

    @SerializedName("walletId")
    private long walletId;

    @SerializedName("sessionId")
    private String sessionId;

    @SerializedName("transactionType")
    private String transactionType;

    @SerializedName("amount")
    private long amount;

    @SerializedName("fee")
    private long fee;

    @SerializedName("netAmount")
    private long netAmount;

    @SerializedName("balanceBefore")
    private long balanceBefore;

    @SerializedName("balanceAfter")
    private long balanceAfter;

    @SerializedName("externalTransactionId")
    private String externalTransactionId;

    @SerializedName("gatewayResponse")
    private String gatewayResponse;

    @SerializedName("status")
    private String status;

    @SerializedName("processedAt")
    private String processedAt;

    @SerializedName("description")
    private String description;

    @SerializedName("metadata")
    private String metadata;

    @SerializedName("createdAt")
    private String createdAt;

    public Transaction() {
    }

    // Getters and Setters
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getWalletId() {
        return walletId;
    }

    public void setWalletId(long walletId) {
        this.walletId = walletId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public long getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(long netAmount) {
        this.netAmount = netAmount;
    }

    public long getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(long balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public long getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(long balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public String getGatewayResponse() {
        return gatewayResponse;
    }

    public void setGatewayResponse(String gatewayResponse) {
        this.gatewayResponse = gatewayResponse;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(String processedAt) {
        this.processedAt = processedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods
    public boolean isTopUp() {
        return "TOP_UP".equals(transactionType);
    }

    public boolean isDeduction() {
        return "DEDUCTION".equals(transactionType);
    }

    public boolean isRefund() {
        return "REFUND".equals(transactionType);
    }

    public boolean isSubscriptionPayment() {
        return "SUBSCRIPTION_PAYMENT".equals(transactionType);
    }

    public boolean isReservationPayment() {
        return "RESERVATION_PAYMENT".equals(transactionType);
    }

    // For backward compatibility - deprecated
    @Deprecated
    public boolean isPayment() {
        return isDeduction() || isSubscriptionPayment() || isReservationPayment();
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }
}

