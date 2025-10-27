package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Response từ API GET /api/v1/user-service/users/{id}
 */
public class UserInfoResponse {

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

        @SerializedName("phone")
        private String phone;

        @SerializedName("fullName")
        private String fullName;

        @SerializedName("firstName")
        private String firstName;

        @SerializedName("lastName")
        private String lastName;

        @SerializedName("idNumber")
        private String idNumber;

        @SerializedName("gender")
        private String gender;

        @SerializedName("dateOfBirth")
        private String dateOfBirth;

        @SerializedName("nationality")
        private String nationality;

        @SerializedName("address")
        private String address;

        @SerializedName("issueDate")
        private String issueDate;

        @SerializedName("issuePlace")
        private String issuePlace;

        @SerializedName("expiryDate")
        private String expiryDate;

        @SerializedName("frontIdPath")
        private String frontIdPath;

        @SerializedName("backIdImgPath")
        private String backIdImgPath;

        @SerializedName("status")
        private String status;

        @SerializedName("role")
        private String role;

        // Getters
        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getFullName() {
            return fullName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getIdNumber() {
            return idNumber;
        }

        public String getGender() {
            return gender;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }

        public String getNationality() {
            return nationality;
        }

        public String getAddress() {
            return address;
        }

        public String getIssueDate() {
            return issueDate;
        }

        public String getIssuePlace() {
            return issuePlace;
        }

        public String getExpiryDate() {
            return expiryDate;
        }

        public String getFrontIdPath() {
            return frontIdPath;
        }

        public String getBackIdImgPath() {
            return backIdImgPath;
        }

        public String getStatus() {
            return status;
        }

        public String getRole() {
            return role;
        }
    }
}

