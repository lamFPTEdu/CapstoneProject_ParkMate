package com.parkmate.android.model.request;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {
    @SerializedName("email")
    private String email;

    @SerializedName("resetCode")
    private String resetCode;

    @SerializedName("newPassword")
    private String newPassword;

    public ResetPasswordRequest(String email, String resetCode, String newPassword) {
        this.email = email;
        this.resetCode = resetCode;
        this.newPassword = newPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getResetCode() {
        return resetCode;
    }

    public void setResetCode(String resetCode) {
        this.resetCode = resetCode;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

