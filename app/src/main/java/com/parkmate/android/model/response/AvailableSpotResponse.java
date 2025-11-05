package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;

public class AvailableSpotResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Data data;

    @SerializedName("error")
    private String error;

    @SerializedName("timestamp")
    private String timestamp;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public static class Data implements java.io.Serializable {
        @SerializedName("totalCapacity")
        private int totalCapacity;

        @SerializedName("availableCapacity")
        private int availableCapacity;

        @SerializedName("pricing")
        private Pricing pricing;

        public int getTotalCapacity() {
            return totalCapacity;
        }

        public int getAvailableCapacity() {
            return availableCapacity;
        }

        public Pricing getPricing() {
            return pricing;
        }
    }

    public static class Pricing implements java.io.Serializable {
        @SerializedName("id")
        private long id;

        @SerializedName("initialCharge")
        private double initialCharge;

        @SerializedName("initialDurationMinute")
        private int initialDurationMinute;

        @SerializedName("stepRate")
        private double stepRate;

        @SerializedName("stepMinute")
        private int stepMinute;

        @SerializedName("estimateTotalFee")
        private double estimateTotalFee;

        public long getId() {
            return id;
        }

        public double getInitialCharge() {
            return initialCharge;
        }

        public int getInitialDurationMinute() {
            return initialDurationMinute;
        }

        public double getStepRate() {
            return stepRate;
        }

        public int getStepMinute() {
            return stepMinute;
        }

        public double getEstimateTotalFee() {
            return estimateTotalFee;
        }
    }
}

