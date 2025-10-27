package com.parkmate.android.model.request;

import com.google.gson.annotations.SerializedName;

public class AddVehicleRequest {
    @SerializedName("licensePlate")
    private String licensePlate;

    @SerializedName("vehicleBrand")
    private String vehicleBrand;

    @SerializedName("vehicleModel")
    private String vehicleModel;

    @SerializedName("vehicleColor")
    private String vehicleColor;

    @SerializedName("vehicleType")
    private String vehicleType;

    @SerializedName("licenseImage")
    private String licenseImage;

    @SerializedName("electric")
    private boolean electric;

    public AddVehicleRequest() {
    }

    public AddVehicleRequest(String licensePlate, String vehicleBrand, String vehicleModel,
                            String vehicleColor, String vehicleType, String licenseImage, boolean electric) {
        this.licensePlate = licensePlate;
        this.vehicleBrand = vehicleBrand;
        this.vehicleModel = vehicleModel;
        this.vehicleColor = vehicleColor;
        this.vehicleType = vehicleType;
        this.licenseImage = licenseImage;
        this.electric = electric;
    }

    // Getters and setters
    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getVehicleBrand() {
        return vehicleBrand;
    }

    public void setVehicleBrand(String vehicleBrand) {
        this.vehicleBrand = vehicleBrand;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehicleColor() {
        return vehicleColor;
    }

    public void setVehicleColor(String vehicleColor) {
        this.vehicleColor = vehicleColor;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getLicenseImage() {
        return licenseImage;
    }

    public void setLicenseImage(String licenseImage) {
        this.licenseImage = licenseImage;
    }

    public boolean isElectric() {
        return electric;
    }

    public void setElectric(boolean electric) {
        this.electric = electric;
    }
}

