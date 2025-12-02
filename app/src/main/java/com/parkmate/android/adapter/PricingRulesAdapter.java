package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.model.response.ParkingLotDetailResponse;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PricingRulesAdapter extends RecyclerView.Adapter<PricingRulesAdapter.ViewHolder> {

    private final List<ParkingLotDetailResponse.PricingRule> rules = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public void submitList(List<ParkingLotDetailResponse.PricingRule> newRules) {
        rules.clear();
        if (newRules != null) {
            // Chỉ thêm các rule active
            for (ParkingLotDetailResponse.PricingRule rule : newRules) {
                if (rule.isActive()) {
                    rules.add(rule);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pricing_rule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(rules.get(position));
    }

    @Override
    public int getItemCount() {
        return rules.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivVehicleIcon;
        private final TextView tvRuleName;
        private final TextView tvActiveBadge;
        private final TextView tvInitialCharge;
        private final TextView tvInitialDuration;
        private final TextView tvStepRate;
        private final TextView tvStepMinute;
        private final LinearLayout layoutOverride;
        private final TextView tvOverrideDetails;
        private final TextView tvOverrideValidity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVehicleIcon = itemView.findViewById(R.id.ivVehicleIcon);
            tvRuleName = itemView.findViewById(R.id.tvRuleName);
            tvActiveBadge = itemView.findViewById(R.id.tvActiveBadge);
            tvInitialCharge = itemView.findViewById(R.id.tvInitialCharge);
            tvInitialDuration = itemView.findViewById(R.id.tvInitialDuration);
            tvStepRate = itemView.findViewById(R.id.tvStepRate);
            tvStepMinute = itemView.findViewById(R.id.tvStepMinute);
            layoutOverride = itemView.findViewById(R.id.layoutOverride);
            tvOverrideDetails = itemView.findViewById(R.id.tvOverrideDetails);
            tvOverrideValidity = itemView.findViewById(R.id.tvOverrideValidity);
        }

        public void bind(ParkingLotDetailResponse.PricingRule rule) {
            // Set icon based on vehicle type
            switch (rule.getVehicleType()) {
                case "CAR_UP_TO_9_SEATS":
                    ivVehicleIcon.setImageResource(R.drawable.ic_directions_car_24);
                    break;
                case "MOTORBIKE":
                    ivVehicleIcon.setImageResource(R.drawable.ic_motorcycle_24);
                    break;
                case "BIKE":
                    ivVehicleIcon.setImageResource(R.drawable.ic_directions_bike_24);
                    break;
                default:
                    ivVehicleIcon.setImageResource(R.drawable.ic_directions_car_24);
                    break;
            }

            // Rule name
            tvRuleName.setText(rule.getRuleName());

            // Active badge (always active because we filtered)
            tvActiveBadge.setVisibility(View.GONE);

            // Initial charge
            tvInitialCharge.setText(formatCurrency(rule.getInitialCharge()));
            tvInitialDuration.setText("(" + rule.getInitialDurationMinute() + " phút)");

            // Step rate
            tvStepRate.setText(formatCurrency(rule.getStepRate()));
            tvStepMinute.setText("(" + rule.getStepMinute() + " phút)");

            // Override pricing (promotion)
            if (rule.getOverridePricingRule() != null && rule.getOverridePricingRule().isActive()) {
                layoutOverride.setVisibility(View.VISIBLE);
                ParkingLotDetailResponse.OverridePricingRule override = rule.getOverridePricingRule();

                String overrideText = override.getRuleName() + ": " +
                        formatCurrency(override.getInitialCharge()) + " đầu (" +
                        override.getInitialDurationMinute() + " phút), " +
                        formatCurrency(override.getStepRate()) + "/bước (" +
                        override.getStepMinute() + " phút)";
                tvOverrideDetails.setText(overrideText);

                if (override.getValidUntil() != null && !override.getValidUntil().isEmpty()) {
                    try {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Date date = inputFormat.parse(override.getValidUntil());
                        tvOverrideValidity.setText("Có hiệu lực đến: " + outputFormat.format(date));
                    } catch (Exception e) {
                        tvOverrideValidity.setText("Có hiệu lực đến: " + override.getValidUntil());
                    }
                } else {
                    tvOverrideValidity.setText("Không giới hạn thời gian");
                }
            } else {
                layoutOverride.setVisibility(View.GONE);
            }
        }

        private String formatCurrency(double amount) {
            // Format as Vietnamese currency
            return String.format(Locale.getDefault(), "%,.0f₫", amount);
        }
    }
}

