package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.model.ParkingSpot;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionSpotAdapter extends RecyclerView.Adapter<SubscriptionSpotAdapter.ViewHolder> {

    public interface OnSpotSelectedListener {
        void onSpotSelected(ParkingSpot spot);
    }

    private List<ParkingSpot> spots = new ArrayList<>();
    private final OnSpotSelectedListener listener;

    public SubscriptionSpotAdapter(OnSpotSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscription_spot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingSpot spot = spots.get(position);
        holder.bind(spot);
    }

    @Override
    public int getItemCount() {
        return spots.size();
    }

    public void updateSpots(List<ParkingSpot> newSpots) {
        this.spots = newSpots;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView tvSpotName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardSpot);
            tvSpotName = itemView.findViewById(R.id.tvSpotName);
        }

        void bind(ParkingSpot spot) {
            tvSpotName.setText(spot.getName());

            // Check if spot is available for subscription
            boolean isAvailable = spot.isAvailableForSubscription();

            if (isAvailable) {
                // Available - Green background
                cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.success_green));
                tvSpotName.setTextColor(itemView.getContext().getColor(R.color.white));
                cardView.setEnabled(true);
                cardView.setAlpha(1.0f);

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onSpotSelected(spot);
                    }
                });
            } else {
                // Not available - Gray background with reason
                cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.gray_300));
                tvSpotName.setTextColor(itemView.getContext().getColor(R.color.gray_600));
                cardView.setEnabled(false);
                cardView.setAlpha(0.5f);
                itemView.setOnClickListener(null);

                // Show unavailability reason if exists
                String reason = spot.getSubscriptionUnavailabilityReason();
                if (reason != null && !reason.isEmpty()) {
                    String reasonText = getReasonDisplayText(reason);
                    tvSpotName.setText(spot.getName() + "\n" + reasonText);
                }
            }
        }

        private String getReasonDisplayText(String reason) {
            switch (reason) {
                case "NOT_SUBSCRIPTION_AREA":
                    return "Không phải khu vực subscription";
                case "ALREADY_ASSIGNED":
                    return "Đã có người đặt";
                case "SPOT_HELD":
                    return "Đang được giữ";
                default:
                    return "Không khả dụng";
            }
        }
    }
}

