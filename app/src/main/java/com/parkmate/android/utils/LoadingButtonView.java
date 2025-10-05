package com.parkmate.android.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.appcompat.widget.AppCompatButton;

/**
 * Custom Button với loading indicator tích hợp sẵn
 * Hiển thị vòng tròn loading quay ngay bên trong nút
 */
public class LoadingButtonView extends AppCompatButton {

    private ProgressBar progressBar;
    private String originalText;
    private boolean isLoading = false;

    public LoadingButtonView(Context context) {
        super(context);
        init();
    }

    public LoadingButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        originalText = getText() != null ? getText().toString() : "";
    }

    /**
     * Hiển thị loading trong button
     */
    public void showLoading() {
        if (isLoading) return;
        isLoading = true;

        // Lưu text hiện tại
        originalText = getText() != null ? getText().toString() : "";

        // Ẩn text
        setText("");
        setEnabled(false);

        // Tạo ProgressBar nếu chưa có
        if (progressBar == null) {
            progressBar = new ProgressBar(getContext());
            progressBar.setIndeterminate(true);

            // Set kích thước cho ProgressBar
            int size = (int) (getContext().getResources().getDisplayMetrics().density * 24);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            params.gravity = Gravity.CENTER;
            progressBar.setLayoutParams(params);

            // Thêm vào view hierarchy
            if (getParent() instanceof FrameLayout) {
                ((FrameLayout) getParent()).addView(progressBar);
            } else {
                // Nếu parent không phải FrameLayout, cần wrap
                wrapInFrameLayout();
            }
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Ẩn loading và khôi phục button
     */
    public void hideLoading() {
        if (!isLoading) return;
        isLoading = false;

        // Khôi phục text và enable button
        setText(originalText);
        setEnabled(true);

        // Ẩn ProgressBar
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Wrap button trong FrameLayout để có thể overlay ProgressBar
     */
    private void wrapInFrameLayout() {
        if (getParent() == null) return;

        android.view.ViewGroup parent = (android.view.ViewGroup) getParent();
        int index = parent.indexOfChild(this);
        android.view.ViewGroup.LayoutParams params = getLayoutParams();

        parent.removeView(this);

        FrameLayout wrapper = new FrameLayout(getContext());
        wrapper.setLayoutParams(params);

        FrameLayout.LayoutParams newParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        setLayoutParams(newParams);

        wrapper.addView(this);

        // Tạo và thêm ProgressBar
        progressBar = new ProgressBar(getContext());
        progressBar.setIndeterminate(true);
        int size = (int) (getContext().getResources().getDisplayMetrics().density * 24);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(size, size);
        progressParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(progressParams);
        progressBar.setVisibility(View.GONE);
        wrapper.addView(progressBar);

        parent.addView(wrapper, index);
    }

    /**
     * Kiểm tra xem có đang loading không
     */
    public boolean isLoading() {
        return isLoading;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        if (!isLoading && text != null) {
            originalText = text.toString();
        }
    }
}

