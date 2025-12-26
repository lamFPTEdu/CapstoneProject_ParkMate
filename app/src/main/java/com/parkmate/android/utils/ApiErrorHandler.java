package com.parkmate.android.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.parkmate.android.model.response.ErrorResponse;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import retrofit2.HttpException;

/**
 * Utility class để xử lý API errors tập trung
 * Cung cấp error messages thân thiện với người dùng Việt Nam
 * 
 * Error codes từ 3 services: USER-SERVICE, PARKING-LOT-SERVICE, PAYMENT-SERVICE
 */
public class ApiErrorHandler {

    private static final String TAG = "ApiErrorHandler";

    // Error code to Vietnamese message mapping
    private static final Map<String, String> ERROR_MESSAGES = new HashMap<String, String>() {
        {
            // ===================================================================
            // USER-SERVICE
            // ===================================================================

            // General Errors (10xx)
            put("UNCATEGORIZED_EXCEPTION", "Có lỗi xảy ra, vui lòng thử lại");
            put("VALIDATION_ERROR", "Thông tin không hợp lệ");
            put("INVALID_REQUEST", "Yêu cầu không hợp lệ");
            put("INVALID_ENUM", "Giá trị không hợp lệ");
            put("IO_ERROR", "Lỗi hệ thống, vui lòng thử lại");
            put("UNAUTHENTICATED", "Bạn cần đăng nhập để tiếp tục");
            put("OTHER_CLIENT_ERROR", "Có lỗi xảy ra, vui lòng thử lại");

            // User Errors (21xx)
            put("USER_NOT_FOUND", "Không tìm thấy người dùng");
            put("USER_ALREADY_EXISTS", "Người dùng đã tồn tại");
            put("EMAIL_ALREADY_EXISTS", "Email đã được sử dụng");
            put("PHONE_ALREADY_EXISTS", "Số điện thoại đã được sử dụng");
            put("USER_NAME_ALREADY_EXISTS", "Tên đăng nhập đã được sử dụng");

            // Mobile Device Errors (22xx)
            put("DEVICE_NOT_FOUND", "Không tìm thấy thiết bị");
            put("DEVICE_ALREADY_REGISTERED", "Thiết bị đã được đăng ký");
            put("DEVICE_NOT_ACTIVE", "Thiết bị không hoạt động");

            // Vehicle Errors (23xx)
            put("VEHICLE_NOT_FOUND", "Không tìm thấy xe");
            put("VEHICLE_ALREADY_EXISTS", "Biển số xe đã tồn tại");
            put("INVALID_VEHICLE_TYPE", "Loại xe không hợp lệ");
            put("VEHICLE_NOT_BELONG_TO_USER", "Xe không thuộc về bạn");
            put("VEHICLE_HAS_ACTIVE_RESERVATION", "Không thể xóa xe đang có đặt chỗ");
            put("VEHICLE_HAS_ACTIVE_SUBSCRIPTION", "Không thể xóa xe đang có gói đăng ký");
            put("VEHICLE_HAS_ACTIVE_SESSION", "Không thể xóa xe đang trong phiên gửi xe");
            put("VEHICLE_RECENTLY_DELETED", "Xe này vừa bị xóa gần đây");

            // Partner Errors (24xx)
            put("PARTNER_NOT_FOUND", "Không tìm thấy đối tác");
            put("PARTNER_ALREADY_EXISTS", "Đối tác đã tồn tại");
            put("PARTNER_INACTIVE", "Đối tác không hoạt động");

            // Account Errors (25xx)
            put("ACCOUNT_NOT_FOUND", "Tài khoản không tồn tại");
            put("ACCOUNT_ALREADY_EXISTS", "Tài khoản đã tồn tại");
            put("ACCOUNT_INACTIVE", "Tài khoản không hoạt động");
            put("EMAIL_ALREADY_VERIFIED", "Email đã được xác thực");
            put("EMAIL_RESEND_FAILED", "Không thể gửi lại email xác thực");
            put("ACCOUNT_NOT_VERIFIED", "Vui lòng xác thực email trước khi đăng nhập");
            put("ACCOUNT_TEMPORARILY_LOCKED", "Tài khoản tạm thời bị khóa, vui lòng thử lại sau");

            // Auth Errors (26xx)
            put("PASSWORD_MISMATCH", "Mật khẩu không đúng");
            put("USER_INFO_NOT_FOUND", "Không tìm thấy thông tin người dùng");
            put("STORE_REFRESH_TOKEN_FAILED", "Lỗi hệ thống, vui lòng đăng nhập lại");
            put("INVALID_REFRESH_TOKEN", "Phiên đăng nhập hết hạn");
            put("INVALID_VALIDATION_CODE", "Mã xác thực không đúng");
            put("INVALID_PASSWORD_RESET_CODE", "Mã đặt lại mật khẩu không hợp lệ");
            put("PASSWORD_RESET_CODE_EXPIRED", "Mã đặt lại mật khẩu đã hết hạn");
            put("PASSWORD_MISMATCH_WITH_ATTEMPTS", "Sai mật khẩu");

            // Partner Registration Errors (27xx)
            put("TAX_NUMBER_ALREADY_EXISTS", "Mã số thuế đã tồn tại");
            put("PARTNER_REGISTRATION_NOT_FOUND", "Không tìm thấy yêu cầu đăng ký đối tác");
            put("PARTNER_REGISTRATION_ALREADY_REVIEWED", "Yêu cầu đăng ký đã được xem xét");

            // File Upload Errors (28xx)
            put("FILE_EMPTY", "File tải lên trống");
            put("FILE_SIZE_EXCEEDED", "File tải lên quá lớn");
            put("FILE_TYPE_NOT_ALLOWED", "Định dạng file không được hỗ trợ");
            put("S3_UPLOAD_FAILED", "Tải file lên thất bại");

            // Reservation Errors (29xx)
            put("INVALID_RESERVATION_TIME", "Thời gian đặt chỗ không hợp lệ");
            put("RESERVATION_NOT_FOUND", "Không tìm thấy đặt chỗ");
            put("RESERVATION_ALREADY_USED", "Đặt chỗ đã được sử dụng");
            put("RESERVATION_EXPIRED", "Đặt chỗ đã hết hạn");
            put("INVALID_RESERVATION_DATA", "Thông tin đặt chỗ không hợp lệ");
            put("RESERVATION_ALREADY_CANCELLED", "Đặt chỗ đã bị hủy");
            put("RESERVATION_CANNOT_BE_CANCELLED", "Không thể hủy đặt chỗ ở trạng thái hiện tại");
            put("RESERVATION_NOT_BELONG_TO_USER", "Đặt chỗ không thuộc về bạn");
            put("MAX_ACTIVE_RESERVATIONS_EXCEEDED", "Đã đạt giới hạn số đặt chỗ");

            // Wallet Errors - User Service (32xx)
            put("WALLET_NOT_FOUND", "Không tìm thấy ví");
            put("WALLET_ALREADY_EXISTS", "Ví đã tồn tại");
            put("INSUFFICIENT_WALLET_BALANCE", "Số dư ví không đủ");
            put("WALLET_TOPUP_FAILED", "Nạp tiền thất bại");
            put("WALLET_TRANSACTION_NOT_FOUND", "Không tìm thấy giao dịch");
            put("WALLET_TRANSACTION_ALREADY_EXISTS", "Giao dịch đã tồn tại");
            put("INVALID_WALLET_OPERATION", "Thao tác ví không hợp lệ");
            put("WALLET_IS_INACTIVE", "Ví không hoạt động");
            put("WALLET_DEDUCTION_FAILED", "Trừ tiền thất bại");

            // User Subscription Errors (21xxx)
            put("INVALID_USER_SUBSCRIPTION_TIME", "Thời gian đăng ký không hợp lệ");
            put("USER_SUBSCRIPTION_NOT_FOUND", "Không tìm thấy gói đăng ký");
            put("USER_SUBSCRIPTION_ALREADY_USED", "Gói đăng ký đã được sử dụng");
            put("USER_SUBSCRIPTION_EXPIRED", "Gói đăng ký đã hết hạn");
            put("INVALID_USER_SUBSCRIPTION_DATA", "Thông tin gói đăng ký không hợp lệ");
            put("USER_SUBSCRIPTION_NOT_BELONG_TO_USER", "Gói đăng ký không thuộc về bạn");
            put("USER_SUBSCRIPTION_ALREADY_CANCELLED", "Gói đăng ký đã bị hủy trước đó");
            put("USER_SUBSCRIPTION_CANCEL_FAILED", "Không thể hủy gói đăng ký, vui lòng thử lại");

            // ===================================================================
            // PARKING-LOT-SERVICE
            // ===================================================================

            // General Errors
            put("UNAUTHORIZED", "Bạn không có quyền truy cập");
            put("VEHICLE_TYPE_MISMATCH", "Loại xe không phù hợp");
            put("UNABLE_TO_DELETE_MAP", "Không thể xóa bản đồ");
            put("RATING_NOT_FOUND", "Không tìm thấy đánh giá");

            // Parking Lot Errors (31xx)
            put("PARKING_NOT_FOUND", "Không tìm thấy bãi đỗ xe");
            put("INVALID_PARKING_LOT_STATUS_TRANSITION", "Không thể xóa bãi đỗ xe đang chờ duyệt");
            put("REASON_REQUIRED", "Vui lòng nhập lý do");

            // Parking Floor Errors (32xx)
            put("PARKING_FLOOR_NOT_FOUND", "Không tìm thấy tầng đỗ xe");
            put("INVALID_PARKING_FLOOR_STATUS_TRANSITION", "Tầng đỗ xe đang bị vô hiệu hóa");
            put("INACTIVE_FLOOR", "Tầng đỗ xe không hoạt động");

            // Parking Area Errors (33xx)
            put("PARKING_AREA_NOT_FOUND", "Không tìm thấy khu vực đỗ xe");

            // Pricing Rule Errors (34xx)
            put("PRICING_RULE_NOT_FOUND", "Không tìm thấy quy tắc giá");
            put("INVALID_RULE_SCOPE", "Phạm vi quy tắc không hợp lệ");
            put("PRICING_RULE_CAPACITY_MISMATCH", "Quy tắc giá không phù hợp với sức chứa");
            put("DUPLICATE_PRICING_RULE", "Quy tắc giá đã tồn tại");

            // Spot Errors (35xx)
            put("SPOT_NOT_FOUND", "Không tìm thấy chỗ đỗ");
            put("SPOT_COUNT_MISS_MATCH", "Số lượng chỗ đỗ không khớp");
            put("VEHICLE_TYPE_MISS_MATCH", "Không thể thêm chỗ đỗ vào khu vực này");
            put("BLOCK_REASON_REQUIRED", "Vui lòng nhập lý do khóa");
            put("SPOT_RELEASED_FAILED", "Không thể giải phóng chỗ đỗ");
            put("SPOT_HELD_FAILED", "Không thể giữ chỗ đỗ");

            // Session Errors (36xx)
            put("SESSION_NOT_FOUND", "Không tìm thấy phiên gửi xe");
            put("INVALID_IMAGE", "Hình ảnh không hợp lệ");

            // Subscription Errors (37xx)
            put("SUBSCRIPTION_NOT_FOUND", "Không tìm thấy gói đăng ký");

            // Policy Errors (38xx)
            put("POLICY_NOT_ENOUGH", "Chính sách không đủ");
            put("DUPLICATE_POLICY", "Chính sách đã tồn tại");
            put("MISSING_POLICY", "Thiếu chính sách");
            put("POLICY_NOT_FOUND", "Không tìm thấy chính sách");

            // Device Errors - Parking (39xx)
            put("DEVICE_ID_EXISTS", "Mã thiết bị đã tồn tại");

            // ===================================================================
            // PAYMENT-SERVICE
            // ===================================================================

            // General Errors
            put("DEVICE_FEE_CONFIG_NOT_FOUND", "Không tìm thấy cấu hình phí thiết bị");

            // Payment Errors (31xx)
            put("PAYMENT_NOT_FOUND", "Không tìm thấy thanh toán");
            put("PAYMENT_ALREADY_EXISTS", "Thanh toán đã tồn tại");
            put("PAYMENT_METHOD_NOT_SUPPORTED", "Phương thức thanh toán không được hỗ trợ");
            put("PAYMENT_PROCESSING_FAILED", "Xử lý thanh toán thất bại");
            put("INSUFFICIENT_FUNDS", "Số dư không đủ");
            put("REFUND_NOT_ALLOWED", "Không thể hoàn tiền cho thanh toán này");
            put("TRANSACTION_NOT_FOUND", "Không tìm thấy giao dịch");
            put("TRANSACTION_ALREADY_EXISTS", "Giao dịch đã tồn tại");
            put("TRANSACTION_FAILED", "Giao dịch thất bại");
            put("CURRENCY_NOT_SUPPORTED", "Loại tiền tệ không được hỗ trợ");
            put("EXCHANGE_RATE_NOT_FOUND", "Không tìm thấy tỷ giá");
            put("LIMIT_EXCEEDED", "Vượt quá giới hạn giao dịch");
            put("FRAUD_DETECTED", "Phát hiện giao dịch bất thường");
            put("PAYMENT_GATEWAY_UNAVAILABLE", "Cổng thanh toán tạm thời không khả dụng");
            put("INVALID_PAYMENT_DETAILS", "Thông tin thanh toán không hợp lệ");
            put("PAYMENT_ALREADY_PAID", "Không thể hủy thanh toán đã hoàn tất");

            // Wallet Errors - Payment (32xx)
            put("INVALID_TRANSACTION_TYPE", "Loại giao dịch không hợp lệ");

            // PayOS Errors (33xx)
            put("PAYOS_PAYMENT_CREATION_FAILED", "Tạo thanh toán PayOS thất bại");
            put("PAYOS_INVALID_SIGNATURE", "Chữ ký PayOS không hợp lệ");
            put("PAYOS_WEBHOOK_VERIFICATION_FAILED", "Xác thực PayOS webhook thất bại");
            put("WEBHOOK_PROCESS_FAILED", "Xử lý webhook thất bại");
            put("PAYOS_INVALID_AMOUNT", "Số tiền thanh toán tối thiểu là 1.000đ");
            put("PAYOS_ORDER_NOT_FOUND", "Không tìm thấy đơn hàng PayOS");
            put("CANCEL_FAILED", "Hủy thanh toán thất bại");
            put("PAYOUT_STATUS_CHECK_FAILED", "Kiểm tra trạng thái rút tiền thất bại");
            put("WITHDRAWAL_AMOUNT_TOO_LOW", "Số tiền rút tối thiểu là 10.000đ");
            put("WITHDRAWAL_AMOUNT_TOO_HIGH", "Số tiền rút vượt quá giới hạn");
            put("PAYOUT_CREATION_FAILED", "Tạo yêu cầu rút tiền thất bại");
            put("INVALID_BANK_ACCOUNT", "Thông tin tài khoản ngân hàng không hợp lệ");

            // Config & Withdrawal Errors (39xx)
            put("OPERATIONAL_FEE_CONFIG_NOT_FOUND", "Không tìm thấy cấu hình phí vận hành");
            put("SYSTEM_CONFIG_NOT_FOUND", "Không tìm thấy cấu hình hệ thống");
            put("INVALID_CONFIG_VALUE", "Giá trị cấu hình không hợp lệ");
            put("WITHDRAWAL_PERIOD_NOT_FOUND", "Không tìm thấy kỳ rút tiền");
            put("PERIOD_ALREADY_WITHDRAWN", "Kỳ này đã được rút tiền");
            put("INVALID_PERIOD_OWNER", "Kỳ rút tiền không thuộc về bạn");
            put("WITHDRAWAL_NOT_FOUND", "Không tìm thấy yêu cầu rút tiền");

            // ===================================================================
            // LEGACY CODES (for backward compatibility)
            // ===================================================================
            put("INVALID_CREDENTIALS", "Sai email hoặc mật khẩu");
            put("BAD_CREDENTIALS", "Sai email hoặc mật khẩu");
            put("ACCOUNT_NOT_ACTIVE", "Tài khoản chưa được kích hoạt");
            put("ACCOUNT_LOCKED", "Tài khoản đã bị khóa");
            put("ACCOUNT_DISABLED", "Tài khoản đã bị vô hiệu hóa");
            put("ACCOUNT_EXPIRED", "Tài khoản đã hết hạn");
            put("TOKEN_EXPIRED", "Phiên đăng nhập đã hết hạn");
            put("TOKEN_INVALID", "Phiên đăng nhập không hợp lệ");
            put("ACCESS_DENIED", "Bạn không có quyền thực hiện thao tác này");
            put("FORBIDDEN", "Bạn không có quyền truy cập");
            put("INVALID_OTP", "Mã OTP không đúng");
            put("OTP_INVALID", "Mã OTP không đúng");
            put("OTP_EXPIRED", "Mã OTP đã hết hạn");
            put("OTP_ALREADY_USED", "Mã OTP đã được sử dụng");
            put("OTP_MAX_ATTEMPTS", "Bạn đã nhập sai quá nhiều lần");
            put("NOT_FOUND", "Không tìm thấy dữ liệu");
            put("BAD_REQUEST", "Yêu cầu không hợp lệ");
            put("CONFLICT", "Dữ liệu xung đột, vui lòng thử lại");
            put("INTERNAL_ERROR", "Lỗi hệ thống, vui lòng thử lại sau");
            put("SERVICE_UNAVAILABLE", "Dịch vụ tạm thời không khả dụng");
        }
    };

