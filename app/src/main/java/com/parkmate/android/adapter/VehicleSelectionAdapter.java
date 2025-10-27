package com.parkmate.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.parkmate.android.R;
import com.parkmate.android.model.Vehicle;

import java.util.List;

public class VehicleSelectionAdapter extends RecyclerView.Adapter<VehicleSelectionAdapter.VehicleViewHolder> {

    private final Context context;
    private List<Vehicle> vehicles;
    private Vehicle selectedVehicle;
    private final OnVehicleClickListener listener;

    public interface OnVehicleClickListener {
        void onVehicleClick(Vehicle vehicle);
    }

    public VehicleSelectionAdapter(Context context, List<Vehicle> vehicles, OnVehicleClickListener listener) {
        this.context = context;
        this.vehicles = vehicles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vehicle_selection, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicles.get(position);
        holder.bind(vehicle);
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    public void updateData(List<Vehicle> newVehicles) {
        this.vehicles = newVehicles;
        notifyDataSetChanged();
    }

    public void setSelectedVehicle(Vehicle vehicle) {
        this.selectedVehicle = vehicle;
        notifyDataSetChanged();
    }

    class VehicleViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardVehicle;
        private final ImageView ivVehicleIcon;
        private final TextView tvLicensePlate;
        private final TextView tvVehicleBrand;
        private final TextView tvVehicleType;
        private final ImageView ivSelected;
        private final TextView ivDefault;  // Đổi từ ImageView sang TextView

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            cardVehicle = itemView.findViewById(R.id.cardVehicle);
            ivVehicleIcon = itemView.findViewById(R.id.ivVehicleIcon);
            tvLicensePlate = itemView.findViewById(R.id.tvLicensePlate);
            tvVehicleBrand = itemView.findViewById(R.id.tvVehicleBrand);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            ivSelected = itemView.findViewById(R.id.ivSelected);
            ivDefault = itemView.findViewById(R.id.ivDefault);  // Bây giờ đúng kiểu TextView
        }

        public void bind(Vehicle vehicle) {
            tvLicensePlate.setText(vehicle.getLicensePlate());

            // Set vehicle brand and model
            String brandModel = "";
            if (vehicle.getBrand() != null && !vehicle.getBrand().isEmpty()) {
                brandModel = vehicle.getBrand();
                if (vehicle.getModel() != null && !vehicle.getModel().isEmpty()) {
                    brandModel += " " + vehicle.getModel();
                }
            } else {
                brandModel = "Chưa có thông tin";
            }
            tvVehicleBrand.setText(brandModel);

            // Set vehicle type
            tvVehicleType.setText(getVehicleTypeDisplayName(vehicle.getVehicleType()));

            // Set vehicle icon based on type
            ivVehicleIcon.setImageResource(getVehicleIcon(vehicle.getVehicleType()));

            // Show/hide default badge
            ivDefault.setVisibility(vehicle.isDefault() ? View.VISIBLE : View.GONE);

            // Show/hide selected indicator
            boolean isSelected = selectedVehicle != null &&
                                vehicle.getId().equals(selectedVehicle.getId());
            ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            // Highlight selected card
            if (isSelected) {
                cardVehicle.setCardBackgroundColor(context.getResources().getColor(R.color.primary_light, null));
                cardVehicle.setStrokeColor(context.getResources().getColor(R.color.primary, null));
                cardVehicle.setStrokeWidth(4);
            } else {
                cardVehicle.setCardBackgroundColor(context.getResources().getColor(R.color.white, null));
                cardVehicle.setStrokeColor(context.getResources().getColor(R.color.border_color, null));
                cardVehicle.setStrokeWidth(2);
            }

            // Set click listener
            cardVehicle.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVehicleClick(vehicle);
                }
            });
        }

        private String getVehicleTypeDisplayName(String vehicleType) {
            if (vehicleType == null) return "";
            switch (vehicleType) {
                case "CAR_UP_TO_9_SEATS": return "Ô tô (dưới 9 chỗ)";
                case "MOTORBIKE": return "Xe máy";
                case "BIKE": return "Xe đạp";
                default: return vehicleType;
            }
        }

        private int getVehicleIcon(String vehicleType) {
            if (vehicleType == null) return R.drawable.ic_car;
            switch (vehicleType) {
                case "CAR_UP_TO_9_SEATS": return R.drawable.ic_car;
                case "MOTORBIKE": return R.drawable.ic_motorcycle;
                case "BIKE": return R.drawable.ic_bicycle;
                default: return R.drawable.ic_car;
            }
        }
    }
}
