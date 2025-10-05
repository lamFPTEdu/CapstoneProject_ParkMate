package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.parkmate.android.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private ImageButton btnBack;
    private String userEmail;

    // Email Screen Views
    private TextInputEditText etEmail;
    private Button btnSend;

    // Check Email Screen Views
    private TextView tvUserEmail;
    private Button btnContinue;

    // OTP Screen Views
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private TextView tvResendCode;
    private Button btnVerify;

    // New Password Screen Views
    private TextInputEditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;

    // Success Screen View
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();

        // Handle back press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        });
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        btnBack = findViewById(R.id.btnBack);

        // Initialize Email Screen Views
        View emailView = viewFlipper.getChildAt(0);
        etEmail = emailView.findViewById(R.id.etEmail);
        btnSend = emailView.findViewById(R.id.btnSend);

        // Initialize Check Email Screen Views
        View checkEmailView = viewFlipper.getChildAt(1);
        tvUserEmail = checkEmailView.findViewById(R.id.tvUserEmail);
        btnContinue = checkEmailView.findViewById(R.id.btnContinue);

        // Initialize OTP Screen Views
        View otpView = viewFlipper.getChildAt(2);
        etOtp1 = otpView.findViewById(R.id.etOtp1);
        etOtp2 = otpView.findViewById(R.id.etOtp2);
        etOtp3 = otpView.findViewById(R.id.etOtp3);
        etOtp4 = otpView.findViewById(R.id.etOtp4);
        etOtp5 = otpView.findViewById(R.id.etOtp5);
        etOtp6 = otpView.findViewById(R.id.etOtp6);
        tvResendCode = otpView.findViewById(R.id.tvResendCode);
        btnVerify = otpView.findViewById(R.id.btnVerify);

        // Initialize New Password Screen Views
        View newPasswordView = viewFlipper.getChildAt(3);
        etNewPassword = newPasswordView.findViewById(R.id.etNewPassword);
        etConfirmPassword = newPasswordView.findViewById(R.id.etConfirmPassword);
        btnResetPassword = newPasswordView.findViewById(R.id.btnResetPassword);

        // Initialize Success Screen Views
        View successView = viewFlipper.getChildAt(4);
        btnLogin = successView.findViewById(R.id.btnLogin);
    }

    private void setupListeners() {
        // Back button listener
        btnBack.setOnClickListener(v -> handleBackPress());

        // Email Screen Listeners
        btnSend.setOnClickListener(v -> {
            if (validateEmail()) {
                userEmail = etEmail.getText().toString().trim();
                // Here would be API call to send OTP to email
                sendOtp(userEmail);
            }
        });

        // Check Email Screen Listeners
        btnContinue.setOnClickListener(v -> {
            viewFlipper.setDisplayedChild(2); // Move to OTP screen
        });

        // OTP Screen Listeners
        setupOtpInputs();
        tvResendCode.setOnClickListener(v -> {
            // Here would be API call to resend OTP
            Toast.makeText(this, "OTP resent to your email", Toast.LENGTH_SHORT).show();
        });
        btnVerify.setOnClickListener(v -> {
            if (validateOtp()) {
                // Here would be API call to verify OTP
                verifyOtp();
            }
        });

        // New Password Screen Listeners
        btnResetPassword.setOnClickListener(v -> {
            if (validatePasswords()) {
                // Here would be API call to reset password
                resetPassword();
            }
        });

        // Success Screen Listeners
        btnLogin.setOnClickListener(v -> {
            // Navigate to login screen
            finish(); // This would typically go to your login activity
        });
    }

    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Email cannot be empty");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            return false;
        }
        return true;
    }

    private void sendOtp(String email) {
        // Simulate API call to send OTP
        // In a real app, you would make an API call here

        tvUserEmail.setText(email);
        viewFlipper.setDisplayedChild(1); // Move to Check Email screen
    }

    private void setupOtpInputs() {
        EditText[] otpFields = {etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6};

        for (int i = 0; i < otpFields.length; i++) {
            final int currentIndex = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && currentIndex < otpFields.length - 1) {
                        otpFields[currentIndex + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private boolean validateOtp() {
        // Get OTP from all fields
        String otp = etOtp1.getText().toString() +
                etOtp2.getText().toString() +
                etOtp3.getText().toString() +
                etOtp4.getText().toString() +
                etOtp5.getText().toString() +
                etOtp6.getText().toString();

        if (otp.length() != 6) {
            Toast.makeText(this, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void verifyOtp() {
        // Simulate API call to verify OTP
        // In a real app, you would make an API call here

        viewFlipper.setDisplayedChild(3); // Move to New Password screen
    }

    private boolean validatePasswords() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            etNewPassword.setError("Password cannot be empty");
            return false;
        } else if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            return false;
        } else if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Confirm password cannot be empty");
            return false;
        } else if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    private void resetPassword() {
        // Simulate API call to reset password
        // In a real app, you would make an API call here

        viewFlipper.setDisplayedChild(4); // Move to Success screen
    }

    private void handleBackPress() {
        int currentScreen = viewFlipper.getDisplayedChild();
        if (currentScreen > 0) {
            viewFlipper.setDisplayedChild(currentScreen - 1);
        } else {
            finish();
        }
    }
}