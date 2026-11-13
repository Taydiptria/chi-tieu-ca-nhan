package com.example.quanlichitieu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PieChartView extends View {

    private final Paint slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textAmountPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF sliceBounds = new RectF();

    private List<CategoryStat> statList = new ArrayList<>();
    private double totalAmount = 0;
    private String centerTitle = "Tổng chi tiêu";

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        slicePaint.setStyle(Paint.Style.FILL);

        centerPaint.setColor(Color.WHITE);
        centerPaint.setStyle(Paint.Style.FILL);

        textTitlePaint.setColor(Color.parseColor("#7B8494"));
        textTitlePaint.setTextSize(36f);
        textTitlePaint.setTextAlign(Paint.Align.CENTER);

        textAmountPaint.setColor(Color.parseColor("#172B4D"));
        textAmountPaint.setTextSize(44f);
        textAmountPaint.setFakeBoldText(true);
        textAmountPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<CategoryStat> data, double total, String title) {
        this.statList = data != null ? data : new ArrayList<>();
        this.totalAmount = total;
        this.centerTitle = title != null ? title : "Tổng";
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        int minDimension = Math.min(width, height);
        float radius = minDimension / 2f - 20f;
        float centerX = width / 2f;
        float centerY = height / 2f;

        sliceBounds.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        if (statList.isEmpty() || totalAmount <= 0) {
            // Draw default empty circle
            slicePaint.setColor(Color.parseColor("#E2E8F0"));
            canvas.drawCircle(centerX, centerY, radius, slicePaint);
        } else {
            float startAngle = -90f;
            for (CategoryStat stat : statList) {
                float sweepAngle = (float) ((stat.getAmount() / totalAmount) * 360f);
                if (sweepAngle <= 0) continue;

                slicePaint.setColor(stat.getColor());
                canvas.drawArc(sliceBounds, startAngle, sweepAngle, true, slicePaint);
                startAngle += sweepAngle;
            }
        }

        // Inner hole for Donut effect
        float innerRadius = radius * 0.65f;
        canvas.drawCircle(centerX, centerY, innerRadius, centerPaint);

        // Text inside Donut
        float titleY = centerY - 10f;
        float amountY = centerY + 35f;

        canvas.drawText(centerTitle, centerX, titleY, textTitlePaint);

        String formattedAmount = formatMoneyShort(totalAmount);
        canvas.drawText(formattedAmount, centerX, amountY, textAmountPaint);
    }

    private String formatMoneyShort(double amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " đ";
    }
}
