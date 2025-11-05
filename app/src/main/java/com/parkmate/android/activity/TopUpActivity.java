package com.parkmate.android.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.parkmate.android.R;
import com.parkmate.android.model.Payment;
import com.parkmate.android.model.PaymentStatus;
import com.parkmate.android.repository.PaymentRepository;

import java.text.NumberFormat;
import java.util.Locale;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class TopUpActivity extends AppCompatActivity {

    private static final String TAG = "TopUpActivity";
    private static final long MIN_AMOUNT = 1000;

    // Views - Common
    private ImageButton btnBack;
    private FrameLayout loadingOverlay;

    // Views - Input Section
    private LinearLayout llInputSection;
    private TextInputEditText etAmount;
    private TextView tvAmountError;
    private MaterialButton btnGenerateQR;
    private MaterialButton btnAmount10k, btnAmount50k, btnAmount100k;
    private MaterialButton btnAmount200k, btnAmount500k, btnAmount1M;

    // Views - QR Section
    private LinearLayout llQRSection;
    private ImageView ivQRCode;
    private TextView tvAccountNumber;
    private TextView tvAccountName;
    private TextView tvAmount;
    private TextView tvDescription;
    private TextView tvWarningMessage;
    private MaterialButton btnCancel;
    private MaterialButton btnConfirm;

    // Repository
    private PaymentRepository paymentRepository;
    private CompositeDisposable compositeDisposable;

    // Data
    private long currentOrderCode;
    private long selectedAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_top_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        paymentRepository = new PaymentRepository();
        compositeDisposable = new CompositeDisposable();

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        llInputSection = findViewById(R.id.llInputSection);
        etAmount = findViewById(R.id.etAmount);
        tvAmountError = findViewById(R.id.tvAmountError);
        btnGenerateQR = findViewById(R.id.btnGenerateQR);
        btnAmount10k = findViewById(R.id.btnAmount10k);
        btnAmount50k = findViewById(R.id.btnAmount50k);
        btnAmount100k = findViewById(R.id.btnAmount100k);
        btnAmount200k = findViewById(R.id.btnAmount200k);
        btnAmount500k = findViewById(R.id.btnAmount500k);
        btnAmount1M = findViewById(R.id.btnAmount1M);
        llQRSection = findViewById(R.id.llQRSection);
        ivQRCode = findViewById(R.id.ivQRCode);
        tvAccountNumber = findViewById(R.id.tvAccountNumber);
        tvAccountName = findViewById(R.id.tvAccountName);
        tvAmount = findViewById(R.id.tvAmount);
        tvDescription = findViewById(R.id.tvDescription);
        tvWarningMessage = findViewById(R.id.tvWarningMessage);
        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);
        showInputSection();
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateAmount();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        btnAmount10k.setOnClickListener(v -> setAmount(10000));
        btnAmount50k.setOnClickListener(v -> setAmount(50000));
        btnAmount100k.setOnClickListener(v -> setAmount(100000));
        btnAmount200k.setOnClickListener(v -> setAmount(200000));
        btnAmount500k.setOnClickListener(v -> setAmount(500000));
        btnAmount1M.setOnClickListener(v -> setAmount(1000000));
        btnGenerateQR.setOnClickListener(v -> {
            if (validateAmount()) generatePayment();
        });
        btnCancel.setOnClickListener(v -> cancelPayment());
        btnConfirm.setOnClickListener(v -> checkPaymentStatus());
    }

    private void setAmount(long amount) {
        etAmount.setText(String.valueOf(amount));
        selectedAmount = amount;
        validateAmount();
    }

    private boolean validateAmount() {
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        if (amountStr.isEmpty()) {
            tvAmountError.setVisibility(View.VISIBLE);
            tvAmountError.setText("Vui lòng nhập số tiền");
            btnGenerateQR.setEnabled(false);
            return false;
        }
        try {
            selectedAmount = Long.parseLong(amountStr);
            if (selectedAmount < MIN_AMOUNT) {
                tvAmountError.setVisibility(View.VISIBLE);
                tvAmountError.setText("Số tiền tối thiểu là " + formatCurrency(MIN_AMOUNT));
                btnGenerateQR.setEnabled(false);
                return false;
            }
            tvAmountError.setVisibility(View.GONE);
            btnGenerateQR.setEnabled(true);
            return true;
        } catch (NumberFormatException e) {
            tvAmountError.setVisibility(View.VISIBLE);
            tvAmountError.setText("Số tiền không hợp lệ");
            btnGenerateQR.setEnabled(false);
            return false;
        }
    }

    private void generatePayment() {
        showLoading();
        Disposable disposable = paymentRepository.createPayment(selectedAmount)
                .subscribe(
                        response -> {
                            hideLoading();
                            if (response.isSuccess() && response.getData() != null) {
                                displayPaymentInfo(response.getData());
                            } else {
                                String errorMsg = response.getError() != null ? response.getError() : "Không thể tạo mã thanh toán";
                                showError(errorMsg);
                            }
                        },
                        error -> {
                            hideLoading();
                            Log.e(TAG, "Lỗi khi tạo thanh toán: " + error.getMessage(), error);
                            showError("Không thể kết nối đến server. Vui lòng thử lại.");
                        }
                );
        compositeDisposable.add(disposable);
    }

    private void displayPaymentInfo(Payment paymentData) {
        currentOrderCode = paymentData.getOrderCode();
        if (paymentData.getQrCode() != null && !paymentData.getQrCode().isEmpty()) {
            Bitmap qrBitmap = decodeBase64ToBitmap(paymentData.getQrCode());
            if (qrBitmap != null) ivQRCode.setImageBitmap(qrBitmap);
        }
        tvAccountNumber.setText(paymentData.getAccountNumber());
        tvAccountName.setText(paymentData.getAccountName());
        tvAmount.setText(formatCurrency(paymentData.getAmount()));
        tvDescription.setText(paymentData.getDescription());
        String warning = "⚠️ LƯU Ý QUAN TRỌNG:\n\n" +
                "Nếu bạn chuyển khoản thủ công (không quét QR), vui lòng nhập CHÍNH XÁC:\n\n" +
                "• Số tiền: " + formatCurrency(paymentData.getAmount()) + "\n" +
                "• Nội dung: " + paymentData.getDescription() + "\n\n" +
                "Nếu sai thông tin, giao dịch sẽ KHÔNG THÀNH CÔNG!";
        tvWarningMessage.setText(warning);
        showQRSection();
        Log.d(TAG, "Hiển thị thông tin thanh toán: OrderCode=" + currentOrderCode);
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi decode base64 QR code: " + e.getMessage(), e);
            return null;
        }
    }

    private void cancelPayment() {
        showLoading();
        Disposable disposable = paymentRepository.cancelPayment(currentOrderCode)
                .subscribe(
                        response -> {
                            hideLoading();
                            if (response.isSuccess()) {
                                Toast.makeText(this, "Đã hủy giao dịch", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK); // Thông báo cho WalletActivity reload dữ liệu
                                finish();
                            } else {
                                String errorMsg = response.getError() != null ? response.getError() : "Không thể hủy giao dịch";
                                showError(errorMsg);
                            }
                        },
                        error -> {
                            hideLoading();
                            Log.e(TAG, "Lỗi khi hủy thanh toán: " + error.getMessage(), error);
                            showError("Không thể kết nối đến server. Vui lòng thử lại.");
                        }
                );
        compositeDisposable.add(disposable);
    }

    private void checkPaymentStatus() {
        showLoading();
        Disposable disposable = paymentRepository.checkPaymentStatus(currentOrderCode)
                .subscribe(
                        response -> {
                            hideLoading();
                            if (response.isSuccess() && response.getData() != null) {
                                PaymentStatus statusData = response.getData();
                                if ("COMPLETED".equals(statusData.getTransactionStatus())) {
                                    Toast.makeText(this, "Nạp tiền thành công: " + formatCurrency(statusData.getAmount()), Toast.LENGTH_LONG).show();
                                    setResult(RESULT_OK); // Reload WalletActivity
                                    finish();
                                } else if ("CANCELLED".equals(statusData.getTransactionStatus())) {
                                    showError("Giao dịch đã bị hủy");
                                    setResult(RESULT_OK); // Reload WalletActivity để cập nhật trạng thái
                                    finish();
                                } else if ("PENDING".equals(statusData.getTransactionStatus())) {
                                    Toast.makeText(this, "Giao dịch đang chờ xử lý. Vui lòng kiểm tra lại sau.", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK); // Reload WalletActivity để cập nhật trạng thái
                                    finish();
                                } else {
                                    Toast.makeText(this, "Trạng thái: " + statusData.getTransactionStatus(), Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK); // Reload WalletActivity để cập nhật trạng thái
                                    finish();
                                }
                            } else {
                                String errorMsg = response.getError() != null ? response.getError() : "Không thể kiểm tra trạng thái";
                                showError(errorMsg);
                            }
                        },
                        error -> {
                            hideLoading();
                            Log.e(TAG, "Lỗi khi kiểm tra trạng thái: " + error.getMessage(), error);
                            showError("Không thể kết nối đến server. Vui lòng thử lại.");
                        }
                );
        compositeDisposable.add(disposable);
    }

    private void showInputSection() {
        llInputSection.setVisibility(View.VISIBLE);
        llQRSection.setVisibility(View.GONE);
    }

    private void showQRSection() {
        llInputSection.setVisibility(View.GONE);
        llQRSection.setVisibility(View.VISIBLE);
    }

    private String formatCurrency(long amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showLoading() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
    }
}

