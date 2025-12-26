package com.parkmate.android.model.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request body for cancelling a user subscription
 */
public class CancelSubscriptionRequest {

    @SerializedName("reason")
    private String reason;

    public CancelSubscriptionRequest() {
    }

    public CancelSubscriptionRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