    // HTTP status code to Vietnamese message mapping
    private static final Map<Integer, String> HTTP_STATUS_MESSAGES = new HashMap<Integer, String>() {
        {
            put(400, "Yêu cầu không hợp lệ");
            put(401, "Phiên đăng nhập đã hết hạn");
            put(402, "Số dư không đủ");
            put(403, "Bạn không có quyền thực hiện thao tác này");
            put(404, "Không tìm thấy dữ liệu");
            put(409, "Dữ liệu đã tồn tại");
            put(422, "Thông tin không hợp lệ");
            put(429, "Quá nhiều yêu cầu, vui lòng thử lại sau");
            put(500, "Lỗi máy chủ, vui lòng thử lại sau");
            put(502, "Lỗi kết nối máy chủ");
            put(503, "Dịch vụ tạm thời không khả dụng");
            put(504, "Kết nối quá chậm, vui lòng thử lại");
        }
    };

    /**
     * Get Vietnamese error message from error code
     */
    public static String getErrorMessage(String errorCode) {
        if (errorCode == null) {
            return "Có lỗi xảy ra";
        }
        return ERROR_MESSAGES.getOrDefault(errorCode, "Có lỗi xảy ra");
    }

    /**
     * Get Vietnamese message from HTTP status code
     */
    public static String getHttpErrorMessage(int statusCode) {
        return HTTP_STATUS_MESSAGES.getOrDefault(statusCode, "Lỗi kết nối (mã " + statusCode + ")");
    }

