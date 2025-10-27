package com.parkmate.android.repository;

import android.util.Log;

import com.parkmate.android.model.Payment;
import com.parkmate.android.model.PaymentCancel;
import com.parkmate.android.model.PaymentStatus;
import com.parkmate.android.model.response.ApiResponse;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.network.ApiService;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Repository để xử lý các thao tác liên quan đến Payment
 */
public class PaymentRepository {
    private static final String TAG = "PaymentRepository";
    private final ApiService apiService;

    public PaymentRepository() {
        this.apiService = ApiClient.getApiService();
    }

    /**
     * Tạo mã QR thanh toán
     */
    public Single<ApiResponse<Payment>> createPayment(long amount) {
        return apiService.createPayment(amount)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> Log.d(TAG, "Đang tạo mã thanh toán với số tiền: " + amount))
                .doOnSuccess(response -> {
                    if (response.isSuccess()) {
                        Log.d(TAG, "Tạo mã thanh toán thành công");
                    } else {
                        Log.e(TAG, "Tạo mã thanh toán thất bại: " + response.getError());
                    }
                })
                .doOnError(error -> Log.e(TAG, "Lỗi khi tạo mã thanh toán: " + error.getMessage()));
    }

    /**
     * Hủy thanh toán
     */
    public Single<ApiResponse<PaymentCancel>> cancelPayment(long orderCode, String reason) {
        return apiService.cancelPayment(orderCode, reason)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> Log.d(TAG, "Đang hủy thanh toán: " + orderCode))
                .doOnSuccess(response -> {
                    if (response.isSuccess()) {
                        Log.d(TAG, "Hủy thanh toán thành công");
                    } else {
                        Log.e(TAG, "Hủy thanh toán thất bại: " + response.getError());
                    }
                })
                .doOnError(error -> Log.e(TAG, "Lỗi khi hủy thanh toán: " + error.getMessage()));
    }

    /**
     * Kiểm tra trạng thái thanh toán
     */
    public Single<ApiResponse<PaymentStatus>> checkPaymentStatus(long orderCode) {
        return apiService.checkPaymentStatus(orderCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> Log.d(TAG, "Đang kiểm tra trạng thái thanh toán: " + orderCode))
                .doOnSuccess(response -> {
                    if (response.isSuccess()) {
                        Log.d(TAG, "Kiểm tra trạng thái thành công: " + response.getData().getTransactionStatus());
                    } else {
                        Log.e(TAG, "Kiểm tra trạng thái thất bại: " + response.getError());
                    }
                })
                .doOnError(error -> Log.e(TAG, "Lỗi khi kiểm tra trạng thái: " + error.getMessage()));
    }
}

