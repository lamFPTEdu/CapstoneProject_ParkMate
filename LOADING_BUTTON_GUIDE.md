# Hướng dẫn sử dụng Loading Button

## Tổng quan
Thay vì hiển thị dialog loading riêng biệt, bạn có thể hiển thị vòng tròn loading quay ngay bên trong button khi người dùng click.

## Cách sử dụng

### Phương án 1: LoadingButton (Helper Class) - ĐỀ XUẤT

Dùng với Button/MaterialButton thông thường đã có sẵn.

#### Trong Activity/Fragment:

```java
import com.parkmate.android.utils.LoadingButton;

public class LoginActivity extends AppCompatActivity {
    private MaterialButton btnLogin;
    private LoadingButton loadingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);
        
        // Khởi tạo button và LoadingButton
        btnLogin = findViewById(R.id.btnLogin);
        loadingButton = new LoadingButton(btnLogin);
        
        // Setup click listener
        btnLogin.setOnClickListener(v -> {
            // Validate input
            if (validateInput()) {
                performLogin();
            }
        });
    }
    
    private void performLogin() {
        // Hiển thị loading trong button
        loadingButton.showLoading("Đang đăng nhập...");
        
        // Gọi API
        authRepository.login(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    loadingButton.hideLoading(); // Ẩn loading
                    handleSuccess(response);
                },
                error -> {
                    loadingButton.hideLoading(); // Ẩn loading
                    handleError(error);
                }
            );
    }
}
```

#### Các phương thức:

- `showLoading()` - Hiển thị loading với text mặc định "Đang xử lý..."
- `showLoading(String text)` - Hiển thị loading với text tùy chỉnh
- `hideLoading()` - Ẩn loading và khôi phục button về trạng thái ban đầu
- `isLoading()` - Kiểm tra xem có đang loading không
- `setText(String text)` - Thay đổi text của button

### Phương án 2: LoadingButtonView (Custom View)

Custom Button với loading tích hợp sẵn.

#### Trong XML:

```xml
<com.parkmate.android.utils.LoadingButtonView
    android:id="@+id/btnSubmit"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Đăng nhập"
    android:textColor="@color/white"
    android:background="@drawable/button_primary" />
```

#### Trong Activity/Fragment:

```java
import com.parkmate.android.utils.LoadingButtonView;

public class RegisterActivity extends AppCompatActivity {
    private LoadingButtonView btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register);
        
        btnSubmit = findViewById(R.id.btnSubmit);
        
        btnSubmit.setOnClickListener(v -> {
            if (validateInput()) {
                performRegister();
            }
        });
    }
    
    private void performRegister() {
        btnSubmit.showLoading(); // Hiển thị loading
        
        // Gọi API
        authRepository.register(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    btnSubmit.hideLoading(); // Ẩn loading
                    handleSuccess(response);
                },
                error -> {
                    btnSubmit.hideLoading(); // Ẩn loading
                    handleError(error);
                }
            );
    }
}
```

## Ví dụ thực tế đã áp dụng

### 1. LoginActivity
Đã cập nhật `LoginActivity` để sử dụng `LoadingButton` thay vì `LoadingDialogFragment`:

**Trước đây (dùng Dialog):**
```java
private LoadingDialogFragment loadingDialog;

private void performLogin() {
    showLoading("Đang đăng nhập...");
    // ... API call
}

private void showLoading(String message) {
    if (loadingDialog == null) {
        loadingDialog = LoadingDialogFragment.newInstance(message);
        loadingDialog.show(getSupportFragmentManager(), "login_loading");
    }
}

private void hideLoading() {
    if (loadingDialog != null) {
        loadingDialog.dismissAllowingStateLoss();
    }
}
```

**Bây giờ (dùng Loading Button):**
```java
private LoadingButton loadingButton;

private void initViews() {
    btnLogin = findViewById(R.id.btnLogin);
    loadingButton = new LoadingButton(btnLogin);
}

private void performLogin() {
    loadingButton.showLoading("Đang đăng nhập...");
    // ... API call với hideLoading() trong callback
}
```

### 2. RegisterActivity - Flow đăng ký hoàn chỉnh
Đã cập nhật toàn bộ flow đăng ký với 2 loading buttons:

**Button "Tiếp tục" (gửi đăng ký và nhận OTP):**
```java
private LoadingButton loadingButtonContinue;

private void setupCheckEmailScreen() {
    Button btnEmailContinue = checkEmailView.findViewById(R.id.btnContinue);
    loadingButtonContinue = new LoadingButton(btnEmailContinue);
    
    btnEmailContinue.setOnClickListener(v -> {
        loadingButtonContinue.showLoading("Đang đăng ký...");
        performRegisterForOtp();
    });
}

private void performRegisterForOtp() {
    authRepository.register(request)
        .subscribe(
            resp -> {
                loadingButtonContinue.hideLoading();
                // Chuyển sang màn OTP
            },
            err -> {
                loadingButtonContinue.hideLoading();
                // Xử lý lỗi
            }
        );
}
```

**Button "Xác thực OTP":**
```java
private LoadingButton loadingButtonVerify;

private void setupOtpScreen() {
    btnVerify = otpView.findViewById(R.id.btnVerify);
    loadingButtonVerify = new LoadingButton(btnVerify);
    
    btnVerify.setOnClickListener(v -> {
        if (validateOTP()) {
            String otp = collectOtp();
            verifyOtpOnly(otp);
        }
    });
}

private void verifyOtpOnly(String otp) {
    loadingButtonVerify.showLoading("Đang xác thực...");
    authRepository.verifyOtp(userEmail, otp)
        .subscribe(
            this::onOtpVerifiedSuccess,
            this::onOtpVerifyFailedFinal
        );
}

private void onOtpVerifiedSuccess(OtpVerifyResponse resp) {
    loadingButtonVerify.hideLoading();
    // Xử lý thành công
}

private void onOtpVerifyFailedFinal(Throwable throwable) {
    loadingButtonVerify.hideLoading();
    // Xử lý lỗi
}
```

## Ưu điểm

✅ **UX tốt hơn**: Người dùng thấy loading ngay tại button họ vừa click
✅ **Đơn giản hơn**: Không cần quản lý dialog lifecycle
✅ **Nhẹ hơn**: Không tạo thêm dialog overlay
✅ **Trực quan hơn**: Rõ ràng action nào đang được xử lý
✅ **Tránh double-click**: Button tự động disable khi loading

## Các màn hình đã được cập nhật

1. ✅ **LoginActivity** - Loading khi đăng nhập
2. ✅ **RegisterActivity** - Loading khi:
   - Gửi yêu cầu đăng ký và nhận OTP
   - Xác thực mã OTP

## Lưu ý

- Button sẽ tự động bị disable khi `showLoading()` được gọi
- Text gốc của button sẽ được lưu lại và khôi phục khi `hideLoading()`
- Nhớ gọi `hideLoading()` trong cả success và error callback
- Nếu muốn thay đổi text button, dùng `setText()` thay vì `button.setText()`
- Mỗi button cần một instance `LoadingButton` riêng

## Khi nào nên dùng Loading Button vs Loading Dialog?

**Dùng Loading Button khi:**
- Thao tác nhanh (< 3 giây): đăng nhập, đăng ký, gửi form
- Người dùng cần biết button nào đang xử lý
- Không muốn chặn toàn bộ UI

**Dùng Loading Dialog khi:**
- Thao tác lâu (> 3 giây): upload file lớn, xử lý dữ liệu phức tạp
- Cần chặn toàn bộ UI để tránh thao tác khác
- Cần hiển thị progress percentage hoặc multiple steps
