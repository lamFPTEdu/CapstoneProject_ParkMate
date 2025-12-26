package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.parkmate.android.R;
import com.parkmate.android.fragment.SubscriptionAreaFragment;
import com.parkmate.android.fragment.SubscriptionFloorFragment;
import com.parkmate.android.fragment.SubscriptionSpotFragment;
import com.parkmate.android.model.SubscriptionPackage;
import com.parkmate.android.utils.SubscriptionHoldManager;
import com.parkmate.android.viewmodel.SubscriptionLocationViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Activity chính để chọn vị trí đỗ xe cho subscription
 * Gồm 3 tab: Floor → Area → Spot
 * Sử dụng ViewModel để share data giữa các Fragments
 */
public class SubscriptionLocationSelectionActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private SubscriptionHoldManager holdManager;
    private SubscriptionLocationViewModel viewModel;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    // Step indicator views
    private View step1Container, step2Container, step3Container;
    private View step1Circle, step2Circle, step3Circle;
    private TextView step1Number, step2Number, step3Number;
    private TextView step1Label, step2Label, step3Label;
    private View connector1, connector2;

    // Additional data to forward to Summary
    private String parkingLotName;
    private String vehiclePlateNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_location_selection);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SubscriptionLocationViewModel.class);
        holdManager = new SubscriptionHoldManager(this);

        // Only load intent data if ViewModel is empty (fresh start)
        if (viewModel.getParkingLotIdValue() == null) {
            loadIntentData();
        }

        initializeViews();
        setupToolbar();
        setupViewPager();
        setupBackPressedHandler();
        observeViewModel();
        updateStepIndicators(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Clear spot to allow fresh selection (but keep floor/area)
        viewModel.clearSpotSelection();
    }

    private void loadIntentData() {
        long parkingLotId = getIntent().getLongExtra("PARKING_LOT_ID", -1);
        long vehicleId = getIntent().getLongExtra("VEHICLE_ID", -1);
        long packageId = getIntent().getLongExtra("PACKAGE_ID", -1);
        String startDate = getIntent().getStringExtra("START_DATE");
        SubscriptionPackage subscriptionPackage = (SubscriptionPackage) getIntent().getSerializableExtra("PACKAGE");

        // Get additional data to forward
        parkingLotName = getIntent().getStringExtra("PARKING_LOT_NAME");
        vehiclePlateNumber = getIntent().getStringExtra("VEHICLE_PLATE_NUMBER");

        if (parkingLotId == -1 || vehicleId == -1 || packageId == -1 || startDate == null) {
            Toast.makeText(this, "Thông tin không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Store in ViewModel
        viewModel.setParkingLotId(parkingLotId);
        viewModel.setVehicleId(vehicleId);
        viewModel.setPackageId(packageId);
        viewModel.setStartDate(startDate);
        viewModel.setSubscriptionPackage(subscriptionPackage);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Step containers (for pill background)
        step1Container = findViewById(R.id.step1Container);
        step2Container = findViewById(R.id.step2Container);
        step3Container = findViewById(R.id.step3Container);

        // Step circles and numbers
        step1Circle = findViewById(R.id.step1Circle);
        step2Circle = findViewById(R.id.step2Circle);
        step3Circle = findViewById(R.id.step3Circle);
        step1Number = findViewById(R.id.step1Number);
        step2Number = findViewById(R.id.step2Number);
        step3Number = findViewById(R.id.step3Number);
        step1Label = findViewById(R.id.step1Label);
        step2Label = findViewById(R.id.step2Label);
        step3Label = findViewById(R.id.step3Label);
        connector1 = findViewById(R.id.connector1);
        connector2 = findViewById(R.id.connector2);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chọn vị trí đỗ xe");
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupViewPager() {
        LocationPagerAdapter adapter = new LocationPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Disable swipe between tabs
        viewPager.setUserInputEnabled(false);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("1. Tầng");
                            break;
                        case 1:
                            tab.setText("2. Khu vực");
                            break;
                        case 2:
                            tab.setText("3. Chỗ đỗ");
                            break;
                    }
                }).attach();

        // Detect tab changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                viewModel.setCurrentStep(position);
                updateStepIndicators(position);

                // Release held spot when going back
                if (position < 2) {
                    releaseHeldSpot();
                }
            }
        });
    }

    private void updateStepIndicators(int currentStep) {
        // Step 1 - always active since we start here
        if (currentStep >= 0) {
            step1Container.setBackgroundResource(R.drawable.bg_step_pill_active);
            step1Circle.setBackgroundResource(R.drawable.bg_circle_white);
            step1Number.setTextColor(ContextCompat.getColor(this, R.color.primary));
            step1Label.setTextColor(ContextCompat.getColor(this, R.color.white));
        }

        // Step 2
        if (currentStep >= 1) {
            step2Container.setBackgroundResource(R.drawable.bg_step_pill_active);
            step2Circle.setBackgroundResource(R.drawable.bg_circle_white);
            step2Number.setTextColor(ContextCompat.getColor(this, R.color.primary));
            step2Label.setTextColor(ContextCompat.getColor(this, R.color.white));
            connector1.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
        } else {
            step2Container.setBackgroundResource(R.drawable.bg_step_pill_inactive);
            step2Circle.setBackgroundResource(R.drawable.bg_circle_gray);
            step2Number.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            step2Label.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            connector1.setBackgroundColor(ContextCompat.getColor(this, R.color.border_color));
        }

        // Step 3
        if (currentStep >= 2) {
            step3Container.setBackgroundResource(R.drawable.bg_step_pill_active);
            step3Circle.setBackgroundResource(R.drawable.bg_circle_white);
            step3Number.setTextColor(ContextCompat.getColor(this, R.color.primary));
            step3Label.setTextColor(ContextCompat.getColor(this, R.color.white));
            connector2.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
        } else {
            step3Container.setBackgroundResource(R.drawable.bg_step_pill_inactive);
            step3Circle.setBackgroundResource(R.drawable.bg_circle_gray);
            step3Number.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            step3Label.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            connector2.setBackgroundColor(ContextCompat.getColor(this, R.color.border_color));
        }
    }

    private void observeViewModel() {
        // Observe floor selection to enable Area tab
        viewModel.getSelectedFloorId().observe(this, floorId -> {
            if (floorId != null) {
                TabLayout.Tab tab = tabLayout.getTabAt(1);
                if (tab != null) {
                    tab.view.setEnabled(true);
                }
            }
        });

        // Observe area selection to enable Spot tab
        viewModel.getSelectedAreaId().observe(this, areaId -> {
            if (areaId != null) {
                TabLayout.Tab tab = tabLayout.getTabAt(2);
                if (tab != null) {
                    tab.view.setEnabled(true);
                }
            }
        });
        // Note: Spot selection navigation is handled directly in onSpotSelected()
    }

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int currentItem = viewPager.getCurrentItem();

                if (currentItem == 0) {
                    // Đang ở tab Floor, back ra ngoài Activity
                    releaseHeldSpot();
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                } else {
                    // Đang ở tab Area hoặc Spot, back về tab trước
                    if (currentItem == 2) {
                        releaseHeldSpot();
                    }
                    viewPager.setCurrentItem(currentItem - 1, true);
                }
            }
        });
    }

    /**
     * Được gọi từ FloorFragment khi user chọn floor
     */
    public void onFloorSelected(Long floorId) {
        viewModel.setSelectedFloorId(floorId);
        viewPager.setCurrentItem(1, true);
    }

    /**
     * Được gọi từ AreaFragment khi user chọn area
     */
    public void onAreaSelected(Long areaId) {
        viewModel.setSelectedAreaId(areaId);
        viewPager.setCurrentItem(2, true);
    }

    /**
     * Được gọi từ SpotFragment khi user chọn spot
     */
    public void onSpotSelected(Long spotId, String spotName) {
        viewModel.setSelectedSpot(spotId, spotName);
        // Navigate directly instead of using observer to avoid timing issues
        navigateToSummary();
    }

    private void navigateToSummary() {
        Intent intent = new Intent(this, SubscriptionSummaryActivity.class);
        intent.putExtra("PARKING_LOT_ID", viewModel.getParkingLotIdValue());
        intent.putExtra("PARKING_LOT_NAME", parkingLotName);
        intent.putExtra("VEHICLE_ID", viewModel.getVehicleIdValue());
        intent.putExtra("VEHICLE_PLATE_NUMBER", vehiclePlateNumber);
        intent.putExtra("PACKAGE_ID", viewModel.getPackageIdValue());
        intent.putExtra("START_DATE", viewModel.getStartDateValue());
        intent.putExtra("SPOT_ID", viewModel.getSelectedSpotIdValue());
        intent.putExtra("SPOT_NAME",
                viewModel.getSelectedSpotNameValue() != null ? viewModel.getSelectedSpotNameValue() : "");

        SubscriptionPackage pkg = viewModel.getSubscriptionPackageValue();
        if (pkg != null) {
            intent.putExtra("PACKAGE_NAME", pkg.getName());
            intent.putExtra("PACKAGE_PRICE", pkg.getPrice());
        } else {
            intent.putExtra("PACKAGE_NAME", "");
            intent.putExtra("PACKAGE_PRICE", 0L);
        }

        startActivity(intent);
    }

    /**
     * Được gọi từ SpotFragment khi spot được hold thành công
     */
    public void setHeldSpotId(Long spotId) {
        viewModel.setHeldSpotId(spotId);
        if (spotId != null) {
            holdManager.setHeldSpot(spotId);
        }
    }

    /**
     * Release held spot nếu có
     */
    private void releaseHeldSpot() {
        Long heldSpotId = viewModel.getHeldSpotIdValue();
        if (heldSpotId != null) {
            // Tìm và gọi SpotFragment để release
            Fragment spotFragment = getSupportFragmentManager().findFragmentByTag("f2");
            if (spotFragment instanceof SubscriptionSpotFragment) {
                ((SubscriptionSpotFragment) spotFragment).releaseCurrentHeldSpot();
            } else {
                // Fallback: release trực tiếp
                releaseHeldSpotDirectly(heldSpotId);
            }
            viewModel.clearHeldSpotId();
        }
    }

    private void releaseHeldSpotDirectly(Long spotId) {
        compositeDisposable.add(
                com.parkmate.android.network.ApiClient.getApiService()
                        .releaseHoldSpot(spotId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    android.util.Log.d("SubscriptionLocation", "Spot released: " + spotId);
                                    holdManager.clearHeldSpot();
                                },
                                throwable -> {
                                    android.util.Log.e("SubscriptionLocation", "Error releasing spot", throwable);
                                    holdManager.clearHeldSpot();
                                }));
    }

    // ========== Getter methods cho Fragments (backward compatibility) ==========

    public SubscriptionLocationViewModel getViewModel() {
        return viewModel;
    }

    public long getParkingLotId() {
        Long value = viewModel.getParkingLotIdValue();
        return value != null ? value : -1;
    }

    public long getVehicleId() {
        Long value = viewModel.getVehicleIdValue();
        return value != null ? value : -1;
    }

    public long getPackageId() {
        Long value = viewModel.getPackageIdValue();
        return value != null ? value : -1;
    }

    public String getStartDate() {
        return viewModel.getStartDateValue();
    }

    public Long getSelectedFloorId() {
        return viewModel.getSelectedFloorIdValue();
    }

    public Long getSelectedAreaId() {
        return viewModel.getSelectedAreaIdValue();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        releaseHeldSpot();
    }

    /**
     * Adapter cho ViewPager2
     */
    private class LocationPagerAdapter extends FragmentStateAdapter {

        public LocationPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new SubscriptionFloorFragment();
                case 1:
                    return new SubscriptionAreaFragment();
                case 2:
                    return new SubscriptionSpotFragment();
                default:
                    return new SubscriptionFloorFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
