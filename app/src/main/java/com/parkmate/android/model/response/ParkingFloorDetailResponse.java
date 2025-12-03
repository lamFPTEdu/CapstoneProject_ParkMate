package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ParkingFloorDetailResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private FloorData data;

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
    public FloorData getData() { return data; }
    public void setData(FloorData data) { this.data = data; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public Object getMeta() { return meta; }
    public void setMeta(Object meta) { this.meta = meta; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public static class FloorData {
        @SerializedName("id")
        private Long id;

        @SerializedName("floorNumber")
        private Integer floorNumber;

        @SerializedName("floorTopLeftX")
        private Double floorTopLeftX;

        @SerializedName("floorTopLeftY")
        private Double floorTopLeftY;

        @SerializedName("floorWidth")
        private Double floorWidth;

        @SerializedName("floorHeight")
        private Double floorHeight;

        @SerializedName("floorName")
        private String floorName;

        @SerializedName("isActive")
        private Boolean isActive;

        @SerializedName("parkingFloorCapacity")
        private List<FloorCapacity> parkingFloorCapacity;

        @SerializedName("areas")
        private List<Area> areas;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getFloorNumber() { return floorNumber; }
        public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }
        public Double getFloorTopLeftX() { return floorTopLeftX; }
        public void setFloorTopLeftX(Double floorTopLeftX) { this.floorTopLeftX = floorTopLeftX; }
        public Double getFloorTopLeftY() { return floorTopLeftY; }
        public void setFloorTopLeftY(Double floorTopLeftY) { this.floorTopLeftY = floorTopLeftY; }
        public Double getFloorWidth() { return floorWidth; }
        public void setFloorWidth(Double floorWidth) { this.floorWidth = floorWidth; }
        public Double getFloorHeight() { return floorHeight; }
        public void setFloorHeight(Double floorHeight) { this.floorHeight = floorHeight; }
        public String getFloorName() { return floorName; }
        public void setFloorName(String floorName) { this.floorName = floorName; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public List<FloorCapacity> getParkingFloorCapacity() { return parkingFloorCapacity; }
        public void setParkingFloorCapacity(List<FloorCapacity> parkingFloorCapacity) { this.parkingFloorCapacity = parkingFloorCapacity; }
        public List<Area> getAreas() { return areas; }
        public void setAreas(List<Area> areas) { this.areas = areas; }

        // Helper: Đếm tổng số chỗ trống trong tầng
        public int getTotalAvailableSpots() {
            int count = 0;
            if (areas != null) {
                for (Area area : areas) {
                    // Nếu có spots data thì đếm spots available
                    if (area.getSpots() != null && !area.getSpots().isEmpty()) {
                        count += area.getAvailableSpotCount();
                    } else {
                        // Nếu không có spots data, giả định tất cả spots đều available
                        // (thực tế cần gọi API khác để lấy thông tin chi tiết)
                        if (area.getTotalSpots() != null) {
                            count += area.getTotalSpots();
                        }
                    }
                }
            }
            return count;
        }
    }

    public static class FloorCapacity {
        @SerializedName("capacity")
        private Integer capacity;

        @SerializedName("vehicleType")
        private String vehicleType;

        @SerializedName("supportElectricVehicle")
        private Boolean supportElectricVehicle;

        @SerializedName("isActive")
        private Boolean isActive;

        // Getters and Setters
        public Integer getCapacity() { return capacity; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }
        public String getVehicleType() { return vehicleType; }
        public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
        public Boolean getSupportElectricVehicle() { return supportElectricVehicle; }
        public void setSupportElectricVehicle(Boolean supportElectricVehicle) { this.supportElectricVehicle = supportElectricVehicle; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }

    public static class Area {
        @SerializedName("id")
        private Long id;

        @SerializedName("name")
        private String name;

        @SerializedName("vehicleType")
        private String vehicleType;

        @SerializedName("totalSpots")
        private Integer totalSpots;

        @SerializedName("areaTopLeftX")
        private Integer areaTopLeftX;

        @SerializedName("areaTopLeftY")
        private Integer areaTopLeftY;

        @SerializedName("areaWidth")
        private Integer areaWidth;

        @SerializedName("areaHeight")
        private Integer areaHeight;

        @SerializedName("isActive")
        private Boolean isActive;

        @SerializedName("supportElectricVehicle")
        private Boolean supportElectricVehicle;

        @SerializedName("areaType")
        private String areaType; // WALK_IN_ONLY, RESERVED_ONLY, etc.

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        @SerializedName("spots")
        private List<Spot> spots;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getVehicleType() { return vehicleType; }
        public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
        public Integer getTotalSpots() { return totalSpots; }
        public void setTotalSpots(Integer totalSpots) { this.totalSpots = totalSpots; }
        public Integer getAreaTopLeftX() { return areaTopLeftX; }
        public void setAreaTopLeftX(Integer areaTopLeftX) { this.areaTopLeftX = areaTopLeftX; }
        public Integer getAreaTopLeftY() { return areaTopLeftY; }
        public void setAreaTopLeftY(Integer areaTopLeftY) { this.areaTopLeftY = areaTopLeftY; }
        public Integer getAreaWidth() { return areaWidth; }
        public void setAreaWidth(Integer areaWidth) { this.areaWidth = areaWidth; }
        public Integer getAreaHeight() { return areaHeight; }
        public void setAreaHeight(Integer areaHeight) { this.areaHeight = areaHeight; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public Boolean getSupportElectricVehicle() { return supportElectricVehicle; }
        public void setSupportElectricVehicle(Boolean supportElectricVehicle) { this.supportElectricVehicle = supportElectricVehicle; }
        public String getAreaType() { return areaType; }
        public void setAreaType(String areaType) { this.areaType = areaType; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
        public List<Spot> getSpots() { return spots; }
        public void setSpots(List<Spot> spots) { this.spots = spots; }

        // Helper: Đếm số chỗ trống trong khu vực này
        public int getAvailableSpotCount() {
            if (spots == null) return 0;
            int count = 0;
            for (Spot spot : spots) {
                if ("AVAILABLE".equals(spot.getStatus())) {
                    count++;
                }
            }
            return count;
        }
    }

    public static class Spot {
        @SerializedName("id")
        private Long id;

        @SerializedName("name")
        private String name;

        @SerializedName("spotTopLeftX")
        private Integer spotTopLeftX;

        @SerializedName("spotTopLeftY")
        private Integer spotTopLeftY;

        @SerializedName("spotWidth")
        private Integer spotWidth;

        @SerializedName("spotHeight")
        private Integer spotHeight;

        @SerializedName("status")
        private String status; // AVAILABLE, OCCUPIED, BLOCKED, RESERVED

        @SerializedName("blockReason")
        private String blockReason;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getSpotTopLeftX() { return spotTopLeftX; }
        public void setSpotTopLeftX(Integer spotTopLeftX) { this.spotTopLeftX = spotTopLeftX; }
        public Integer getSpotTopLeftY() { return spotTopLeftY; }
        public void setSpotTopLeftY(Integer spotTopLeftY) { this.spotTopLeftY = spotTopLeftY; }
        public Integer getSpotWidth() { return spotWidth; }
        public void setSpotWidth(Integer spotWidth) { this.spotWidth = spotWidth; }
        public Integer getSpotHeight() { return spotHeight; }
        public void setSpotHeight(Integer spotHeight) { this.spotHeight = spotHeight; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getBlockReason() { return blockReason; }
        public void setBlockReason(String blockReason) { this.blockReason = blockReason; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

        // Helper methods
        public boolean isAvailable() {
            return "AVAILABLE".equals(status);
        }

        public boolean isOccupied() {
            return "OCCUPIED".equals(status);
        }

        public boolean isBlocked() {
            return "BLOCKED".equals(status);
        }

        public boolean isReserved() {
            return "RESERVED".equals(status);
        }
    }
}
