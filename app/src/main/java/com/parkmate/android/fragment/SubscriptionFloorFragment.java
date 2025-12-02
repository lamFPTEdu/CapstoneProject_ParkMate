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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.activity.SubscriptionLocationSelectionActivity;
import com.parkmate.android.adapter.SubscriptionFloorAdapter;
import com.parkmate.android.model.ParkingFloor;
import com.parkmate.android.network.ApiClient;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Fragment để chọn tầng (Floor)
 */
public class SubscriptionFloorFragment extends Fragment {

    private RecyclerView rvFloors;
    private ProgressBar progressBar;
    private TextView tvNoFloors;

    private SubscriptionFloorAdapter floorAdapter;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subscription_floor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        loadFloors();
    }

    private void initializeViews(View view) {
        rvFloors = view.findViewById(R.id.rvFloors);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoFloors = view.findViewById(R.id.tvNoFloors);

        floorAdapter = new SubscriptionFloorAdapter(floor -> {
            SubscriptionLocationSelectionActivity activity = (SubscriptionLocationSelectionActivity) getActivity();
            if (activity != null) {
                activity.onFloorSelected(floor.getId());
            }
        });

        rvFloors.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFloors.setAdapter(floorAdapter);
    }

    private void loadFloors() {
        SubscriptionLocationSelectionActivity activity = (SubscriptionLocationSelectionActivity) getActivity();
        if (activity == null) return;

        showLoading(true);

        String formattedStartDate = activity.getStartDate() + "T00:00:00";

        compositeDisposable.add(
                ApiClient.getApiService()
                        .getAvailableFloors(
                                activity.getParkingLotId(),
                                activity.getVehicleId(),
                                activity.getPackageId(),
                                formattedStartDate
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    showLoading(false);
                                    if (response.isSuccess() && response.getData() != null) {
                                        List<ParkingFloor> floors = response.getData();

                                        if (!floors.isEmpty()) {
                                            floorAdapter.updateFloors(floors);
                                            rvFloors.setVisibility(View.VISIBLE);
                                            tvNoFloors.setVisibility(View.GONE);
                                        } else {
                                            showNoFloors();
                                        }
                                    } else {
                                        showNoFloors();
                                        showError(response.getError() != null ? response.getError() : "Không thể tải danh sách tầng");
                                    }
                                },
                                throwable -> {
                                    showLoading(false);
                                    showNoFloors();
                                    showError("Lỗi: " + throwable.getMessage());
                                }
                        )
        );
    }

    private void showNoFloors() {
        rvFloors.setVisibility(View.GONE);
        tvNoFloors.setVisibility(View.VISIBLE);
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

