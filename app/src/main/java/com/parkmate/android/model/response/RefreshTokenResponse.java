package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;

public class RefreshTokenResponse {
    @SerializedName("success")
    private Boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private AuthData data;

    public static class AuthData {
        @SerializedName("accessToken")
        private String accessToken;

        @SerializedName("refreshToken")
        private String refreshToken;

        @SerializedName("tokenType")
        private String tokenType;

        @SerializedName("expiresIn")
        private Long expiresIn;

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public Long getExpiresIn() {
            return expiresIn;
        }
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public AuthData getData() {
        return data;
    }
}

