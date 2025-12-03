package com.parkmate.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.parkmate.android.activity.ProfileActivity;

/**
 * Helper class để kiểm tra trạng thái xác thực căn cước công dân
 * Sử dụng trước khi cho phép user tạo reservation hoặc subscription
 */
public class IdVerificationHelper {

    /**
     * Kiểm tra xem user đã xác thực căn cước chưa
     * Nếu chưa, hiển thị dialog yêu cầu xác thực
     *
     * @param context Context để hiển thị dialog
     * @return true nếu đã xác thực, false nếu chưa
     */
    public static boolean checkIdVerification(Context context) {
        boolean isVerified = UserManager.getInstance().isIdVerified();

        if (!isVerified) {
            showIdVerificationDialog(context);
        }

        return isVerified;
    }

    /**
     * Hiển thị dialog yêu cầu xác thực căn cước
     */
    private static void showIdVerificationDialog(Context context) {
        new AlertDialog.Builder(context)
            .setTitle("Yêu cầu xác thực danh tính")
            .setMessage("Bạn cần xác thực căn cước công dân trước khi thực hiện chức năng này.\n\n" +
                       "Vui lòng vào Hồ sơ → Thông tin cá nhân để cập nhật.")
            .setPositiveButton("Đến Hồ sơ", (dialog, which) -> {
                // Navigate to Profile screen
                if (context instanceof Activity) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    context.startActivity(intent);
                }
            })
            .setNegativeButton("Đóng", null)
            .setCancelable(true)
            .show();
    }

    /**
     * Kiểm tra và hiển thị toast nếu chưa xác thực
     * Dùng cho trường hợp đơn giản, không cần dialog
     */
    public static boolean checkWithToast(Context context) {
        boolean isVerified = UserManager.getInstance().isIdVerified();

        if (!isVerified) {
            Toast.makeText(context,
                "⚠️ Tài khoản chưa xác thực. Vui lòng cập nhật căn cước công dân trong Hồ sơ.",
                Toast.LENGTH_LONG).show();
        }

        return isVerified;
    }
}

