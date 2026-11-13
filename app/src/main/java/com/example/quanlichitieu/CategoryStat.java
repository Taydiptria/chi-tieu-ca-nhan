package com.example.quanlichitieu;

public class CategoryStat {
    private String category;
    private double amount;
    private float percentage;
    private int color;
    private int count;
    private boolean isTop;

    public CategoryStat(String category, double amount, float percentage, int color, int count, boolean isTop) {
        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
        this.color = color;
        this.count = count;
        this.isTop = isTop;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public float getPercentage() {
        return percentage;
    }

    public int getColor() {
        return color;
    }

    public int getCount() {
        return count;
    }

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean top) {
        isTop = top;
    }
}
