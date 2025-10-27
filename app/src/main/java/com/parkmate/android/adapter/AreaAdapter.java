package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.model.response.ParkingFloorDetailResponse;

import java.util.ArrayList;
import java.util.List;

public class AreaAdapter extends RecyclerView.Adapter<AreaAdapter.AreaViewHolder> {

    private List<ParkingFloorDetailResponse.Area> areas = new ArrayList<>();
    private final OnAreaClickListener listener;

    public interface OnAreaClickListener {
        void onAreaClick(ParkingFloorDetailResponse.Area area);
    }

    public AreaAdapter(List<ParkingFloorDetailResponse.Area> areas, OnAreaClickListener listener) {
        this.areas = areas != null ? areas : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<ParkingFloorDetailResponse.Area> newAreas) {
        this.areas.clear();
        this.areas.addAll(newAreas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AreaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_area, parent, false);
        return new AreaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AreaViewHolder holder, int position) {
        ParkingFloorDetailResponse.Area area = areas.get(position);
        holder.bind(area);
    }

    @Override
    public int getItemCount() {
        return areas.size();
    }

    class AreaViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivAreaIcon;
        private final TextView tvAreaName;
        private final TextView tvAvailableSpots;
        private final TextView tvVehicleType;
        private final TextView tvElectricSupport;
        private final TextView tvPricing;

        public AreaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAreaIcon = itemView.findViewById(R.id.ivAreaIcon);
            tvAreaName = itemView.findViewById(R.id.tvAreaName);
            tvAvailableSpots = itemView.findViewById(R.id.tvAvailableSpots);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            tvElectricSupport = itemView.findViewById(R.id.tvElectricSupport);
            tvPricing = itemView.findViewById(R.id.tvPricing);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAreaClick(areas.get(position));
                }
            });
        }

        public void bind(ParkingFloorDetailResponse.Area area) {
            tvAreaName.setText(area.getName() + " - " + getVehicleTypeDisplayName(area.getVehicleType()));

            // Calculate available spots
            int totalSpots = area.getTotalSpots() != null ? area.getTotalSpots() : 0;

            // If spot data is available from the API, count the number of AVAILABLE spots
            // If no spot data is available, hide the available spots display
            if (area.getSpots() != null && !area.getSpots().isEmpty()) {
                int availableSpots = area.getAvailableSpotCount();
                tvAvailableSpots.setText(availableSpots + "/" + totalSpots);
                tvAvailableSpots.setVisibility(View.VISIBLE);
            } else {
                tvAvailableSpots.setVisibility(View.GONE);
            }

            tvVehicleType.setText(getVehicleTypeIcon(area.getVehicleType()) + " " + getVehicleTypeDisplayName(area.getVehicleType()));

            // Show electric vehicle support
            if (area.getSupportElectricVehicle() != null && area.getSupportElectricVehicle()) {
                tvElectricSupport.setVisibility(View.VISIBLE);
                tvElectricSupport.setText("⚡ Electric vehicle supported");
            } else {
                tvElectricSupport.setVisibility(View.GONE);
            }

            // Show pricing information if available
            // Note: Floor API doesn't include pricing, only Area detail API has it
            // So pricing will only show in SpotSelectionActivity when we have full area details
            tvPricing.setVisibility(View.GONE);
        }

        private String getVehicleTypeDisplayName(String vehicleType) {
            switch (vehicleType) {
                case "CAR_UP_TO_9_SEATS": return "Car (up to 9 seats)";
                case "MOTORBIKE": return "Motorbike";
                case "BIKE": return "Bike";
                default: return vehicleType;
            }
        }

        private String getVehicleTypeIcon(String vehicleType) {
            switch (vehicleType) {
                case "CAR_UP_TO_9_SEATS": return "🚗";
                case "MOTORBIKE": return "🏍️";
                case "BIKE": return "🚲";
                default: return "🚗";
            }
        }
    }
}
