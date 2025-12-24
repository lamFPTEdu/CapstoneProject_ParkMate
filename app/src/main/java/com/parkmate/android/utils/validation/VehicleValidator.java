package com.parkmate.android.utils.validation;

import java.util.regex.Pattern;

/**
 * Utility class để validate thông tin xe
 * Sử dụng trong AddVehicleActivity và EditVehicleActivity
 */
public class VehicleValidator {

    // Vietnam license plate regex patterns
    // Format: 51H-12345, 30A12345, 59C1-23456, etc.
    private static final Pattern LICENSE_PLATE_PATTERN = Pattern.compile(
            "^[0-9]{2}[A-Z][0-9]?[-]?[0-9]{4,5}$");

    // Brand: chữ cái, số, khoảng trắng, dấu gạch ngang
    private static final Pattern BRAND_PATTERN = Pattern.compile(
            "^[a-zA-ZÀ-ỹ0-9\\s\\-]+$");

    // Model: chữ cái, số, khoảng trắng, dấu gạch ngang, dấu chấm
    private static final Pattern MODEL_PATTERN = Pattern.compile(
            "^[a-zA-ZÀ-ỹ0-9\\s\\-.]+$");

    // Color: chỉ chữ cái và khoảng trắng
    private static final Pattern COLOR_PATTERN = Pattern.compile(
            "^[a-zA-ZÀ-ỹ\\s]+$");

    // Length limits
    private static final int LICENSE_PLATE_MIN_LENGTH = 7;
    private static final int LICENSE_PLATE_MAX_LENGTH = 12;
    private static final int BRAND_MIN_LENGTH = 2;
    private static final int BRAND_MAX_LENGTH = 50;
    private static final int MODEL_MIN_LENGTH = 1;
    private static final int MODEL_MAX_LENGTH = 50;
    private static final int COLOR_MIN_LENGTH = 2;
    private static final int COLOR_MAX_LENGTH = 30;

    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
    }

    /**
     * Validate biển số xe Việt Nam
     */
    public static ValidationResult validateLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            return ValidationResult.error("Vui lòng nhập biển số xe");
        }

        String plate = licensePlate.trim().toUpperCase().replace(" ", "");

        if (plate.length() < LICENSE_PLATE_MIN_LENGTH) {
            return ValidationResult.error("Biển số xe quá ngắn (tối thiểu 7 ký tự)");
        }

        if (plate.length() > LICENSE_PLATE_MAX_LENGTH) {
            return ValidationResult.error("Biển số xe quá dài (tối đa 12 ký tự)");
        }

        if (!LICENSE_PLATE_PATTERN.matcher(plate).matches()) {
            return ValidationResult.error("Biển số xe không đúng định dạng (VD: 51H-12345)");
        }

        return ValidationResult.success();
    }

    /**
     * Validate hãng xe
     */
    public static ValidationResult validateBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            return ValidationResult.error("Vui lòng nhập hãng xe");
        }

        String trimmedBrand = brand.trim();

        if (trimmedBrand.length() < BRAND_MIN_LENGTH) {
            return ValidationResult.error("Hãng xe quá ngắn (tối thiểu 2 ký tự)");
        }

        if (trimmedBrand.length() > BRAND_MAX_LENGTH) {
            return ValidationResult.error("Hãng xe quá dài (tối đa 50 ký tự)");
        }

        if (!BRAND_PATTERN.matcher(trimmedBrand).matches()) {
            return ValidationResult.error("Hãng xe chỉ được chứa chữ cái, số và dấu gạch ngang");
        }

        return ValidationResult.success();
    }

    /**
     * Validate model xe
     */
    public static ValidationResult validateModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            return ValidationResult.error("Vui lòng nhập model xe");
        }

        String trimmedModel = model.trim();

        if (trimmedModel.length() < MODEL_MIN_LENGTH) {
            return ValidationResult.error("Model xe quá ngắn");
        }

        if (trimmedModel.length() > MODEL_MAX_LENGTH) {
            return ValidationResult.error("Model xe quá dài (tối đa 50 ký tự)");
        }

        if (!MODEL_PATTERN.matcher(trimmedModel).matches()) {
            return ValidationResult.error("Model xe chứa ký tự không hợp lệ");
        }

        return ValidationResult.success();
    }

    /**
     * Validate màu xe
     */
    public static ValidationResult validateColor(String color) {
        if (color == null || color.trim().isEmpty()) {
            return ValidationResult.error("Vui lòng nhập màu xe");
        }

        String trimmedColor = color.trim();

        if (trimmedColor.length() < COLOR_MIN_LENGTH) {
            return ValidationResult.error("Màu xe quá ngắn (tối thiểu 2 ký tự)");
        }

        if (trimmedColor.length() > COLOR_MAX_LENGTH) {
            return ValidationResult.error("Màu xe quá dài (tối đa 30 ký tự)");
        }

        if (!COLOR_PATTERN.matcher(trimmedColor).matches()) {
            return ValidationResult.error("Màu xe chỉ được chứa chữ cái");
        }

        return ValidationResult.success();
    }

    /**
     * Validate loại xe
     */
    public static ValidationResult validateVehicleType(String vehicleType) {
        if (vehicleType == null || vehicleType.trim().isEmpty()) {
            return ValidationResult.error("Vui lòng chọn loại xe");
        }
        return ValidationResult.success();
    }

    /**
     * Format biển số xe - tự động uppercase và thêm dấu gạch ngang nếu cần
     */
    public static String formatLicensePlate(String licensePlate) {
        if (licensePlate == null)
            return "";

        String plate = licensePlate.trim().toUpperCase().replace(" ", "");

        // Tự động thêm dấu gạch ngang nếu chưa có và đủ điều kiện
        // Format: 51H12345 -> 51H-12345
        if (plate.length() >= 7 && !plate.contains("-")) {
            // Tìm vị trí sau chữ cái (index 2 hoặc 3)
            int dashPosition = -1;
            for (int i = 2; i < Math.min(4, plate.length()); i++) {
                if (Character.isLetter(plate.charAt(i - 1)) && Character.isDigit(plate.charAt(i))) {
                    dashPosition = i;
                    break;
                }
            }
            // Thêm vị trí đặc biệt cho biển số dạng 51H1-12345
            if (dashPosition == -1 && plate.length() >= 8) {
                for (int i = 3; i < Math.min(5, plate.length()); i++) {
                    if (Character.isDigit(plate.charAt(i - 1)) && Character.isDigit(plate.charAt(i))) {
                        dashPosition = i;
                        break;
                    }
                }
            }

            if (dashPosition > 0 && dashPosition < plate.length()) {
                plate = plate.substring(0, dashPosition) + "-" + plate.substring(dashPosition);
            }
        }

        return plate;
    }
}
