package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Phản hồi đăng ký (mở rộng để bắt các trường token khác nhau nếu BE trả về).
 */
public class RegisterResponse {
    @SerializedName("success")
    private Boolean success;
    @SerializedName("message")
    private String message;
    @SerializedName("userId")
    private String userId;
    @SerializedName("entityId")
    private Long entityId; // ID để upload ảnh CCCD

    // Các biến thể token phổ biến (legacy support)
    @SerializedName("token")
    private String token;
    @SerializedName("accessToken")
    private String accessToken;
    @SerializedName("bearerToken")
    private String bearerToken;

    // Backend trả về data chứa authResponse và userResponse
    @SerializedName("data")
    private Data data;

    // Legacy support - userResponse trực tiếp
    @SerializedName("userResponse")
    private UserResponse userResponse;

    @SerializedName("error")
    private Object error;
    @SerializedName("meta")
    private Object meta;
    @SerializedName("timestamp")
    private String timestamp;

    public Boolean getSuccess() { return success; }
    public String getMessage() { return message; }
    public String getUserId() { return userId; }
    public Long getEntityId() { return entityId; }
    public String getToken() { return token; }
    public String getAccessToken() { return accessToken; }
    public String getBearerToken() { return bearerToken; }
    public Data getData() { return data; }
    public UserResponse getUserResponse() { return userResponse; }

    // Trả về token đầu tiên không null
    public String getAnyToken() {
        // Ưu tiên lấy từ data.authResponse
        if (data != null && data.authResponse != null) {
            if (data.authResponse.accessToken != null && !data.authResponse.accessToken.isEmpty()) {
                return data.authResponse.accessToken;
            }
            if (data.authResponse.refreshToken != null && !data.authResponse.refreshToken.isEmpty()) {
                return data.authResponse.refreshToken;
            }
        }

        // Fallback sang các trường legacy
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
        // Legacy fields
        @SerializedName("token")
        private String token;
        @SerializedName("accessToken")
        private String accessToken;
        @SerializedName("bearerToken")
        private String bearerToken;
        @SerializedName("userId")
        private String userId;
        @SerializedName("entityId")
        private Long entityId;

        // Actual backend structure
        @SerializedName("authResponse")
        private AuthResponse authResponse;
        @SerializedName("userResponse")
        private UserResponse userResponse;

        public String getToken() { return token; }
        public String getAccessToken() { return accessToken; }
        public String getBearerToken() { return bearerToken; }
        public String getUserId() { return userId; }
        public Long getEntityId() { return entityId; }
        public AuthResponse getAuthResponse() { return authResponse; }
        public UserResponse getUserResponse() { return userResponse; }
    }

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

    public static class UserResponse {
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
        @SerializedName("dateOfBirth")
        private String dateOfBirth;
        @SerializedName("address")
        private String address;
        @SerializedName("idNumber")
        private String idNumber;
        @SerializedName("issuePlace")
        private String issuePlace;
        @SerializedName("issueDate")
        private String issueDate;
        @SerializedName("expiryDate")
        private String expiryDate;
        @SerializedName("frontPhotoPresignedUrl")
        private String frontPhotoPresignedUrl;
        @SerializedName("backPhotoPresignedUrl")
        private String backPhotoPresignedUrl;
        @SerializedName("createdAt")
        private String createdAt;
        @SerializedName("updatedAt")
        private String updatedAt;

        public Long getId() { return id; }
        public String getPhone() { return phone; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getFullName() { return fullName; }
        public String getDateOfBirth() { return dateOfBirth; }
        public String getAddress() { return address; }
        public String getIdNumber() { return idNumber; }
        public String getIssuePlace() { return issuePlace; }
        public String getIssueDate() { return issueDate; }
        public String getExpiryDate() { return expiryDate; }
        public String getFrontPhotoPresignedUrl() { return frontPhotoPresignedUrl; }
        public String getBackPhotoPresignedUrl() { return backPhotoPresignedUrl; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
    }
}
