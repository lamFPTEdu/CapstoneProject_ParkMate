package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model cho notification item
 */
public class Notification {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("type")
    private String type; // RESERVATION, PAYMENT, SYSTEM, etc.

    @SerializedName("isRead")
    private boolean isRead;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("data")
    private String data; // JSON data for extra information

    public Notification() {
    }

    public Notification(String id, String title, String message, String type, boolean isRead, String timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}

