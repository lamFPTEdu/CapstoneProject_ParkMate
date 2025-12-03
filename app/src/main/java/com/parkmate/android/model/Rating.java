package com.parkmate.android.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Model cho Rating (Đánh giá bãi đỗ xe)
 */
public class Rating implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("id")
    private Long id;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("parkingLotId")
    private Long parkingLotId;

    @SerializedName("overallRating")
    private Integer overallRating; // 1-5 stars

    @SerializedName("title")
    private String title;

    @SerializedName("comment")
    private String comment;

    @SerializedName("isVisible")
    private Boolean isVisible;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // Constructor
    public Rating() {
    }

    public Rating(Long userId, Long parkingLotId, Integer overallRating, String title, String comment) {
        this.userId = userId;
        this.parkingLotId = parkingLotId;
        this.overallRating = overallRating;
        this.title = title;
        this.comment = comment;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(Long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public Integer getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(Integer overallRating) {
        this.overallRating = overallRating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}

