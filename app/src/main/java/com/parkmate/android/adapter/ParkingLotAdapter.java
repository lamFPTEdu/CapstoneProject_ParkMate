package com.parkmate.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.model.response.ParkingLotResponse;

import java.util.ArrayList;
import java.util.List;

public class ParkingLotAdapter extends RecyclerView.Adapter<ParkingLotAdapter.ParkingLotViewHolder> {

    private List<ParkingLotResponse.ParkingLot> parkingLots = new ArrayList<>();
    private final OnParkingLotClickListener listener;
    private final Context context;

    public interface OnParkingLotClickListener {
        void onParkingLotClick(ParkingLotResponse.ParkingLot parkingLot);
    }

    public ParkingLotAdapter(List<ParkingLotResponse.ParkingLot> parkingLots, OnParkingLotClickListener listener) {
        this.parkingLots = parkingLots != null ? parkingLots : new ArrayList<>();
        this.listener = listener;
        this.context = null; // Will be set from activity
    }

    public void setContext(Context context) {
        // This is a workaround since we can't get context in constructor
        // In real implementation, pass context or use activity context
    }

    public void updateData(List<ParkingLotResponse.ParkingLot> newParkingLots) {
        this.parkingLots.clear();
        this.parkingLots.addAll(newParkingLots);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ParkingLotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parking_lot, parent, false);
        return new ParkingLotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingLotViewHolder holder, int position) {
        ParkingLotResponse.ParkingLot parkingLot = parkingLots.get(position);
        holder.bind(parkingLot);
    }

    @Override
    public int getItemCount() {
        return parkingLots.size();
    }

    class ParkingLotViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivParkingIcon;
        private final TextView tvParkingName;
        private final TextView tvStatusBadge;
        private final TextView tvAddress;
        private final TextView tvOperatingInfo;
        private final TextView tvFloors;
        private final TextView tvDistance;

        public ParkingLotViewHolder(@NonNull View itemView) {
            super(itemView);
            ivParkingIcon = itemView.findViewById(R.id.ivParkingIcon);
            tvParkingName = itemView.findViewById(R.id.tvParkingName);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvOperatingInfo = itemView.findViewById(R.id.tvOperatingInfo);
            tvFloors = itemView.findViewById(R.id.tvFloors);
            tvDistance = itemView.findViewById(R.id.tvDistance);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onParkingLotClick(parkingLots.get(position));
                }
            });
        }

        public void bind(ParkingLotResponse.ParkingLot parkingLot) {
            tvParkingName.setText(parkingLot.getName());
            tvAddress.setText(parkingLot.getFullAddress());

            // Set status badge
            tvStatusBadge.setText(parkingLot.getStatus());
            if ("ACTIVE".equals(parkingLot.getStatus())) {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_status_active);
            } else {
                tvStatusBadge.setBackgroundResource(R.drawable.bg_status_inactive);
            }

            // Set operating hours only
            tvOperatingInfo.setText(parkingLot.getOperatingHours());

            // Set floors separately
            if (tvFloors != null && parkingLot.getTotalFloors() != null) {
                tvFloors.setText(parkingLot.getTotalFloors() + " tầng");
            } else if (tvFloors != null) {
                tvFloors.setText("");
            }

            // TODO: Calculate and set distance
            tvDistance.setText(""); // Hide distance for now
        }
    }
}
