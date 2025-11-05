package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.model.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Thêm notifications vào cuối list (dùng cho infinite scroll)
     */
    public void addNotifications(List<Notification> newNotifications) {
        if (newNotifications == null || newNotifications.isEmpty()) {
            return;
        }
        int startPosition = this.notifications.size();
        this.notifications.addAll(newNotifications);
        notifyItemRangeInserted(startPosition, newNotifications.size());
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification, listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivNotificationIcon;
        private final TextView tvNotificationTitle;
        private final TextView tvNotificationMessage;
        private final TextView tvNotificationTime;
        private final View vUnreadDot;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivNotificationIcon = itemView.findViewById(R.id.ivNotificationIcon);
            tvNotificationTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvNotificationMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvNotificationTime = itemView.findViewById(R.id.tvNotificationTime);
            vUnreadDot = itemView.findViewById(R.id.vUnreadDot);
        }

        public void bind(Notification notification, OnNotificationClickListener listener) {
            tvNotificationTitle.setText(notification.getTitle());
            tvNotificationMessage.setText(notification.getMessage());
            tvNotificationTime.setText(formatTime(notification.getTimestamp()));

            // Hiển thị unread dot nếu notification chưa đọc
            vUnreadDot.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }

        private String formatTime(String timestamp) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = sdf.parse(timestamp);
                if (date == null) return "Vừa xong";

                long diff = System.currentTimeMillis() - date.getTime();
                long seconds = diff / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;

                if (days > 0) return days + " ngày trước";
                if (hours > 0) return hours + " giờ trước";
                if (minutes > 0) return minutes + " phút trước";
                return "Vừa xong";
            } catch (Exception e) {
                return "Vừa xong";
            }
        }
    }
}

