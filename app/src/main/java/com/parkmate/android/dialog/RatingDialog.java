package com.parkmate.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.parkmate.android.R;

/**
 * Dialog để người dùng đánh giá bãi đỗ xe
 */
public class RatingDialog extends Dialog {

    public interface OnRatingSubmitListener {
        void onSubmit(int rating, String title, String comment);
    }

    private final String parkingLotName;
    private final OnRatingSubmitListener listener;

    private ImageView star1, star2, star3, star4, star5;
    private TextView tvParkingLotName, tvRatingDescription;
    private TextInputEditText etTitle, etComment;
    private MaterialButton btnCancel, btnSubmit;

    private int selectedRating = 0;

    public RatingDialog(@NonNull Context context, String parkingLotName, OnRatingSubmitListener listener) {
        super(context);
        this.parkingLotName = parkingLotName;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_rating);

        // Set dialog window properties
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Set dialog width to 90% of screen width
            getWindow().setLayout(
                    (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        initViews();
        setupListeners();
        tvParkingLotName.setText(parkingLotName);
    }

    private void initViews() {
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);

        tvParkingLotName = findViewById(R.id.tvParkingLotName);
        tvRatingDescription = findViewById(R.id.tvRatingDescription);

        etTitle = findViewById(R.id.etTitle);
        etComment = findViewById(R.id.etComment);

        btnCancel = findViewById(R.id.btnCancel);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupListeners() {
        star1.setOnClickListener(v -> setRating(1));
        star2.setOnClickListener(v -> setRating(2));
        star3.setOnClickListener(v -> setRating(3));
        star4.setOnClickListener(v -> setRating(4));
        star5.setOnClickListener(v -> setRating(5));

        btnCancel.setOnClickListener(v -> dismiss());

        btnSubmit.setOnClickListener(v -> {
            if (selectedRating > 0) {
                String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
                String comment = etComment.getText() != null ? etComment.getText().toString().trim() : "";

                if (listener != null) {
                    listener.onSubmit(selectedRating, title, comment);
                }
                dismiss();
            }
        });
    }

    private void setRating(int rating) {
        selectedRating = rating;
        updateStars();
        updateRatingDescription();
        btnSubmit.setEnabled(true);
    }

    private void updateStars() {
        ImageView[] stars = {star1, star2, star3, star4, star5};
        int starColor = ContextCompat.getColor(getContext(), R.color.rating_star);
        int emptyColor = ContextCompat.getColor(getContext(), R.color.text_tertiary);

        for (int i = 0; i < stars.length; i++) {
            if (i < selectedRating) {
                stars[i].setImageResource(R.drawable.ic_star_filled);
                stars[i].setColorFilter(starColor);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_outline);
                stars[i].setColorFilter(emptyColor);
            }
        }
    }

    private void updateRatingDescription() {
        String description;
        switch (selectedRating) {
            case 1:
                description = "Rất tệ";
                break;
            case 2:
                description = "Tệ";
                break;
            case 3:
                description = "Bình thường";
                break;
            case 4:
                description = "Tốt";
                break;
            case 5:
                description = "Rất tốt";
                break;
            default:
                description = "Chọn số sao";
                break;
        }
        tvRatingDescription.setText(description);
    }
}

