package com.parkmate.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.activity.SubscriptionLocationSelectionActivity;
import com.parkmate.android.adapter.SubscriptionAreaAdapter;
import com.parkmate.android.model.ParkingArea;
import com.parkmate.android.network.ApiClient;
import com.parkmate.android.viewmodel.SubscriptionLocationViewModel;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Fragment để chọn khu vực (Area)
 * Sử dụng ViewModel để share data với Activity và các Fragments khác
 */
public class SubscriptionAreaFragment extends Fragment {

    private RecyclerView rvAreas;
    private ProgressBar progressBar;
    private TextView tvNoAreas;

    private SubscriptionAreaAdapter areaAdapter;
    private SubscriptionLocationViewModel viewModel;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subscription_area, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel from Activity scope
        viewModel = new ViewModelProvider(requireActivity()).get(SubscriptionLocationViewModel.class);

        initializeViews(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAreas();
    }

    private void initializeViews(View view) {
        rvAreas = view.findViewById(R.id.rvAreas);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoAreas = view.findViewById(R.id.tvNoAreas);

        areaAdapter = new SubscriptionAreaAdapter(area -> {
            // Notify Activity của selection
            SubscriptionLocationSelectionActivity activity = (SubscriptionLocationSelectionActivity) getActivity();
            if (activity != null) {
                activity.onAreaSelected(area.getId());
            }
        });

        rvAreas.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAreas.setAdapter(areaAdapter);
    }

    private void loadAreas() {
        // Get data từ ViewModel
        Long floorId = viewModel.getSelectedFloorIdValue();
        Long vehicleId = viewModel.getVehicleIdValue();
        Long packageId = viewModel.getPackageIdValue();
        String startDate = viewModel.getFormattedStartDate();

        if (floorId == null || vehicleId == null || packageId == null || startDate == null) {
            return;
        }

        showLoading(true);

        compositeDisposable.add(
                ApiClient.getApiService()
                        .getAvailableAreas(floorId, vehicleId, packageId, startDate)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    showLoading(false);
                                    if (response.isSuccess() && response.getData() != null) {
                                        List<ParkingArea> areas = response.getData();

                                        if (!areas.isEmpty()) {
                                            areaAdapter.updateAreas(areas);
                                            rvAreas.setVisibility(View.VISIBLE);
                                            tvNoAreas.setVisibility(View.GONE);
                                        } else {
                                            showNoAreas();
                                        }
                                    } else {
                                        showNoAreas();
                                        showError(response.getError() != null ? response.getError()
                                                : "Không thể tải danh sách khu vực");
                                    }
                                },
                                throwable -> {
                                    showLoading(false);
                                    showNoAreas();
                                    showError("Lỗi: " + throwable.getMessage());
                                }));
    }

    private void showNoAreas() {
        rvAreas.setVisibility(View.GONE);
        tvNoAreas.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        compositeDisposable.clear();
    }
}
