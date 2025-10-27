package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.adapter.VehicleAdapter;
import com.parkmate.android.model.Vehicle;
import com.parkmate.android.model.response.VehicleResponse;
import com.parkmate.android.model.response.ApiResponse;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class VehicleActivity extends AppCompatActivity {

    private RecyclerView rvVehicleList;
    private VehicleAdapter vehicleAdapter;
    private Button btnAddVehicle;
    private ImageButton btnBack;
    private List<Vehicle> vehicleList;
    private ProgressBar progressBar;
    private ProgressBar progressBarLoadMore;

    private ApiService apiService;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    // Pagination variables
    private int currentPage = 0;
    private final int pageSize = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private LinearLayoutManager layoutManager;

    // ActivityResultLauncher để nhận kết quả từ AddVehicleActivity
    private final ActivityResultLauncher<Intent> addVehicleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Reload danh sách xe sau khi thêm thành công
                    loadVehicles();
                }
            }
    );

    // ActivityResultLauncher để nhận kết quả từ ViewVehicleDetailActivity
    private final ActivityResultLauncher<Intent> viewVehicleDetailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Reload danh sách xe sau khi có thay đổi từ EditVehicleActivity
                    loadVehicles();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vehicle);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiService = ApiClient.getApiService();
        vehicleList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadVehicles();
        setupListeners();
    }

    private void initViews() {
        rvVehicleList = findViewById(R.id.rvVehicleList);
        btnAddVehicle = findViewById(R.id.btnAddVehicle);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        progressBarLoadMore = findViewById(R.id.progressBarLoadMore);
    }

    private void setupRecyclerView() {
        vehicleAdapter = new VehicleAdapter(new VehicleAdapter.OnVehicleClickListener() {
            @Override
            public void onDeleteClick(Vehicle vehicle, int position) {
                showDeleteConfirmDialog(vehicle, position);
            }

            @Override
            public void onVehicleClick(Vehicle vehicle, int position) {
                // Mở màn hình xem chi tiết xe
                Intent intent = new Intent(VehicleActivity.this, ViewVehicleDetailActivity.class);
                intent.putExtra(ViewVehicleDetailActivity.EXTRA_VEHICLE_ID, vehicle.getId());
                viewVehicleDetailLauncher.launch(intent);
            }
        });

        layoutManager = new LinearLayoutManager(this);
        rvVehicleList.setLayoutManager(layoutManager);
        rvVehicleList.setAdapter(vehicleAdapter);

        // Thêm scroll listener cho infinite scroll
        rvVehicleList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Kiểm tra nếu đang cuộn xuống
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Nếu chưa loading và chưa đến trang cuối
                    if (!isLoading && !isLastPage) {
                        // Kiểm tra nếu đã cuộn gần đến cuối (còn 2 items nữa là hết)
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
                                && firstVisibleItemPosition >= 0) {
                            loadMoreVehicles();
                        }
                    }
                }
            }
        });
    }

    private void loadVehicles() {
        // Reset về trang đầu
        currentPage = 0;
        isLastPage = false;
        vehicleList.clear();

        // Hiển thị loading
        showLoading(true);

        // Gọi API lấy danh sách xe với ownedByMe=true
        loadVehiclesFromApi();
    }

    private void loadMoreVehicles() {
        if (isLoading || isLastPage) {
            return;
        }

        currentPage++;
        showLoadMoreProgress(true);
        loadVehiclesFromApi();
    }

    private void loadVehiclesFromApi() {
        isLoading = true;

        compositeDisposable.add(
            apiService.getVehicles(currentPage, pageSize, "createdAt", "desc", true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    this::handleVehicleResponse,
                    this::handleError
                )
        );
    }

    private void handleVehicleResponse(ApiResponse<VehicleResponse> apiResponse) {
        isLoading = false;
        showLoading(false);
        showLoadMoreProgress(false);

        if (apiResponse != null && apiResponse.isSuccess() && apiResponse.getData() != null) {
            VehicleResponse response = apiResponse.getData();

            if (response.getContent() != null) {
                List<Vehicle> newVehicles = response.getContent();

                // Thêm xe mới vào danh sách
                vehicleList.addAll(newVehicles);
                vehicleAdapter.setVehicleList(vehicleList);

                // Kiểm tra xem đã hết dữ liệu chưa
                isLastPage = response.isLast() || newVehicles.isEmpty();

                // Hiển thị thông báo nếu danh sách trống
                if (vehicleList.isEmpty() && currentPage == 0) {
                    Toast.makeText(this, "Bạn chưa có xe nào. Hãy thêm xe mới!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // Xử lý khi API trả về success = false
            String errorMsg = apiResponse != null && apiResponse.getMessage() != null
                ? apiResponse.getMessage()
                : "Không thể tải danh sách xe";
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleError(Throwable throwable) {
        isLoading = false;
        showLoading(false);
        showLoadMoreProgress(false);

        // Nếu lỗi khi load more, giảm currentPage về trước đó
        if (currentPage > 0) {
            currentPage--;
        }

        String errorMessage = "Không thể tải danh sách xe: " + throwable.getMessage();
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();

        // Log error để debug
        throwable.printStackTrace();
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        rvVehicleList.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showLoadMoreProgress(boolean show) {
        if (progressBarLoadMore != null) {
            progressBarLoadMore.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAddVehicle.setOnClickListener(v -> {
            // Mở màn hình thêm xe mới
            Intent intent = new Intent(VehicleActivity.this, AddVehicleActivity.class);
            addVehicleLauncher.launch(intent);
        });
    }

    private void showDeleteConfirmDialog(Vehicle vehicle, int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.vehicle_delete_title)
                .setMessage(getString(R.string.vehicle_delete_message, vehicle.getLicensePlate()))
                .setPositiveButton(R.string.vehicle_delete_confirm, (dialog, which) -> {
                    deleteVehicle(vehicle, position);
                })
                .setNegativeButton(R.string.vehicle_delete_cancel, null)
                .show();
    }

    private void deleteVehicle(Vehicle vehicle, int position) {
        // Hiển thị progress
        showLoading(true);

        compositeDisposable.add(
            apiService.deleteVehicle(vehicle.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        showLoading(false);
                        if (response != null && response.isSuccess()) {
                            // Xóa thành công
                            vehicleAdapter.removeVehicle(position);
                            vehicleList.remove(position);
                            Toast.makeText(this, R.string.vehicle_deleted_success, Toast.LENGTH_SHORT).show();
                        } else {
                            // API trả về lỗi
                            String errorMsg = response != null && response.getMessage() != null
                                ? response.getMessage()
                                : "Không thể xóa xe";
                            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    },
                    throwable -> {
                        showLoading(false);
                        String errorMessage = "Không thể xóa xe: " + throwable.getMessage();
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        throwable.printStackTrace();
                    }
                )
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}