    /**
     * Parse and get error message from Throwable
     * Returns user-friendly Vietnamese message
     */
    public static String parseError(Throwable throwable) {
        if (throwable == null) {
            return "Có lỗi xảy ra";
        }

        // Network errors
        if (throwable instanceof TimeoutException || throwable instanceof SocketTimeoutException) {
            return "Kết nối quá chậm, vui lòng thử lại";
        }

        if (throwable instanceof UnknownHostException || throwable instanceof ConnectException) {
            return "Không có kết nối mạng";
        }

        if (throwable instanceof IOException) {
            return "Lỗi kết nối mạng";
        }

        // HTTP errors
        if (throwable instanceof HttpException) {
            return parseHttpException((HttpException) throwable);
        }

        // Default
        String message = throwable.getMessage();
        return message != null ? message : "Có lỗi xảy ra";
    }

    /**
     * Parse HttpException to get error message
     */
    public static String parseHttpException(HttpException httpException) {
        try {
            if (httpException.response() == null || httpException.response().errorBody() == null) {
                return getHttpErrorMessage(httpException.code());
            }

            String body = httpException.response().errorBody().string();

            if (body == null || body.isEmpty()) {
                return getHttpErrorMessage(httpException.code());
            }

            ErrorResponse errorResponse = new Gson().fromJson(body, ErrorResponse.class);

            if (errorResponse != null) {
                // Try to get error code message first
                if (errorResponse.getError() != null && errorResponse.getError().getCode() != null) {
                    String codeMessage = getErrorMessage(errorResponse.getError().getCode());
                    if (!codeMessage.equals("Có lỗi xảy ra")) {
                        return codeMessage;
                    }

                    // Use server message if available
                    if (errorResponse.getError().getMessage() != null &&
                            !errorResponse.getError().getMessage().isEmpty() &&
                            !errorResponse.getError().getMessage().equals("Uncategorized error") &&
                            !errorResponse.getError().getMessage().equals("An unexpected error occurred")) {
                        return errorResponse.getError().getMessage();
                    }
                }

                // Use top-level message
                if (errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty() &&
                        !errorResponse.getMessage().equals("An unexpected error occurred") &&
                        !errorResponse.getMessage().equals("Uncategorized error")) {
                    return errorResponse.getMessage();
                }
            }

            return getHttpErrorMessage(httpException.code());

        } catch (Exception e) {
            Log.e(TAG, "Error parsing HttpException", e);
            return getHttpErrorMessage(httpException.code());
        }
    }

