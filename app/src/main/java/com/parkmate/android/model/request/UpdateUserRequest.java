package com.parkmate.android.model.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request để cập nhật thông tin user (đặc biệt là thông tin CCCD)
 * API: PUT /api/v1/user-service/users
 */
public class UpdateUserRequest {

    @SerializedName("phone")
    private String phone;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("address")
    private String address;

    @SerializedName("profilePictureUrl")
    private String profilePictureUrl;

    @SerializedName("frontPhotoPath")
    private String frontPhotoPath;

    @SerializedName("backPhotoPath")
    private String backPhotoPath;

    @SerializedName("fullName")
    private String fullName; // Họ tên đầy đủ trên CCCD

    @SerializedName("idNumber")
    private String idNumber; // Số CCCD

    @SerializedName("gender")
    private String gender; // Giới tính

    @SerializedName("dateOfBirth")
    private String dateOfBirth; // ISO 8601

    @SerializedName("nationality")
    private String nationality; // Quốc tịch

    @SerializedName("permanentAddress")
    private String permanentAddress; // Địa chỉ thường trú

    @SerializedName("issuePlace")
    private String issuePlace; // Nơi cấp

    @SerializedName("issueDate")
    private String issueDate; // Ngày cấp ISO 8601

    @SerializedName("expiryDate")
    private String expiryDate; // Ngày hết hạn ISO 8601

    @SerializedName("frontIdPath")
    private String frontIdPath;

    @SerializedName("backIdImgPath")
    private String backIdImgPath;

    public UpdateUserRequest() {
    }

    // Setters cho các trường CCCD
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public void setIssuePlace(String issuePlace) {
        this.issuePlace = issuePlace;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setFrontIdPath(String frontIdPath) {
        this.frontIdPath = frontIdPath;
    }

    public void setBackIdImgPath(String backIdImgPath) {
        this.backIdImgPath = backIdImgPath;
    }

    // Getters
    public String getPhone() { return phone; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getAddress() { return address; }
    public String getFullName() { return fullName; }
    public String getIdNumber() { return idNumber; }
    public String getGender() { return gender; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getNationality() { return nationality; }
    public String getPermanentAddress() { return permanentAddress; }
    public String getIssuePlace() { return issuePlace; }
    public String getIssueDate() { return issueDate; }
    public String getExpiryDate() { return expiryDate; }
    public String getFrontIdPath() { return frontIdPath; }
    public String getBackIdImgPath() { return backIdImgPath; }
}
