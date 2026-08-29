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
import java.util.Calendar;
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
        
        // Initial load
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
        List<Record> allRecords = account.getRecords();
        List<Record> filtered = new ArrayList<>();
        
        long now = System.currentTimeMillis();
        long startTime = 0;
        
        if (timeMode == 1) { // Last 7 Days
            startTime = now - (7L * 24 * 60 * 60 * 1000);
            tvDateRange.setText(context.getString(R.string.auto_last_7_days_26));
        } else if (timeMode == 2) { // Last 30 Days
            startTime = now - (30L * 24 * 60 * 60 * 1000);
            tvDateRange.setText(context.getString(R.string.auto_last_30_days_27));
        } else {
            tvDateRange.setText(context.getString(R.string.auto_all_time_28));
        }

        double totalAmount = 0;
        double highestTxn = 0;
        
        java.util.TreeMap<Long, Double> dailyTotals = new java.util.TreeMap<>();

        for (Record r : allRecords) {
            long rTs = 0;
            try {
                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                java.util.Date parsedDate = format.parse(r.getDate());
                if (parsedDate != null) rTs = parsedDate.getTime();
            } catch(Exception ex) {
                android.util.Log.e("NoteCalc", "Error parsing date", ex);
            }

            if (rTs >= startTime) {
                filtered.add(r);
                double amt = r.getAmount();
                totalAmount += amt;
                if (amt > highestTxn) highestTxn = amt;
                
                Calendar c = Calendar.getInstance();
                if (rTs > 0) {
                    c.setTimeInMillis(rTs);
                } else {
                    c.setTimeInMillis(System.currentTimeMillis());
                }
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                long dayStart = c.getTimeInMillis();
                
                Double currentDayTotal = dailyTotals.get(dayStart);
                double totalSoFar = (currentDayTotal != null) ? currentDayTotal : 0.0;
                dailyTotals.put(dayStart, totalSoFar + amt);
            }
        }

        long highDayTs = 0;
        double highDayAmt = 0;
        for (java.util.Map.Entry<Long, Double> entry : dailyTotals.entrySet()) {
            if (entry.getValue() > highDayAmt) {
                highDayAmt = entry.getValue();
                highDayTs = entry.getKey();
            }
        }

        int days = dailyTotals.size();
        double dailyAvg = days > 0 ? (totalAmount / days) : 0;

        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance();
        tvTotal.setText(nf.format(totalAmount));
        tvHighTxn.setText(nf.format(highestTxn));
        tvDailyAvg.setText(nf.format(dailyAvg));
        
        if (highDayTs > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
            tvHighDay.setText(context.getString(R.string.analytics_high_day, sdf.format(new java.util.Date(highDayTs)), nf.format(highDayAmt)));
        } else {
            tvHighDay.setText(context.getString(R.string.auto_none_29));
        }
        
        if (account.hasBudget()) {
            tvBudgetPct.setVisibility(View.VISIBLE);
            double budget = account.calculateTotalBudget();
            if (budget > 0) {
                double expenses = 0;
                for (Record r : filtered) {
                    expenses += r.getAmount();
                }
                double pct = (expenses / budget) * 100.0;
                tvBudgetPct.setText(String.format(java.util.Locale.getDefault(), "%.1f%% of budget spent", pct));
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
        
        for (java.util.Map.Entry<Long, Double> entry : dailyTotals.entrySet()) {
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
