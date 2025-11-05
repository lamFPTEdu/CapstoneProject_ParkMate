package com.parkmate.android.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parkmate.android.R;
import com.parkmate.android.activity.HomeActivity;
import com.parkmate.android.model.Notification;
import com.parkmate.android.model.request.CreateMobileDeviceRequest;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.network.ApiService;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Firebase Cloud Messaging Service
 * Xử lý nhận và hiển thị push notifications
 */
public class ParkMateFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "parkmate_notifications";
    private static final String CHANNEL_NAME = "ParkMate Notifications";
    private static final String PREF_NAME = "ParkMatePrefs";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    // Notification storage
    private static final String NOTIF_PREFS_NAME = "parkmate_notifications";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_UNREAD_COUNT = "unread_count";

    private final Gson gson = new Gson();

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed FCM token: " + token);

        // Lưu token vào SharedPreferences
        saveFcmToken(token);

        // Gửi token lên server
        sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        String title = "";
        String body = "";
        Map<String, String> data = remoteMessage.getData();

        // Kiểm tra nếu message có notification payload
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Message Notification Title: " + title);
            Log.d(TAG, "Message Notification Body: " + body);
        }

        // Kiểm tra nếu message có data payload
        if (!data.isEmpty()) {
            Log.d(TAG, "Message data payload: " + data);

            // Nếu không có notification payload, lấy title/body từ data
            if (title == null || title.isEmpty()) {
                title = data.get("title");
            }
            if (body == null || body.isEmpty()) {
                body = data.get("body");
            }
        }

        // Lưu notification vào SharedPreferences
        if (title != null && !title.isEmpty()) {
            saveNotificationToLocal(title, body, data);
        }

        // Hiển thị notification trong system tray
        showNotification(title, body);
    }

    /**
     * Lưu notification vào SharedPreferences
     */
    private void saveNotificationToLocal(String title, String message, Map<String, String> data) {
        try {
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(NOTIF_PREFS_NAME, Context.MODE_PRIVATE);

            // Đọc danh sách notifications hiện tại
            String notificationsJson = prefs.getString(KEY_NOTIFICATIONS, null);
            List<Notification> notifications;

            if (notificationsJson == null || notificationsJson.isEmpty()) {
                notifications = new ArrayList<>();
            } else {
                Type listType = new TypeToken<List<Notification>>(){}.getType();
                notifications = gson.fromJson(notificationsJson, listType);
                if (notifications == null) {
                    notifications = new ArrayList<>();
                }
            }

            // Tạo notification mới
            String notifId = UUID.randomUUID().toString();
            String type = data.get("type");
            if (type == null || type.isEmpty()) {
                type = "SYSTEM";
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .format(new Date());

            Notification newNotification = new Notification(
                    notifId,
                    title,
                    message,
                    type,
                    false, // isRead = false (chưa đọc)
                    timestamp
            );

            // Lưu data nếu có
            if (!data.isEmpty()) {
                String dataJson = gson.toJson(data);
                newNotification.setData(dataJson);
            }

            // Thêm vào đầu list (mới nhất trước)
            notifications.add(0, newNotification);

            // Giới hạn số lượng notifications (giữ tối đa 100)
            if (notifications.size() > 100) {
                notifications = notifications.subList(0, 100);
            }

            // Lưu lại vào SharedPreferences
            String updatedJson = gson.toJson(notifications);
            prefs.edit().putString(KEY_NOTIFICATIONS, updatedJson).apply();

            // Cập nhật unread count
            int unreadCount = prefs.getInt(KEY_UNREAD_COUNT, 0);
            prefs.edit().putInt(KEY_UNREAD_COUNT, unreadCount + 1).apply();

            Log.d(TAG, "Notification saved to local storage. Total: " + notifications.size() + ", Unread: " + (unreadCount + 1));

        } catch (Exception e) {
            Log.e(TAG, "Error saving notification to local: " + e.getMessage(), e);
        }
    }

    /**
     * Lưu FCM token vào SharedPreferences
     */
    private void saveFcmToken(String token) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply();
        Log.d(TAG, "FCM token saved to SharedPreferences");
    }

    /**
     * Gửi FCM token lên server để đăng ký device
     * Backend sẽ check ownedByMe=true và lấy userId từ JWT token
     */
    private void sendTokenToServer(String token) {
        try {
            String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
            String deviceOs = "ANDROID";

            Log.d(TAG, "Registering device - DeviceId: " + deviceId + ", DeviceName: " + deviceName);

            // Backend sẽ tự lấy userId từ JWT token khi check ownedByMe=true
            CreateMobileDeviceRequest request = new CreateMobileDeviceRequest(
                    deviceId,
                    deviceName,
                    deviceOs,
                    token
            );

            ApiService apiService = ApiClient.getApiService();
            apiService.registerMobileDevice(request)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            response -> {
                                if (response.isSuccess()) {
                                    Log.d(TAG, "Device registered successfully with server");
                                    Log.d(TAG, "Response: " + response.getData());
                                } else {
                                    Log.e(TAG, "Failed to register device: " + response.getMessage());
                                }
                            },
                            error -> {
                                Log.e(TAG, "Error registering device: " + error.getMessage(), error);
                            }
                    );

        } catch (Exception e) {
            Log.e(TAG, "Error sending token to server: " + e.getMessage(), e);
        }
    }

    /**
     * Hiển thị notification
     */
    private void showNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo notification channel cho Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications from ParkMate app");
            notificationManager.createNotificationChannel(channel);
        }

        // Tạo intent để mở app khi click vào notification
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_24)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        // Hiển thị notification
        notificationManager.notify(0, notificationBuilder.build());
    }

    /**
     * Lấy FCM token đã lưu từ SharedPreferences
     */
    public static String getSavedFcmToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_FCM_TOKEN, null);
    }
}

