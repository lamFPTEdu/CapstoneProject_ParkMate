package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.model.response.ParkingLotDetailResponse;

import java.util.ArrayList;
import java.util.List;

public class AvailableSpotsAdapter extends RecyclerView.Adapter<AvailableSpotsAdapter.ViewHolder> {

    private final List<ParkingLotDetailResponse.AvailableSpot> spots = new ArrayList<>();

    public void submitList(List<ParkingLotDetailResponse.AvailableSpot> newSpots) {
        spots.clear();
        if (newSpots != null) {
            // Chỉ thêm các loại xe có capacity > 0
            for (ParkingLotDetailResponse.AvailableSpot spot : newSpots) {
                if (spot.getTotalCapacity() > 0) {
                    spots.add(spot);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_available_spot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(spots.get(position));
    }

    @Override
    public int getItemCount() {
        return spots.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivVehicleIcon;
        private final TextView tvAvailableCount;
        private final TextView tvTotalCapacity;
        private final TextView tvVehicleType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVehicleIcon = itemView.findViewById(R.id.ivVehicleIcon);
            tvAvailableCount = itemView.findViewById(R.id.tvAvailableCount);
            tvTotalCapacity = itemView.findViewById(R.id.tvTotalCapacity);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
        }

        public void bind(ParkingLotDetailResponse.AvailableSpot spot) {
            // Set icon based on vehicle type
            switch (spot.getVehicleType()) {
                case "CAR_UP_TO_9_SEATS":
                    ivVehicleIcon.setImageResource(R.drawable.ic_directions_car_24);
                    tvVehicleType.setText("Ô tô");
                    break;
                case "MOTORBIKE":
                    ivVehicleIcon.setImageResource(R.drawable.ic_motorcycle_24);
                    tvVehicleType.setText("Xe máy");
                    break;
                case "BIKE":
                    ivVehicleIcon.setImageResource(R.drawable.ic_directions_bike_24);
                    tvVehicleType.setText("Xe đạp");
                    break;
                default:
                    ivVehicleIcon.setImageResource(R.drawable.ic_directions_car_24);
                    tvVehicleType.setText("Khác");
                    break;
            }

            // Set availability count
            tvAvailableCount.setText(String.valueOf(spot.getAvailableCapacity()));
            tvTotalCapacity.setText("/ " + spot.getTotalCapacity());

            // Change color based on availability
            int availableCount = spot.getAvailableCapacity();
            int totalCapacity = spot.getTotalCapacity();
            float percentage = totalCapacity > 0 ? (float) availableCount / totalCapacity : 0;

            if (percentage > 0.5f) {
                tvAvailableCount.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else if (percentage > 0.2f) {
                tvAvailableCount.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
            } else {
                tvAvailableCount.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
            }
        }
    }
}

