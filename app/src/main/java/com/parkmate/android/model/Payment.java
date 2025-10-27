package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;

public class Payment {
    @SerializedName("bin")
    private String bin;

    @SerializedName("accountNumber")
    private String accountNumber;

    @SerializedName("accountName")
    private String accountName;

    @SerializedName("amount")
    private long amount;

    @SerializedName("description")
    private String description;

    @SerializedName("orderCode")
    private long orderCode;

    @SerializedName("currency")
    private String currency;

    @SerializedName("paymentLinkId")
    private String paymentLinkId;

    @SerializedName("status")
    private String status;

    @SerializedName("expiredAt")
    private String expiredAt;

    @SerializedName("checkoutUrl")
    private String checkoutUrl;

    @SerializedName("qrCode")
    private String qrCode;

    public Payment() {
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(long orderCode) {
        this.orderCode = orderCode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentLinkId() {
        return paymentLinkId;
    }

    public void setPaymentLinkId(String paymentLinkId) {
        this.paymentLinkId = paymentLinkId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(String expiredAt) {
        this.expiredAt = expiredAt;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}

