package com.parkmate.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.parkmate.android.R;
import com.parkmate.android.fragment.SubscriptionAreaFragment;
import com.parkmate.android.fragment.SubscriptionFloorFragment;
import com.parkmate.android.fragment.SubscriptionSpotFragment;
import com.parkmate.android.utils.SubscriptionHoldManager;

/**
 * Activity chính để chọn vị trí đỗ xe cho subscription
 * Gồm 3 tab: Floor → Area → Spot
 */
public class SubscriptionLocationSelectionActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private SubscriptionHoldManager holdManager;

    private long parkingLotId;
    private long vehicleId;
    private long packageId;
    private String startDate;
    private com.parkmate.android.model.SubscriptionPackage subscriptionPackage;

    private Long selectedFloorId;
    private Long selectedAreaId;
    private Long selectedSpotId;
    private Long heldSpotId; // Track held spot để release khi cần

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_location_selection);

        holdManager = new SubscriptionHoldManager(this);

        parkingLotId = getIntent().getLongExtra("PARKING_LOT_ID", -1);
        vehicleId = getIntent().getLongExtra("VEHICLE_ID", -1);
        packageId = getIntent().getLongExtra("PACKAGE_ID", -1);
        startDate = getIntent().getStringExtra("START_DATE");
        subscriptionPackage = (com.parkmate.android.model.SubscriptionPackage) getIntent().getSerializableExtra("PACKAGE");

        if (parkingLotId == -1 || vehicleId == -1 || packageId == -1 || startDate == null) {
            Toast.makeText(this, "Thông tin không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        setupViewPager();
        setupBackPressedHandler();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
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
                }
        ).attach();

        // Initially disable tabs 2 and 3
        TabLayout.Tab tab1 = tabLayout.getTabAt(1);
        TabLayout.Tab tab2 = tabLayout.getTabAt(2);
        if (tab1 != null) tab1.view.setEnabled(false);
        if (tab2 != null) tab2.view.setEnabled(false);

        // Detect tab changes to release held spot when going back
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Nếu user back về tab trước (không phải tab Spot) thì release held spot
                if (position < 2) { // Tab 0 (Floor) hoặc Tab 1 (Area)
                    releaseHeldSpot();
                }
            }
        });
    }

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int currentItem = viewPager.getCurrentItem();

                if (currentItem == 0) {
                    // Đang ở tab Floor, back ra ngoài Activity
                    // Release held spot trước khi finish (nếu có)
                    releaseHeldSpot();
                    // Disable this callback and let the system handle back press
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                } else {
                    // Đang ở tab Area hoặc Spot, back về tab trước
                    if (currentItem == 2) {
                        // Từ Spot về Area - release held spot
                        releaseHeldSpot();
                    }
                    viewPager.setCurrentItem(currentItem - 1, true);
                }
            }
        });
    }

    /**
     * Được gọi khi user chọn floor
     */
    public void onFloorSelected(Long floorId) {
        this.selectedFloorId = floorId;

        // Enable Area tab and move to it
        TabLayout.Tab tab = tabLayout.getTabAt(1);
        if (tab != null) {
            tab.view.setEnabled(true);
            viewPager.setCurrentItem(1, true);
        }
    }

    /**
     * Được gọi khi user chọn area
     */
    public void onAreaSelected(Long areaId) {
        this.selectedAreaId = areaId;

        // Enable Spot tab and move to it
        TabLayout.Tab tab = tabLayout.getTabAt(2);
        if (tab != null) {
            tab.view.setEnabled(true);
            viewPager.setCurrentItem(2, true);
        }
    }

    /**
     * Được gọi khi user chọn spot
     */
    public void onSpotSelected(Long spotId, String spotName) {
        this.selectedSpotId = spotId;

        // Navigate to summary với đầy đủ thông tin
        Intent intent = new Intent(this, SubscriptionSummaryActivity.class);
        intent.putExtra("PARKING_LOT_ID", parkingLotId);
        intent.putExtra("VEHICLE_ID", vehicleId);
        intent.putExtra("PACKAGE_ID", packageId);
        intent.putExtra("START_DATE", startDate);
        intent.putExtra("SPOT_ID", selectedSpotId);
        intent.putExtra("SPOT_NAME", spotName != null ? spotName : "");

        // Truyền thông tin package - null-safe
        if (subscriptionPackage != null) {
            intent.putExtra("PACKAGE_NAME", subscriptionPackage.getName());
            intent.putExtra("PACKAGE_PRICE", subscriptionPackage.getPrice());
        } else {
            intent.putExtra("PACKAGE_NAME", "");
            intent.putExtra("PACKAGE_PRICE", 0L);
        }

        startActivity(intent);
    }

    // Getters for fragments
    public long getParkingLotId() {
        return parkingLotId;
    }

    public long getVehicleId() {
        return vehicleId;
    }

    public long getPackageId() {
        return packageId;
    }

    public String getStartDate() {
        return startDate;
    }

    public Long getSelectedFloorId() {
        return selectedFloorId;
    }

    public Long getSelectedAreaId() {
        return selectedAreaId;
    }

    /**
     * Được gọi từ Fragment khi spot được hold thành công
     */
    public void setHeldSpotId(Long spotId) {
        this.heldSpotId = spotId;
        // Save to SharedPreferences for persistence
        if (spotId != null) {
            holdManager.setHeldSpot(spotId);
        }
    }

    /**
     * Release held spot nếu có
     */
    private void releaseHeldSpot() {
        if (heldSpotId != null) {
            // Tìm Fragment hiện tại
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());

            // Nếu không tìm thấy, thử tìm fragment ở position 2 (Spot tab)
            if (!(currentFragment instanceof SubscriptionSpotFragment)) {
                currentFragment = getSupportFragmentManager().findFragmentByTag("f2");
            }

            // Gọi Fragment để release
            if (currentFragment instanceof SubscriptionSpotFragment) {
                ((SubscriptionSpotFragment) currentFragment).releaseCurrentHeldSpot();
            } else {
                // Fallback: Gọi API trực tiếp nếu không tìm thấy Fragment
                releaseHeldSpotDirectly(heldSpotId);
            }

            heldSpotId = null;
        }
    }

    /**
     * Fallback method để release spot trực tiếp từ Activity
     */
    private void releaseHeldSpotDirectly(Long spotId) {
        com.parkmate.android.network.ApiClient.getApiService()
                .releaseHoldSpot(spotId)
                .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            android.util.Log.d("SubscriptionLocation", "Spot released directly: " + spotId);
                            holdManager.clearHeldSpot();
                        },
                        throwable -> {
                            android.util.Log.e("SubscriptionLocation", "Error releasing spot: " + throwable.getMessage());
                            holdManager.clearHeldSpot();
                        }
                );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release held spot khi activity bị destroy
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

