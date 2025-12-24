package com.parkmate.android.utils.validation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class để validate thông tin CCCD/CMND
 * Sử dụng trong VerifyCccdActivity
 */
public class CccdValidator {

    // CCCD pattern: 12 số, bắt đầu bằng 0
    private static final Pattern CCCD_PATTERN = Pattern.compile("^0[0-9]{11}$");
    // CMND pattern: 9 số
    private static final Pattern CMND_PATTERN = Pattern.compile("^[0-9]{9}$");

    // Vietnamese name pattern: chữ cái tiếng Việt và khoảng trắng
    private static final Pattern VIETNAMESE_NAME_PATTERN = Pattern.compile(
            "^[a-zA-ZÀ-ỹ\\s]+$");

    // Mã tỉnh hợp lệ (001-096)
    private static final int MIN_PROVINCE_CODE = 1;
    private static final int MAX_PROVINCE_CODE = 96;

    // Tuổi tối thiểu để làm CCCD
    private static final int MIN_AGE_FOR_CCCD = 14;

    // Date format
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    static {
        DATE_FORMAT.setLenient(false);
    }

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
     * Validate số CCCD/CMND
     */
    public static ValidationResult validateCccdNumber(String cccdNumber) {
        if (cccdNumber == null || cccdNumber.trim().isEmpty()) {
            return ValidationResult.error("Vui lòng nhập số CCCD/CMND");
        }

        String number = cccdNumber.trim().replaceAll("\\s", "");

        // Check if contains only digits
        if (!number.matches("^[0-9]+$")) {
            return ValidationResult.error("Số CCCD/CMND chỉ được chứa số");
        }

        // Check length
        if (number.length() == 9) {
            // CMND cũ - chỉ cần 9 số
            if (!CMND_PATTERN.matcher(number).matches()) {
                return ValidationResult.error("Số CMND phải có đúng 9 số");
            }
        } else if (number.length() == 12) {
            // CCCD mới - 12 số, bắt đầu bằng 0
            if (!CCCD_PATTERN.matcher(number).matches()) {
                return ValidationResult.error("Số CCCD phải có 12 số và bắt đầu bằng 0");
            }

            // Validate mã tỉnh (3 số đầu tiên)
            try {
                int provinceCode = Integer.parseInt(number.substring(0, 3));
                if (provinceCode < MIN_PROVINCE_CODE || provinceCode > MAX_PROVINCE_CODE) {
                    return ValidationResult.error("Mã tỉnh không hợp lệ (001-096)");
                }
            } catch (NumberFormatException e) {
                return ValidationResult.error("Số CCCD không hợp lệ");
            }
        } else {
            return ValidationResult.error("Số CCCD phải có 12 số hoặc CMND có 9 số");
        }

        return ValidationResult.success();
    }

    /**
     * Validate họ và tên
     */
    public static ValidationResult validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return ValidationResult.error("Vui lòng nhập họ và tên");
        }

        String name = fullName.trim();

        if (name.length() < 5) {
            return ValidationResult.error("Họ tên quá ngắn (tối thiểu 5 ký tự)");
        }

        if (name.length() > 100) {
            return ValidationResult.error("Họ tên quá dài (tối đa 100 ký tự)");
        }

        if (!VIETNAMESE_NAME_PATTERN.matcher(name).matches()) {
            return ValidationResult.error("Họ tên chỉ được chứa chữ cái");
        }

        // Check at least 2 words (họ + tên)
        String[] words = name.split("\\s+");
        if (words.length < 2) {
            return ValidationResult.error("Vui lòng nhập đầy đủ họ và tên");
        }

        return ValidationResult.success();
    }

    /**
     * Validate ngày sinh (đơn giản)
     */
    public static ValidationResult validateDateOfBirth(String dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            return ValidationResult.error("Vui lòng chọn ngày sinh");
        }

        try {
            Date dob = DATE_FORMAT.parse(dateOfBirth.trim());
            Date today = new Date();

            // Cannot be in the future
            if (dob.after(today)) {
                return ValidationResult.error("Ngày sinh không thể ở tương lai");
            }

            // Cannot be more than 150 years old
            Calendar maxAgeCalendar = Calendar.getInstance();
            maxAgeCalendar.add(Calendar.YEAR, -150);

            if (dob.before(maxAgeCalendar.getTime())) {
                return ValidationResult.error("Ngày sinh không hợp lệ");
            }

        } catch (ParseException e) {
            return ValidationResult.error("Ngày sinh không đúng định dạng (dd/MM/yyyy)");
        }

        return ValidationResult.success();
    }

    /**
     * Validate ngày cấp (đơn giản)
     */
    public static ValidationResult validateIssueDate(String issueDate, String dateOfBirth) {
        if (issueDate == null || issueDate.trim().isEmpty()) {
            return ValidationResult.error("Vui lòng chọn ngày cấp");
        }

        try {
            Date issue = DATE_FORMAT.parse(issueDate.trim());
            Date today = new Date();

            // Cannot be in the future
            if (issue.after(today)) {
                return ValidationResult.error("Ngày cấp không thể ở tương lai");
            }

        } catch (ParseException e) {
            return ValidationResult.error("Ngày cấp không đúng định dạng (dd/MM/yyyy)");
        }

        return ValidationResult.success();
    }

    /**
     * Validate ngày hết hạn (đơn giản)
     */
    public static ValidationResult validateExpiryDate(String expiryDate, String issueDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            return ValidationResult.error("Vui lòng chọn ngày hết hạn");
        }

        try {
            Date expiry = DATE_FORMAT.parse(expiryDate.trim());

            // Check format is valid
            if (expiry == null) {
                return ValidationResult.error("Ngày hết hạn không hợp lệ");
            }

        } catch (ParseException e) {
            return ValidationResult.error("Ngày hết hạn không đúng định dạng (dd/MM/yyyy)");
        }

        return ValidationResult.success();
    }

    /**
     * Validate địa chỉ thường trú
     */
    public static ValidationResult validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return ValidationResult.error("Vui lòng nhập địa chỉ thường trú");
        }

        String addr = address.trim();

        if (addr.length() < 10) {
            return ValidationResult.error("Địa chỉ quá ngắn (tối thiểu 10 ký tự)");
        }

        if (addr.length() > 200) {
            return ValidationResult.error("Địa chỉ quá dài (tối đa 200 ký tự)");
        }

        return ValidationResult.success();
    }

    /**
     * Validate nơi cấp
     */
    public static ValidationResult validateIssuePlace(String issuePlace) {
        if (issuePlace == null || issuePlace.trim().isEmpty()) {
            return ValidationResult.error("Vui lòng nhập nơi cấp");
        }

        String place = issuePlace.trim();

        if (place.length() < 5) {
            return ValidationResult.error("Nơi cấp quá ngắn");
        }

        if (place.length() > 100) {
            return ValidationResult.error("Nơi cấp quá dài");
        }

        return ValidationResult.success();
    }

    /**
     * Validate giới tính
     */
    public static ValidationResult validateGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return ValidationResult.error("Vui lòng chọn giới tính");
        }
        return ValidationResult.success();
    }

    /**
     * Validate quốc tịch
     */
    public static ValidationResult validateNationality(String nationality) {
        if (nationality == null || nationality.trim().isEmpty()) {
            return ValidationResult.error("Vui lòng chọn quốc tịch");
        }
        return ValidationResult.success();
    }
}
