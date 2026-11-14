package com.example.quanlichitieu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryDeleteListener {
        void onDelete(CategoryEntity category);
    }

    private final List<CategoryEntity> categoryList;
    private OnCategoryDeleteListener deleteListener;

    public CategoryAdapter(List<CategoryEntity> categoryList) {
        this.categoryList = categoryList;
    }

    public void setOnCategoryDeleteListener(OnCategoryDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryEntity category = categoryList.get(position);
        holder.tvName.setText(category.name);
        holder.tvType.setText("Loại: " + category.type);

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvType;
        ImageButton btnDelete;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvType = itemView.findViewById(R.id.tvCategoryType);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}
