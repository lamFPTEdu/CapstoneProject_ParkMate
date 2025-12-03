package com.parkmate.android.model.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request model để tạo rating cho parking lot
 */
public class CreateRatingRequest {

    @SerializedName("userId")
    private Long userId;

    @SerializedName("overallRating")
    private Integer overallRating; // 1-5 stars (required)

    @SerializedName("title")
    private String title; // Optional, max 200 chars

    @SerializedName("comment")
    private String comment; // Optional

    public CreateRatingRequest() {
    }

    public CreateRatingRequest(Long userId, Integer overallRating, String title, String comment) {
        this.userId = userId;
        this.overallRating = overallRating;
        this.title = title;
        this.comment = comment;
    }

    // Getters & Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
}

