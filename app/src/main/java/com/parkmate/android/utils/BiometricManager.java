package com.parkmate.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Quản lý xác thực sinh trắc học (vân tay, FaceID)
 * Sử dụng Android Keystore để mã hóa password an toàn
 */
public class BiometricManager {

    private static final String TAG = "BiometricManager";
    private static final String KEY_NAME = "parkmate_biometric_key";
    private static final String PREFS_NAME = "biometric_prefs";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email"; // Đổi từ username sang email để đăng nhập đúng
    private static final String KEY_ENCRYPTED_PASSWORD = "encrypted_password";
    private static final String KEY_IV = "iv";

    private static BiometricManager instance;
    private final Context context;

    private BiometricManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static BiometricManager getInstance(Context context) {
        if (instance == null) {
            instance = new BiometricManager(context);
        }
        return instance;
    }

    /**
     * Kiểm tra xem thiết bị có hỗ trợ biometric không (vân tay HOẶC khuôn mặt)
     */
    public boolean isBiometricAvailable() {
        androidx.biometric.BiometricManager biometricManager =
            androidx.biometric.BiometricManager.from(context);

        // Thử BIOMETRIC_STRONG trước (vân tay)
        int canAuthenticateStrong = biometricManager.canAuthenticate(
            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG);

        if (canAuthenticateStrong == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
            Log.d(TAG, "Device has STRONG biometric (fingerprint)");
            return true;
        }

        // Nếu không có STRONG, thử BIOMETRIC_WEAK (khuôn mặt hoặc vân tay yếu)
        int canAuthenticateWeak = biometricManager.canAuthenticate(
            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK);

        if (canAuthenticateWeak == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
            Log.d(TAG, "Device has WEAK biometric (face unlock or weak fingerprint)");
            return true;
        }

        // Log lý do không khả dụng
        String reason = getAuthenticateErrorReason(canAuthenticateStrong);
        Log.w(TAG, "Biometric not available: " + reason);

        return false;
    }

    /**
     * Lấy lý do lỗi khi authenticate không khả dụng
     */
    private String getAuthenticateErrorReason(int errorCode) {
        switch (errorCode) {
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return "Device không có biometric hardware";
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return "Biometric hardware tạm thời không khả dụng";
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return "Chưa đăng ký vân tay hoặc khuôn mặt trong Settings";
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                return "Cần update bảo mật";
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                return "API không hỗ trợ";
            case androidx.biometric.BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                return "Trạng thái không xác định";
            default:
                return "Error code: " + errorCode;
        }
    }

