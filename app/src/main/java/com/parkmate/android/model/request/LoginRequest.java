package com.parkmate.android.model.request;

import com.google.gson.annotations.SerializedName;

/**
 * Body đăng nhập: {"email": "...", "password": "..."}
 */
public class LoginRequest {
    @SerializedName("email")
    private final String email;
    @SerializedName("password")
    private final String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}

