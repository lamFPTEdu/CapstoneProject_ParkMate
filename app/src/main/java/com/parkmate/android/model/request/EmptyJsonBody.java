package com.parkmate.android.model.request;

/**
 * Dùng để gửi body rỗng dạng {} cho các method (PUT) yêu cầu body không null.
 */
public class EmptyJsonBody {
    // Không field -> Gson sẽ serialize thành {}
    public static final EmptyJsonBody INSTANCE = new EmptyJsonBody();
    private EmptyJsonBody() {}
}

