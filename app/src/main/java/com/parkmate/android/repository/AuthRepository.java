package com.parkmate.android.repository;

import android.util.Log;

import com.parkmate.android.model.request.RegisterRequest;
import com.parkmate.android.model.request.EmptyJsonBody;
import com.parkmate.android.model.response.OtpVerifyResponse;
import com.parkmate.android.model.response.RegisterResponse;
import com.parkmate.android.model.response.UploadImageResponse;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.network.ApiService;
import com.parkmate.android.model.request.LoginRequest;
import com.parkmate.android.model.response.LoginResponse;
import com.parkmate.android.model.request.UpdateUserRequest;
import com.parkmate.android.model.response.UpdateUserResponse;
import com.parkmate.android.model.response.UserInfoResponse;

import java.io.File;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.HttpException;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private final ApiService apiService;

    public AuthRepository() { this.apiService = ApiClient.getApiService(); }

    public Single<OtpVerifyResponse> verifyOtp(String email, String otp) {
        String code = otp == null ? null : otp.trim();
        Log.d(TAG, "Verify OTP (single PUT) code=" + code + ", emailHint=" + email);
        return apiService.verifyOtp(code, EmptyJsonBody.INSTANCE)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Verify OTP HTTP " + he.code() + " msg=" + he.message());
                    } else {
                        Log.e(TAG, "Verify OTP error: " + err.getMessage(), err);
                    }
                });
    }

    /**
     * Resend OTP code (chỉ dành cho đăng ký)
     * @param email Email của user
     */
    public Single<com.parkmate.android.model.response.ApiResponse<Void>> resendOtp(String email) {
        Log.d(TAG, "Resending OTP to email: " + email);
        return apiService.resendOtp(email)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Resend OTP HTTP " + he.code() + " msg=" + he.message());
                        try {
                            if (he.response() != null && he.response().errorBody() != null) {
                                String errorBody = he.response().errorBody().string();
                                Log.e(TAG, "Resend OTP error body: " + errorBody);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot parse error body", e);
                        }
                    } else {
                        Log.e(TAG, "Resend OTP error: " + err.getMessage(), err);
                    }
                });
    }

    public Single<RegisterResponse> register(RegisterRequest request) {
        return apiService.register(request);
    }

    // Login
    public Single<LoginResponse> login(LoginRequest request) {
        return apiService.login(request);
    }

    /**
     * Refresh access token using refresh token
     * @param refreshToken The refresh token
     * @return New access token and refresh token
     */
    public Single<com.parkmate.android.model.response.RefreshTokenResponse> refreshToken(String refreshToken) {
        Log.d(TAG, "Refreshing access token...");
        com.parkmate.android.model.request.RefreshTokenRequest request =
            new com.parkmate.android.model.request.RefreshTokenRequest(refreshToken);
        return apiService.refreshToken(request)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Refresh token HTTP " + he.code() + " msg=" + he.message());
                        try {
                            if (he.response() != null && he.response().errorBody() != null) {
                                String errorBody = he.response().errorBody().string();
                                Log.e(TAG, "Refresh token error body: " + errorBody);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot parse error body", e);
                        }
                    } else {
                        Log.e(TAG, "Refresh token error: " + err.getMessage(), err);
                    }
                });
    }

    /**
     * Upload ảnh CCCD (mặt trước hoặc mặt sau)
     * @param entityId ID của entity (user)
     * @param imageType Loại ảnh (FRONT_ID_CARD hoặc BACK_ID_CARD)
     * @param imageFile File ảnh cần upload
     */
    public Single<UploadImageResponse> uploadIdImage(Long entityId, String imageType, File imageFile) {
        // Detect MIME type từ file extension
        String mimeType = "image/jpeg"; // Default
        String fileName = imageFile.getName().toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            mimeType = "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            mimeType = "image/png";
        } else if (fileName.endsWith(".webp")) {
            mimeType = "image/webp";
        }

        Log.d(TAG, "Uploading image: entityId=" + entityId + ", type=" + imageType);
        Log.d(TAG, "File: " + imageFile.getName() + ", size=" + imageFile.length() + " bytes");
        Log.d(TAG, "MIME type: " + mimeType);

        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        return apiService.uploadIdImage(entityId, imageType, body)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Upload image HTTP " + he.code() + " msg=" + he.message());
                        try {
                            if (he.response() != null && he.response().errorBody() != null) {
                                String errorBody = he.response().errorBody().string();
                                Log.e(TAG, "Upload error body: " + errorBody);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot parse error body", e);
                        }
                    } else {
                        Log.e(TAG, "Upload image error: " + err.getMessage(), err);
                    }
                });
    }

    /**
     * Cập nhật thông tin user (xác thực CCCD)
     */
    public Single<UpdateUserResponse> updateUser(UpdateUserRequest request) {
        Log.d(TAG, "Updating user info with CCCD data");
        return apiService.updateUser(request)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Update user HTTP " + he.code() + " msg=" + he.message());
                        try {
                            if (he.response() != null && he.response().errorBody() != null) {
                                String errorBody = he.response().errorBody().string();
                                Log.e(TAG, "Update error body: " + errorBody);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot parse error body", e);
                        }
                    } else {
                        Log.e(TAG, "Update user error: " + err.getMessage(), err);
                    }
                });
    }

    /**
     * Upload ảnh profile của user
     * @param entityId ID của user (account)
     * @param imageFile File ảnh cần upload
     */
    public Single<UploadImageResponse> uploadProfileImage(Long entityId, File imageFile) {
        // Detect MIME type từ file extension
        String mimeType = "image/jpeg"; // Default
        String fileName = imageFile.getName().toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            mimeType = "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            mimeType = "image/png";
        } else if (fileName.endsWith(".webp")) {
            mimeType = "image/webp";
        }

        Log.d(TAG, "Uploading profile image: entityId=" + entityId);
        Log.d(TAG, "File: " + imageFile.getName() + ", size=" + imageFile.length() + " bytes");
        Log.d(TAG, "MIME type: " + mimeType);

        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        return apiService.uploadIdImage(entityId, "AVATAR", body)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Upload profile image HTTP " + he.code() + " msg=" + he.message());
                        try {
                            if (he.response() != null && he.response().errorBody() != null) {
                                String errorBody = he.response().errorBody().string();
                                Log.e(TAG, "Upload error body: " + errorBody);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot parse error body", e);
                        }
                    } else {
                        Log.e(TAG, "Upload profile image error: " + err.getMessage(), err);
                    }
                });
    }

    /**
     * Lấy thông tin user theo ID
     */
    public Single<UserInfoResponse> getUserInfo(String userId) {
        Log.d(TAG, "Getting user info for userId: " + userId);
        return apiService.getUserInfo(userId)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Get user info HTTP " + he.code() + " msg=" + he.message());
                        try {
                            if (he.response() != null && he.response().errorBody() != null) {
                                String errorBody = he.response().errorBody().string();
                                Log.e(TAG, "Get user info error body: " + errorBody);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot parse error body", e);
                        }
                    } else {
                        Log.e(TAG, "Get user info error: " + err.getMessage(), err);
                    }
                });
    }

    /**
     * Gửi mã reset password về email
     * @param email Email của user
     */
    public Single<com.parkmate.android.model.response.ForgotPasswordResponse> forgotPassword(String email) {
        Log.d(TAG, "Sending reset code to email: " + email);
        com.parkmate.android.model.request.ForgotPasswordRequest request =
            new com.parkmate.android.model.request.ForgotPasswordRequest(email);
        return apiService.forgotPassword(request)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Forgot password HTTP " + he.code() + " msg=" + he.message());
                        try {
                            if (he.response() != null && he.response().errorBody() != null) {
                                String errorBody = he.response().errorBody().string();
                                Log.e(TAG, "Forgot password error body: " + errorBody);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot parse error body", e);
                        }
                    } else {
                        Log.e(TAG, "Forgot password error: " + err.getMessage(), err);
                    }
                });
    }

    /**
     * Reset password với mã code đã nhận
     * @param email Email của user
     * @param resetCode Mã code nhận được từ email
     * @param newPassword Mật khẩu mới
     */
    public Single<com.parkmate.android.model.response.ResetPasswordResponse> resetPassword(
            String email, String resetCode, String newPassword) {
        Log.d(TAG, "Resetting password for email: " + email);
        com.parkmate.android.model.request.ResetPasswordRequest request =
            new com.parkmate.android.model.request.ResetPasswordRequest(email, resetCode, newPassword);
        return apiService.resetPassword(request)
                .doOnError(err -> {
                    if (err instanceof HttpException) {
                        HttpException he = (HttpException) err;
                        Log.e(TAG, "Reset password HTTP " + he.code() + " msg=" + he.message());
                        try {
                            if (he.response() != null && he.response().errorBody() != null) {
                                String errorBody = he.response().errorBody().string();
                                Log.e(TAG, "Reset password error body: " + errorBody);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot parse error body", e);
                        }
                    } else {
                        Log.e(TAG, "Reset password error: " + err.getMessage(), err);
                    }
                });
    }
}
