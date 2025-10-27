package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AreaDetailResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private AreaData data;

    @SerializedName("error")
    private Object error;

    @SerializedName("meta")
    private Object meta;

    @SerializedName("timestamp")
    private String timestamp;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public AreaData getData() {
        return data;
    }

    public static class AreaData {
        @SerializedName("id")
        private Long id;

        @SerializedName("name")
        private String name;

        @SerializedName("vehicleType")
        private String vehicleType;

        @SerializedName("totalSpots")
        private Integer totalSpots;

        @SerializedName("areaTopLeftX")
        private Double areaTopLeftX;

        @SerializedName("areaTopLeftY")
        private Double areaTopLeftY;

        @SerializedName("areaWidth")
        private Double areaWidth;

        @SerializedName("areaHeight")
        private Double areaHeight;

        @SerializedName("isActive")
        private Boolean isActive;

        @SerializedName("supportElectricVehicle")
        private Boolean supportElectricVehicle;

        @SerializedName("areaType")
        private String areaType;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        @SerializedName("spots")
        private List<Spot> spots;

        @SerializedName("pricingRule")
        private PricingRule pricingRule;

        // Getters
        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getVehicleType() {
            return vehicleType;
        }

        public Integer getTotalSpots() {
            return totalSpots;
        }

        public Double getAreaTopLeftX() {
            return areaTopLeftX;
        }

        public Double getAreaTopLeftY() {
            return areaTopLeftY;
        }

        public Double getAreaWidth() {
            return areaWidth;
        }

        public Double getAreaHeight() {
            return areaHeight;
        }

        public Boolean getActive() {
            return isActive;
        }

        public Boolean getSupportElectricVehicle() {
            return supportElectricVehicle;
        }

        public String getAreaType() {
            return areaType;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public List<Spot> getSpots() {
            return spots;
        }

        public PricingRule getPricingRule() {
            return pricingRule;
        }
    }

    public static class Spot {
        @SerializedName("id")
        private Long id;

        @SerializedName("name")
        private String name;

        @SerializedName("spotTopLeftX")
        private Double spotTopLeftX;

        @SerializedName("spotTopLeftY")
        private Double spotTopLeftY;

        @SerializedName("spotWidth")
        private Double spotWidth;

        @SerializedName("spotHeight")
        private Double spotHeight;

        @SerializedName("status")
        private String status;

        @SerializedName("blockReason")
        private String blockReason;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        // Getters
        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Double getSpotTopLeftX() {
            return spotTopLeftX;
        }

        public Double getSpotTopLeftY() {
            return spotTopLeftY;
        }

        public Double getSpotWidth() {
            return spotWidth;
        }

        public Double getSpotHeight() {
            return spotHeight;
        }

        public String getStatus() {
            return status;
        }

        public String getBlockReason() {
            return blockReason;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        // Setters for compatibility
        public void setId(Long id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setBlockReason(String blockReason) {
            this.blockReason = blockReason;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    public static class PricingRule {
        @SerializedName("id")
        private Long id;

        @SerializedName("vehicleType")
        private String vehicleType;

        @SerializedName("ruleName")
        private String ruleName;

        @SerializedName("stepRate")
        private Integer stepRate;

        @SerializedName("stepMinute")
        private Integer stepMinute;

        @SerializedName("initialCharge")
        private Integer initialCharge;

        @SerializedName("initialDurationMinute")
        private Integer initialDurationMinute;

        @SerializedName("isActive")
        private Boolean isActive;

        @SerializedName("validFrom")
        private String validFrom;

        @SerializedName("validUntil")
        private String validUntil;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        @SerializedName("overridePricingRule")
        private OverridePricingRule overridePricingRule;

        // Getters
        public Long getId() {
            return id;
        }

        public String getVehicleType() {
            return vehicleType;
        }

        public String getRuleName() {
            return ruleName;
        }

        public Integer getStepRate() {
            return stepRate;
        }

        public Integer getStepMinute() {
            return stepMinute;
        }

        public Integer getInitialCharge() {
            return initialCharge;
        }

        public Integer getInitialDurationMinute() {
            return initialDurationMinute;
        }

        public Boolean getActive() {
            return isActive;
        }

        public String getValidFrom() {
            return validFrom;
        }

        public String getValidUntil() {
            return validUntil;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public OverridePricingRule getOverridePricingRule() {
            return overridePricingRule;
        }
    }

    public static class OverridePricingRule {
        @SerializedName("id")
        private Long id;

        @SerializedName("ruleName")
        private String ruleName;

        @SerializedName("stepRate")
        private Integer stepRate;

        @SerializedName("stepMinute")
        private Integer stepMinute;

        @SerializedName("initialCharge")
        private Integer initialCharge;

        @SerializedName("initialDurationMinute")
        private Integer initialDurationMinute;

        @SerializedName("isActive")
        private Boolean isActive;

        @SerializedName("validFrom")
        private String validFrom;

        @SerializedName("validUntil")
        private String validUntil;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("updatedAt")
        private String updatedAt;

        // Getters
        public Long getId() {
            return id;
        }

        public String getRuleName() {
            return ruleName;
        }

        public Integer getStepRate() {
            return stepRate;
        }

        public Integer getStepMinute() {
            return stepMinute;
        }

        public Integer getInitialCharge() {
            return initialCharge;
        }

        public Integer getInitialDurationMinute() {
            return initialDurationMinute;
        }

        public Boolean getActive() {
            return isActive;
        }

        public String getValidFrom() {
            return validFrom;
        }

        public String getValidUntil() {
            return validUntil;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }
    }
}

