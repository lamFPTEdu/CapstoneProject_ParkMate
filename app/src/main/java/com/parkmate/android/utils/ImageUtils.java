package com.parkmate.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

/**
 * Utility class để xử lý hình ảnh
 */
public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * Decode Base64 string thành Bitmap
     * @param base64String String có format "data:image/png;base64,iVBORw0KGgo..." hoặc chỉ Base64 thuần
     * @return Bitmap hoặc null nếu decode thất bại
     */
    public static Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            if (base64String == null || base64String.isEmpty()) {
                Log.w(TAG, "Base64 string is null or empty");
                return null;
            }

            // Loại bỏ prefix "data:image/png;base64," hoặc "data:image/jpeg;base64," nếu có
            String base64Image = base64String;
            if (base64String.contains(",")) {
                String[] parts = base64String.split(",");
                if (parts.length > 1) {
                    base64Image = parts[1];
                }
            }

            // Decode Base64 string thành byte array
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);

            // Chuyển byte array thành Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap == null) {
                Log.e(TAG, "Không thể decode Base64 thành Bitmap");
            } else {
                Log.d(TAG, "Decode thành công: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            }

            return bitmap;

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Lỗi decode Base64: " + e.getMessage(), e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi không xác định khi decode QR code: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Kiểm tra xem string có phải Base64 image không
     */
    public static boolean isBase64Image(String str) {
        return str != null && (str.startsWith("data:image/") || str.matches("^[A-Za-z0-9+/=]+$"));
    }

    /**
     * Scale bitmap về kích thước mong muốn
     */
    public static Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        if (bitmap == null) return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        float scaleWidth = ((float) maxWidth) / width;
        float scaleHeight = ((float) maxHeight) / height;
        float scale = Math.min(scaleWidth, scaleHeight);

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}

