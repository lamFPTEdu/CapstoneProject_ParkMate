package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parkmate.android.R;
import com.parkmate.android.model.Vehicle;
import com.parkmate.android.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private List<Vehicle> vehicleList = new ArrayList<>();
    private final OnVehicleClickListener listener;

    public interface OnVehicleClickListener {
        void onDeleteClick(Vehicle vehicle, int position);
        void onVehicleClick(Vehicle vehicle, int position);
    }

    public VehicleAdapter(OnVehicleClickListener listener) {
        this.listener = listener;
    }

    public void setVehicleList(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
        notifyDataSetChanged();
    }

    public void removeVehicle(int position) {
        vehicleList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, vehicleList.size());
    }

    public void updateVehicle(int position, Vehicle updatedVehicle) {
        if (position >= 0 && position < vehicleList.size()) {
            vehicleList.set(position, updatedVehicle);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle, parent, false);
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
        private final ImageView ivVehicleIcon;
        private final TextView tvLicensePlate;
        private final TextView tvVehicleTypeBadge;
        private final TextView tvVehicleInfo;
        private final TextView tvStatusBadge;
        private final ImageButton btnDelete;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVehicleIcon = itemView.findViewById(R.id.ivVehicleIcon);
            tvLicensePlate = itemView.findViewById(R.id.tvLicensePlate);
            tvVehicleTypeBadge = itemView.findViewById(R.id.tvVehicleTypeBadge);
            tvVehicleInfo = itemView.findViewById(R.id.tvVehicleInfo);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Vehicle vehicle, int position) {
            // Load ảnh xe
            if (vehicle.getVehiclePhotoUrl() != null && !vehicle.getVehiclePhotoUrl().isEmpty()) {
                String imageUrl = vehicle.getVehiclePhotoUrl();
                if (!imageUrl.startsWith("http")) {
                    imageUrl = ApiClient.getBaseUrl() + imageUrl;
                }

                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_car_24)
                        .error(R.drawable.ic_car_24)
                        .centerCrop()
                        .into(ivVehicleIcon);

                // Xóa tint khi có ảnh thật
                ivVehicleIcon.setImageTintList(null);
            } else {
                // Hiển thị icon mặc định nếu không có ảnh
                ivVehicleIcon.setImageResource(R.drawable.ic_car_24);
                ivVehicleIcon.setImageTintList(itemView.getContext().getColorStateList(R.color.colorPrimary));
            }

            tvLicensePlate.setText(vehicle.getLicensePlate());

            // Hiển thị loại xe với màu sắc phù hợp
            String vehicleType = vehicle.getVehicleType();

            // Rút gọn tên loại xe để hiển thị
            String displayVehicleType = getDisplayVehicleType(vehicleType);
            tvVehicleTypeBadge.setText(displayVehicleType);

            // Set background color theo loại xe
            int backgroundRes = getVehicleTypeBackground(vehicleType);
            tvVehicleTypeBadge.setBackgroundResource(backgroundRes);

            // Tạo chuỗi thông tin xe (chỉ còn hãng và màu)
            String vehicleInfo = vehicle.getBrand() + " • " + vehicle.getColor();
            tvVehicleInfo.setText(vehicleInfo);

            // Hiển thị badge trạng thái nếu có
            if (vehicle.isHasSubscriptionInThisParkingLot()) {
                tvStatusBadge.setVisibility(View.VISIBLE);
                tvStatusBadge.setText("Đã có vé tháng");
                tvStatusBadge.setBackgroundResource(R.drawable.bg_status_inactive);
            } else if (vehicle.isInReservation()) {
                tvStatusBadge.setVisibility(View.VISIBLE);
                tvStatusBadge.setText("Đang đặt chỗ");
                tvStatusBadge.setBackgroundResource(R.drawable.bg_status_inactive);
            } else {
                tvStatusBadge.setVisibility(View.GONE);
            }

            // Xử lý sự kiện xóa
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(vehicle, position);
                }
            });

            // Xử lý sự kiện click vào item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVehicleClick(vehicle, position);
                }
            });
        }

        private String getDisplayVehicleType(String vehicleType) {
            if (vehicleType == null) return "";

            String upperType = vehicleType.toUpperCase().trim();

            // Rút gọn các tên loại xe dài
            if (upperType.contains("CAR_UP_TO_9") || upperType.contains("CAR UP TO 9")) {
                return "Car ≤ 9";
            } else if (upperType.contains("MOTORBIKE") || upperType.equals("XE MÁY")) {
                return "Xe máy";
            } else if (upperType.equals("CAR") || upperType.equals("Ô TÔ")) {
                return "Ô tô";
            } else if (upperType.contains("BICYCLE") || upperType.contains("BIKE") || upperType.equals("XE ĐẠP")) {
                return "Xe đạp";
            }

            // Trả về tên gốc nếu không match
            return vehicleType;
        }

        private int getVehicleTypeBackground(String vehicleType) {
            if (vehicleType == null) return R.drawable.bg_vehicle_type_other;

            String upperType = vehicleType.toUpperCase().trim();

            // Set background color theo loại xe
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
