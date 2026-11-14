package com.example.quanlichitieu;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTransactionActivity extends AppCompatActivity {

    private TextView tvTitle;
    private EditText edtAmount, edtNote;
    private RadioGroup rgType;
    private Spinner spCategory;
    private Button btnDate, btnSave, btnDelete;

    private String selectedDate;
    private int transactionId = -1;
    private TransactionEntity currentTransaction;

    private AppDatabase database;
    private ArrayAdapter<String> categoryAdapter;
    private final List<String> categoryList = new ArrayList<>();

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        database = AppDatabase.getDatabase(this);

        tvTitle = findViewById(R.id.tvAddTransactionTitle);
        edtAmount = findViewById(R.id.edtAmount);
        edtNote = findViewById(R.id.edtNote);
        rgType = findViewById(R.id.rgType);
        spCategory = findViewById(R.id.spCategory);
        btnDate = findViewById(R.id.btnDate);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryList
        );

        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spCategory.setAdapter(categoryAdapter);

        selectedDate = getCurrentDate();
        btnDate.setText(selectedDate);

        btnDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveTransaction());
        btnDelete.setOnClickListener(v -> confirmDeleteTransaction());

        if (getIntent().hasExtra("transaction_id")) {
            transactionId = getIntent().getIntExtra("transaction_id", -1);
        }

        if (transactionId != -1) {
            tvTitle.setText("Sửa giao dịch");
            btnSave.setText("Cập nhật giao dịch");
            btnDelete.setVisibility(View.VISIBLE);
            loadTransactionForEdit();
        }

        loadCategoriesFromDatabase();
    }

    private void loadCategoriesFromDatabase() {
        executor.execute(() -> {
            CategoryDao dao = database.categoryDao();
            List<CategoryEntity> entities = dao.getAllCategories();
            if (entities.isEmpty()) {
                dao.insert(new CategoryEntity("Ăn uống", "Chi tiêu"));
                dao.insert(new CategoryEntity("Học tập", "Chi tiêu"));
                dao.insert(new CategoryEntity("Đi lại", "Chi tiêu"));
                dao.insert(new CategoryEntity("Lương", "Thu nhập"));
                dao.insert(new CategoryEntity("Thưởng", "Thu nhập"));
                dao.insert(new CategoryEntity("Khác", "Chi tiêu"));
                entities = dao.getAllCategories();
            }

            List<String> names = new ArrayList<>();
            for (CategoryEntity cat : entities) {
                names.add(cat.name);
            }

            runOnUiThread(() -> {
                categoryList.clear();
                categoryList.addAll(names);
                categoryAdapter.notifyDataSetChanged();
                populateCategorySelection();
            });
        });
    }

    private String getCurrentDate() {
        return new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(Calendar.getInstance().getTime());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {

                    selectedDate = String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                    );

                    btnDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void loadTransactionForEdit() {
        executor.execute(() -> {
            currentTransaction = database.transactionDao().getTransactionById(transactionId);
            if (currentTransaction != null) {
                runOnUiThread(this::populateTransactionFields);
            }
        });
    }

    private void populateTransactionFields() {
        if (currentTransaction == null) return;

        double amount = currentTransaction.amount;
        if (amount % 1 == 0) {
            edtAmount.setText(String.format(Locale.US, "%.0f", amount));
        } else {
            edtAmount.setText(String.valueOf(amount));
        }

        if ("Thu nhập".equals(currentTransaction.type)) {
            rgType.check(R.id.rbIncome);
        } else {
            rgType.check(R.id.rbExpense);
        }

        populateCategorySelection();

        if (currentTransaction.date != null && !currentTransaction.date.isEmpty()) {
            selectedDate = currentTransaction.date;
            btnDate.setText(selectedDate);
        }

        if (currentTransaction.note != null) {
            edtNote.setText(currentTransaction.note);
        }
    }

    private void populateCategorySelection() {
        if (currentTransaction != null && currentTransaction.category != null) {
            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).equalsIgnoreCase(currentTransaction.category)) {
                    spCategory.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveTransaction() {

        String amountText =
                edtAmount.getText().toString().trim();

        if (amountText.isEmpty()) {
            edtAmount.setError("Vui lòng nhập số tiền");
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(
                    amountText.replace(",", ".")
            );
        } catch (NumberFormatException e) {
            edtAmount.setError("Số tiền không hợp lệ");
            return;
        }

        if (amount <= 0) {
            edtAmount.setError("Số tiền phải lớn hơn 0");
            return;
        }

        String type;

        if (rgType.getCheckedRadioButtonId() == R.id.rbIncome) {
            type = "Thu nhập";
        } else {
            type = "Chi tiêu";
        }

        String category = (spCategory.getSelectedItem() != null)
                ? spCategory.getSelectedItem().toString()
                : "Khác";

        String note = edtNote.getText().toString().trim();

        btnSave.setEnabled(false);

        executor.execute(() -> {
            try {
                if (transactionId != -1 && currentTransaction != null) {
                    currentTransaction.amount = amount;
                    currentTransaction.type = type;
                    currentTransaction.category = category;
                    currentTransaction.note = note;
                    currentTransaction.date = selectedDate;

                    database.transactionDao().update(currentTransaction);

                    runOnUiThread(() -> {
                        Toast.makeText(
                                this,
                                "Cập nhật giao dịch thành công!",
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                    });

                } else {
                    TransactionEntity transaction = new TransactionEntity(
                            amount,
                            type,
                            category,
                            note,
                            selectedDate
                    );

                    database.transactionDao().insert(transaction);

                    runOnUiThread(() -> {
                        Toast.makeText(
                                this,
                                "Lưu giao dịch thành công!",
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                    });
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSave.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Lỗi khi lưu giao dịch: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void confirmDeleteTransaction() {
        if (currentTransaction == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch này không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteTransaction())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteTransaction() {
        if (currentTransaction == null) return;

        btnDelete.setEnabled(false);
        executor.execute(() -> {
            try {
                database.transactionDao().delete(currentTransaction);

                runOnUiThread(() -> {
                    Toast.makeText(
                            this,
                            "Đã xóa giao dịch thành công!",
                            Toast.LENGTH_SHORT
                    ).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnDelete.setEnabled(true);
                    Toast.makeText(
                            this,
                            "Lỗi khi xóa giao dịch: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
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
