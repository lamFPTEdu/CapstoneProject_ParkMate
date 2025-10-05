package com.parkmate.android.utils;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

/**
 * Helper class để thêm loading indicator vào button.
 * Sử dụng compound drawable để hiển thị ProgressBar bên trong button.
 */
public class LoadingButton {

    private final Button button;
    private String originalText;
    private boolean isLoading = false;
    private boolean originalEnabledState = true;

    public LoadingButton(Button button) {
        this.button = button;
        this.originalText = button.getText().toString();
        this.originalEnabledState = button.isEnabled();
    }

    /**
     * Hiển thị loading trong button
     */
    public void showLoading() {
        showLoading("Đang xử lý...");
    }

    /**
     * Hiển thị loading trong button với text tùy chỉnh
     */
    public void showLoading(String loadingText) {
        if (isLoading) return;
        isLoading = true;

        // Lưu trạng thái ban đầu
        originalText = button.getText().toString();
        originalEnabledState = button.isEnabled();

        // Tạo ProgressBar nhỏ
        ProgressBar progressBar = new ProgressBar(button.getContext());
        progressBar.setIndeterminate(true);

        // Đo kích thước để tạo drawable
        int size = (int) (button.getTextSize() * 1.2); // Kích thước dựa trên text size
        progressBar.measure(size, size);
        progressBar.layout(0, 0, size, size);

        // Disable button và thay đổi text
        button.setEnabled(false);
        button.setText(loadingText);

        // Thêm padding để tạo khoảng cách cho spinner
        int paddingStart = button.getPaddingStart();
        button.setPadding(paddingStart + size + 16, button.getPaddingTop(),
                         button.getPaddingEnd(), button.getPaddingBottom());

        // Note: Để hiển thị ProgressBar thực sự trong button, cần custom view
        // Phương pháp này chỉ disable button và thay đổi text
    }

    /**
     * Ẩn loading và khôi phục button
     */
    public void hideLoading() {
        if (!isLoading) return;
        isLoading = false;

        // Khôi phục trạng thái ban đầu
        button.setEnabled(originalEnabledState);
        button.setText(originalText);

        // Reset padding
        int paddingStart = button.getPaddingStart();
        int size = (int) (button.getTextSize() * 1.2);
        button.setPadding(Math.max(0, paddingStart - size - 16), button.getPaddingTop(),
                         button.getPaddingEnd(), button.getPaddingBottom());
    }

    /**
     * Kiểm tra xem có đang loading không
     */
    public boolean isLoading() {
        return isLoading;
    }

    /**
     * Set text mới cho button (sẽ được khôi phục khi hideLoading)
     */
    public void setText(String text) {
        originalText = text;
        if (!isLoading) {
            button.setText(text);
        }
    }

    /**
     * Lấy button gốc
     */
    public Button getButton() {
        return button;
    }
}
