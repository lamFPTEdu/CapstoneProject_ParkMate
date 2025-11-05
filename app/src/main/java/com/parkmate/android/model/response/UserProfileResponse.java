package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Response từ API GET /api/v1/user-service/users/me
 * Chứa toàn bộ thông tin user bao gồm QR code
 */
public class UserProfileResponse {

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

    @SerializedName("dateOfBirth")
    private String dateOfBirth;

    @SerializedName("address")
    private String address;

    @SerializedName("gender")
    private String gender;

    @SerializedName("nationality")
    private String nationality;

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

    @SerializedName("profilePicturePresignedUrl")
    private String profilePicturePresignedUrl;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("qrCode")
    private String qrCode;

    // Getters
    public Account getAccount() {
        return account;
    }

    public Long getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public String getGender() {
        return gender;
    }

    public String getNationality() {
        return nationality;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getIssuePlace() {
        return issuePlace;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getFrontPhotoPresignedUrl() {
        return frontPhotoPresignedUrl;
    }

    public String getBackPhotoPresignedUrl() {
        return backPhotoPresignedUrl;
    }

    public String getProfilePicturePresignedUrl() {
        return profilePicturePresignedUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getQrCode() {
        return qrCode;
    }

    public static class Account {
        @SerializedName("id")
        private Long id;

        @SerializedName("email")
        private String email;

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

        public String getStatus() {
            return status;
        }

        public String getRole() {
            return role;
        }
    }
}

