package com.parkmate.android.repository;

import android.util.Log;

import com.parkmate.android.model.response.ApiResponse;
import com.parkmate.android.model.response.TransactionResponse;
import com.parkmate.android.model.response.WalletResponse;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.network.ApiService;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Repository để xử lý các thao tác liên quan đến Wallet
 */
public class WalletRepository {
    private static final String TAG = "WalletRepository";
    private final ApiService apiService;

    public WalletRepository() {
        this.apiService = ApiClient.getApiService();
    }

    /**
     * Lấy thông tin ví của user hiện tại
     * Token sẽ được tự động thêm vào header thông qua AuthInterceptor
     */
    public Single<WalletResponse> getMyWallet() {
        return apiService.getMyWallet()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> Log.d(TAG, "Đang lấy thông tin ví..."))
                .doOnSuccess(response -> {
                    if (response.isSuccess()) {
                        Log.d(TAG, "Lấy thông tin ví thành công");
                    } else {
                        Log.e(TAG, "Lấy thông tin ví thất bại: " + response.getError());
                    }
                })
                .doOnError(error -> Log.e(TAG, "Lỗi khi lấy thông tin ví: " + error.getMessage()));
    }

    /**
     * Lấy danh sách giao dịch
     * @param page Trang hiện tại (bắt đầu từ 0)
     * @param size Số lượng items trên mỗi trang
     * @param sortBy Sắp xếp theo field nào (mặc định: id)
     * @param sortOrder Thứ tự sắp xếp (asc/desc)
     */
    public Single<ApiResponse<TransactionResponse>> getTransactions(int page, int size, String sortBy, String sortOrder) {
        String criteria = "{\"ownedByMe\":true}";
        return apiService.getTransactions(page, size, sortBy, sortOrder, criteria)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> Log.d(TAG, "Đang lấy danh sách giao dịch..."))
                .doOnSuccess(response -> {
                    if (response.isSuccess()) {
                        Log.d(TAG, "Lấy danh sách giao dịch thành công");
                    } else {
                        Log.e(TAG, "Lấy danh sách giao dịch thất bại: " + response.getError());
                    }
                })
                .doOnError(error -> Log.e(TAG, "Lỗi khi lấy danh sách giao dịch: " + error.getMessage()));
    }
}

