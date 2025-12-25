package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.parkmate.android.R;
import com.parkmate.android.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionVehicleAdapter extends RecyclerView.Adapter<SubscriptionVehicleAdapter.ViewHolder> {

    public interface OnVehicleSelectedListener {
        void onVehicleSelected(Vehicle vehicle);
    }

    private List<Vehicle> vehicles = new ArrayList<>();
    private final OnVehicleSelectedListener listener;
    private final long parkingLotId;
    private int selectedPosition = -1;

    public SubscriptionVehicleAdapter(OnVehicleSelectedListener listener, long parkingLotId) {
        this.listener = listener;
        this.parkingLotId = parkingLotId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscription_vehicle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vehicle vehicle = vehicles.get(position);
        holder.bind(vehicle, position);
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    public void updateVehicles(List<Vehicle> newVehicles) {
        List<Vehicle> sortedVehicles = new ArrayList<>();
        List<Vehicle> availableVehicles = new ArrayList<>();
        List<Vehicle> disabledVehicles = new ArrayList<>();

        for (Vehicle vehicle : newVehicles) {
            boolean isDisabled = vehicle.isHasSubscriptionInThisParkingLot()
                    || vehicle.isInReservation()
                    || !vehicle.isSupported();

            if (isDisabled) {
                disabledVehicles.add(vehicle);
            } else {
                availableVehicles.add(vehicle);
            }
        }

        sortedVehicles.addAll(availableVehicles);
        sortedVehicles.addAll(disabledVehicles);

        this.vehicles = sortedVehicles;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView tvLicensePlate;
        private final TextView tvVehicleInfo;
        private final TextView tvVehicleType;
        private final TextView tvWarning;
        private final RadioButton radioButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardVehicle);
            tvLicensePlate = itemView.findViewById(R.id.tvLicensePlate);
            tvVehicleInfo = itemView.findViewById(R.id.tvVehicleInfo);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            tvWarning = itemView.findViewById(R.id.tvWarning);
            radioButton = itemView.findViewById(R.id.radioButton);
        }

        void bind(Vehicle vehicle, int position) {
            // License plate
            tvLicensePlate.setText(vehicle.getLicensePlate());

            // Brand & Model - đồng bộ với VehicleSelectionAdapter
            String desc = "";
            if (vehicle.getBrand() != null && !vehicle.getBrand().isEmpty()) {
                desc = vehicle.getBrand();
            }
            if (vehicle.getModel() != null && !vehicle.getModel().isEmpty()) {
                desc += (desc.isEmpty() ? "" : " - ") + vehicle.getModel();
            }
            if (desc.isEmpty()) {
                desc = "Không có thông tin";
            }
            tvVehicleInfo.setText(desc);

            // Vehicle type badge - đồng bộ với VehicleAdapter
            tvVehicleType.setText(getDisplayVehicleType(vehicle.getVehicleType()));
            tvVehicleType.setBackgroundResource(getVehicleTypeBackground(vehicle.getVehicleType()));

            // Check if vehicle can be selected
            boolean isDisabled = vehicle.isHasSubscriptionInThisParkingLot()
                    || vehicle.isInReservation()
                    || !vehicle.isSupported();

            if (isDisabled) {
                // Disabled state - always same styling
                cardView.setEnabled(false);
                cardView.setAlpha(0.6f);
                cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.background));
                radioButton.setEnabled(false);
                radioButton.setChecked(false);
                cardView.setStrokeColor(itemView.getContext().getColor(R.color.gray_200));
                cardView.setStrokeWidth(1);
                // Clear click listener to prevent any interaction
                itemView.setOnClickListener(null);
                itemView.setClickable(false);
                cardView.setClickable(false);

                tvWarning.setVisibility(View.VISIBLE);
                if (!vehicle.isSupported()) {
                    tvWarning.setText("Loại xe không được hỗ trợ");
                } else if (vehicle.isHasSubscriptionInThisParkingLot()) {
                    tvWarning.setText("Đã có vé tháng cho bãi này");
                } else if (vehicle.isInReservation()) {
                    tvWarning.setText("Đang trong phiên đặt chỗ");
                }
            } else {
                // Enabled state
                cardView.setEnabled(true);
                cardView.setAlpha(1.0f);
                cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.white));
                radioButton.setEnabled(true);
                tvWarning.setVisibility(View.GONE);
                itemView.setClickable(true);
                cardView.setClickable(true);

                boolean isSelected = selectedPosition == position;
                radioButton.setChecked(isSelected);

                // Card stroke based on selection
                if (isSelected) {
                    cardView.setStrokeColor(itemView.getContext().getColor(R.color.primary));
                    cardView.setStrokeWidth(6);
                } else {
                    cardView.setStrokeColor(itemView.getContext().getColor(R.color.gray_200));
                    cardView.setStrokeWidth(1);
                }

                itemView.setOnClickListener(v -> {
                    int oldPosition = selectedPosition;

                    // Toggle: if clicking same item, deselect
                    if (selectedPosition == position) {
                        selectedPosition = -1;
                        notifyItemChanged(position);
                        if (listener != null) {
                            listener.onVehicleSelected(null); // Pass null to indicate deselection
                        }
                    } else {
                        selectedPosition = position;

                        if (oldPosition != -1) {
                            notifyItemChanged(oldPosition);
                        }
                        notifyItemChanged(position);

                        if (listener != null) {
                            listener.onVehicleSelected(vehicle);
                        }
                    }
                });
            }
        }

        /**
         * Đồng bộ với VehicleAdapter - getDisplayVehicleType
         */
        private String getDisplayVehicleType(String vehicleType) {
            if (vehicleType == null)
                return "";

            String upperType = vehicleType.toUpperCase().trim();

            if (upperType.contains("CAR_UP_TO_9") || upperType.contains("CAR UP TO 9")) {
                return "Ô tô";
            } else if (upperType.contains("MOTORBIKE") || upperType.equals("XE MÁY")) {
                return "Xe máy";
            } else if (upperType.equals("CAR") || upperType.equals("Ô TÔ")) {
                return "Ô tô";
            } else if (upperType.contains("BICYCLE") || upperType.contains("BIKE") || upperType.equals("XE ĐẠP")) {
                return "Xe đạp";
            }

            return vehicleType;
        }

        /**
         * Đồng bộ với VehicleAdapter - getVehicleTypeBackground
         */
        private int getVehicleTypeBackground(String vehicleType) {
            if (vehicleType == null)
                return R.drawable.bg_vehicle_type_other;

            String upperType = vehicleType.toUpperCase().trim();

            if (upperType.contains("MOTORBIKE") || upperType.contains("XE MÁY")) {
                return R.drawable.bg_vehicle_type_motorbike;
            } else if (upperType.contains("CAR") || upperType.contains("Ô TÔ")) {
                return R.drawable.bg_vehicle_type_car;
            } else if (upperType.contains("BICYCLE") || upperType.contains("BIKE") || upperType.contains("XE ĐẠP")) {
                return R.drawable.bg_vehicle_type_bicycle;
            } else {
                return R.drawable.bg_vehicle_type_other;
            }
        }
    }
}
