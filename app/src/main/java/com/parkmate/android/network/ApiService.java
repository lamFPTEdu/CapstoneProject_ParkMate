package com.parkmate.android.network;

import com.parkmate.android.model.request.RegisterRequest;
import com.parkmate.android.model.response.RegisterResponse;
import com.parkmate.android.model.response.OtpVerifyResponse;
import com.parkmate.android.model.response.UploadImageResponse;
import com.parkmate.android.model.request.EmptyJsonBody;
import com.parkmate.android.model.request.LoginRequest;
import com.parkmate.android.model.response.LoginResponse;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Interface API rút gọn: chỉ giữ endpoint cần dùng thực tế.
 */
public interface ApiService {
    @POST("api/v1/user-service/auth/register")
    Single<RegisterResponse> register(@Body RegisterRequest request);

    // Endpoint verify OTP chuẩn: PUT /auth/verify?verifyCode=XXXXXX với body rỗng {}
    @PUT("api/v1/user-service/auth/verify")
    Single<OtpVerifyResponse> verifyOtp(@Query("verifyCode") String verifyCode, @Body EmptyJsonBody emptyBody);

    // Đăng nhập
    @POST("api/v1/user-service/auth/login")
    Single<LoginResponse> login(@Body LoginRequest request);

    // Upload ảnh CCCD
    @Multipart
    @POST("api/v1/user-service/upload/image/entity")
    Single<UploadImageResponse> uploadIdImage(
            @Query("entityId") Long entityId,
            @Query("imageType") String imageType,
            @Part MultipartBody.Part file
    );
}
