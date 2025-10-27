package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Response từ API PUT /api/v1/user-service/users
 */
public class UpdateUserResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private UserData data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public UserData getData() {
        return data;
    }

    public static class UserData {
        @SerializedName("id")
        private Long id;

        @SerializedName("email")
        private String email;

        @SerializedName("fullName")
        private String fullName;

        @SerializedName("idNumber")
        private String idNumber;

        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getFullName() {
            return fullName;
        }

        public String getIdNumber() {
            return idNumber;
        }
    }
}

