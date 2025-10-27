package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.model.response.ParkingFloorDetailResponse;

import java.util.ArrayList;
import java.util.List;

public class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.SpotViewHolder> {

    private List<ParkingFloorDetailResponse.Spot> spots = new ArrayList<>();
    private final OnSpotClickListener listener;
    private ParkingFloorDetailResponse.Spot selectedSpot;

    public interface OnSpotClickListener {
        void onSpotClick(ParkingFloorDetailResponse.Spot spot);
    }

    public SpotAdapter(List<ParkingFloorDetailResponse.Spot> spots, OnSpotClickListener listener) {
        this.spots = spots != null ? spots : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<ParkingFloorDetailResponse.Spot> newSpots) {
        this.spots.clear();
        this.spots.addAll(newSpots);
        notifyDataSetChanged();
    }

    public ParkingFloorDetailResponse.Spot getSelectedSpot() {
        return selectedSpot;
    }

    public void setSelectedSpot(ParkingFloorDetailResponse.Spot selectedSpot) {
        this.selectedSpot = selectedSpot;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SpotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spot, parent, false);
        return new SpotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpotViewHolder holder, int position) {
        ParkingFloorDetailResponse.Spot spot = spots.get(position);
        holder.bind(spot, spot.equals(selectedSpot));
    }

    @Override
    public int getItemCount() {
        return spots.size();
    }

    class SpotViewHolder extends RecyclerView.ViewHolder {
        private final CardView cvSpot;
        private final TextView tvSpotName;
        private final TextView tvSelectedLabel;
        private final View vSelectedBorder;
        private final View vStatusIndicator;

        public SpotViewHolder(@NonNull View itemView) {
            super(itemView);
            cvSpot = itemView.findViewById(R.id.cvSpot);
            tvSpotName = itemView.findViewById(R.id.tvSpotName);
            tvSelectedLabel = itemView.findViewById(R.id.tvSelectedLabel);
            vSelectedBorder = itemView.findViewById(R.id.vSelectedBorder);
            vStatusIndicator = itemView.findViewById(R.id.vStatusIndicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    ParkingFloorDetailResponse.Spot spot = spots.get(position);
                    if ("AVAILABLE".equals(spot.getStatus())) {
                        listener.onSpotClick(spot);
                    }
                }
            });
        }

        public void bind(ParkingFloorDetailResponse.Spot spot, boolean isSelected) {
            String status = spot.getStatus();

            // Set card background and styling based on status and selection
            if (isSelected) {
                // Selected state: light blue background with blue border
                cvSpot.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.white));
                cvSpot.setCardBackgroundColor(0xFFE8F0FE);
                vSelectedBorder.setVisibility(View.VISIBLE);
                tvSpotName.setVisibility(View.GONE);
                tvSelectedLabel.setVisibility(View.VISIBLE);
                cvSpot.setCardElevation(4f);
            } else if ("OCCUPIED".equals(status)) {
                // Occupied: show car icon (using emoji for simplicity)
                cvSpot.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.white));
                vSelectedBorder.setVisibility(View.GONE);
                tvSpotName.setText("🚗");
                tvSpotName.setTextSize(24);
                tvSpotName.setVisibility(View.VISIBLE);
                tvSelectedLabel.setVisibility(View.GONE);
                cvSpot.setCardElevation(1f);
            } else if ("BLOCKED".equals(status)) {
                // Blocked: gray background with spot name
                cvSpot.setCardBackgroundColor(0xFFE0E0E0);
                vSelectedBorder.setVisibility(View.GONE);
                tvSpotName.setText(spot.getName());
                tvSpotName.setTextSize(14);
                tvSpotName.setTextColor(0xFF757575);
                tvSpotName.setVisibility(View.VISIBLE);
                tvSelectedLabel.setVisibility(View.GONE);
                cvSpot.setCardElevation(1f);
            } else if ("AVAILABLE".equals(status)) {
                // Available: white background with spot name
                cvSpot.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.white));
                vSelectedBorder.setVisibility(View.GONE);
                tvSpotName.setText(spot.getName());
                tvSpotName.setTextSize(14);
                tvSpotName.setTextColor(0xFF757575);
                tvSpotName.setVisibility(View.VISIBLE);
                tvSelectedLabel.setVisibility(View.GONE);
                cvSpot.setCardElevation(1f);
            } else {
                // Default: gray text
                cvSpot.setCardBackgroundColor(itemView.getContext().getColor(android.R.color.white));
                vSelectedBorder.setVisibility(View.GONE);
                tvSpotName.setText(spot.getName());
                tvSpotName.setTextSize(14);
                tvSpotName.setTextColor(0xFF9E9E9E);
                tvSpotName.setVisibility(View.VISIBLE);
                tvSelectedLabel.setVisibility(View.GONE);
                cvSpot.setCardElevation(1f);
            }
        }

        private int getStatusColorRes(String status) {
            switch (status) {
                case "AVAILABLE": return R.drawable.bg_spot_available;
                case "OCCUPIED": return R.drawable.bg_spot_occupied;
                case "BLOCKED": return R.drawable.bg_spot_blocked;
                case "RESERVED": return R.drawable.bg_spot_reserved;
                default: return R.drawable.bg_spot_blocked;
            }
        }
    }
}
