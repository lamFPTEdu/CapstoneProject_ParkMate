package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;
import com.parkmate.android.model.ParkingSession;

import java.util.List;

/**
 * Response cho API lấy danh sách parking sessions
 */
public class ParkingSessionResponse {
    @SerializedName("content")
    private List<ParkingSession> content;

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

    // Getters
    public List<ParkingSession> getContent() {
        return content;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public boolean isLast() {
        return last;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public int getSize() {
        return size;
    }

    public int getNumber() {
        return number;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isEmpty() {
        return empty;
    }

    // Pageable class
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

