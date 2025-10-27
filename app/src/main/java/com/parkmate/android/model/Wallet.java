package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Wallet {
    @SerializedName("userId")
    private long userId;

    @SerializedName("balance")
    private long balance;

    @SerializedName("currency")
    private String currency;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("createdAt")
    private List<Integer> createdAt;

    @SerializedName("updatedAt")
    private List<Integer> updatedAt;

    public Wallet() {
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<Integer> getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(List<Integer> createdAt) {
        this.createdAt = createdAt;
    }

    public List<Integer> getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(List<Integer> updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Format số tiền VND
    public String getFormattedBalance() {
        return String.format("%,dđ", balance);
    }
}

