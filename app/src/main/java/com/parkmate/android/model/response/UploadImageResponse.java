package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Response cho API upload image
 */
public class UploadImageResponse {
    @SerializedName("success")
    private Boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("imagePath")
    private String imagePath;

    public Boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImagePath() {
        return imagePath;
    }
}

