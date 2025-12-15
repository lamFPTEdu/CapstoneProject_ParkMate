package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.model.SubscriptionPackage;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SubscriptionPackageAdapter extends RecyclerView.Adapter<SubscriptionPackageAdapter.ViewHolder> {

    public interface OnPackageClickListener {
        void onPackageClick(SubscriptionPackage pkg);
    }

    private List<SubscriptionPackage> packages = new ArrayList<>();
    private OnPackageClickListener listener;

    // Constructor without listener (for detail display)
    public SubscriptionPackageAdapter() {
        this.listener = null;
    }

    // Constructor with listener (for selection)
    public SubscriptionPackageAdapter(OnPackageClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SubscriptionPackage> newPackages) {
        packages.clear();
        if (newPackages != null) {
            // Chỉ thêm package active
            for (SubscriptionPackage pkg : newPackages) {
                if (pkg.isActive()) {
                    packages.add(pkg);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Alias method for backward compatibility
    public void updatePackages(List<SubscriptionPackage> newPackages) {
        submitList(newPackages);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subscription_package, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(packages.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPackageName;
        private final TextView tvPackageDesc;
        private final TextView tvPackagePrice;
        private final TextView tvVehicleType;
        private final TextView tvDuration;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPackageName = itemView.findViewById(R.id.tvPackageName);
            tvPackageDesc = itemView.findViewById(R.id.tvPackageDesc);
            tvPackagePrice = itemView.findViewById(R.id.tvPackagePrice);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }

        void bind(SubscriptionPackage pkg, OnPackageClickListener clickListener) {
            tvPackageName.setText(pkg.getName());
            tvPackageDesc.setText(pkg.getDescription());

            // Format price
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String priceText = formatter.format(pkg.getPrice());
            tvPackagePrice.setText(priceText);

            // Vehicle type
            tvVehicleType.setText(getVehicleTypeDisplay(pkg.getVehicleType()));

            // Duration
            tvDuration.setText(getDurationDisplay(pkg.getDurationType(), pkg.getDurationValue()));

            // Click listener
            if (clickListener != null) {
                itemView.setOnClickListener(v -> clickListener.onPackageClick(pkg));
            } else {
                itemView.setOnClickListener(null);
                itemView.setClickable(false);
            }
        }

        private String getVehicleTypeDisplay(String vehicleType) {
            if (vehicleType == null) return "";
            switch (vehicleType) {
                case "CAR_UP_TO_9_SEATS":
                    return "🚗 Ô tô";
                case "MOTORBIKE":
                    return "🏍️ Xe máy";
                case "BIKE":
                    return "🚲 Xe đạp";
                default:
                    return vehicleType;
            }
        }

        private String getDurationDisplay(String durationType, Integer durationValue) {
            if (durationType == null || durationValue == null) return "";

            switch (durationType) {
                case "MONTHLY":
                    // durationValue is number of DAYS (30 days = 1 month)
                    int months = durationValue / 30;
                    if (months == 1) {
                        return "1 tháng";
                    } else {
                        return months + " tháng";
                    }
                case "QUARTERLY":
                    // durationValue is number of DAYS (90 days = 1 quarter = 3 months)
                    int quarters = durationValue / 90;
                    if (quarters == 1) {
                        return "1 quý (3 tháng)";
                    } else {
                        return quarters + " quý (" + (quarters * 3) + " tháng)";
                    }
                case "YEARLY":
                    // durationValue is number of DAYS (365 days = 1 year)
                    int years = durationValue / 365;
                    if (years == 1) {
                        return "1 năm";
                    } else {
                        return years + " năm";
                    }
                default:
                    return durationValue + " ngày";
            }
        }
    }
}

