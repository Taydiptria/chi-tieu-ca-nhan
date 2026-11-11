package com.example.quanlichitieu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionListActivity extends AppCompatActivity {

    private RecyclerView rvTransactions;
    private TextView tvEmpty;
    private EditText edtSearchTransaction;
    private ImageButton btnBack;
    private BottomNavigationView bottomNav;
    private TransactionAdapter adapter;

    private final List<TransactionEntity> transactions = new ArrayList<>();
    private final List<TransactionEntity> allTransactionsList = new ArrayList<>();
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        database = AppDatabase.getDatabase(this);

        rvTransactions = findViewById(R.id.rvTransactions);
        tvEmpty = findViewById(R.id.tvEmptyTransactions);
        edtSearchTransaction = findViewById(R.id.edtSearchTransaction);
        btnBack = findViewById(R.id.btnBack);
        bottomNav = findViewById(R.id.bottomNav);

        btnBack.setOnClickListener(v -> navigateToHome());

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

        bottomNav.setSelectedItemId(R.id.nav_transaction);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                navigateToHome();
                return true;
            }

            return id == R.id.nav_transaction;
        });

        rvTransactions.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new TransactionAdapter(transactions);
        adapter.setOnItemClickListener(transaction -> {
            Intent intent = new Intent(TransactionListActivity.this, AddTransactionActivity.class);
            intent.putExtra("transaction_id", transaction.id);
            startActivity(intent);
        });
        rvTransactions.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions();
    }

    private void navigateToHome() {
        if (isTaskRoot()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        finish();
    }

    private void loadTransactions() {
        executor.execute(() -> {
            List<TransactionEntity> data =
                    database.transactionDao().getAllTransactions();

            runOnUiThread(() -> {
                allTransactionsList.clear();
                allTransactionsList.addAll(data);

                filterTransactions(edtSearchTransaction.getText().toString());
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

        adapter.notifyDataSetChanged();

        tvEmpty.setText(searchText.isEmpty()
                ? "Chưa có giao dịch nào"
                : "Không tìm thấy giao dịch phù hợp");

        tvEmpty.setVisibility(
                transactions.isEmpty() ? View.VISIBLE : View.GONE
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
