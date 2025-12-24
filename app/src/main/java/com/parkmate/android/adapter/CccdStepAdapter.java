package com.parkmate.android.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.parkmate.android.fragment.CccdStep1Fragment;
import com.parkmate.android.fragment.CccdStep2Fragment;
import com.parkmate.android.fragment.CccdStep3Fragment;

/**
 * Adapter cho ViewPager2 trong màn hình xác thực CCCD
 * Quản lý 3 bước: Thông tin cơ bản -> Chi tiết -> Upload ảnh
 */
public class CccdStepAdapter extends FragmentStateAdapter {

    private static final int NUM_STEPS = 3;

    public CccdStepAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new CccdStep1Fragment();
            case 1:
                return new CccdStep2Fragment();
            case 2:
                return new CccdStep3Fragment();
            default:
                return new CccdStep1Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_STEPS;
    }
}
