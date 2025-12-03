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

public class MyRatingsAdapter extends ListAdapter<ParkingLotRating, MyRatingsAdapter.RatingViewHolder> {

    private static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private OnRatingActionListener listener;

    public interface OnRatingActionListener {
        void onEditRating(ParkingLotRating rating);
        void onDeleteRating(ParkingLotRating rating);
    }

    public MyRatingsAdapter(OnRatingActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ParkingLotRating> DIFF_CALLBACK = new DiffUtil.ItemCallback<ParkingLotRating>() {
        @Override
        public boolean areItemsTheSame(@NonNull ParkingLotRating oldItem, @NonNull ParkingLotRating newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ParkingLotRating oldItem, @NonNull ParkingLotRating newItem) {
            return oldItem.getOverallRating() != null && oldItem.getOverallRating().equals(newItem.getOverallRating())
                    && oldItem.getTitle() != null && oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getComment() != null && oldItem.getComment().equals(newItem.getComment());
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

    class RatingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivUserAvatar;
        private final TextView tvUserInitial;
        private final TextView tvUserName;
        private final TextView tvRatingDate;
        private final ImageView[] stars;
        private final TextView tvRatingTitle;
        private final TextView tvRatingComment;
        private final LinearLayout layoutActions;
        private final ImageView btnEditRating;
        private final ImageView btnDeleteRating;

        public RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserInitial = itemView.findViewById(R.id.tvUserInitial);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvRatingDate = itemView.findViewById(R.id.tvRatingDate);

            stars = new ImageView[]{
                    itemView.findViewById(R.id.star1),
                    itemView.findViewById(R.id.star2),
                    itemView.findViewById(R.id.star3),
                    itemView.findViewById(R.id.star4),
                    itemView.findViewById(R.id.star5)
            };

            tvRatingTitle = itemView.findViewById(R.id.tvRatingTitle);
            tvRatingComment = itemView.findViewById(R.id.tvRatingComment);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnEditRating = itemView.findViewById(R.id.btnEditRating);
            btnDeleteRating = itemView.findViewById(R.id.btnDeleteRating);
        }

        public void bind(ParkingLotRating rating) {
            // Show action buttons for user's own ratings
            layoutActions.setVisibility(View.VISIBLE);

            // User name
            String fullName = rating.getFullName();
            if (fullName != null && !fullName.isEmpty()) {
                tvUserName.setText(fullName);
            } else {
                tvUserName.setText("Người dùng");
            }

            // User avatar
            String avatarUrl = rating.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                ivUserAvatar.setVisibility(View.VISIBLE);
                tvUserInitial.setVisibility(View.GONE);

                String fullAvatarUrl = avatarUrl.startsWith("http") ? avatarUrl :
                    "https://parkmate-image-bucket.s3.ap-southeast-1.amazonaws.com/" + avatarUrl;

                Glide.with(itemView.getContext())
                        .load(fullAvatarUrl)
                        .placeholder(R.drawable.ic_person_24)
                        .error(R.drawable.ic_person_24)
                        .circleCrop()
                        .into(ivUserAvatar);
            } else {
                ivUserAvatar.setVisibility(View.GONE);
                tvUserInitial.setVisibility(View.VISIBLE);
                String initial = fullName != null && !fullName.isEmpty() ?
                        String.valueOf(fullName.charAt(0)).toUpperCase() : "U";
                tvUserInitial.setText(initial);
            }

            // Date
            String createdAt = rating.getCreatedAt();
            if (createdAt != null) {
                try {
                    Date date = INPUT_FORMAT.parse(createdAt);
                    if (date != null) {
                        tvRatingDate.setText(OUTPUT_FORMAT.format(date));
                    }
                } catch (ParseException e) {
                    tvRatingDate.setText(createdAt);
                }
            }

            // Star rating
            Integer overallRating = rating.getOverallRating();
            if (overallRating != null) {
                for (int i = 0; i < stars.length; i++) {
                    if (i < overallRating) {
                        stars[i].setImageResource(R.drawable.ic_star_filled);
                    } else {
                        stars[i].setImageResource(R.drawable.ic_star_outline);
                    }
                }
            }

            // Title
            String title = rating.getTitle();
            if (title != null && !title.isEmpty()) {
                tvRatingTitle.setVisibility(View.VISIBLE);
                tvRatingTitle.setText(title);
            } else {
                tvRatingTitle.setVisibility(View.GONE);
            }

            // Comment
            String comment = rating.getComment();
            if (comment != null && !comment.isEmpty()) {
                tvRatingComment.setVisibility(View.VISIBLE);
                tvRatingComment.setText(comment);
            } else {
                tvRatingComment.setVisibility(View.GONE);
            }

            // Edit button click
            btnEditRating.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditRating(rating);
                }
            });

            // Delete button click
            btnDeleteRating.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteRating(rating);
                }
            });
        }
    }
}

