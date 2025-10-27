package com.parkmate.android.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Helper class để xử lý edge-to-edge display cho tất cả các Activity
 * Đảm bảo UI không bị tràn lên status bar và navigation bar
 */
public class EdgeToEdgeHelper {

    /**
     * Enable edge-to-edge display và xử lý WindowInsets cho Activity
     * @param activity Activity cần setup
     */
    public static void setupEdgeToEdge(Activity activity) {
        if (activity == null) return;

        Window window = activity.getWindow();
        if (window == null) return;

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false);

        // Apply window insets to root view
        View rootView = window.getDecorView().findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

                // Apply padding to root view
                v.setPadding(0, insets.top, 0, insets.bottom);

                return WindowInsetsCompat.CONSUMED;
            });
        }
    }

    /**
     * Setup edge-to-edge với custom padding cho các view cụ thể
     * @param activity Activity cần setup
     * @param applyTop Có apply padding top (cho status bar) không
     * @param applyBottom Có apply padding bottom (cho navigation bar) không
     */
    public static void setupEdgeToEdge(Activity activity, boolean applyTop, boolean applyBottom) {
        if (activity == null) return;

        Window window = activity.getWindow();
        if (window == null) return;

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false);

        // Apply window insets to root view
        View rootView = window.getDecorView().findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

                // Apply padding selectively
                v.setPadding(
                    0,
                    applyTop ? insets.top : 0,
                    0,
                    applyBottom ? insets.bottom : 0
                );

                return WindowInsetsCompat.CONSUMED;
            });
        }
    }

    /**
     * Setup edge-to-edge cho một view cụ thể thay vì root view
     * @param view View cần apply insets
     * @param applyTop Có apply padding top không
     * @param applyBottom Có apply padding bottom không
     */
    public static void applyInsetsToView(View view, boolean applyTop, boolean applyBottom) {
        if (view == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                v.getPaddingLeft(),
                applyTop ? insets.top : v.getPaddingTop(),
                v.getPaddingRight(),
                applyBottom ? insets.bottom : v.getPaddingBottom()
            );

            return windowInsets;
        });
    }

    /**
     * Setup edge-to-edge với margin thay vì padding
     * @param view View cần apply margin
     * @param applyTop Có apply margin top không
     * @param applyBottom Có apply margin bottom không
     */
    public static void applyInsetsAsMargin(View view, boolean applyTop, boolean applyBottom) {
        if (view == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            if (v.getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams) {
                android.view.ViewGroup.MarginLayoutParams params =
                    (android.view.ViewGroup.MarginLayoutParams) v.getLayoutParams();

                if (applyTop) {
                    params.topMargin = insets.top;
                }
                if (applyBottom) {
                    params.bottomMargin = insets.bottom;
                }

                v.setLayoutParams(params);
            }

            return windowInsets;
        });
    }
}
