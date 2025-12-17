package com.parkmate.android.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
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

        void onRateClick(Reservation reservation);

        void onCancelClick(Reservation reservation);
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

        // Hiển thị biển số xe thay vì ID
        String licensePlate = reservation.getVehicleLicensePlate();
        if (licensePlate != null && !licensePlate.isEmpty()) {
            holder.tvReservationId.setText(licensePlate);
        } else {
            holder.tvReservationId.setText(String.format("#%s", reservation.getId()));
        }

        holder.tvStatus.setText(reservation.getStatusDisplayName());

        // Hiển thị tổng phí (totalFee hoặc initialFee)
        int displayFee = 0;
        if (reservation.getTotalFee() != null && reservation.getTotalFee() > 0) {
            displayFee = reservation.getTotalFee();
        } else if (reservation.getInitialFee() != null) {
            displayFee = reservation.getInitialFee();
        }
        holder.tvReservationFee.setText(String.format(Locale.getDefault(), "%,dđ", displayFee));

        // Hiển thị thời gian với format mới từ BE: "yyyy-MM-dd HH:mm:ss"
        holder.tvReservedFrom.setText(formatDateTime(reservation.getReservedFrom()));

        // Hiển thị tên bãi đỗ (không còn thông tin spot từ API)
        String parkingLotInfo = reservation.getParkingLotName() != null
                ? reservation.getParkingLotName()
                : "Bãi đỗ xe";
        holder.tvSpotInfo.setText(parkingLotInfo);

        // Màu status background giống ReservationDetailActivity
        setStatusBackground(holder.tvStatus, reservation.getStatus());

        // Hiển thị refund policy và button cancel chỉ cho status PENDING
        if ("PENDING".equals(reservation.getStatus())) {
            // Hiển thị refund policy
            com.parkmate.android.model.RefundPolicy policy = reservation.getRefundPolicy();
            int refundMinutes = policy != null ? policy.getRefundWindowMinutes() : 30;

            holder.tvRefundPolicy.setVisibility(View.VISIBLE);
            holder.tvRefundPolicy.setText(String.format(Locale.getDefault(),
                    "💡 Hủy trước %d phút để được hoàn tiền", refundMinutes));

            // Hiển thị button cancel
            holder.btnCancelReservation.setVisibility(View.VISIBLE);
            holder.btnCancelReservation.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelClick(reservation);
                }
            });
        } else {
            holder.tvRefundPolicy.setVisibility(View.GONE);
            holder.btnCancelReservation.setVisibility(View.GONE);
        }

        // Show rating button only for COMPLETED status
        if ("COMPLETED".equals(reservation.getStatus())) {
            holder.layoutRatingButton.setVisibility(View.VISIBLE);
            holder.btnRate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRateClick(reservation);
                }
            });
        } else {
            holder.layoutRatingButton.setVisibility(View.GONE);
        }

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
        if (status == null)
            return;

        int backgroundRes;
        switch (status) {
            case "PENDING":
                backgroundRes = R.drawable.bg_status_pending; // Orange - Đặt rồi nhưng chưa vào bãi
                break;
            case "ACTIVE":
                backgroundRes = R.drawable.bg_status_active; // Green - Xe đang trong bãi
                break;
            case "COMPLETED":
                backgroundRes = R.drawable.bg_status_active; // Green - Hoàn thành
                break;
            case "CANCELLED":
                backgroundRes = R.drawable.bg_status_cancelled; // Red - Đã hủy
                break;
            case "EXPIRED":
                backgroundRes = R.drawable.bg_status_expired; // Gray - Hết hạn
                break;
            default:
                backgroundRes = R.drawable.bg_status_expired; // Gray
                break;
        }

        tvStatus.setBackgroundResource(backgroundRes);
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
        TextView tvRefundPolicy;
        LinearLayout layoutRatingButton;
        MaterialButton btnRate;
        MaterialButton btnCancelReservation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvReservationId = itemView.findViewById(R.id.tvReservationId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvReservationFee = itemView.findViewById(R.id.tvReservationFee);
            tvReservedFrom = itemView.findViewById(R.id.tvReservedFrom);
            tvSpotInfo = itemView.findViewById(R.id.tvSpotInfo);
            tvRefundPolicy = itemView.findViewById(R.id.tvRefundPolicy);
            layoutRatingButton = itemView.findViewById(R.id.layoutRatingButton);
            btnRate = itemView.findViewById(R.id.btnRate);
            btnCancelReservation = itemView.findViewById(R.id.btnCancelReservation);
        }
    }
}
