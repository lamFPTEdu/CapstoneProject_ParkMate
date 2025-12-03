package com.parkmate.android.utils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;

import com.parkmate.android.activity.LoginActivity;

/**
 * AuthHelper - Utility class để quản lý authentication và guest mode
 * Cung cấp các phương thức tiện ích cho việc check login và điều hướng
 */
public class AuthHelper {

    /**
     * Kiểm tra xem user đã đăng nhập hay chưa
     * @return true nếu đã đăng nhập, false nếu chưa (guest mode)
     */
    public static boolean isUserLoggedIn() {
        try {
            TokenManager tokenManager = TokenManager.getInstance();
            String token = tokenManager.getToken();
            return !TextUtils.isEmpty(token);
        } catch (IllegalStateException e) {
            // TokenManager chưa được init
            return false;
        }
    }

    /**
     * Hiển thị dialog yêu cầu đăng nhập
     * @param context Context
     * @param featureName Tên chức năng cần đăng nhập (vd: "đặt chỗ đỗ xe", "mua vé tháng")
     */
    public static void showLoginRequiredDialog(Context context, String featureName) {
        new AlertDialog.Builder(context)
                .setTitle("Yêu cầu đăng nhập")
                .setMessage("Bạn cần đăng nhập để sử dụng chức năng " + featureName + ".")
                .setPositiveButton("Đăng nhập", (dialog, which) -> {
                    navigateToLogin(context);
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }

    /**
     * Hiển thị dialog yêu cầu đăng nhập với custom message
     * @param context Context
     * @param message Custom message
     */
    public static void showLoginRequiredDialogWithMessage(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle("Yêu cầu đăng nhập")
                .setMessage(message)
                .setPositiveButton("Đăng nhập", (dialog, which) -> {
                    navigateToLogin(context);
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }

    /**
     * Chuyển đến màn hình đăng nhập
     * @param context Context
     */
    public static void navigateToLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        // Clear back stack để user không quay lại screen trước khi đăng nhập
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * Chuyển đến màn hình đăng nhập với flag để quay lại màn hình hiện tại sau khi đăng nhập
     * @param context Context
     */
    public static void navigateToLoginForResult(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        // Không clear back stack - cho phép user quay lại sau khi login
        context.startActivity(intent);
    }

    /**
     * Kiểm tra login và hiển thị dialog nếu chưa đăng nhập
     * @param context Context
     * @param featureName Tên chức năng
     * @return true nếu đã đăng nhập, false nếu chưa đăng nhập (đã hiển thị dialog)
     */
    public static boolean checkLoginOrShowDialog(Context context, String featureName) {
        if (!isUserLoggedIn()) {
            showLoginRequiredDialog(context, featureName);
            return false;
        }
        return true;
    }
}

