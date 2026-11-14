package com.example.quanlichitieu;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Spinner spMonthFilter;
    private RadioGroup rgTypeFilter;
    private RadioButton rbExpense, rbIncome;
    private TextView tvTotalIncome, tvTotalExpense, tvBalance, tvTopCategoryInfo, tvChartTitle, tvEmptyStats;
    private PieChartView pieChartView;
    private RecyclerView rvCategoryStats;
    private BottomNavigationView bottomNav;

    private AppDatabase database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final List<TransactionEntity> allTransactions = new ArrayList<>();
    private final List<String> monthOptions = new ArrayList<>();
    private final List<CategoryStat> categoryStats = new ArrayList<>();
    private CategoryStatAdapter adapter;

    private String selectedMonthKey = "ALL"; // "ALL" or "YYYY-MM"
    private String selectedType = "Chi tiêu"; // "Chi tiêu" or "Thu nhập"

    // Vibrant color palette for categories
    private static final int[] PALETTE = new int[]{
            Color.parseColor("#FF5722"), // Deep Orange
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#FFC107"), // Amber
            Color.parseColor("#E91E63"), // Pink
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#795548"), // Brown
            Color.parseColor("#607D8B")  // Blue Grey
    };

    private static final Map<String, Integer> CATEGORY_COLOR_MAP = new HashMap<>();

    static {
        CATEGORY_COLOR_MAP.put("Ăn uống", Color.parseColor("#FF5722"));
        CATEGORY_COLOR_MAP.put("Học tập", Color.parseColor("#2196F3"));
        CATEGORY_COLOR_MAP.put("Đi lại", Color.parseColor("#4CAF50"));
        CATEGORY_COLOR_MAP.put("Lương", Color.parseColor("#9C27B0"));
        CATEGORY_COLOR_MAP.put("Thưởng", Color.parseColor("#FFC107"));
        CATEGORY_COLOR_MAP.put("Khác", Color.parseColor("#607D8B"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        database = AppDatabase.getDatabase(this);

        initViews();
        setupEvents();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        spMonthFilter = findViewById(R.id.spMonthFilter);
        rgTypeFilter = findViewById(R.id.rgTypeFilter);
        rbExpense = findViewById(R.id.rbExpense);
        rbIncome = findViewById(R.id.rbIncome);

        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvBalance = findViewById(R.id.tvBalance);
        tvTopCategoryInfo = findViewById(R.id.tvTopCategoryInfo);
        tvChartTitle = findViewById(R.id.tvChartTitle);
        tvEmptyStats = findViewById(R.id.tvEmptyStats);

        pieChartView = findViewById(R.id.pieChartView);
        rvCategoryStats = findViewById(R.id.rvCategoryStats);
        bottomNav = findViewById(R.id.bottomNav);

        rvCategoryStats.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryStatAdapter(categoryStats);
        rvCategoryStats.setAdapter(adapter);

        bottomNav.setSelectedItemId(R.id.nav_statistics);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> navigateToHome());

        rgTypeFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbExpense) {
                selectedType = "Chi tiêu";
                tvChartTitle.setText("Biểu đồ phân bổ chi tiêu");
            } else {
                selectedType = "Thu nhập";
                tvChartTitle.setText("Biểu đồ phân bổ thu nhập");
            }
            calculateAndDisplayStats();
        });

        spMonthFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedMonthKey = "ALL";
                } else if (position - 1 < monthOptions.size()) {
                    selectedMonthKey = monthOptions.get(position - 1);
                }
                calculateAndDisplayStats();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                navigateToHome();
                return true;
            }
            if (id == R.id.nav_transaction) {
                startActivity(new Intent(StatisticsActivity.this, TransactionListActivity.class));
                finish();
                return true;
            }
            if (id == R.id.nav_category) {
                startActivity(new Intent(StatisticsActivity.this, CategoryActivity.class));
                finish();
                return true;
            }
            return id == R.id.nav_statistics;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_statistics);
        }
        loadDataFromDatabase();
    }

    private void navigateToHome() {
        if (isTaskRoot()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        finish();
    }

    private void loadDataFromDatabase() {
        executor.execute(() -> {
            List<TransactionEntity> data = database.transactionDao().getAllTransactions();

            runOnUiThread(() -> {
                allTransactions.clear();
                allTransactions.addAll(data);

                setupMonthSpinner();
                calculateAndDisplayStats();
            });
        });
    }

    private void setupMonthSpinner() {
        Set<String> yearMonths = new LinkedHashSet<>();
        for (TransactionEntity t : allTransactions) {
            if (t.date != null && t.date.length() >= 7) {
                // date format "YYYY-MM-DD"
                String ym = t.date.substring(0, 7);
                yearMonths.add(ym);
            }
        }

        monthOptions.clear();
        monthOptions.addAll(yearMonths);
        Collections.sort(monthOptions, Collections.reverseOrder());

        List<String> displaySpinnerItems = new ArrayList<>();
        displaySpinnerItems.add("Tất cả các tháng");

        for (String ym : monthOptions) {
            String[] parts = ym.split("-");
            if (parts.length == 2) {
                displaySpinnerItems.add("Tháng " + parts[1] + "/" + parts[0]);
            } else {
                displaySpinnerItems.add(ym);
            }
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                displaySpinnerItems
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonthFilter.setAdapter(spinnerAdapter);
    }

    private void calculateAndDisplayStats() {
        double totalIncome = 0;
        double totalExpense = 0;

        Map<String, Double> categorySums = new HashMap<>();
        Map<String, Integer> categoryCounts = new HashMap<>();

        for (TransactionEntity t : allTransactions) {
            // Check month filter
            if (!"ALL".equals(selectedMonthKey)) {
                if (t.date == null || !t.date.startsWith(selectedMonthKey)) {
                    continue;
                }
            }

            if ("Thu nhập".equals(t.type)) {
                totalIncome += t.amount;
            } else {
                totalExpense += t.amount;
            }

            // Aggregate by category for selected type
            if (selectedType.equals(t.type)) {
                String cat = (t.category != null && !t.category.trim().isEmpty()) ? t.category.trim() : "Khác";
                categorySums.put(cat, categorySums.getOrDefault(cat, 0.0) + t.amount);
                categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0) + 1);
            }
        }

        double balance = totalIncome - totalExpense;

        tvTotalIncome.setText(formatMoney(totalIncome));
        tvTotalExpense.setText(formatMoney(totalExpense));
        tvBalance.setText(formatMoney(balance));

        double targetTotal = "Chi tiêu".equals(selectedType) ? totalExpense : totalIncome;

        categoryStats.clear();

        if (categorySums.isEmpty() || targetTotal <= 0) {
            tvEmptyStats.setVisibility(View.VISIBLE);
            rvCategoryStats.setVisibility(View.GONE);
            tvTopCategoryInfo.setText(selectedType + " nhiều nhất: Chưa có dữ liệu");
            pieChartView.setData(new ArrayList<>(), 0, "Tổng " + selectedType);
            adapter.notifyDataSetChanged();
            return;
        }

        tvEmptyStats.setVisibility(View.GONE);
        rvCategoryStats.setVisibility(View.VISIBLE);

        int paletteIndex = 0;
        for (Map.Entry<String, Double> entry : categorySums.entrySet()) {
            String cat = entry.getKey();
            double amt = entry.getValue();
            float percent = (float) ((amt / targetTotal) * 100f);
            int count = categoryCounts.getOrDefault(cat, 0);

            int color;
            if (CATEGORY_COLOR_MAP.containsKey(cat)) {
                color = CATEGORY_COLOR_MAP.get(cat);
            } else {
                color = PALETTE[paletteIndex % PALETTE.length];
                paletteIndex++;
            }

            categoryStats.add(new CategoryStat(cat, amt, percent, color, count, false));
        }

        // Sort descending by amount
        Collections.sort(categoryStats, (a, b) -> Double.compare(b.getAmount(), a.getAmount()));

        // Mark top category
        if (!categoryStats.isEmpty()) {
            CategoryStat topStat = categoryStats.get(0);
            topStat.setTop(true);
            String topText = (selectedType.equals("Chi tiêu") ? "Chi nhiều nhất: " : "Thu nhiều nhất: ")
                    + topStat.getCategory() + " (" + formatMoney(topStat.getAmount())
                    + " - " + String.format(Locale.getDefault(), "%.1f%%", topStat.getPercentage()) + ")";
            tvTopCategoryInfo.setText(topText);
        }

        adapter.notifyDataSetChanged();
        pieChartView.setData(categoryStats, targetTotal, "Tổng " + selectedType);
    }

    private String formatMoney(double amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " đ";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
