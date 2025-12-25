package com.parkmate.android.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Helper class for QR code display enhancements:
 * - Max brightness when showing QR screens
 * - Fullscreen QR dialog when tapping QR code
 */
public class QrCodeHelper {

    private static float originalBrightness = -1f;

    /**
     * Set screen brightness to maximum for easier QR scanning
     * Call this in onCreate() or onResume()
     */
    public static void setMaxBrightness(Activity activity) {
        if (activity == null)
            return;

        Window window = activity.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            // Store original brightness to restore later
            originalBrightness = layoutParams.screenBrightness;
            // Set to maximum brightness (1.0f = 100%)
            layoutParams.screenBrightness = 1.0f;
            window.setAttributes(layoutParams);
        }
    }

    /**
     * Restore screen brightness to original value
     * Call this in onDestroy() or onPause()
     */
    public static void restoreBrightness(Activity activity) {
        if (activity == null)
            return;

        Window window = activity.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            // Restore original brightness (-1 = system default)
            layoutParams.screenBrightness = originalBrightness >= 0 ? originalBrightness : -1f;
            window.setAttributes(layoutParams);
        }
    }

    /**
     * Show fullscreen QR code dialog
     * 
     * @param activity The activity context
     * @param qrBitmap The QR code bitmap to display
     */
    public static void showFullscreenQrDialog(Activity activity, Bitmap qrBitmap) {
        if (activity == null || qrBitmap == null)
            return;

        Dialog dialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Create fullscreen ImageView
        ImageView imageView = new ImageView(activity);
        imageView.setImageBitmap(qrBitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundColor(Color.WHITE);
        imageView.setPadding(48, 48, 48, 48);

        // Set layout params
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(params);

        // Tap to dismiss
        imageView.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(imageView);

        // Set dialog properties
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setGravity(Gravity.CENTER);

            // Keep max brightness in dialog
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = 1.0f;
            window.setAttributes(layoutParams);
        }

        dialog.setCancelable(true);
        dialog.show();
    }

    /**
     * Setup QR ImageView to show fullscreen on tap
     * 
     * @param activity    The activity context
     * @param qrImageView The QR code ImageView
     * @param qrBitmap    The QR code bitmap (can be set later via returned
     *                    listener)
     */
    public static void setupQrClickToFullscreen(Activity activity, ImageView qrImageView, Bitmap qrBitmap) {
        if (qrImageView == null)
            return;

        qrImageView.setOnClickListener(v -> {
            showFullscreenQrDialog(activity, qrBitmap);
        });
    }
}
