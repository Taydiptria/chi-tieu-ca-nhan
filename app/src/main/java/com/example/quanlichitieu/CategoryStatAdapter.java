package com.example.quanlichitieu;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CategoryStatAdapter extends RecyclerView.Adapter<CategoryStatAdapter.ViewHolder> {

    private final List<CategoryStat> statList;

    public CategoryStatAdapter(List<CategoryStat> statList) {
        this.statList = statList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryStat item = statList.get(position);

        holder.tvCategoryName.setText(item.getCategory());
        holder.tvCategoryAmount.setText(formatMoney(item.getAmount()));
        holder.tvCategoryPercent.setText(String.format(Locale.getDefault(), "%.1f%%", item.getPercentage()));
        holder.tvCategoryCount.setText(item.getCount() + " giao dịch");

        // Set color indicator dot rounded
        GradientDrawable dotDrawable = new GradientDrawable();
        dotDrawable.setShape(GradientDrawable.OVAL);
        dotDrawable.setColor(item.getColor());
        holder.vColorIndicator.setBackground(dotDrawable);

        // Progress bar
        holder.pbCategory.setProgress((int) item.getPercentage());
        holder.pbCategory.setProgressTintList(ColorStateList.valueOf(item.getColor()));

        // Highlight top spending category
        if (item.isTop()) {
            holder.tvTopBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvTopBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return statList != null ? statList.size() : 0;
    }

    private String formatMoney(double amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " đ";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View vColorIndicator;
        TextView tvCategoryName, tvTopBadge, tvCategoryPercent, tvCategoryAmount, tvCategoryCount;
        ProgressBar pbCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vColorIndicator = itemView.findViewById(R.id.vColorIndicator);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvTopBadge = itemView.findViewById(R.id.tvTopBadge);
            tvCategoryPercent = itemView.findViewById(R.id.tvCategoryPercent);
            tvCategoryAmount = itemView.findViewById(R.id.tvCategoryAmount);
            tvCategoryCount = itemView.findViewById(R.id.tvCategoryCount);
            pbCategory = itemView.findViewById(R.id.pbCategory);
        }
    }
}
