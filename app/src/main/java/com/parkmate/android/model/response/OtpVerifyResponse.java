package com.parkmate.android.model.response;

import com.google.gson.annotations.SerializedName;

public class OtpVerifyResponse {
    @SerializedName("success")
    private Boolean success;
    @SerializedName("message")
    private String message;
    // Có thể backend trả thêm token tạm, userId, v.v. -> TODO cập nhật khi có swagger đầy đủ

    public Boolean getSuccess() { return success; }
    public String getMessage() { return message; }
}

