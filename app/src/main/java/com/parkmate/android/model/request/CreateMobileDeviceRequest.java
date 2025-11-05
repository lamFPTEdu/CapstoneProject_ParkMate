package com.parkmate.android.model.request;

import com.google.gson.annotations.SerializedName;

public class CreateMobileDeviceRequest {
    @SerializedName("ownedByMe")
    private boolean ownedByMe;

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("deviceName")
    private String deviceName;

    @SerializedName("deviceOs")
    private String deviceOs;

    @SerializedName("pushToken")
    private String pushToken;

    public CreateMobileDeviceRequest(String deviceId, String deviceName, String deviceOs, String pushToken) {
        this.ownedByMe = true; // Backend sẽ check token để lấy userId
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.deviceOs = deviceOs;
        this.pushToken = pushToken;
    }

    // Getters and Setters
    public boolean isOwnedByMe() {
        return ownedByMe;
    }

    public void setOwnedByMe(boolean ownedByMe) {
        this.ownedByMe = ownedByMe;
    }


    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceOs() {
        return deviceOs;
    }

    public void setDeviceOs(String deviceOs) {
        this.deviceOs = deviceOs;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }
}

