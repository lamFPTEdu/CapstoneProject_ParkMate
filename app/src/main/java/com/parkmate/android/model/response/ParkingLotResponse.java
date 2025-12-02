package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ParkingLotResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private PageData data;

    @SerializedName("error")
    private String error;

    @SerializedName("meta")
    private Object meta;

    @SerializedName("timestamp")
    private String timestamp;

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public PageData getData() { return data; }
    public void setData(PageData data) { this.data = data; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public Object getMeta() { return meta; }
    public void setMeta(Object meta) { this.meta = meta; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public static class PageData {
        @SerializedName("content")
        private List<ParkingLot> content;

        @SerializedName("pageable")
        private Pageable pageable;

        @SerializedName("totalPages")
        private int totalPages;

        @SerializedName("totalElements")
        private int totalElements;

        @SerializedName("last")
        private boolean last;

        @SerializedName("first")
        private boolean first;

        @SerializedName("size")
        private int size;

        @SerializedName("number")
        private int number;

        @SerializedName("numberOfElements")
        private int numberOfElements;

        @SerializedName("empty")
        private boolean empty;

        @SerializedName("sort")
        private List<Sort> sort;

        // Getters and Setters
        public List<ParkingLot> getContent() { return content; }
        public void setContent(List<ParkingLot> content) { this.content = content; }
        public Pageable getPageable() { return pageable; }
        public void setPageable(Pageable pageable) { this.pageable = pageable; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
        public int getTotalElements() { return totalElements; }
        public void setTotalElements(int totalElements) { this.totalElements = totalElements; }
        public boolean isLast() { return last; }
        public void setLast(boolean last) { this.last = last; }
        public boolean isFirst() { return first; }
        public void setFirst(boolean first) { this.first = first; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public int getNumber() { return number; }
        public void setNumber(int number) { this.number = number; }
        public int getNumberOfElements() { return numberOfElements; }
        public void setNumberOfElements(int numberOfElements) { this.numberOfElements = numberOfElements; }
        public boolean isEmpty() { return empty; }
        public void setEmpty(boolean empty) { this.empty = empty; }
        public List<Sort> getSort() { return sort; }
        public void setSort(List<Sort> sort) { this.sort = sort; }
    }

    public static class Pageable {
        @SerializedName("pageNumber")
        private int pageNumber;

        @SerializedName("pageSize")
        private int pageSize;

        @SerializedName("sort")
        private List<Sort> sort;

        @SerializedName("offset")
        private int offset;

        @SerializedName("paged")
        private boolean paged;

        @SerializedName("unpaged")
        private boolean unpaged;

        // Getters and Setters
        public int getPageNumber() { return pageNumber; }
        public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        public List<Sort> getSort() { return sort; }
        public void setSort(List<Sort> sort) { this.sort = sort; }
        public int getOffset() { return offset; }
        public void setOffset(int offset) { this.offset = offset; }
        public boolean isPaged() { return paged; }
        public void setPaged(boolean paged) { this.paged = paged; }
        public boolean isUnpaged() { return unpaged; }
        public void setUnpaged(boolean unpaged) { this.unpaged = unpaged; }
    }

    public static class Sort {
        @SerializedName("direction")
        private String direction;

        @SerializedName("property")
        private String property;

        @SerializedName("ignoreCase")
        private boolean ignoreCase;

        @SerializedName("nullHandling")
        private String nullHandling;

        @SerializedName("descending")
        private boolean descending;

        @SerializedName("ascending")
        private boolean ascending;

        // Getters and Setters
        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }
        public String getProperty() { return property; }
        public void setProperty(String property) { this.property = property; }
        public boolean isIgnoreCase() { return ignoreCase; }
        public void setIgnoreCase(boolean ignoreCase) { this.ignoreCase = ignoreCase; }
        public String getNullHandling() { return nullHandling; }
        public void setNullHandling(String nullHandling) { this.nullHandling = nullHandling; }
        public boolean isDescending() { return descending; }
        public void setDescending(boolean descending) { this.descending = descending; }
        public boolean isAscending() { return ascending; }
        public void setAscending(boolean ascending) { this.ascending = ascending; }
    }

    public static class ParkingLot implements java.io.Serializable {
        @SerializedName("id")
        private Long id;

        @SerializedName("partnerId")
        private Long partnerId;

        @SerializedName("name")
        private String name;

        @SerializedName("streetAddress")
        private String streetAddress;

        @SerializedName("ward")
        private String ward;

        @SerializedName("city")
        private String city;

        @SerializedName("latitude")
        private Double latitude;

        @SerializedName("longitude")
        private Double longitude;

        @SerializedName("totalFloors")
        private Integer totalFloors;

        @SerializedName("openTime")
        private String openTime;

        @SerializedName("closeTime")
        private String closeTime;

        @SerializedName("is24Hour")
        private Boolean is24Hour;

        @SerializedName("boundaryTopLeftX")
        private Double boundaryTopLeftX;

        @SerializedName("boundaryTopLeftY")
        private Double boundaryTopLeftY;

        @SerializedName("boundaryWidth")
        private Double boundaryWidth;

        @SerializedName("boundaryHeight")
        private Double boundaryHeight;

        @SerializedName("status")
        private String status;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        @SerializedName("subscriptions")
        private java.util.List<com.parkmate.android.model.SubscriptionPackage> subscriptions;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getPartnerId() { return partnerId; }
        public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStreetAddress() { return streetAddress; }
        public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
        public String getWard() { return ward; }
        public void setWard(String ward) { this.ward = ward; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public Integer getTotalFloors() { return totalFloors; }
        public void setTotalFloors(Integer totalFloors) { this.totalFloors = totalFloors; }
        public String getOpenTime() { return openTime; }
        public void setOpenTime(String openTime) { this.openTime = openTime; }
        public String getCloseTime() { return closeTime; }
        public void setCloseTime(String closeTime) { this.closeTime = closeTime; }
        public Boolean getIs24Hour() { return is24Hour; }
        public void setIs24Hour(Boolean is24Hour) { this.is24Hour = is24Hour; }
        public Double getBoundaryTopLeftX() { return boundaryTopLeftX; }
        public void setBoundaryTopLeftX(Double boundaryTopLeftX) { this.boundaryTopLeftX = boundaryTopLeftX; }
        public Double getBoundaryTopLeftY() { return boundaryTopLeftY; }
        public void setBoundaryTopLeftY(Double boundaryTopLeftY) { this.boundaryTopLeftY = boundaryTopLeftY; }
        public Double getBoundaryWidth() { return boundaryWidth; }
        public void setBoundaryWidth(Double boundaryWidth) { this.boundaryWidth = boundaryWidth; }
        public Double getBoundaryHeight() { return boundaryHeight; }
        public void setBoundaryHeight(Double boundaryHeight) { this.boundaryHeight = boundaryHeight; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

        public java.util.List<com.parkmate.android.model.SubscriptionPackage> getSubscriptions() { return subscriptions; }
        public void setSubscriptions(java.util.List<com.parkmate.android.model.SubscriptionPackage> subscriptions) { this.subscriptions = subscriptions; }

        // Helper method để lấy địa chỉ đầy đủ
        public String getFullAddress() {
            return streetAddress + ", " + ward + ", " + city;
        }

        // Helper method để check bãi xe có đang hoạt động không
        public boolean isActive() {
            return "ACTIVE".equals(status);
        }

        // Helper method để lấy giờ hoạt động
        public String getOperatingHours() {
            if (is24Hour != null && is24Hour) {
                return "24/7";
            }
            return openTime + " - " + closeTime;
        }
    }
}
