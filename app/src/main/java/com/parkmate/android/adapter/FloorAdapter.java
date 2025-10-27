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

public class FloorAdapter extends RecyclerView.Adapter<FloorAdapter.FloorViewHolder> {

    private List<ParkingLotDetailResponse.ParkingFloor> floors = new ArrayList<>();
    private final OnFloorClickListener listener;

    public interface OnFloorClickListener {
        void onFloorClick(ParkingLotDetailResponse.ParkingFloor floor);
    }

    public FloorAdapter(List<ParkingLotDetailResponse.ParkingFloor> floors, OnFloorClickListener listener) {
        this.floors = floors != null ? floors : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<ParkingLotDetailResponse.ParkingFloor> newFloors) {
        this.floors.clear();
        this.floors.addAll(newFloors);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FloorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_floor, parent, false);
        return new FloorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorViewHolder holder, int position) {
        ParkingLotDetailResponse.ParkingFloor floor = floors.get(position);
        holder.bind(floor);
    }

    @Override
    public int getItemCount() {
        return floors.size();
    }

    class FloorViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivFloorIcon;
        private final TextView tvFloorName;
        private final TextView tvAvailableSpots;
        private final TextView tvCapacityInfo;
        private final TextView tvVehicleTypes;

        public FloorViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFloorIcon = itemView.findViewById(R.id.ivFloorIcon);
            tvFloorName = itemView.findViewById(R.id.tvFloorName);
            tvAvailableSpots = itemView.findViewById(R.id.tvAvailableSpots);
            tvCapacityInfo = itemView.findViewById(R.id.tvCapacityInfo);
            tvVehicleTypes = itemView.findViewById(R.id.tvVehicleTypes);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onFloorClick(floors.get(position));
                }
            });
        }

        public void bind(ParkingLotDetailResponse.ParkingFloor floor) {
            tvFloorName.setText(floor.getFloorName());

            // Calculate available spots (this would need to be calculated from spots data)
            // For now, show total capacity
            if (floor.getParkingFloorCapacity() != null && !floor.getParkingFloorCapacity().isEmpty()) {
                int totalCapacity = floor.getParkingFloorCapacity().stream()
                    .mapToInt(ParkingLotDetailResponse.Capacity::getCapacity)
                    .sum();
                tvAvailableSpots.setText(totalCapacity + " spots");

                // Build capacity info
                StringBuilder capacityBuilder = new StringBuilder("Total: " + totalCapacity);
                for (ParkingLotDetailResponse.Capacity capacity : floor.getParkingFloorCapacity()) {
                    capacityBuilder.append(" • ")
                        .append(getVehicleTypeShortName(capacity.getVehicleType()))
                        .append(": ")
                        .append(capacity.getCapacity());
                }
                tvCapacityInfo.setText(capacityBuilder.toString());

                // Build vehicle types
                StringBuilder vehicleBuilder = new StringBuilder();
                for (ParkingLotDetailResponse.Capacity capacity : floor.getParkingFloorCapacity()) {
                    if (vehicleBuilder.length() > 0) {
                        vehicleBuilder.append(" • ");
                    }
                    vehicleBuilder.append(getVehicleTypeIcon(capacity.getVehicleType()))
                        .append(" ")
                        .append(getVehicleTypeShortName(capacity.getVehicleType()));
                }
                tvVehicleTypes.setText(vehicleBuilder.toString());
            }
        }

        private String getVehicleTypeShortName(String vehicleType) {
            switch (vehicleType) {
                case "CAR_UP_TO_9_SEATS": return "Car";
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
