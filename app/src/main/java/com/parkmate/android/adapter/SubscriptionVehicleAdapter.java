package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

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
        // Sắp xếp: xe có thể chọn lên đầu, xe disable xuống dưới
        List<Vehicle> sortedVehicles = new ArrayList<>();
        List<Vehicle> availableVehicles = new ArrayList<>();
        List<Vehicle> disabledVehicles = new ArrayList<>();

        for (Vehicle vehicle : newVehicles) {
            // Check điều kiện disable: hasSubscriptionInThisParkingLot, inReservation, hoặc !supported
            boolean isDisabled = vehicle.isHasSubscriptionInThisParkingLot()
                    || vehicle.isInReservation()
                    || !vehicle.isSupported();

            if (isDisabled) {
                disabledVehicles.add(vehicle);
            } else {
                availableVehicles.add(vehicle);
            }
        }

        // Ghép: available trước, disabled sau
        sortedVehicles.addAll(availableVehicles);
        sortedVehicles.addAll(disabledVehicles);

        this.vehicles = sortedVehicles;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView tvLicensePlate;
        private final TextView tvVehicleType;
        private final TextView tvWarning;
        private final RadioButton radioButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardVehicle);
            tvLicensePlate = itemView.findViewById(R.id.tvLicensePlate);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            tvWarning = itemView.findViewById(R.id.tvWarning);
            radioButton = itemView.findViewById(R.id.radioButton);
        }

        void bind(Vehicle vehicle, int position) {
            tvLicensePlate.setText(vehicle.getLicensePlate());
            tvVehicleType.setText(getVehicleTypeDisplay(vehicle.getVehicleType()));

            // Check if vehicle can be selected
            boolean isDisabled = vehicle.isHasSubscriptionInThisParkingLot()
                    || vehicle.isInReservation()
                    || !vehicle.isSupported();

            if (isDisabled) {
                cardView.setEnabled(false);
                cardView.setAlpha(0.5f);
                radioButton.setEnabled(false);
                radioButton.setChecked(false);

                tvWarning.setVisibility(View.VISIBLE);
                if (!vehicle.isSupported()) {
                    tvWarning.setText("Loại xe không được hỗ trợ");
                } else if (vehicle.isHasSubscriptionInThisParkingLot()) {
                    tvWarning.setText("Đã có vé tháng cho bãi này");
                } else if (vehicle.isInReservation()) {
                    tvWarning.setText("Đang trong phiên đặt chỗ");
                }
            } else {
                cardView.setEnabled(true);
                cardView.setAlpha(1.0f);
                radioButton.setEnabled(true);
                tvWarning.setVisibility(View.GONE);

                radioButton.setChecked(selectedPosition == position);

                itemView.setOnClickListener(v -> {
                    int oldPosition = selectedPosition;
                    selectedPosition = position;

                    if (oldPosition != -1) {
                        notifyItemChanged(oldPosition);
                    }
                    notifyItemChanged(selectedPosition);

                    if (listener != null) {
                        listener.onVehicleSelected(vehicle);
                    }
                });
            }
        }

        private String getVehicleTypeDisplay(String vehicleType) {
            if (vehicleType == null) return "N/A";
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

