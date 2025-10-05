package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Phản hồi đăng nhập.
 * Backend có thể trả token ở nhiều field -> gom lại giống RegisterResponse để tái sử dụng logic.
 */
public class LoginResponse {
    @SerializedName("success")
    private Boolean success;
    @SerializedName("message")
    private String message;
    @SerializedName("userId")
    private String userId;
    @SerializedName("username")
    private String username; // Thêm username

    // Các biến thể token phổ biến
    @SerializedName("token")
    private String token;
    @SerializedName("accessToken")
    private String accessToken;
    @SerializedName("bearerToken")
    private String bearerToken;

    @SerializedName("data")
    private Data data;

    public Boolean getSuccess() { return success; }
    public String getMessage() { return message; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; } // Getter cho username
    public String getToken() { return token; }
    public String getAccessToken() { return accessToken; }
    public String getBearerToken() { return bearerToken; }
    public Data getData() { return data; }

    public String getAnyToken() {
        if (bearerToken != null && !bearerToken.isEmpty()) return bearerToken;
        if (accessToken != null && !accessToken.isEmpty()) return accessToken;
        if (token != null && !token.isEmpty()) return token;
        if (data != null) {
            if (data.bearerToken != null && !data.bearerToken.isEmpty()) return data.bearerToken;
            if (data.accessToken != null && !data.accessToken.isEmpty()) return data.accessToken;
            if (data.token != null && !data.token.isEmpty()) return data.token;
        }
        return null;
    }

    public static class Data {
        @SerializedName("token")
        private String token;
        @SerializedName("accessToken")
        private String accessToken;
        @SerializedName("bearerToken")
        private String bearerToken;
        @SerializedName("userId")
        private String userId;
        @SerializedName("username")
        private String username; // Thêm username trong Data

        public String getToken() { return token; }
        public String getAccessToken() { return accessToken; }
        public String getBearerToken() { return bearerToken; }
        public String getUserId() { return userId; }
        public String getUsername() { return username; } // Getter cho username
    }
}
