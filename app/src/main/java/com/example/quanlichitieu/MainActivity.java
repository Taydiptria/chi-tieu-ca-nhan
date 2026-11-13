package com.example.quanlichitieu;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView tvBalance, tvIncome, tvExpense, tvEmpty;
    private EditText edtSearchTransaction;
    private RecyclerView rvRecentTransactions;
    private BottomNavigationView bottomNav;

    private AppDatabase database;
    private TransactionAdapter adapter;
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final List<TransactionEntity> transactions = new ArrayList<>();
    private final List<TransactionEntity> allTransactionsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = AppDatabase.getDatabase(this);

        tvBalance = findViewById(R.id.tvBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvEmpty = findViewById(R.id.tvEmpty);
        edtSearchTransaction = findViewById(R.id.edtSearchTransaction);

        edtSearchTransaction.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTransactions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        rvRecentTransactions =
                findViewById(R.id.rvRecentTransactions);

        rvRecentTransactions.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new TransactionAdapter(transactions);
        adapter.setOnItemClickListener(transaction -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            intent.putExtra("transaction_id", transaction.id);
            startActivity(intent);
        });
        rvRecentTransactions.setAdapter(adapter);

        // NÚT THÊM GIAO DỊCH
        Button btnAddTransaction = findViewById(R.id.btnAddTransaction);

        btnAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddTransactionActivity.class
            );
            startActivity(intent);
        });

// THANH ĐIỀU HƯỚNG
        bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_transaction) {
                startActivity(new Intent(
                        MainActivity.this,
                        TransactionListActivity.class
                ));
                return true;
            }

            if (id == R.id.nav_statistics) {
                startActivity(new Intent(
                        MainActivity.this,
                        StatisticsActivity.class
                ));
                return true;
            }

            if (id == R.id.nav_home) {
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
        loadTransactions();
    }

    private void loadTransactions() {

        executor.execute(() -> {

            List<TransactionEntity> allTransactions =
                    database.transactionDao()
                            .getAllTransactions();

            double totalIncome = 0;
            double totalExpense = 0;

            for (TransactionEntity transaction : allTransactions) {

                if ("Thu nhập".equals(transaction.type)) {
                    totalIncome += transaction.amount;
                } else {
                    totalExpense += transaction.amount;
                }
            }

            double balance = totalIncome - totalExpense;
            final double income = totalIncome;
            final double expense = totalExpense;
            final double balanceFinal = balance;
            runOnUiThread(() -> {

                transactions.clear();

                allTransactionsList.clear();
                allTransactionsList.addAll(allTransactions);

                filterTransactions(edtSearchTransaction.getText().toString());

                adapter.notifyDataSetChanged();

                tvIncome.setText(formatMoney(income));
                tvExpense.setText(formatMoney(expense));
                tvBalance.setText(formatMoney(balanceFinal));

                tvEmpty.setVisibility(
                        transactions.isEmpty()
                                ? TextView.VISIBLE
                                : TextView.GONE
                );
            });
        });
    }
    private void filterTransactions(String keyword) {
        String searchText = keyword.trim().toLowerCase(Locale.ROOT);

        transactions.clear();

        for (TransactionEntity transaction : allTransactionsList) {
            String category = transaction.category == null
                    ? ""
                    : transaction.category.toLowerCase(Locale.ROOT);

            String note = transaction.note == null
                    ? ""
                    : transaction.note.toLowerCase(Locale.ROOT);

            if (category.contains(searchText) || note.contains(searchText)) {
                transactions.add(transaction);
            }
        }

        // Chỉ hiển thị tối đa 5 giao dịch gần nhất.
        while (transactions.size() > 5) {
            transactions.remove(transactions.size() - 1);
        }

        adapter.notifyDataSetChanged();

        tvEmpty.setText(searchText.isEmpty()
                ? "Chưa có giao dịch nào"
                : "Không tìm thấy giao dịch phù hợp");

        tvEmpty.setVisibility(
                transactions.isEmpty() ? TextView.VISIBLE : TextView.GONE
        );
    }
    private String formatMoney(double amount) {

        return NumberFormat.getNumberInstance(
                new Locale("vi", "VN")
        ).format(amount) + " đ";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
