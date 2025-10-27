package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;
import com.parkmate.android.model.Wallet;

public class WalletResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Wallet data;

    @SerializedName("error")
    private String error;

    @SerializedName("meta")
    private Object meta;

    @SerializedName("timestamp")
    private String timestamp;

    public WalletResponse() {
    }

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

    public Wallet getData() {
        return data;
    }

    public void setData(Wallet data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getMeta() {
        return meta;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

