package com.example.quanlichitieu;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText edtCategoryName;
    private RadioGroup rgCategoryType;
    private Button btnAddCategory;
    private RecyclerView rvCategories;
    private TextView tvEmptyCategories;

    private AppDatabase database;
    private CategoryAdapter adapter;
    private final List<CategoryEntity> categoryList = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        database = AppDatabase.getDatabase(this);

        initViews();
        setupEvents();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtCategoryName = findViewById(R.id.edtCategoryName);
        rgCategoryType = findViewById(R.id.rgCategoryType);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        rvCategories = findViewById(R.id.rvCategories);
        tvEmptyCategories = findViewById(R.id.tvEmptyCategories);

        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(categoryList);
        rvCategories.setAdapter(adapter);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        btnAddCategory.setOnClickListener(v -> addCategory());

        adapter.setOnCategoryDeleteListener(this::confirmDeleteCategory);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }

    private void loadCategories() {
        executor.execute(() -> {
            CategoryDao dao = database.categoryDao();
            List<CategoryEntity> list = dao.getAllCategories();
            if (list.isEmpty()) {
                dao.insert(new CategoryEntity("Ăn uống", "Chi tiêu"));
                dao.insert(new CategoryEntity("Học tập", "Chi tiêu"));
                dao.insert(new CategoryEntity("Đi lại", "Chi tiêu"));
                dao.insert(new CategoryEntity("Lương", "Thu nhập"));
                dao.insert(new CategoryEntity("Thưởng", "Thu nhập"));
                dao.insert(new CategoryEntity("Khác", "Chi tiêu"));
                list = dao.getAllCategories();
            }

            final List<CategoryEntity> finalResult = list;
            runOnUiThread(() -> {
                categoryList.clear();
                categoryList.addAll(finalResult);
                adapter.notifyDataSetChanged();

                tvEmptyCategories.setVisibility(categoryList.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void addCategory() {
        String name = edtCategoryName.getText().toString().trim();
        if (name.isEmpty()) {
            edtCategoryName.setError("Vui lòng nhập tên danh mục");
            Toast.makeText(this, "Vui lòng nhập tên danh mục!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check duplicate
        for (CategoryEntity cat : categoryList) {
            if (cat.name != null && cat.name.trim().equalsIgnoreCase(name)) {
                edtCategoryName.setError("Tên danh mục này đã tồn tại");
                Toast.makeText(this, "Tên danh mục '" + name + "' đã tồn tại!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String type = (rgCategoryType.getCheckedRadioButtonId() == R.id.rbIncome)
                ? "Thu nhập"
                : "Chi tiêu";

        btnAddCategory.setEnabled(false);

        executor.execute(() -> {
            try {
                CategoryEntity newCategory = new CategoryEntity(name, type);
                database.categoryDao().insert(newCategory);

                runOnUiThread(() -> {
                    btnAddCategory.setEnabled(true);
                    edtCategoryName.setText("");
                    Toast.makeText(this, "Thêm danh mục thành công!", Toast.LENGTH_SHORT).show();
                    loadCategories();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnAddCategory.setEnabled(true);
                    Toast.makeText(this, "Lỗi khi thêm danh mục: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void confirmDeleteCategory(CategoryEntity category) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa danh mục '" + category.name + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCategory(category))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteCategory(CategoryEntity category) {
        executor.execute(() -> {
            try {
                database.categoryDao().delete(category);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã xóa danh mục thành công!", Toast.LENGTH_SHORT).show();
                    loadCategories();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi xóa danh mục: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
