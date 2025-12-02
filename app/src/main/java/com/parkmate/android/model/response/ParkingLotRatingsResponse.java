package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ParkingLotRatingsResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private RatingsData data;

    @SerializedName("error")
    private String error;

    @SerializedName("meta")
    private Object meta;

    @SerializedName("timestamp")
    private String timestamp;

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RatingsData getData() {
        return data;
    }

    public void setData(RatingsData data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getMeta() {
        return meta;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public static class RatingsData {
        @SerializedName("content")
        private List<com.parkmate.android.model.ParkingLotRating> content;

        @SerializedName("totalPages")
        private Integer totalPages;

        @SerializedName("totalElements")
        private Integer totalElements;

        @SerializedName("size")
        private Integer size;

        @SerializedName("number")
        private Integer number;

        @SerializedName("first")
        private Boolean first;

        @SerializedName("last")
        private Boolean last;

        @SerializedName("empty")
        private Boolean empty;

        // Getters and Setters
        public List<com.parkmate.android.model.ParkingLotRating> getContent() {
            return content;
        }

        public void setContent(List<com.parkmate.android.model.ParkingLotRating> content) {
            this.content = content;
        }

        public Integer getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }

        public Integer getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(Integer totalElements) {
            this.totalElements = totalElements;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        public Boolean getFirst() {
            return first;
        }

        public void setFirst(Boolean first) {
            this.first = first;
        }

        public Boolean getLast() {
            return last;
        }

        public void setLast(Boolean last) {
            this.last = last;
        }

        public Boolean getEmpty() {
            return empty;
        }

        public void setEmpty(Boolean empty) {
            this.empty = empty;
        }
    }
}