    /**
     * Parse error and get ErrorResponse object
     */
    public static ErrorResponse parseErrorResponse(Throwable throwable) {
        if (!(throwable instanceof HttpException)) {
            return null;
        }

        try {
            HttpException httpException = (HttpException) throwable;
            if (httpException.response() == null || httpException.response().errorBody() == null) {
                return null;
            }

            String body = httpException.response().errorBody().string();
            if (body == null || body.isEmpty()) {
                return null;
            }

            return new Gson().fromJson(body, ErrorResponse.class);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing ErrorResponse", e);
            return null;
        }
    }

    /**
     * Show error toast
     */
    public static void showError(Context context, Throwable throwable) {
        String message = parseError(throwable);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Show error toast with custom prefix
     */
    public static void showError(Context context, Throwable throwable, String prefix) {
        String message = parseError(throwable);
        Toast.makeText(context, prefix + ": " + message, Toast.LENGTH_LONG).show();
    }

    /**
     * Check if error is authentication related
     */
    public static boolean isAuthError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            int code = ((HttpException) throwable).code();
            return code == 401 || code == 403;
        }
        return false;
    }

    /**
     * Check if error is network related
     */
    public static boolean isNetworkError(Throwable throwable) {
        return throwable instanceof UnknownHostException ||
                throwable instanceof ConnectException ||
                throwable instanceof SocketTimeoutException ||
                throwable instanceof TimeoutException ||
                throwable instanceof IOException;
    }

    /**
     * Check if error is server error (5xx)
     */
    public static boolean isServerError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            int code = ((HttpException) throwable).code();
            return code >= 500 && code < 600;
        }
        return false;
    }

    /**
     * Get error code from throwable
     */
    public static String getErrorCode(Throwable throwable) {
        ErrorResponse errorResponse = parseErrorResponse(throwable);
        if (errorResponse != null && errorResponse.getError() != null) {
            return errorResponse.getError().getCode();
        }
        return null;
    }
}
