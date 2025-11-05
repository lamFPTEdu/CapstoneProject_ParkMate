package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.parkmate.android.R;
import com.parkmate.android.model.Vehicle;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter mới riêng cho việc chọn xe khi đặt chỗ
 * Check 2 điều kiện disable:
 * 1. hasSubscriptionInThisParkingLot = true
 * 2. inReservation = true
 * Chỉ cần 1 trong 2 là true thì sẽ disable xe đó
 */
public class VehicleSelectionAdapter extends RecyclerView.Adapter<VehicleSelectionAdapter.VehicleViewHolder> {

    private List<Vehicle> vehicleList = new ArrayList<>();
    private int selectedPosition = -1;
    private OnVehicleSelectedListener listener;

    public interface OnVehicleSelectedListener {
        void onVehicleSelected(Vehicle vehicle, int position);
    }

    public VehicleSelectionAdapter(OnVehicleSelectedListener listener) {
        this.listener = listener;
    }

    public void updateVehicles(List<Vehicle> vehicles) {
        this.vehicleList.clear();
        if (vehicles != null) {
            this.vehicleList.addAll(vehicles);
        }
        this.selectedPosition = -1;
        notifyDataSetChanged();
    }

    public Vehicle getSelectedVehicle() {
        if (selectedPosition >= 0 && selectedPosition < vehicleList.size()) {
            return vehicleList.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle_selection, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);
        holder.bind(vehicle, position);
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    class VehicleViewHolder extends RecyclerView.ViewHolder {
        private com.google.android.material.card.MaterialCardView cardVehicle;
        private RadioButton rbSelectVehicle;
        private TextView tvVehiclePlate;
        private TextView tvVehicleDesc;
        private TextView tvVehicleStatus;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            cardVehicle = itemView.findViewById(R.id.cardVehicle);
            rbSelectVehicle = itemView.findViewById(R.id.rbSelectVehicle);
            tvVehiclePlate = itemView.findViewById(R.id.tvVehiclePlate);
            tvVehicleDesc = itemView.findViewById(R.id.tvVehicleDesc);
            tvVehicleStatus = itemView.findViewById(R.id.tvVehicleStatus);
        }

        public void bind(Vehicle vehicle, int position) {
            // Check nếu xe bị disable (1 trong 2 điều kiện = true)
            boolean isDisabled = vehicle.isHasSubscriptionInThisParkingLot() || vehicle.isInReservation();

            // Hiển thị thông tin xe
            tvVehiclePlate.setText(vehicle.getLicensePlate() != null ? vehicle.getLicensePlate() : "N/A");

            String desc = "";
            if (vehicle.getBrand() != null && !vehicle.getBrand().isEmpty()) {
                desc = vehicle.getBrand();
            }
            if (vehicle.getModel() != null && !vehicle.getModel().isEmpty()) {
                desc += (desc.isEmpty() ? "" : " - ") + vehicle.getModel();
            }
            if (desc.isEmpty()) {
                desc = vehicle.getVehicleType() != null ? vehicle.getVehicleType() : "Xe";
            }
            tvVehicleDesc.setText(desc);

            // Hiển thị trạng thái disable
            if (isDisabled) {
                tvVehicleStatus.setVisibility(View.VISIBLE);
                if (vehicle.isHasSubscriptionInThisParkingLot()) {
                    tvVehicleStatus.setText(R.string.vehicle_selection_has_subscription);
                } else if (vehicle.isInReservation()) {
                    tvVehicleStatus.setText(R.string.vehicle_selection_in_reservation);
                }
            } else {
                tvVehicleStatus.setVisibility(View.GONE);
            }

            // Set trạng thái radio button
            rbSelectVehicle.setChecked(position == selectedPosition);
            rbSelectVehicle.setEnabled(!isDisabled);

            // Set stroke color cho card khi được chọn
            if (position == selectedPosition && !isDisabled) {
                cardVehicle.setStrokeColor(itemView.getContext().getColor(R.color.primary));
                cardVehicle.setStrokeWidth(4);
            } else {
                cardVehicle.setStrokeColor(itemView.getContext().getColor(R.color.gray_300));
                cardVehicle.setStrokeWidth(2);
            }

            // Set alpha cho item bị disable
            itemView.setAlpha(isDisabled ? 0.5f : 1.0f);

            // Handle click
            if (!isDisabled) {
                itemView.setOnClickListener(v -> {
                    int oldPosition = selectedPosition;
                    selectedPosition = getAdapterPosition();

                    // Notify changes
                    if (oldPosition >= 0) {
                        notifyItemChanged(oldPosition);
                    }
                    notifyItemChanged(selectedPosition);

                    // Callback
                    if (listener != null) {
                        listener.onVehicleSelected(vehicle, selectedPosition);
                    }
                });

                rbSelectVehicle.setOnClickListener(v -> itemView.performClick());
            } else {
                itemView.setOnClickListener(null);
                rbSelectVehicle.setOnClickListener(null);
            }
        }
    }
}

