package com.parkmate.android.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.parkmate.android.R;
import com.parkmate.android.model.UserSubscription;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserSubscriptionAdapter extends RecyclerView.Adapter<UserSubscriptionAdapter.ViewHolder> {

    public interface OnSubscriptionClickListener {
        void onSubscriptionClick(UserSubscription subscription);

        void onRateClick(UserSubscription subscription);

        void onRenewClick(UserSubscription subscription);

        void onCancelClick(UserSubscription subscription);
    }

    private List<UserSubscription> subscriptions = new ArrayList<>();
    private final OnSubscriptionClickListener listener;
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public UserSubscriptionAdapter(OnSubscriptionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_subscription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserSubscription subscription = subscriptions.get(position);
        holder.bind(subscription);
    }

    @Override
    public int getItemCount() {
        return subscriptions.size();
    }

    public void addSubscriptions(List<UserSubscription> newSubscriptions) {
        int startPosition = subscriptions.size();
        subscriptions.addAll(newSubscriptions);
        notifyItemRangeInserted(startPosition, newSubscriptions.size());
    }

    public void clearSubscriptions() {
        subscriptions.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvParkingLotName;
        private final TextView tvStatus;
        private final TextView tvVehicleInfo;
        private final TextView tvPackageName;
        private final LinearLayout layoutSpotInfo;
        private final TextView tvSpotName;
        private final TextView tvDateRange;
        private final TextView tvPrice;
        private final TextView tvDaysRemaining;
        private final LinearLayout layoutRatingButton;
        private final MaterialButton btnRate;
        private final LinearLayout layoutRenewalButton;
        private final MaterialButton btnRenew;
        private final LinearLayout layoutCancelButton;
        private final MaterialButton btnCancel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvParkingLotName = itemView.findViewById(R.id.tvParkingLotName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvVehicleInfo = itemView.findViewById(R.id.tvVehicleInfo);
            tvPackageName = itemView.findViewById(R.id.tvPackageName);
            layoutSpotInfo = itemView.findViewById(R.id.layoutSpotInfo);
            tvSpotName = itemView.findViewById(R.id.tvSpotName);
            tvDateRange = itemView.findViewById(R.id.tvDateRange);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDaysRemaining = itemView.findViewById(R.id.tvDaysRemaining);
            layoutRatingButton = itemView.findViewById(R.id.layoutRatingButton);
            btnRate = itemView.findViewById(R.id.btnRate);
            layoutRenewalButton = itemView.findViewById(R.id.layoutRenewalButton);
            btnRenew = itemView.findViewById(R.id.btnRenew);
            layoutCancelButton = itemView.findViewById(R.id.layoutCancelButton);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }

        void bind(UserSubscription subscription) {
            tvParkingLotName.setText(subscription.getParkingLotName());

            // Set status with Vietnamese labels
            String status = subscription.getStatus();
            String statusDisplay = getStatusDisplay(status);
            tvStatus.setText(statusDisplay);
            setStatusBackground(status);

            // Vehicle info
            String vehicleTypeDisplay = getVehicleTypeDisplay(subscription.getVehicleType());
            tvVehicleInfo.setText(String.format("%s • %s",
                    subscription.getVehicleLicensePlate(),
                    vehicleTypeDisplay));

            // Package name - handle null
            String packageName = subscription.getSubscriptionPackageName();
            if (packageName != null && !packageName.isEmpty()) {
                tvPackageName.setText(packageName);
                tvPackageName.setVisibility(View.VISIBLE);
            } else {
                tvPackageName.setText("Gói đăng ký");
                tvPackageName.setVisibility(View.VISIBLE);
            }

            // Spot name - handle null (ẩn cho xe máy/xe đạp)
            String spotName = subscription.getAssignedSpotName();
            if (spotName != null && !spotName.isEmpty()) {
                layoutSpotInfo.setVisibility(View.VISIBLE);
                tvSpotName.setText(String.format("Chỗ: %s", spotName));
            } else {
                layoutSpotInfo.setVisibility(View.GONE);
            }

            // Date range
            String startDate = formatDate(subscription.getStartDate());
            String endDate = formatDate(subscription.getEndDate());
            tvDateRange.setText(String.format("%s - %s", startDate, endDate));

            // Price
            tvPrice.setText(String.format("%sđ", formatter.format(subscription.getPaidAmount())));

            // Days remaining (show for ACTIVE and INACTIVE subscriptions)
            if (("ACTIVE".equals(status) || "INACTIVE".equals(status)) && subscription.getDaysRemaining() > 0) {
                tvDaysRemaining.setVisibility(View.VISIBLE);
                tvDaysRemaining.setText(String.format("Còn %d ngày", subscription.getDaysRemaining()));
            } else {
                tvDaysRemaining.setVisibility(View.GONE);
            }

            // Show rating button only for EXPIRED status
            if ("EXPIRED".equals(status)) {
                layoutRatingButton.setVisibility(View.VISIBLE);
                btnRate.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRateClick(subscription);
                    }
                });
            } else {
                layoutRatingButton.setVisibility(View.GONE);
            }

            // Show renewal button if needRenewalDecision is true
            if (subscription.isNeedRenewalDecision()) {
                layoutRenewalButton.setVisibility(View.VISIBLE);
                btnRenew.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRenewClick(subscription);
                    }
                });
            } else {
                layoutRenewalButton.setVisibility(View.GONE);
            }

            // Show cancel button for ACTIVE or INACTIVE subscriptions (not EXPIRED,
            // CANCELLED, or PENDING_PAYMENT)
            if ("ACTIVE".equals(status) || "INACTIVE".equals(status)) {
                layoutCancelButton.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onCancelClick(subscription);
                    }
                });
            } else {
                layoutCancelButton.setVisibility(View.GONE);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSubscriptionClick(subscription);
                }
            });
        }

        private void setStatusBackground(String status) {
            switch (status) {
                case "PENDING_PAYMENT":
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                    tvStatus.setTextColor(Color.WHITE);
                    break;
                case "ACTIVE":
                    tvStatus.setBackgroundResource(R.drawable.bg_status_active);
                    tvStatus.setTextColor(Color.WHITE);
                    break;
                case "INACTIVE":
                    tvStatus.setBackgroundResource(R.drawable.bg_status_inactive);
                    tvStatus.setTextColor(Color.WHITE);
                    break;
                case "EXPIRED":
                    tvStatus.setBackgroundResource(R.drawable.bg_status_expired);
                    tvStatus.setTextColor(Color.WHITE);
                    break;
                case "CANCELLED":
                    tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                    tvStatus.setTextColor(Color.WHITE);
                    break;
                default:
                    tvStatus.setBackgroundResource(R.drawable.bg_status_inactive);
                    tvStatus.setTextColor(Color.WHITE);
                    break;
            }
        }

        private String getStatusDisplay(String status) {
            if (status == null)
                return "";
            switch (status) {
                case "PENDING_PAYMENT":
                    return "Chờ thanh toán";
                case "ACTIVE":
                    return "Đang trong bãi";
                case "INACTIVE":
                    return "Ngoài bãi";
                case "EXPIRED":
                    return "Hết hạn";
                case "CANCELLED":
                    return "Đã hủy";
                default:
                    return status;
            }
        }

        private String getVehicleTypeDisplay(String vehicleType) {
            if (vehicleType == null)
                return "";
            switch (vehicleType) {
                case "CAR_UP_TO_9_SEATS":
                    return "Ô tô";
                case "MOTORBIKE":
                    return "Xe máy";
                case "BIKE":
                    return "Xe đạp";
                default:
                    return vehicleType;
            }
        }

        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "";
            }
            try {
                Date date = inputDateFormat.parse(dateString);
                if (date != null) {
                    return outputDateFormat.format(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return dateString;
        }
    }
}
