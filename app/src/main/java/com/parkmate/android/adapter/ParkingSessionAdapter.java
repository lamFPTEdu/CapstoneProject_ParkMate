package com.parkmate.android.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.model.ParkingSession;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách parking sessions
 */
public class ParkingSessionAdapter extends RecyclerView.Adapter<ParkingSessionAdapter.SessionViewHolder> {

    private Context context;
    private List<ParkingSession> sessionList;
    private OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(ParkingSession session);
    }

    public ParkingSessionAdapter(Context context, List<ParkingSession> sessionList, OnSessionClickListener listener) {
        this.context = context;
        this.sessionList = sessionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parking_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        ParkingSession session = sessionList.get(position);
        holder.bind(session);
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvLicensePlate;
        TextView tvStatus;
        TextView tvEntryTime;
        TextView tvExitTime;
        TextView tvDuration;
        TextView tvReferenceType;
        TextView tvTotalAmount;
        LinearLayout layoutExitTime;

        SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLicensePlate = itemView.findViewById(R.id.tvLicensePlate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvEntryTime = itemView.findViewById(R.id.tvEntryTime);
            tvExitTime = itemView.findViewById(R.id.tvExitTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvReferenceType = itemView.findViewById(R.id.tvReferenceType);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            layoutExitTime = itemView.findViewById(R.id.layoutExitTime);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSessionClick(sessionList.get(position));
                }
            });
        }

        void bind(ParkingSession session) {
            // License plate
            tvLicensePlate.setText(session.getLicensePlate() != null ? session.getLicensePlate() : "N/A");

            // Status
            String status = session.getStatus();
            if ("COMPLETED".equals(status)) {
                tvStatus.setText("Hoàn thành");
                tvStatus.setBackgroundResource(R.drawable.badge_completed);
                tvStatus.setTextColor(Color.parseColor("#2E7D32"));
            } else if ("ACTIVE".equals(status)) {
                tvStatus.setText("Đang đỗ");
                tvStatus.setBackgroundResource(R.drawable.badge_active);
                tvStatus.setTextColor(Color.parseColor("#1976D2"));
            } else {
                tvStatus.setText(status);
                tvStatus.setBackgroundResource(R.drawable.badge_background);
            }

            // Entry time
            tvEntryTime.setText(formatDateTime(session.getEntryTime()));

            // Exit time
            if (session.getExitTime() != null && !session.getExitTime().isEmpty()) {
                layoutExitTime.setVisibility(View.VISIBLE);
                tvExitTime.setText(formatDateTime(session.getExitTime()));
            } else {
                layoutExitTime.setVisibility(View.GONE);
            }

            // Duration
            if (session.getDurationMinute() != null) {
                int minutes = session.getDurationMinute();
                int hours = minutes / 60;
                int mins = minutes % 60;
                String durationText = String.format(Locale.getDefault(),
                        "Thời lượng: %d phút (%dh %d')", minutes, hours, mins);
                tvDuration.setText(durationText);
            } else {
                tvDuration.setText("Thời lượng: Đang tính...");
            }

            // Reference type
            String refType = session.getReferenceType();
            String refTypeText = "Loại: ";
            if ("WALK_IN".equals(refType)) {
                refTypeText += "Vãng lai";
            } else if ("RESERVATION".equals(refType)) {
                refTypeText += "Đặt chỗ";
            } else if ("SUBSCRIPTION".equals(refType)) {
                refTypeText += "Đăng ký";
            } else {
                refTypeText += refType;
            }
            tvReferenceType.setText(refTypeText);

            // Total amount
            if (session.getTotalAmount() != null) {
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                String formattedAmount = formatter.format(session.getTotalAmount()) + "đ";
                tvTotalAmount.setText(formattedAmount);
            } else {
                tvTotalAmount.setText("0đ");
            }
        }

        private String formatDateTime(String dateTimeStr) {
            if (dateTimeStr == null || dateTimeStr.isEmpty()) {
                return "N/A";
            }

            try {
                // Parse: "2025-11-03 11:30:48"
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(dateTimeStr);

                if (date != null) {
                    // Format: "11:30 - 03/11/2025"
                    SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
                    return outputFormat.format(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return dateTimeStr;
        }
    }

    public void addSessions(List<ParkingSession> newSessions) {
        int startPosition = sessionList.size();
        sessionList.addAll(newSessions);
        notifyItemRangeInserted(startPosition, newSessions.size());
    }

    public void clearSessions() {
        sessionList.clear();
        notifyDataSetChanged();
    }
}

