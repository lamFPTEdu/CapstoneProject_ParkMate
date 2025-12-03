package com.parkmate.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parkmate.android.R;
import com.parkmate.android.model.Transaction;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    public void updateData(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    public void addData(List<Transaction> newTransactions) {
        int oldSize = transactions.size();
        transactions.addAll(newTransactions);
        notifyItemRangeInserted(oldSize, newTransactions.size());
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivTransactionIcon;
        private TextView tvTransactionTitle;
        private TextView tvTransactionDate;
        private TextView tvTransactionAmount;
        private TextView tvTransactionStatus;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTransactionIcon = itemView.findViewById(R.id.ivTransactionIcon);
            tvTransactionTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvTransactionStatus = itemView.findViewById(R.id.tvTransactionStatus);
        }

        public void bind(Transaction transaction) {
            // Set icon and title based on transaction type
            if (transaction.isTopUp()) {
                ivTransactionIcon.setImageResource(R.drawable.ic_arrow_downward_24);
                ivTransactionIcon.setBackgroundResource(R.drawable.bg_image_placeholder);
                tvTransactionTitle.setText("Nạp tiền vào ví");
            } else if (transaction.isDeduction()) {
                ivTransactionIcon.setImageResource(R.drawable.ic_arrow_upward_24);
                ivTransactionIcon.setBackgroundResource(R.drawable.bg_image_placeholder);
                tvTransactionTitle.setText("Trừ tiền");
            } else if (transaction.isRefund()) {
                ivTransactionIcon.setImageResource(R.drawable.ic_arrow_downward_24);
                ivTransactionIcon.setBackgroundResource(R.drawable.bg_image_placeholder);
                tvTransactionTitle.setText("Hoàn tiền");
            } else if (transaction.isSubscriptionPayment()) {
                ivTransactionIcon.setImageResource(R.drawable.ic_arrow_upward_24);
                ivTransactionIcon.setBackgroundResource(R.drawable.bg_image_placeholder);
                tvTransactionTitle.setText("Thanh toán gói đăng ký");
            } else if (transaction.isReservationPayment()) {
                ivTransactionIcon.setImageResource(R.drawable.ic_arrow_upward_24);
                ivTransactionIcon.setBackgroundResource(R.drawable.bg_image_placeholder);
                tvTransactionTitle.setText("Thanh toán đặt chỗ");
            } else {
                ivTransactionIcon.setImageResource(R.drawable.ic_account_balance_wallet_24);
                tvTransactionTitle.setText(transaction.getDescription());
            }

            // Format date
            tvTransactionDate.setText(formatDate(transaction.getCreatedAt()));

            // Format amount with color
            String formattedAmount = formatCurrency(transaction.getAmount());
            // Nạp tiền và hoàn tiền là tiền vào (màu xanh, dấu +)
            if (transaction.isTopUp() || transaction.isRefund()) {
                tvTransactionAmount.setText("+" + formattedAmount);
                tvTransactionAmount.setTextColor(itemView.getContext().getColor(R.color.success_green));
            } else {
                // Trừ tiền, thanh toán subscription, thanh toán reservation là tiền ra (màu đỏ, dấu -)
                tvTransactionAmount.setText("-" + formattedAmount);
                tvTransactionAmount.setTextColor(itemView.getContext().getColor(R.color.error_red));
            }

            // Set status with color
            String statusText = getStatusText(transaction.getStatus());
            tvTransactionStatus.setText(statusText);

            if (transaction.isCompleted()) {
                tvTransactionStatus.setTextColor(itemView.getContext().getColor(R.color.success_green));
            } else if (transaction.isCancelled()) {
                tvTransactionStatus.setTextColor(itemView.getContext().getColor(R.color.error_red));
            } else {
                tvTransactionStatus.setTextColor(itemView.getContext().getColor(R.color.warning_yellow));
            }
        }

        private String formatCurrency(long amount) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(amount);
        }

        private String formatDate(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return date != null ? outputFormat.format(date) : dateString;
            } catch (ParseException e) {
                return dateString;
            }
        }

        private String getStatusText(String status) {
            switch (status) {
                case "COMPLETED":
                    return "Thành công";
                case "CANCELLED":
                    return "Đã hủy";
                case "PENDING":
                    return "Đang xử lý";
                case "FAILED":
                    return "Thất bại";
                default:
                    return status;
            }
        }
    }
}

