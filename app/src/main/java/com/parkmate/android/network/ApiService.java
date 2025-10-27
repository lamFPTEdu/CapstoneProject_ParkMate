package com.parkmate.android.network;

import com.parkmate.android.model.request.RegisterRequest;
import com.parkmate.android.model.response.RegisterResponse;
import com.parkmate.android.model.response.OtpVerifyResponse;
import com.parkmate.android.model.response.UploadImageResponse;
import com.parkmate.android.model.response.VehicleResponse;
import com.parkmate.android.model.response.ApiResponse;
import com.parkmate.android.model.request.EmptyJsonBody;
import com.parkmate.android.model.request.LoginRequest;
import com.parkmate.android.model.response.LoginResponse;
import com.parkmate.android.model.request.AddVehicleRequest;
import com.parkmate.android.model.Vehicle;
import com.parkmate.android.model.request.UpdateUserRequest;
import com.parkmate.android.model.response.UpdateUserResponse;
import com.parkmate.android.model.response.UserInfoResponse;
import com.parkmate.android.model.response.ParkingLotResponse;
import com.parkmate.android.model.response.ParkingLotDetailResponse;
import com.parkmate.android.model.response.ParkingFloorDetailResponse;
import com.parkmate.android.model.response.AreaDetailResponse;
import com.parkmate.android.model.request.ReservationRequest;
import com.parkmate.android.model.response.ReservationResponse;
import com.parkmate.android.model.response.ReservationListResponse;
import com.parkmate.android.model.response.WalletResponse;
import com.parkmate.android.model.Payment;
import com.parkmate.android.model.PaymentCancel;
import com.parkmate.android.model.PaymentStatus;
import com.parkmate.android.model.response.TransactionResponse;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
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

    // Lấy danh sách xe của user
    @GET("api/v1/user-service/vehicle")
    Single<ApiResponse<VehicleResponse>> getVehicles(
            @Query("page") int page,
            @Query("size") int size,
            @Query("sortBy") String sortBy,
            @Query("sortOrder") String sortOrder,
            @Query("ownedByMe") boolean ownedByMe
    );

    // Thêm xe mới
    @POST("api/v1/user-service/vehicle")
    Single<ApiResponse<Vehicle>> addVehicle(@Body AddVehicleRequest request);

    // Cập nhật thông tin user (xác thực CCCD)
    @PUT("api/v1/user-service/users")
    Single<UpdateUserResponse> updateUser(@Body UpdateUserRequest request);

    // Lấy thông tin user theo ID
    @GET("api/v1/user-service/users/{id}")
    Single<UserInfoResponse> getUserInfo(@Path("id") String userId);

    // Xóa xe
    @DELETE("api/v1/user-service/vehicle/{id}")
    Single<ApiResponse<Void>> deleteVehicle(@Path("id") Long vehicleId);

    // Cập nhật thông tin xe
    @PUT("api/v1/user-service/vehicle/{id}")
    Single<ApiResponse<Vehicle>> updateVehicle(@Path("id") Long vehicleId, @Body AddVehicleRequest request);

    // Lấy thông tin chi tiết xe theo ID
    @GET("api/v1/user-service/vehicle/{id}")
    Single<ApiResponse<Vehicle>> getVehicleById(@Path("id") Long vehicleId);

    // Parking Service APIs
    @GET("/api/v1/parking-service/lots")
    Single<ParkingLotResponse> getParkingLots(
        @Query("ownedByMe") Boolean ownedByMe,
        @Query("name") String name,
        @Query("city") String city,
        @Query("is24Hour") Boolean is24Hour,
        @Query("status") String status,
        @Query("page") Integer page,
        @Query("size") Integer size,
        @Query("sortBy") String sortBy,
        @Query("sortOrder") String sortOrder
    );

    @GET("/api/v1/parking-service/lots/{id}")
    Single<ParkingLotDetailResponse> getParkingLotDetail(@Path("id") Long id);

    @GET("/api/v1/parking-service/floors/{id}")
    Single<ParkingFloorDetailResponse> getParkingFloorDetail(@Path("id") Long id);

    @GET("/api/v1/parking-service/areas/{id}")
    Single<AreaDetailResponse> getAreaDetail(@Path("id") Long id);

    // Reservation APIs
    @POST("/api/v1/user-service/reservations")
    Single<ReservationResponse> createReservation(@Body ReservationRequest request);

    @GET("/api/v1/user-service/reservations")
    Single<ReservationListResponse> getMyReservations(@Query("ownedByMe") Boolean ownedByMe);

    @GET("/api/v1/user-service/reservations/{id}")
    Single<ReservationResponse> getReservationById(@Path("id") Long id);

    @PUT("/api/v1/user-service/reservations/{id}/cancel")
    Single<ReservationResponse> cancelReservation(@Path("id") Long id);


    // Payment APIs
    @POST("/api/v1/payment-service/payos/payment")
    Single<ApiResponse<Payment>> createPayment(@Query("amount") long amount);

    @PUT("/api/v1/payment-service/payos/cancel")
    Single<ApiResponse<PaymentCancel>> cancelPayment(
            @Query("orderCode") long orderCode,
            @Query("reason") String reason
    );

    @GET("/api/v1/payment-service/payos/status/{orderCode}")
    Single<ApiResponse<PaymentStatus>> checkPaymentStatus(@Path("orderCode") long orderCode);

    // Transaction APIs
    @GET("/api/v1/payment-service/transactions")
    Single<ApiResponse<TransactionResponse>> getTransactions(
            @Query("page") int page,
            @Query("size") int size,
            @Query("sortBy") String sortBy,
            @Query("sortOrder") String sortOrder,
            @Query("criteria") String criteria
    );

    // Wallet APIs
    @GET("/api/v1/payment-service/wallets/me")
    Single<WalletResponse> getMyWallet();
}
