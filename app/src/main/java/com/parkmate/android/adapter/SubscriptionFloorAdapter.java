package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.model.ParkingFloor;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionFloorAdapter extends RecyclerView.Adapter<SubscriptionFloorAdapter.ViewHolder> {

    public interface OnFloorSelectedListener {
        void onFloorSelected(ParkingFloor floor);
    }

    private List<ParkingFloor> floors = new ArrayList<>();
    private final OnFloorSelectedListener listener;

    public SubscriptionFloorAdapter(OnFloorSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscription_floor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingFloor floor = floors.get(position);
        holder.bind(floor);
    }

    @Override
    public int getItemCount() {
        return floors.size();
    }

    public void updateFloors(List<ParkingFloor> newFloors) {
        this.floors = newFloors;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView tvFloorName;
        private final TextView tvFloorNumber;
        private final TextView tvAvailableSpots;
        private final TextView tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardFloor);
            tvFloorName = itemView.findViewById(R.id.tvFloorName);
            tvFloorNumber = itemView.findViewById(R.id.tvFloorNumber);
            tvAvailableSpots = itemView.findViewById(R.id.tvAvailableSpots);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void bind(ParkingFloor floor) {
            tvFloorName.setText(floor.getFloorName());
            tvFloorNumber.setText(String.format("Tầng %d", floor.getFloorNumber()));
            tvAvailableSpots.setText(String.format("%d/%d chỗ trống",
                    floor.getAvailableSubscriptionSpots(),
                    floor.getTotalSubscriptionSpots()));

            if (floor.getAvailableSubscriptionSpots() > 0) {
                tvStatus.setText("Còn chỗ");
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.success_green));
                cardView.setEnabled(true);
                cardView.setAlpha(1.0f);

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onFloorSelected(floor);
                    }
                });
            } else {
                tvStatus.setText("Hết chỗ");
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.red));
                cardView.setEnabled(false);
                cardView.setAlpha(0.5f);
            }
        }
    }
}

