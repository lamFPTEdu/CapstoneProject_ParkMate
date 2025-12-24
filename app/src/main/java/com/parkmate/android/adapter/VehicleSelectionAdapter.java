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

/**
 * Adapter cho việc chọn xe khi đặt chỗ
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
            List<Vehicle> availableVehicles = new ArrayList<>();
            List<Vehicle> disabledVehicles = new ArrayList<>();

            for (Vehicle vehicle : vehicles) {
                boolean isDisabled = vehicle.isHasSubscriptionInThisParkingLot()
                        || vehicle.isInReservation()
                        || !vehicle.isSupported();

                if (isDisabled) {
                    disabledVehicles.add(vehicle);
                } else {
                    availableVehicles.add(vehicle);
                }
            }

            this.vehicleList.addAll(availableVehicles);
            this.vehicleList.addAll(disabledVehicles);
        }

        this.selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void clearSelection() {
        int oldPosition = selectedPosition;
        selectedPosition = -1;
        if (oldPosition >= 0) {
            notifyItemChanged(oldPosition);
        }
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
        private MaterialCardView cardVehicle;
        private RadioButton rbSelectVehicle;
        private TextView tvVehiclePlate;
        private TextView tvVehicleDesc;
        private TextView tvVehicleType;
        private TextView tvVehicleStatus;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            cardVehicle = itemView.findViewById(R.id.cardVehicle);
            rbSelectVehicle = itemView.findViewById(R.id.rbSelectVehicle);
            tvVehiclePlate = itemView.findViewById(R.id.tvVehiclePlate);
            tvVehicleDesc = itemView.findViewById(R.id.tvVehicleDesc);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            tvVehicleStatus = itemView.findViewById(R.id.tvVehicleStatus);
        }

        public void bind(Vehicle vehicle, int position) {
            boolean isDisabled = vehicle.isHasSubscriptionInThisParkingLot()
                    || vehicle.isInReservation()
                    || !vehicle.isSupported();

            // License plate
            tvVehiclePlate.setText(vehicle.getLicensePlate() != null ? vehicle.getLicensePlate() : "N/A");

            // Brand & Model
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
            tvVehicleDesc.setText(desc);

            // Vehicle type badge - đồng bộ với trang quản lý xe
            if (tvVehicleType != null) {
                tvVehicleType.setText(getDisplayVehicleType(vehicle.getVehicleType()));
                tvVehicleType.setBackgroundResource(getVehicleTypeBackground(vehicle.getVehicleType()));
            }

            // Warning status for disabled vehicles
            if (isDisabled) {
                tvVehicleStatus.setVisibility(View.VISIBLE);
                if (!vehicle.isSupported()) {
                    tvVehicleStatus.setText("Loại xe không được hỗ trợ");
                } else if (vehicle.isHasSubscriptionInThisParkingLot()) {
                    tvVehicleStatus.setText(R.string.vehicle_selection_has_subscription);
                } else if (vehicle.isInReservation()) {
                    tvVehicleStatus.setText(R.string.vehicle_selection_in_reservation);
                }
            } else {
                tvVehicleStatus.setVisibility(View.GONE);
            }

            // Radio button state
            rbSelectVehicle.setChecked(position == selectedPosition);
            rbSelectVehicle.setEnabled(!isDisabled);

            // Card stroke
            if (position == selectedPosition && !isDisabled) {
                cardVehicle.setStrokeColor(itemView.getContext().getColor(R.color.primary));
                cardVehicle.setStrokeWidth(6);
            } else {
                cardVehicle.setStrokeColor(itemView.getContext().getColor(R.color.gray_200));
                cardVehicle.setStrokeWidth(3);
            }

            // Alpha for disabled
            itemView.setAlpha(isDisabled ? 0.5f : 1.0f);

            // Click handling
            if (!isDisabled) {
                itemView.setOnClickListener(v -> {
                    int oldPosition = selectedPosition;
                    selectedPosition = getAdapterPosition();

                    if (oldPosition >= 0) {
                        notifyItemChanged(oldPosition);
                    }
                    notifyItemChanged(selectedPosition);

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
                return R.drawable.bg_vehicle_type_motorbike; // Xanh lá
            } else if (upperType.contains("CAR") || upperType.contains("Ô TÔ")) {
                return R.drawable.bg_vehicle_type_car; // Xanh dương
            } else if (upperType.contains("BICYCLE") || upperType.contains("BIKE") || upperType.contains("XE ĐẠP")) {
                return R.drawable.bg_vehicle_type_bicycle; // Vàng
            } else {
                return R.drawable.bg_vehicle_type_other; // Tím
            }
        }
    }
}