    /**
     * Kiểm tra xem user hiện tại có bật biometric không
     */
    public boolean isBiometricEnabled() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    /**
     * Kiểm tra xem biometric có được bật cho user cụ thể không
     */
    public boolean isBiometricEnabledForUser(String userId) {
        if (!isBiometricEnabled()) {
            return false;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUserId = prefs.getString(KEY_USER_ID, null);

        // Nếu userId khác với user đã lưu -> disable biometric
        if (savedUserId == null || !savedUserId.equals(userId)) {
            disableBiometric();
            return false;
        }

        return true;
    }

    /**
     * Lấy userId đã được lưu
     */
    public String getSavedUserId() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Bật biometric và lưu encrypted credentials
     * @param userId ID của user
     * @param email Email dùng để đăng nhập
     * @param password Password dùng để đăng nhập
     */
    public boolean enableBiometric(String userId, String email, String password) {
        try {
            // Validate input
            if (email == null || email.isEmpty()) {
                Log.e(TAG, "Cannot enable biometric: email is null or empty");
                return false;
            }

            if (password == null || password.isEmpty()) {
                Log.e(TAG, "Cannot enable biometric: password is null or empty");
                return false;
            }

            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "Cannot enable biometric: userId is null or empty");
                return false;
            }

            Log.d(TAG, "Enabling biometric for userId: " + userId + ", email: " + email);

            // Generate hoặc lấy key từ keystore
            SecretKey secretKey = getOrCreateKey();

            // Mã hóa password
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();
            byte[] encryptedPassword = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));

            // Lưu encrypted data
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_EMAIL, email) // Lưu email thay vì username
                .putBoolean(KEY_BIOMETRIC_ENABLED, true)
                .putString(KEY_ENCRYPTED_PASSWORD, Base64.encodeToString(encryptedPassword, Base64.DEFAULT))
                .putString(KEY_IV, Base64.encodeToString(iv, Base64.DEFAULT))
                .apply();

            Log.d(TAG, "Biometric enabled successfully for user: " + userId + ", email: " + email);

            // Verify data was saved
            String savedEmail = prefs.getString(KEY_EMAIL, null);
            Log.d(TAG, "Verified saved email: " + savedEmail);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error enabling biometric", e);
            return false;
        }
    }

    /**
     * Tắt biometric và xóa stored credentials
     */
    public void disableBiometric() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        Log.d(TAG, "Biometric disabled and data cleared");
    }

    /**
     * Xác thực và lấy decrypted password
     * Hỗ trợ cả vân tay (STRONG) và khuôn mặt (WEAK)
     */
    public void authenticate(FragmentActivity activity, BiometricCallback callback) {
        try {
            SecretKey secretKey = getOrCreateKey();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            // Lấy IV để giải mã
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String ivString = prefs.getString(KEY_IV, null);
            if (ivString == null) {
                callback.onError("Không tìm thấy dữ liệu sinh trắc học");
                return;
            }

            byte[] iv = Base64.decode(ivString, Base64.DEFAULT);
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            // Prompt hỗ trợ cả STRONG (vân tay) và WEAK (khuôn mặt)
            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Xác thực sinh trắc học")
                .setSubtitle("Sử dụng vân tay hoặc khuôn mặt để đăng nhập")
                .setDescription("ParkMate cần xác thực để đăng nhập an toàn")
                .setAllowedAuthenticators(
                    androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG |
                    androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .setNegativeButtonText("Hủy")
                .build();

            BiometricPrompt biometricPrompt = new BiometricPrompt(activity,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        try {
                            // Kiểm tra xem có CryptoObject không (STRONG biometric)
                            BiometricPrompt.CryptoObject cryptoObject = result.getCryptoObject();

                            if (cryptoObject != null) {
                                // STRONG biometric (vân tay) - có encryption
                                Log.d(TAG, "Authenticated with STRONG biometric (fingerprint)");
                                Cipher cipher = cryptoObject.getCipher();
                                String encryptedPassword = prefs.getString(KEY_ENCRYPTED_PASSWORD, null);
                                byte[] decryptedPassword = cipher.doFinal(
                                    Base64.decode(encryptedPassword, Base64.DEFAULT));
                                String password = new String(decryptedPassword, StandardCharsets.UTF_8);

                                String email = prefs.getString(KEY_EMAIL, null);
                                Log.d(TAG, "Biometric success (STRONG) - Email: " + email + ", Password length: " + (password != null ? password.length() : 0));

                                // Validate email
                                if (email == null || email.isEmpty()) {
                                    Log.e(TAG, "Email is NULL after decryption! Biometric data corrupted.");
                                    callback.onError("❌ Email NULL! Dữ liệu bị lỗi. Tắt và bật lại biometric.");
                                    return;
                                }

                                callback.onSuccess(email, password);
                            } else {
                                // WEAK biometric (khuôn mặt) - không có encryption
                                Log.d(TAG, "Authenticated with WEAK biometric (face unlock)");

                                // Fallback: vẫn decrypt bằng key
                                try {
                                    SecretKey key = getOrCreateKey();
                                    Cipher fallbackCipher = Cipher.getInstance("AES/GCM/NoPadding");
                                    String ivStr = prefs.getString(KEY_IV, null);
                                    byte[] ivBytes = Base64.decode(ivStr, Base64.DEFAULT);
                                    GCMParameterSpec specFallback = new GCMParameterSpec(128, ivBytes);
                                    fallbackCipher.init(Cipher.DECRYPT_MODE, key, specFallback);

                                    String encryptedPassword = prefs.getString(KEY_ENCRYPTED_PASSWORD, null);
                                    byte[] decryptedPassword = fallbackCipher.doFinal(
                                        Base64.decode(encryptedPassword, Base64.DEFAULT));
                                    String password = new String(decryptedPassword, StandardCharsets.UTF_8);

                                    String email = prefs.getString(KEY_EMAIL, null);
                                    Log.d(TAG, "Biometric success (WEAK) - Email: " + email + ", Password length: " + (password != null ? password.length() : 0));

                                    // Validate email
                                    if (email == null || email.isEmpty()) {
                                        Log.e(TAG, "Email is NULL after decryption! Biometric data corrupted.");
                                        callback.onError("❌ Email NULL! Dữ liệu bị lỗi. Tắt và bật lại biometric.");
                                        return;
                                    }

                                    callback.onSuccess(email, password);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error decrypting with WEAK biometric", e);
                                    callback.onError("❌ Lỗi decrypt: " + e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error decrypting password", e);
                            callback.onError("Lỗi giải mã: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        Log.w(TAG, "Biometric authentication error: " + errString);
                        callback.onError(errString.toString());
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        Log.w(TAG, "Biometric authentication failed");
                        callback.onError("Xác thực thất bại");
                    }
                });

            // Thử authenticate với CryptoObject trước (cho STRONG biometric)
            try {
                biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
                Log.d(TAG, "Biometric prompt shown with CryptoObject");
            } catch (Exception e) {
                // Nếu fail (do WEAK biometric không support CryptoObject), authenticate không có crypto
                Log.w(TAG, "CryptoObject not supported, trying without crypto", e);
                biometricPrompt.authenticate(promptInfo);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error authenticating", e);
            callback.onError("Lỗi xác thực: " + e.getMessage());
        }
    }

    /**
     * Lấy hoặc tạo key mới trong Android Keystore
     * Hỗ trợ cả STRONG (vân tay) và WEAK (khuôn mặt) biometric
     */
    private SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_NAME)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            // Cấu hình key để hỗ trợ cả STRONG và WEAK biometric
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setInvalidatedByBiometricEnrollment(true); // Key invalid khi thêm vân tay/khuôn mặt mới

            // KHÔNG set setUserAuthenticationRequired(true) để cho phép WEAK biometric (face unlock)
            // Trade-off: Giảm bảo mật một chút nhưng tăng compatibility với face unlock
            // Password vẫn được mã hóa bằng Android Keystore (hardware-backed)

            KeyGenParameterSpec keyGenParameterSpec = builder.build();

            keyGenerator.init(keyGenParameterSpec);
            SecretKey key = keyGenerator.generateKey();
            Log.d(TAG, "Created new biometric key supporting STRONG and WEAK authenticators");
            return key;
        } else {
            Log.d(TAG, "Retrieved existing biometric key");
            return (SecretKey) keyStore.getKey(KEY_NAME, null);
        }
    }

    /**
     * Callback interface cho biometric authentication
     */
    public interface BiometricCallback {
        /**
         * Called when biometric authentication succeeds
         * @param email Email của user (dùng để đăng nhập)
         * @param password Password đã decrypt
         */
        void onSuccess(String email, String password);

        /**
         * Called when biometric authentication fails
         * @param error Error message
         */
        void onError(String error);
    }
}

