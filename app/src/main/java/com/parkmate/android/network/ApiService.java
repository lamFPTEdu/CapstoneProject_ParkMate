package com.parkmate.android.network;

import com.parkmate.android.model.request.RegisterRequest;
import com.parkmate.android.model.request.ForgotPasswordRequest;
import com.parkmate.android.model.request.ResetPasswordRequest;
import com.parkmate.android.model.response.RegisterResponse;
import com.parkmate.android.model.response.OtpVerifyResponse;
import com.parkmate.android.model.response.ForgotPasswordResponse;
import com.parkmate.android.model.response.ResetPasswordResponse;
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
import com.parkmate.android.model.response.UserProfileResponse;
import com.parkmate.android.model.response.MobileDeviceResponse;
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
import com.parkmate.android.model.request.CreateMobileDeviceRequest;
import com.parkmate.android.model.response.ParkingSessionResponse;
import com.parkmate.android.model.request.CreateRatingRequest;
import com.parkmate.android.model.Rating;

import java.util.Map;

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

        // Endpoint verify OTP chuẩn: PUT /auth/verify?verifyCode=XXXXXX với body rỗng
        // {}
        @PUT("api/v1/user-service/auth/verify")
        Single<OtpVerifyResponse> verifyOtp(@Query("verifyCode") String verifyCode, @Body EmptyJsonBody emptyBody);

        // Resend OTP (chỉ dành cho đăng ký)
        @PUT("api/v1/user-service/auth/resend")
        Single<com.parkmate.android.model.response.ApiResponse<Void>> resendOtp(@Query("email") String email);

        // Đăng nhập
        @POST("api/v1/user-service/auth/login")
        Single<LoginResponse> login(@Body LoginRequest request);

        // Refresh token
        @POST("api/v1/user-service/auth/refresh")
        Single<com.parkmate.android.model.response.RefreshTokenResponse> refreshToken(
                        @Body com.parkmate.android.model.request.RefreshTokenRequest request);

        // Forgot Password - Gửi mã reset về email
        @POST("api/v1/user-service/auth/forgot-password")
        Single<ForgotPasswordResponse> forgotPassword(@Body ForgotPasswordRequest request);

        // Reset Password - Đặt lại mật khẩu với mã reset
        @POST("api/v1/user-service/auth/reset-password")
        Single<ResetPasswordResponse> resetPassword(@Body ResetPasswordRequest request);

        // Upload ảnh CCCD
        @Multipart
        @POST("api/v1/user-service/upload/image/entity")
        Single<UploadImageResponse> uploadIdImage(
                        @Query("entityId") Long entityId,
                        @Query("imageType") String imageType,
                        @Part MultipartBody.Part file);

        // Lấy danh sách xe của user
        @GET("api/v1/user-service/vehicle")
        Single<ApiResponse<VehicleResponse>> getVehicles(
                        @Query("page") int page,
                        @Query("size") int size,
                        @Query("sortBy") String sortBy,
                        @Query("sortOrder") String sortOrder,
                        @Query("ownedByMe") boolean ownedByMe);

        // Lấy danh sách xe với filter (cho reservation)
        @GET("api/v1/user-service/vehicle")
        Single<ApiResponse<VehicleResponse>> getVehiclesWithFilter(
                        @Query("page") int page,
                        @Query("size") int size,
                        @Query("sortBy") String sortBy,
                        @Query("sortOrder") String sortOrder,
                        @Query("ownedByMe") boolean ownedByMe,
                        @Query("parkingLotId") Long parkingLotId);

        // Thêm xe mới
        @POST("api/v1/user-service/vehicle")
        Single<ApiResponse<Vehicle>> addVehicle(@Body AddVehicleRequest request);

        // Cập nhật thông tin user (xác thực CCCD)
        @PUT("api/v1/user-service/users")
        Single<UpdateUserResponse> updateUser(@Body UpdateUserRequest request);

        // Lấy thông tin user theo ID
        @GET("api/v1/user-service/users/{id}")
        Single<UserInfoResponse> getUserInfo(@Path("id") String userId);

        // Lấy thông tin user hiện tại (bao gồm QR code)
        @GET("api/v1/user-service/users/me")
        Single<ApiResponse<UserProfileResponse>> getCurrentUserProfile();

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
                        @Query("sortOrder") String sortOrder);

        @GET("/api/v1/parking-service/lots/{id}")
        Single<ParkingLotDetailResponse> getParkingLotDetail(@Path("id") Long id);

        @GET("/api/v1/parking-service/ratings")
        Single<com.parkmate.android.model.response.ParkingLotRatingsResponse> getParkingLotRatings(
                        @Query("lotId") Long lotId,
                        @Query("page") int page,
                        @Query("size") int size,
                        @Query("sortBy") String sortBy,
                        @Query("sortOrder") String sortOrder);

        // Get all ratings by current user (ownedByMe=true)
        @GET("/api/v1/parking-service/ratings?ownedByMe=true")
        Single<com.parkmate.android.model.response.ParkingLotRatingsResponse> getMyRatings(
                        @Query("page") int page,
                        @Query("size") int size,
                        @Query("sortBy") String sortBy,
                        @Query("sortOrder") String sortOrder);

        // Update rating
        @PUT("/api/v1/parking-service/ratings/{id}")
        Single<ApiResponse<com.parkmate.android.model.ParkingLotRating>> updateRating(
                        @Path("id") long ratingId,
                        @Body com.parkmate.android.model.request.UpdateRatingRequest request);

        // Delete rating
        @DELETE("/api/v1/parking-service/ratings/{id}")
        Single<ApiResponse<String>> deleteRating(@Path("id") long ratingId);

        @GET("/api/v1/parking-service/floors/{id}")
        Single<ParkingFloorDetailResponse> getParkingFloorDetail(@Path("id") Long id);

        @GET("/api/v1/parking-service/areas/{id}")
        Single<AreaDetailResponse> getAreaDetail(@Path("id") Long id);

        @GET("/api/v1/parking-service/lots/{id}/available-spots")
        Single<com.parkmate.android.model.response.AvailableSpotResponse> checkAvailableSpots(
                        @Path("id") Long parkingLotId,
                        @Query("reservedFrom") String reservedFrom,
                        @Query("assumedStayMinute") int assumedStayMinute,
                        @Query("vehicleType") String vehicleType);

        // Reservation APIs
        @POST("/api/v1/user-service/reservations")
        Single<ReservationResponse> createReservation(@Body ReservationRequest request);

        @GET("/api/v1/user-service/reservations")
        Single<ReservationListResponse> getMyReservations(@Query("ownedByMe") Boolean ownedByMe);

        @GET("/api/v1/user-service/reservations")
        Single<ReservationListResponse> getMyReservations(
                        @Query("ownedByMe") Boolean ownedByMe,
                        @Query("page") int page,
                        @Query("size") int size,
                        @Query("sortBy") String sortBy,
                        @Query("sortOrder") String sortOrder);

        @GET("/api/v1/user-service/reservations/{id}")
        Single<ReservationResponse> getReservationById(@Path("id") Long id);

        @DELETE("/api/v1/user-service/reservations/{id}")
        Single<ApiResponse<Void>> cancelReservation(@Path("id") Long reservationId);

        // Hold Reservation APIs
        @POST("/api/v1/user-service/reservations/hold")
        Single<ApiResponse<com.parkmate.android.model.response.HoldReservationResponse>> holdReservation(
                        @Body com.parkmate.android.model.request.HoldReservationRequest request);

        @DELETE("/api/v1/user-service/reservations/hold/{holdId}")
        Single<ApiResponse<Void>> releaseHold(@Path("holdId") String holdId);

        // Payment APIs
        @POST("/api/v1/payment-service/payos/payment")
        Single<ApiResponse<Payment>> createPayment(@Query("amount") long amount);

        @POST("/api/v1/payment-service/payos/cancel")
        Single<ApiResponse<PaymentCancel>> cancelPayment(
                        @Query("orderCode") long orderCode);

        @GET("/api/v1/payment-service/payos/status/{orderCode}")
        Single<ApiResponse<PaymentStatus>> checkPaymentStatus(@Path("orderCode") long orderCode);

        // Transaction APIs
        @GET("/api/v1/payment-service/transactions")
        Single<ApiResponse<TransactionResponse>> getTransactions(
                        @Query("page") int page,
                        @Query("size") int size,
                        @Query("sortBy") String sortBy,
                        @Query("sortOrder") String sortOrder,
                        @Query("criteria") String criteria);

        // Wallet APIs
        @GET("/api/v1/payment-service/wallets/me")
        Single<WalletResponse> getMyWallet();

        // Mobile Device APIs (for Push Notifications)
        // Đăng ký FCM token để nhận push notifications
        @POST("/api/v1/user-service/mobile-device")
        Single<ApiResponse<MobileDeviceResponse>> registerMobileDevice(@Body CreateMobileDeviceRequest request);

        // Parking Session APIs (Lịch sử đỗ xe)
        @GET("/api/v1/parking-service/sessions")
        Single<ApiResponse<ParkingSessionResponse>> getParkingSessions(
                        @Query("page") int page,
                        @Query("size") int size,
                        @Query("sortBy") String sortBy,
                        @Query("sortOrder") String sortOrder,
                        @Query("ownedByMe") boolean ownedByMe);

        @GET("/api/v1/parking-service/sessions")
        Single<ApiResponse<ParkingSessionResponse>> getParkingSessionsWithFilters(
                        @Query("page") int page,
                        @Query("size") int size,
                        @Query("sortBy") String sortBy,
                        @Query("sortOrder") String sortOrder,
                        @Query("ownedByMe") boolean ownedByMe,
                        @Query("sessionStatus") String sessionStatus,
                        @Query("referenceType") String referenceType,
                        @Query("startTime") String startTime,
                        @Query("endTime") String endTime,
                        @Query("totalLessThan") Long totalLessThan,
                        @Query("totalMoreThan") Long totalMoreThan,
                        @Query("durationMinuteGreaterThan") Integer durationMinuteGreaterThan,
                        @Query("durationMinuteLessThan") Integer durationMinuteLessThan);

        // Subscription APIs
        @GET("/api/v1/user-service/user-subscriptions/floors/availability")
        Single<ApiResponse<java.util.List<com.parkmate.android.model.ParkingFloor>>> getAvailableFloors(
                        @Query("parkingLotId") long parkingLotId,
                        @Query("vehicleId") long vehicleId,
                        @Query("subscriptionPackageId") long subscriptionPackageId,
                        @Query("startDate") String startDate);

        @GET("/api/v1/user-service/user-subscriptions/areas/availability")
        Single<ApiResponse<java.util.List<com.parkmate.android.model.ParkingArea>>> getAvailableAreas(
                        @Query("floorId") long floorId,
                        @Query("vehicleId") long vehicleId,
                        @Query("subscriptionPackageId") long subscriptionPackageId,
                        @Query("startDate") String startDate);

        @GET("/api/v1/user-service/user-subscriptions/spots/availability")
        Single<ApiResponse<java.util.List<com.parkmate.android.model.ParkingSpot>>> getAvailableSpots(
                        @Query("areaId") long areaId,
                        @Query("vehicleId") long vehicleId,
                        @Query("subscriptionPackageId") long subscriptionPackageId,
                        @Query("startDate") String startDate);

        @POST("/api/v1/user-service/user-subscriptions/hold-spot")
        Single<ApiResponse<Boolean>> holdSpot(@Query("spotId") long spotId);

        @DELETE("/api/v1/user-service/user-subscriptions/hold-spot")
        Single<ApiResponse<com.parkmate.android.model.ParkingSpot>> releaseHoldSpot(@Query("spotId") long spotId);

        @POST("/api/v1/user-service/user-subscriptions")
        Single<ApiResponse<com.parkmate.android.model.UserSubscription>> createUserSubscription(
                        @Body Map<String, Object> request);

        @GET("/api/v1/user-service/user-subscriptions")
        Single<ApiResponse<com.parkmate.android.model.response.UserSubscriptionResponse>> getUserSubscriptions(
                        @Query("page") int page,
                        @Query("size") int size,
                        @Query("sortBy") String sortBy,
                        @Query("sortOrder") String sortOrder,
                        @Query("ownedByMe") boolean ownedByMe);

        @PUT("/api/v1/user-service/user-subscriptions/{id}")
        Single<ApiResponse<com.parkmate.android.model.UserSubscription>> renewUserSubscription(
                        @Path("id") long subscriptionId,
                        @Body Map<String, Object> request);

        // Rating APIs
        @POST("/api/v1/parking-service/ratings/lot/{lotId}")
        Single<ApiResponse<Rating>> createRating(
                        @Path("lotId") long lotId,
                        @Body CreateRatingRequest request);

        // Cancel Subscription API
        @POST("/api/v1/user-service/user-subscriptions/{id}/cancel")
        Single<ApiResponse<com.parkmate.android.model.UserSubscription>> cancelUserSubscription(
                        @Path("id") long subscriptionId,
                        @Body com.parkmate.android.model.request.CancelSubscriptionRequest request);
}
