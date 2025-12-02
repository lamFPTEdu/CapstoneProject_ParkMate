package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;
import com.parkmate.android.model.UserSubscription;

import java.util.List;

public class UserSubscriptionResponse {
    @SerializedName("content")
    private List<UserSubscription> content;

    @SerializedName("pageable")
    private Pageable pageable;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("totalElements")
    private int totalElements;

    @SerializedName("last")
    private boolean last;

    @SerializedName("numberOfElements")
    private int numberOfElements;

    @SerializedName("size")
    private int size;

    @SerializedName("number")
    private int number;

    @SerializedName("first")
    private boolean first;

    @SerializedName("empty")
    private boolean empty;

    public List<UserSubscription> getContent() {
        return content;
    }

    public void setContent(List<UserSubscription> content) {
        this.content = content;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public static class Pageable {
        @SerializedName("pageNumber")
        private int pageNumber;

        @SerializedName("pageSize")
        private int pageSize;

        @SerializedName("offset")
        private int offset;

        @SerializedName("paged")
        private boolean paged;

        @SerializedName("unpaged")
        private boolean unpaged;

        public int getPageNumber() {
            return pageNumber;
        }

        public int getPageSize() {
            return pageSize;
        }

        public int getOffset() {
            return offset;
        }

        public boolean isPaged() {
            return paged;
        }

        public boolean isUnpaged() {
            return unpaged;
        }
    }
}

