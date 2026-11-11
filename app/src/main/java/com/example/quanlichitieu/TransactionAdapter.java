package com.example.quanlichitieu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter
        extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(TransactionEntity transaction);
    }

    private final List<TransactionEntity> transactions;
    private OnItemClickListener onItemClickListener;

    public TransactionAdapter(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        TransactionEntity transaction = transactions.get(position);

        holder.tvCategory.setText(transaction.category);

        holder.tvNote.setText(
                transaction.note == null || transaction.note.isEmpty()
                        ? "Không có ghi chú"
                        : transaction.note
        );

        holder.tvDate.setText(transaction.date);

        String amount = NumberFormat.getNumberInstance(
                new Locale("vi", "VN")
        ).format(transaction.amount) + " đ";

        if ("Thu nhập".equals(transaction.type)) {
            holder.tvAmount.setText("+ " + amount);
            holder.tvAmount.setTextColor(
                    android.graphics.Color.rgb(24, 138, 98)
            );
        } else {
            holder.tvAmount.setText("- " + amount);
            holder.tvAmount.setTextColor(
                    android.graphics.Color.rgb(217, 87, 87)
            );
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvCategory, tvNote, tvDate, tvAmount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCategory = itemView.findViewById(
                    R.id.tvTransactionCategory
            );

            tvNote = itemView.findViewById(
                    R.id.tvTransactionNote
            );

            tvDate = itemView.findViewById(
                    R.id.tvTransactionDate
            );

            tvAmount = itemView.findViewById(
                    R.id.tvTransactionAmount
            );
        }
    }
}
