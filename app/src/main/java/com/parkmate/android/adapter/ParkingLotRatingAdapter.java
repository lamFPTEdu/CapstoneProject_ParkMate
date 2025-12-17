package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parkmate.android.R;
import com.parkmate.android.model.ParkingLotRating;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ParkingLotRatingAdapter extends ListAdapter<ParkingLotRating, ParkingLotRatingAdapter.RatingViewHolder> {

    private static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            Locale.getDefault());
    private static final SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public ParkingLotRatingAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ParkingLotRating> DIFF_CALLBACK = new DiffUtil.ItemCallback<ParkingLotRating>() {
        @Override
        public boolean areItemsTheSame(@NonNull ParkingLotRating oldItem, @NonNull ParkingLotRating newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ParkingLotRating oldItem, @NonNull ParkingLotRating newItem) {
            boolean sameRating = oldItem.getOverallRating() != null
                    && oldItem.getOverallRating().equals(newItem.getOverallRating());
            boolean sameTitle = (oldItem.getTitle() == null && newItem.getTitle() == null) ||
                    (oldItem.getTitle() != null && oldItem.getTitle().equals(newItem.getTitle()));
            boolean sameComment = (oldItem.getComment() == null && newItem.getComment() == null) ||
                    (oldItem.getComment() != null && oldItem.getComment().equals(newItem.getComment()));
            boolean sameLotName = (oldItem.getLotName() == null && newItem.getLotName() == null) ||
                    (oldItem.getLotName() != null && oldItem.getLotName().equals(newItem.getLotName()));
            return sameRating && sameTitle && sameComment && sameLotName;
        }
    };

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parking_lot_rating, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        ParkingLotRating rating = getItem(position);
        holder.bind(rating);
    }

    static class RatingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivUserAvatar;
        private final TextView tvUserInitial;
        private final TextView tvUserName;
        private final TextView tvRatingDate;
        private final TextView tvParkingLotName;
        private final TextView tvRatingScore;
        private final TextView tvRatingTitle;
        private final TextView tvRatingComment;
        private final ImageView star1, star2, star3, star4, star5;

        public RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserInitial = itemView.findViewById(R.id.tvUserInitial);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvRatingDate = itemView.findViewById(R.id.tvRatingDate);
            tvParkingLotName = itemView.findViewById(R.id.tvParkingLotName);
            tvRatingScore = itemView.findViewById(R.id.tvRatingScore);
            tvRatingTitle = itemView.findViewById(R.id.tvRatingTitle);
            tvRatingComment = itemView.findViewById(R.id.tvRatingComment);
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            star4 = itemView.findViewById(R.id.star4);
            star5 = itemView.findViewById(R.id.star5);
        }

        public void bind(ParkingLotRating rating) {
            // User Avatar and Name
            if (rating.getFullName() != null && !rating.getFullName().isEmpty()) {
                tvUserName.setText(rating.getFullName());
            } else {
                tvUserName.setText("User #" + rating.getUserId());
            }

            // Load Avatar Image or show initial
            if (rating.getAvatarUrl() != null && !rating.getAvatarUrl().isEmpty()) {
                // Determine full avatar URL - handle both full URL and relative path
                String avatarUrl = rating.getAvatarUrl();
                String fullAvatarUrl;

                if (avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://")) {
                    // Already a full URL
                    fullAvatarUrl = avatarUrl;
                } else {
                    // Relative path - add S3 bucket prefix
                    fullAvatarUrl = "https://parkmate-image-bucket.s3.ap-southeast-1.amazonaws.com/" + avatarUrl;
                }

                ivUserAvatar.setVisibility(View.VISIBLE);
                tvUserInitial.setVisibility(View.GONE);

                Glide.with(itemView.getContext())
                        .load(fullAvatarUrl)
                        .placeholder(R.drawable.ic_person_24)
                        .error(R.drawable.ic_person_24)
                        .circleCrop()
                        .into(ivUserAvatar);
            } else {
                // Show initial letter as fallback
                ivUserAvatar.setVisibility(View.GONE);
                tvUserInitial.setVisibility(View.VISIBLE);

                if (rating.getFullName() != null && !rating.getFullName().isEmpty()) {
                    tvUserInitial.setText(String.valueOf(rating.getFullName().charAt(0)).toUpperCase());
                } else {
                    tvUserInitial.setText("U");
                }
            }

            // Date
            if (rating.getCreatedAt() != null) {
                try {
                    Date date = INPUT_FORMAT.parse(rating.getCreatedAt());
                    if (date != null) {
                        tvRatingDate.setText(OUTPUT_FORMAT.format(date));
                    }
                } catch (ParseException e) {
                    tvRatingDate.setText(rating.getCreatedAt());
                }
            }

            // Parking Lot Name - Hide in AllRatings because user already knows which lot
            // they're viewing
            if (tvParkingLotName != null) {
                tvParkingLotName.setVisibility(View.GONE);
            }

            // Title
            if (rating.getTitle() != null && !rating.getTitle().isEmpty()) {
                tvRatingTitle.setVisibility(View.VISIBLE);
                tvRatingTitle.setText(rating.getTitle());
            } else {
                tvRatingTitle.setVisibility(View.GONE);
            }

            // Comment
            if (rating.getComment() != null && !rating.getComment().isEmpty()) {
                tvRatingComment.setVisibility(View.VISIBLE);
                tvRatingComment.setText(rating.getComment());
            } else {
                tvRatingComment.setVisibility(View.GONE);
            }

            // Star rating - set to badge
            int overallRating = rating.getOverallRating() != null ? rating.getOverallRating() : 0;
            if (tvRatingScore != null) {
                tvRatingScore.setText(String.valueOf(overallRating));
            }
        }

    }
}
