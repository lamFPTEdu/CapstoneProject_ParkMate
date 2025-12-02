package com.parkmate.android.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parkmate.android.R;
import com.parkmate.android.adapter.NotificationAdapter;
import com.parkmate.android.model.Notification;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị danh sách notifications từ Local Storage (SharedPreferences)
 * Notifications được lưu khi nhận FCM message
 */
public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity";
    private static final String PREFS_NAME = "parkmate_notifications";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final int PAGE_SIZE = 20; // Load 20 notifications mỗi lần

    // Views
    private ImageButton btnBack;
    private ImageButton btnMarkAllRead;
    private RecyclerView rvNotifications;
    private LinearLayout llEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;

    private NotificationAdapter adapter;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    // Pagination variables
    private List<Notification> allNotifications = new ArrayList<>();
    private int currentPage = 0;
    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate started");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {
            // Initialize SharedPreferences and Gson
            sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            gson = new Gson();
            Log.d(TAG, "SharedPreferences and Gson initialized");

            // Initialize views
            initViews();
            Log.d(TAG, "Views initialized");

            // Setup click listeners
            setupClickListeners();

            // Load notifications from SharedPreferences
            loadNotifications();
            Log.d(TAG, "Loading notifications");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Mark all as read button
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
        }
    }

    private void initViews() {
        try {
            btnBack = findViewById(R.id.btnBack);
            btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
            rvNotifications = findViewById(R.id.rvNotifications);
            llEmptyState = findViewById(R.id.llEmptyState);
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

            if (rvNotifications == null) {
                Log.e(TAG, "rvNotifications is null! Check layout file.");
                return;
            }

            if (llEmptyState == null) {
                Log.e(TAG, "llEmptyState is null! Check layout file.");
            }

            if (swipeRefreshLayout != null) {
                // Setup SwipeRefreshLayout colors
                swipeRefreshLayout.setColorSchemeResources(R.color.primary);
                // Setup refresh listener
                swipeRefreshLayout.setOnRefreshListener(this::refreshNotifications);
            }

            // Setup RecyclerView with LayoutManager
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            rvNotifications.setLayoutManager(layoutManager);
            rvNotifications.setHasFixedSize(true);

            // Setup adapter
            adapter = new NotificationAdapter();
            adapter.setOnNotificationClickListener(notification -> {
                try {
                    // Handle notification click
                    Toast.makeText(this, "Clicked: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
                    // Mark notification as read
                    markNotificationAsRead(notification);
                    // TODO: Navigate to relevant screen based on notification type
                } catch (Exception e) {
                    Log.e(TAG, "Error handling notification click: " + e.getMessage(), e);
                }
            });

            rvNotifications.setAdapter(adapter);

            // Setup infinite scroll
            setupInfiniteScroll(layoutManager);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in initViews: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Refresh notifications khi pull-to-refresh
     */
    private void refreshNotifications() {
        Log.d(TAG, "Refreshing notifications...");

        // Reset pagination
        currentPage = 0;
        isLoading = false;

        // Reload notifications
        loadNotifications();

        // Stop refresh animation
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        Toast.makeText(this, "Đã làm mới danh sách thông báo", Toast.LENGTH_SHORT).show();
    }

    private void loadNotifications() {
        try {
            // Đọc notifications từ SharedPreferences
            String notificationsJson = sharedPreferences.getString(KEY_NOTIFICATIONS, null);

            if (notificationsJson == null || notificationsJson.isEmpty()) {
                Log.d(TAG, "No notifications found in SharedPreferences, showing demo data");
                showDemoData();
                return;
            }

            // Parse JSON thành List<Notification>
            Type listType = new TypeToken<List<Notification>>(){}.getType();
            allNotifications = gson.fromJson(notificationsJson, listType);

            if (allNotifications == null || allNotifications.isEmpty()) {
                Log.d(TAG, "Notifications list is empty");
                showEmptyState();
            } else {
                // Sắp xếp theo timestamp (mới nhất trước)
                allNotifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));

                hideEmptyState();

                // Load trang đầu tiên
                currentPage = 0;
                loadMoreNotifications();

                Log.d(TAG, "Loaded total " + allNotifications.size() + " notifications from local storage");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading notifications from SharedPreferences: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi tải thông báo, hiển thị dữ liệu demo", Toast.LENGTH_SHORT).show();
            showDemoData();
        }
    }

    /**
     * Setup infinite scroll listener
     */
    private void setupInfiniteScroll(LinearLayoutManager layoutManager) {
        rvNotifications.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Chỉ load khi scroll xuống
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Nếu đã scroll gần đến cuối (còn 5 items nữa là hết)
                    if (!isLoading && (visibleItemCount + firstVisibleItemPosition + 5) >= totalItemCount) {
                        loadMoreNotifications();
                    }
                }
            }
        });
    }

    /**
     * Load thêm notifications (pagination)
     */
    private void loadMoreNotifications() {
        if (isLoading) return;

        isLoading = true;

        try {
            // Tính toán index bắt đầu và kết thúc cho trang hiện tại
            int startIndex = currentPage * PAGE_SIZE;
            int endIndex = Math.min(startIndex + PAGE_SIZE, allNotifications.size());

            // Nếu đã load hết
            if (startIndex >= allNotifications.size()) {
                isLoading = false;
                Log.d(TAG, "All notifications loaded");
                return;
            }

            // Lấy notifications cho trang hiện tại
            List<Notification> pageNotifications = allNotifications.subList(startIndex, endIndex);

            // Thêm vào adapter
            if (currentPage == 0) {
                // Trang đầu tiên: set mới
                adapter.setNotifications(new ArrayList<>(pageNotifications));
            } else {
                // Trang tiếp theo: append
                adapter.addNotifications(pageNotifications);
            }

            currentPage++;

            Log.d(TAG, "Loaded page " + currentPage + ", showing " + adapter.getItemCount() + "/" + allNotifications.size() + " notifications");

        } catch (Exception e) {
            Log.e(TAG, "Error loading more notifications: " + e.getMessage(), e);
        } finally {
            isLoading = false;
        }
    }

    /**
     * Mark notification as read
     */
    private void markNotificationAsRead(Notification notification) {
        try {
            if (notification.isRead()) {
                return; // Đã đọc rồi
            }

            // Update notification object
            notification.setRead(true);

            // Update trong allNotifications
            for (Notification n : allNotifications) {
                if (n.getId().equals(notification.getId())) {
                    n.setRead(true);
                    break;
                }
            }

            // Lưu lại vào SharedPreferences
            String updatedJson = gson.toJson(allNotifications);
            sharedPreferences.edit().putString(KEY_NOTIFICATIONS, updatedJson).apply();

            // Update unread count
            int unreadCount = sharedPreferences.getInt("unread_count", 0);
            if (unreadCount > 0) {
                sharedPreferences.edit().putInt("unread_count", unreadCount - 1).apply();
            }

            // Refresh adapter
            adapter.notifyDataSetChanged();

            Log.d(TAG, "Notification marked as read: " + notification.getId());

        } catch (Exception e) {
            Log.e(TAG, "Error marking notification as read: " + e.getMessage(), e);
        }
    }

    /**
     * Hiển thị demo data khi API chưa sẵn sàng hoặc lỗi
     */
    private void showDemoData() {
        List<Notification> demoNotifications = new ArrayList<>();
        demoNotifications.add(new Notification(
                "1",
                "Đặt chỗ thành công",
                "Bạn đã đặt chỗ thành công tại bãi đỗ ABC. Vui lòng check-in trước 10:00 AM.",
                "RESERVATION",
                true,
                "2024-10-28T10:30:00"
        ));
        demoNotifications.add(new Notification(
                "2",
                "Khuyến mãi đặc biệt",
                "Nhận ngay voucher giảm 20% cho lần đặt chỗ tiếp theo!",
                "PROMOTION",
                false,
                "2024-10-28T09:15:00"
        ));
        demoNotifications.add(new Notification(
                "3",
                "Thanh toán thành công",
                "Bạn đã nạp 500.000đ vào ví thành công.",
                "PAYMENT",
                true,
                "2024-10-27T15:20:00"
        ));
        demoNotifications.add(new Notification(
                "4",
                "Cập nhật hệ thống",
                "Hệ thống sẽ bảo trì từ 2:00 AM - 4:00 AM ngày mai.",
                "SYSTEM",
                false,
                "2024-10-27T12:00:00"
        ));
        demoNotifications.add(new Notification(
                "5",
                "Nhắc nhở check-out",
                "Bạn có 30 phút nữa trước khi hết thời gian đỗ xe. Vui lòng gia hạn hoặc check-out.",
                "REMINDER",
                true,
                "2024-10-26T09:00:00"
        ));

        // Lưu demo data vào allNotifications để updateBadge hoạt động
        allNotifications = new ArrayList<>(demoNotifications);

        // Lưu vào SharedPreferences để các activity khác cũng load được
        try {
            String demoJson = gson.toJson(demoNotifications);
            sharedPreferences.edit().putString(KEY_NOTIFICATIONS, demoJson).apply();

            // Đếm số unread và lưu
            int unreadCount = 0;
            for (Notification notif : demoNotifications) {
                if (!notif.isRead()) {
                    unreadCount++;
                }
            }
            sharedPreferences.edit().putInt("unread_count", unreadCount).apply();
            Log.d(TAG, "Demo data saved to SharedPreferences with " + unreadCount + " unread");
        } catch (Exception e) {
            Log.e(TAG, "Error saving demo data: " + e.getMessage(), e);
        }

        hideEmptyState();
        adapter.setNotifications(demoNotifications);
    }

    private void showEmptyState() {
        try {
            if (rvNotifications != null) {
                rvNotifications.setVisibility(View.GONE);
            }
            if (llEmptyState != null) {
                llEmptyState.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing empty state: " + e.getMessage(), e);
        }
    }

    private void hideEmptyState() {
        try {
            if (rvNotifications != null) {
                rvNotifications.setVisibility(View.VISIBLE);
            }
            if (llEmptyState != null) {
                llEmptyState.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding empty state: " + e.getMessage(), e);
        }
    }

    /**
     * Đánh dấu tất cả thông báo là đã đọc
     */
    private void markAllAsRead() {
        try {
            if (allNotifications == null || allNotifications.isEmpty()) {
                Toast.makeText(this, "Không có thông báo nào", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra xem có thông báo chưa đọc không
            boolean hasUnread = false;
            for (Notification notification : allNotifications) {
                if (!notification.isRead()) {
                    notification.setRead(true);
                    hasUnread = true;
                }
            }

            if (!hasUnread) {
                Toast.makeText(this, "Tất cả thông báo đã được đọc", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lưu lại vào SharedPreferences
            String updatedJson = gson.toJson(allNotifications);
            sharedPreferences.edit().putString(KEY_NOTIFICATIONS, updatedJson).apply();

            // Reset unread count
            sharedPreferences.edit().putInt("unread_count", 0).apply();

            // Refresh adapter
            adapter.notifyDataSetChanged();

            Toast.makeText(this, "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show();

            Log.d(TAG, "All notifications marked as read");

        } catch (Exception e) {
            Log.e(TAG, "Error marking all as read: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi đánh dấu đã đọc", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup if needed
    }
}

