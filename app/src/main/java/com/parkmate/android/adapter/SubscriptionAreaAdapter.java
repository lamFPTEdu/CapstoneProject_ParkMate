package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.model.ParkingArea;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionAreaAdapter extends RecyclerView.Adapter<SubscriptionAreaAdapter.ViewHolder> {

    public interface OnAreaSelectedListener {
        void onAreaSelected(ParkingArea area);
    }

    private List<ParkingArea> areas = new ArrayList<>();
    private final OnAreaSelectedListener listener;

    public SubscriptionAreaAdapter(OnAreaSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscription_area, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingArea area = areas.get(position);
        holder.bind(area);
    }

    @Override
    public int getItemCount() {
        return areas.size();
    }

    public void updateAreas(List<ParkingArea> newAreas) {
        this.areas = newAreas;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView tvAreaName;
        private final TextView tvAreaType;
        private final TextView tvVehicleType;
        private final TextView tvAvailableSpots;
        private final TextView tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardArea);
            tvAreaName = itemView.findViewById(R.id.tvAreaName);
            tvAreaType = itemView.findViewById(R.id.tvAreaType);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            tvAvailableSpots = itemView.findViewById(R.id.tvAvailableSpots);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void bind(ParkingArea area) {
            tvAreaName.setText(area.getName());
            tvAreaType.setText(getAreaTypeDisplay(area.getAreaType()));
            tvVehicleType.setText(getVehicleTypeDisplay(area.getVehicleType()));
            tvAvailableSpots.setText(String.format("%d/%d chỗ trống",
                    area.getAvailableSubscriptionSpots(),
                    area.getTotalSpots()));

            if (area.getAvailableSubscriptionSpots() > 0) {
                tvStatus.setText("Còn chỗ");
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.success_green));
                cardView.setEnabled(true);
                cardView.setAlpha(1.0f);

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAreaSelected(area);
                    }
                });
            } else {
                tvStatus.setText("Hết chỗ");
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.red));
                cardView.setEnabled(false);
                cardView.setAlpha(0.5f);
            }
        }

        private String getAreaTypeDisplay(String areaType) {
            if (areaType == null) return "";
            switch (areaType) {
                case "SUBSCRIPTION_ONLY":
                    return "Vé tháng";
                case "STANDARD":
                    return "Tiêu chuẩn";
                case "VIP":
                    return "VIP";
                default:
                    return areaType;
            }
        }

        private String getVehicleTypeDisplay(String vehicleType) {
            if (vehicleType == null) return "";
            switch (vehicleType) {
                case "MOTORBIKE":
                    return "Xe máy";
                case "CAR_UP_TO_9_SEATS":
                    return "Ô tô (≤9 chỗ)";
                case "BIKE":
                    return "Xe đạp";
                default:
                    return vehicleType;
            }
        }
    }
}

