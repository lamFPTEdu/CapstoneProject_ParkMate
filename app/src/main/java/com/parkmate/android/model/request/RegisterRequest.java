package com.parkmate.android.model.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request đăng ký tài khoản theo swagger /api/v1/user-service/auth/register
 * Các trường UI đang có mà swagger CHƯA có sẽ được comment / TODO tại RegisterActivity.
 */
public class RegisterRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String username; // Tạm sinh từ email hoặc full name nếu UI chưa có trường riêng

    @SerializedName("password")
    private String password;

    @SerializedName("phone")
    private String phone; // UI chưa có trường -> tạm placeholder / TODO

    @SerializedName("firstName")
    private String firstName; // Tách từ họ tên đầy đủ

    @SerializedName("lastName")
    private String lastName;  // Tách từ họ tên đầy đủ

    @SerializedName("idNumber")
    private String idNumber; // Số CCCD / CMND

    @SerializedName("dateOfBirth")
    private String dateOfBirth; // ISO 8601 "yyyy-MM-dd'T'00:00:00"

    @SerializedName("address")
    private String address; // Dùng permanentAddress từ UI

    @SerializedName("frontPhotoPath")
    private String frontPhotoPath; // Tạm placeholder - thực tế cần upload để lấy path

    @SerializedName("backPhotoPath")
    private String backPhotoPath;  // Tạm placeholder - thực tế cần upload để lấy path

    public RegisterRequest(String email,
                           String username,
                           String password,
                           String phone,
                           String firstName,
                           String lastName,
                           String idNumber,
                           String dateOfBirth,
                           String address,
                           String frontPhotoPath,
                           String backPhotoPath) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.firstName = firstName;
        this.lastName = lastName;
        this.idNumber = idNumber;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.frontPhotoPath = frontPhotoPath;
        this.backPhotoPath = backPhotoPath;
    }

    // Getters (nếu cần dùng sau này)
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getIdNumber() { return idNumber; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getAddress() { return address; }
    public String getFrontPhotoPath() { return frontPhotoPath; }
    public String getBackPhotoPath() { return backPhotoPath; }
}

