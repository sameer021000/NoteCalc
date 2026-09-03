package com.example.notecalc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsHelper {
    public static void showAnalytics(Context context, Account account, ViewGroup mainContainer, Runnable onBackClicked) {
        View analyticsRoot = LayoutInflater.from(context).inflate(R.layout.layout_analytics, mainContainer, false);
        mainContainer.removeAllViews();
        mainContainer.addView(analyticsRoot);

        android.widget.ImageButton btnBack = analyticsRoot.findViewById(R.id.btn_analytics_back);
        ResponsiveUI.setupClickable(btnBack, true, onBackClicked);

        TextView tvTitle = analyticsRoot.findViewById(R.id.tv_analytics_title);
        tvTitle.setText(context.getString(R.string.analytics_title, account.getTitle()));

        TextView tvTotalSpent = analyticsRoot.findViewById(R.id.tv_total_spent);
        TextView tvHighestTxn = analyticsRoot.findViewById(R.id.tv_highest_txn);
        TextView tvDailyAvg = analyticsRoot.findViewById(R.id.tv_daily_avg);
        TextView tvHighestDay = analyticsRoot.findViewById(R.id.tv_highest_day);
        TextView tvBudgetPercent = analyticsRoot.findViewById(R.id.tv_budget_percent);
        TextView tvDateRange = analyticsRoot.findViewById(R.id.tv_date_range);

        Spinner spinnerTimeframe = analyticsRoot.findViewById(R.id.spinner_timeframe);
        String[] options = {"All Time", "Last 7 Days", "Last 30 Days"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeframe.setAdapter(adapter);

        BarChart chart = analyticsRoot.findViewById(R.id.chart_spending);
        setupChartAppearance(context, chart);

        spinnerTimeframe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateAnalyticsData(context, account, position, chart, tvTotalSpent, tvHighestTxn, tvDailyAvg, tvHighestDay, tvBudgetPercent, tvDateRange);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        updateAnalyticsData(context, account, 0, chart, tvTotalSpent, tvHighestTxn, tvDailyAvg, tvHighestDay, tvBudgetPercent, tvDateRange);
    }

    private static void setupChartAppearance(Context context, BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);
        
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ThemeManager.getSecondaryAccentColor(context));
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(ThemeManager.getSecondaryAccentColor(context));
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(ThemeManager.getBorderColor(context));
        leftAxis.setAxisMinimum(0f);

        chart.getAxisRight().setEnabled(false);
    }

    private static void updateAnalyticsData(Context context, Account account, int timeMode, BarChart chart, TextView tvTotal, TextView tvHighTxn, TextView tvDailyAvg, TextView tvHighDay, TextView tvBudgetPct, TextView tvDateRange) {
        AnalyticsResult result = AnalyticsEngine.calculate(account, timeMode);
        
        tvDateRange.setText(context.getString(result.dateRangeStringResId));

        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance();
        tvTotal.setText(nf.format(result.totalAmount));
        tvHighTxn.setText(nf.format(result.highestTxn));
        tvDailyAvg.setText(nf.format(result.dailyAvg));
        
        if (result.highDayTs > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
            tvHighDay.setText(context.getString(R.string.analytics_high_day, sdf.format(new java.util.Date(result.highDayTs)), nf.format(result.highDayAmt)));
        } else {
            tvHighDay.setText(context.getString(R.string.auto_none_29));
        }
        
        if (result.budgetPct != -1.0) {
            tvBudgetPct.setVisibility(View.VISIBLE);
            if (result.budgetPct > 0) {
                tvBudgetPct.setText(String.format(java.util.Locale.getDefault(), "%.1f%% of budget spent", result.budgetPct));
            } else {
                tvBudgetPct.setText(context.getString(R.string.auto_0_of_budget_spent_30));
            }
        } else {
            tvBudgetPct.setVisibility(View.GONE);
        }

        List<BarEntry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        int i = 0;
        java.text.SimpleDateFormat sdfShort = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault());
        
        for (java.util.Map.Entry<Long, Double> entry : result.dailyTotals.entrySet()) {
            entries.add(new BarEntry(i, entry.getValue().floatValue()));
            labels.add(sdfShort.format(new java.util.Date(entry.getKey())));
            i++;
        }

        if (entries.isEmpty()) {
            chart.clear();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Spending");
        dataSet.setColor(ThemeManager.getPrimaryAccentColor(context));
        dataSet.setValueTextColor(ThemeManager.getSecondaryAccentColor(context));
        dataSet.setValueTextSize(10f);
        
        BarData barData = new BarData(dataSet);
        chart.setData(barData);
        
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) return labels.get(index);
                return "";
            }
        });
        
        chart.invalidate();
        chart.animateY(800);
    }
}
