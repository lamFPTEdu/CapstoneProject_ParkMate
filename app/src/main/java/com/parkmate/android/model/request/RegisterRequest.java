package com.parkmate.android.model.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request đăng ký tài khoản theo swagger /api/v1/user-service/auth/register
 * Backend đã thay đổi: firstName/lastName thay thế username, fullName cho CCCD, thêm expiryDate
 */
public class RegisterRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("phone")
    private String phone;

    @SerializedName("firstName")
    private String firstName; // Họ và tên đệm

    @SerializedName("lastName")
    private String lastName;  // Tên

    @SerializedName("fullName")
    private String fullName; // Họ tên đầy đủ cho CCCD

    @SerializedName("idNumber")
    private String idNumber; // Số CCCD / CMND

    @SerializedName("dateOfBirth")
    private String dateOfBirth; // ISO 8601 "yyyy-MM-dd'T'00:00:00"

    @SerializedName("issuePlace")
    private String issuePlace; // Nơi cấp

    @SerializedName("issueDate")
    private String issueDate; // Ngày cấp ISO 8601

    @SerializedName("expiryDate")
    private String expiryDate; // Ngày hết hạn ISO 8601

    @SerializedName("address")
    private String address; // Địa chỉ thường trú

    @SerializedName("frontIdPath")
    private String frontIdPath; // Đường dẫn ảnh mặt trước (sẽ upload sau khi đăng ký)

    @SerializedName("backIdImgPath")
    private String backIdImgPath; // Đường dẫn ảnh mặt sau (sẽ upload sau khi đăng ký)

    public RegisterRequest(String email,
                           String password,
                           String phone,
                           String firstName,
                           String lastName,
                           String fullName,
                           String idNumber,
                           String dateOfBirth,
                           String issuePlace,
                           String issueDate,
                           String expiryDate,
                           String address,
                           String frontIdPath,
                           String backIdImgPath) {
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.idNumber = idNumber;
        this.dateOfBirth = dateOfBirth;
        this.issuePlace = issuePlace;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.address = address;
        this.frontIdPath = frontIdPath;
        this.backIdImgPath = backIdImgPath;
    }

    // Getters
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return fullName; }
    public String getIdNumber() { return idNumber; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getIssuePlace() { return issuePlace; }
    public String getIssueDate() { return issueDate; }
    public String getExpiryDate() { return expiryDate; }
    public String getAddress() { return address; }
    public String getFrontIdPath() { return frontIdPath; }
    public String getBackIdImgPath() { return backIdImgPath; }
}
