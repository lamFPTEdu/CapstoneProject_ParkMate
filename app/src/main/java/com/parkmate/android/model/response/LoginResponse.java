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
    private String username;

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
    public String getUserId() {
        if (userId != null) return userId;
        if (data != null && data.userResponse != null && data.userResponse.account != null) {
            return String.valueOf(data.userResponse.account.id);
        }
        return null;
    }
    public String getUsername() {
        if (username != null) return username;
        if (data != null && data.userResponse != null && data.userResponse.account != null) {
            return data.userResponse.account.email;
        }
        return null;
    }
    public String getToken() { return token; }
    public String getAccessToken() { return accessToken; }
    public String getBearerToken() { return bearerToken; }
    public Data getData() { return data; }

    public String getAnyToken() {
        // Ưu tiên lấy từ cấu trúc mới: data.authResponse.accessToken
        if (data != null && data.authResponse != null) {
            if (data.authResponse.accessToken != null && !data.authResponse.accessToken.isEmpty()) {
                return data.authResponse.accessToken;
            }
        }

        // Fallback các vị trí khác
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
        private String username;

        // Cấu trúc mới từ backend
        @SerializedName("authResponse")
        private AuthResponse authResponse;

        @SerializedName("userResponse")
        private UserResponse userResponse;

        public String getToken() { return token; }
        public String getAccessToken() { return accessToken; }
        public String getBearerToken() { return bearerToken; }
        public String getUserId() {
            if (userId != null) return userId;
            if (userResponse != null && userResponse.account != null) {
                return String.valueOf(userResponse.account.id);
            }
            return null;
        }
        public String getUsername() {
            if (username != null) return username;
            if (userResponse != null && userResponse.account != null) {
                return userResponse.account.email;
            }
            return null;
        }
        public AuthResponse getAuthResponse() { return authResponse; }
        public UserResponse getUserResponse() { return userResponse; }
    }

    // Cấu trúc authResponse từ backend
    public static class AuthResponse {
        @SerializedName("accessToken")
        private String accessToken;

        @SerializedName("refreshToken")
        private String refreshToken;

        @SerializedName("tokenType")
        private String tokenType;

        @SerializedName("expiresIn")
        private Long expiresIn;

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public String getTokenType() { return tokenType; }
        public Long getExpiresIn() { return expiresIn; }
    }

    // Cấu trúc userResponse từ backend
    public static class UserResponse {
        @SerializedName("account")
        private Account account;

        @SerializedName("id")
        private Long id;

        @SerializedName("phone")
        private String phone;

        @SerializedName("firstName")
        private String firstName;

        @SerializedName("lastName")
        private String lastName;

        @SerializedName("fullName")
        private String fullName;

        public Account getAccount() { return account; }
        public Long getId() { return id; }
        public String getPhone() { return phone; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getFullName() { return fullName; }
    }

    // Account từ userResponse
    public static class Account {
        @SerializedName("id")
        private Long id;

        @SerializedName("email")
        private String email;

        @SerializedName("status")
        private String status;

        @SerializedName("role")
        private String role;

        @SerializedName("isIdVerified")
        private Boolean isIdVerified;

        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getStatus() { return status; }
        public String getRole() { return role; }
        public Boolean getIsIdVerified() { return isIdVerified; }
    }
}

