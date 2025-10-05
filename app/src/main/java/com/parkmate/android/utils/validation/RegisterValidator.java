package com.parkmate.android.utils.validation;

import android.util.Patterns;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class RegisterValidator {
    private static final Pattern USERNAME_ALLOWED = Pattern.compile("^[A-Za-z0-9_-]+$");

    private RegisterValidator() {}

    public static Map<String, String> validateBasic(String fullName, String email, String password, String confirmPassword, String generatedUsername) {
        Map<String, String> errors = new HashMap<>();
        if (isEmpty(fullName)) errors.put("fullName", "Vui lòng nhập họ tên");
        if (isEmpty(email)) {
            errors.put("email", "Vui lòng nhập email");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errors.put("email", "Email không hợp lệ");
        }
        if (isEmpty(password)) {
            errors.put("password", "Vui lòng nhập mật khẩu");
        } else if (password.length() < 8) {
            errors.put("password", "Mật khẩu phải >= 8 ký tự");
        }
        if (isEmpty(confirmPassword)) {
            errors.put("confirmPassword", "Vui lòng xác nhận mật khẩu");
        } else if (!confirmPassword.equals(password)) {
            errors.put("confirmPassword", "Mật khẩu xác nhận không khớp");
        }
        if (!isEmpty(generatedUsername) && !USERNAME_ALLOWED.matcher(generatedUsername).matches()) {
            errors.put("username", "Username sinh ra không hợp lệ");
        }
        return errors;
    }

    public static Map<String, String> validateCccd(String cccdNumber, String idFullName, String dateOfBirth,
                                                   String issueDate, String issuePlace, String gender,
                                                   String nationality, String permanentAddress, boolean commitmentChecked) {
        Map<String, String> errors = new HashMap<>();
        if (isEmpty(cccdNumber)) {
            errors.put("cccdNumber", "Vui lòng nhập số CCCD/CMND");
        } else if (cccdNumber.length() != 9 && cccdNumber.length() != 12) {
            errors.put("cccdNumber", "Số CCCD phải 12 số hoặc CMND 9 số");
        }
        if (isEmpty(idFullName)) errors.put("idFullName", "Vui lòng nhập họ tên trên CCCD");
        if (isEmpty(dateOfBirth)) errors.put("dateOfBirth", "Vui lòng chọn ngày sinh");
        if (isEmpty(gender)) errors.put("gender", "Chọn giới tính");
        if (isEmpty(nationality)) errors.put("nationality", "Nhập quốc tịch");
        if (isEmpty(permanentAddress)) errors.put("permanentAddress", "Nhập địa chỉ thường trú");
        if (isEmpty(issueDate)) errors.put("issueDate", "Chọn ngày cấp");
        if (isEmpty(issuePlace)) errors.put("issuePlace", "Nhập nơi cấp");
        if (!commitmentChecked) errors.put("commitment", "Cần xác nhận cam kết");
        return errors;
    }

    private static boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }
}

