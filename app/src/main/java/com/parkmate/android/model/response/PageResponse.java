package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Generic Page Response cho các API trả về dạng paginated
 */
public class PageResponse<T> {

    @SerializedName("content")
    private List<T> content;

    @SerializedName("pageable")
    private Pageable pageable;

    @SerializedName("totalElements")
    private int totalElements;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("last")
    private boolean last;

    @SerializedName("size")
    private int size;

    @SerializedName("number")
    private int number;

    @SerializedName("numberOfElements")
    private int numberOfElements;

    @SerializedName("first")
    private boolean first;

    @SerializedName("empty")
    private boolean empty;

    // Getters
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
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

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
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

    /**
     * Pageable info class
     */
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

        // Getters & Setters
        public int getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public boolean isPaged() {
            return paged;
        }

        public void setPaged(boolean paged) {
            this.paged = paged;
        }

        public boolean isUnpaged() {
            return unpaged;
        }

        public void setUnpaged(boolean unpaged) {
            this.unpaged = unpaged;
        }
    }
}

