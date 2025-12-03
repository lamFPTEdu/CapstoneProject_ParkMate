package com.parkmate.android.model.request;

import com.google.gson.annotations.SerializedName;

public class UpdateRatingRequest {
    @SerializedName("overallRating")
    private Integer overallRating;

    @SerializedName("title")
    private String title;

    @SerializedName("comment")
    private String comment;

    @SerializedName("isVisible")
    private Boolean isVisible;

    public UpdateRatingRequest() {
    }

    public UpdateRatingRequest(Integer overallRating, String title, String comment) {
        this.overallRating = overallRating;
        this.title = title;
        this.comment = comment;
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
}

