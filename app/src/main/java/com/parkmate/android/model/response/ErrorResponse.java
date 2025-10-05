package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ErrorResponse {
    @SerializedName("success")
    private Boolean success;
    @SerializedName("message")
    private String message;
    @SerializedName("error")
    private ApiError error;

    public Boolean getSuccess() { return success; }
    public String getMessage() { return message; }
    public ApiError getError() { return error; }

    public static class ApiError {
        @SerializedName("code")
        private String code;
        @SerializedName("message")
        private String message;
        @SerializedName("fieldErrors")
        private List<FieldError> fieldErrors;
        @SerializedName("details")
        private Details details;

        public String getCode() { return code; }
        public String getMessage() { return message; }
        public List<FieldError> getFieldErrors() { return fieldErrors; }
        public Details getDetails() { return details; }
    }

    public static class FieldError {
        @SerializedName("field")
        private String field;
        @SerializedName("rejectedValue")
        private String rejectedValue;
        @SerializedName("message")
        private String message;

        public String getField() { return field; }
        public String getRejectedValue() { return rejectedValue; }
        public String getMessage() { return message; }
    }

    public static class Details {
        @SerializedName("errorCode")
        private Integer errorCode;

        public Integer getErrorCode() { return errorCode; }
    }
}
