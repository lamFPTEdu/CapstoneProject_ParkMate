package com.parkmate.android.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.model.Reservation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị danh sách Reservation
 */
public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {

    private static final String TAG = "ReservationAdapter";

    private final Context context;
    private final List<Reservation> reservationList;
    private final OnReservationClickListener listener;

    public interface OnReservationClickListener {
        void onReservationClick(Reservation reservation);
    }

    public ReservationAdapter(Context context, List<Reservation> reservationList, OnReservationClickListener listener) {
        this.context = context;
        this.reservationList = reservationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);

        // Hiển thị thông tin
        holder.tvReservationId.setText(String.format("#%s", reservation.getId()));
        holder.tvStatus.setText(reservation.getStatusDisplayName());

        // Hiển thị tổng phí (totalFee hoặc initialFee)
        int displayFee = reservation.getTotalFee() > 0 ? reservation.getTotalFee() : reservation.getInitialFee();
        holder.tvReservationFee.setText(String.format(Locale.getDefault(), "%,dđ", displayFee));

        // Hiển thị thời gian với format mới từ BE: "yyyy-MM-dd HH:mm:ss"
        holder.tvReservedFrom.setText(formatDateTime(reservation.getReservedFrom()));

        // Hiển thị tên bãi đỗ và vị trí từ API response (không phải ID)
        String spotInfo;
        if (reservation.getParkingLotName() != null && reservation.getSpotName() != null) {
            spotInfo = String.format("%s - Chỗ %s",
                reservation.getParkingLotName(),
                reservation.getSpotName());
        } else {
            // Fallback nếu không có tên
            spotInfo = String.format("Bãi: %s - Chỗ: %s",
                reservation.getParkingLotId(),
                reservation.getSpotId());
        }
        holder.tvSpotInfo.setText(spotInfo);

        // Màu status background giống ReservationDetailActivity
        setStatusBackground(holder.tvStatus, reservation.getStatus());

        // Click listener
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReservationClick(reservation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    private void setStatusBackground(TextView tvStatus, String status) {
        if (status == null) return;

        int backgroundColor;
        switch (status) {
            case "PENDING":
                backgroundColor = 0xFFFF9800; // Orange
                break;
            case "CONFIRMED":
                backgroundColor = 0xFF4CAF50; // Green
                break;
            case "CANCELLED":
                backgroundColor = 0xFFF44336; // Red
                break;
            case "COMPLETED":
                backgroundColor = 0xFF2196F3; // Blue
                break;
            default:
                backgroundColor = 0xFF9E9E9E; // Gray
                break;
        }

        tvStatus.setBackgroundColor(backgroundColor);
        tvStatus.setTextColor(0xFFFFFFFF); // White text
    }

    private String formatDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return "";
        }

        try {
            // BE response format: "yyyy-MM-dd HH:mm:ss"
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = apiFormat.parse(dateTimeStr);

            if (date != null) {
                SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
                return displayFormat.format(date);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + e.getMessage());
        }
        return dateTimeStr;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvReservationId;
        TextView tvStatus;
        TextView tvReservationFee;
        TextView tvReservedFrom;
        TextView tvSpotInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvReservationId = itemView.findViewById(R.id.tvReservationId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvReservationFee = itemView.findViewById(R.id.tvReservationFee);
            tvReservedFrom = itemView.findViewById(R.id.tvReservedFrom);
            tvSpotInfo = itemView.findViewById(R.id.tvSpotInfo);
        }
    }
}
