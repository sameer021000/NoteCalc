import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Add imports
imports = '''import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.Calendar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
'''
if "import com.github.mikephil.charting.charts.BarChart;" not in content:
    content = content.replace('import androidx.appcompat.app.AppCompatActivity;', 'import androidx.appcompat.app.AppCompatActivity;\n' + imports)

# Bind btnAnalytics in showEditor
editor_bind = '''        ImageView btnAnalytics = editorRoot.findViewById(R.id.btn_analytics);
        if (btnAnalytics != null) {
            setupClickable(btnAnalytics, true, () -> showAnalytics(currentEditingAccount));
        }'''
if "btnAnalytics" not in content:
    content = content.replace('ImageView btnBack = editorRoot.findViewById(R.id.btn_back);', 'ImageView btnBack = editorRoot.findViewById(R.id.btn_back);\n' + editor_bind)


# Add showAnalytics method
analytics_method = '''
    private void showAnalytics(Account account) {
        View analyticsRoot = getLayoutInflater().inflate(R.layout.layout_analytics, mainContainer, false);
        mainContainer.removeAllViews();
        mainContainer.addView(analyticsRoot);

        ImageButton btnBack = analyticsRoot.findViewById(R.id.btn_analytics_back);
        setupClickable(btnBack, true, () -> showEditor(account));

        TextView tvTitle = analyticsRoot.findViewById(R.id.tv_analytics_title);
        tvTitle.setText(account.getTitle() + " Analytics");

        TextView tvTotalSpent = analyticsRoot.findViewById(R.id.tv_total_spent);
        TextView tvHighestTxn = analyticsRoot.findViewById(R.id.tv_highest_txn);
        TextView tvDailyAvg = analyticsRoot.findViewById(R.id.tv_daily_avg);
        TextView tvHighestDay = analyticsRoot.findViewById(R.id.tv_highest_day);
        TextView tvBudgetPercent = analyticsRoot.findViewById(R.id.tv_budget_percent);
        TextView tvDateRange = analyticsRoot.findViewById(R.id.tv_date_range);

        Spinner spinnerTimeframe = analyticsRoot.findViewById(R.id.spinner_timeframe);
        String[] options = {"All Time", "Last 7 Days", "Last 30 Days"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeframe.setAdapter(adapter);

        BarChart chart = analyticsRoot.findViewById(R.id.chart_spending);
        setupChartAppearance(chart);

        spinnerTimeframe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateAnalyticsData(account, position, chart, tvTotalSpent, tvHighestTxn, tvDailyAvg, tvHighestDay, tvBudgetPercent, tvDateRange);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Initial load
        updateAnalyticsData(account, 0, chart, tvTotalSpent, tvHighestTxn, tvDailyAvg, tvHighestDay, tvBudgetPercent, tvDateRange);
    }

    private void setupChartAppearance(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);
        
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ThemeManager.getSecondaryAccentColor(this));
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(ThemeManager.getSecondaryAccentColor(this));
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(ThemeManager.getBorderColor(this));
        leftAxis.setAxisMinimum(0f);

        chart.getAxisRight().setEnabled(false);
    }

    private void updateAnalyticsData(Account account, int timeMode, BarChart chart, TextView tvTotal, TextView tvHighTxn, TextView tvDailyAvg, TextView tvHighDay, TextView tvBudgetPct, TextView tvDateRange) {
        List<Record> allRecords = account.getRecords();
        List<Record> filtered = new ArrayList<>();
        
        long now = System.currentTimeMillis();
        long startTime = 0;
        
        if (timeMode == 1) { // Last 7 Days
            startTime = now - (7L * 24 * 60 * 60 * 1000);
            tvDateRange.setText("Last 7 Days");
        } else if (timeMode == 2) { // Last 30 Days
            startTime = now - (30L * 24 * 60 * 60 * 1000);
            tvDateRange.setText("Last 30 Days");
        } else {
            tvDateRange.setText("All Time");
        }

        double totalAmount = 0;
        double highestTxn = 0;
        
        // Group by day for chart
        // Map: Day start timestamp -> total amount
        java.util.TreeMap<Long, Double> dailyTotals = new java.util.TreeMap<>();

        for (Record r : allRecords) {
            if (r.getTimestamp() >= startTime) {
                filtered.add(r);
                double amt = Double.parseDouble(r.getAmount());
                totalAmount += amt;
                if (amt > highestTxn) highestTxn = amt;
                
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(r.getTimestamp());
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                long dayStart = c.getTimeInMillis();
                
                dailyTotals.put(dayStart, dailyTotals.getOrDefault(dayStart, 0.0) + amt);
            }
        }

        // High day
        long highDayTs = 0;
        double highDayAmt = 0;
        for (java.util.Map.Entry<Long, Double> entry : dailyTotals.entrySet()) {
            if (entry.getValue() > highDayAmt) {
                highDayAmt = entry.getValue();
                highDayTs = entry.getKey();
            }
        }

        // Daily avg
        int days = dailyTotals.size();
        double dailyAvg = days > 0 ? (totalAmount / days) : 0;

        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance();
        tvTotal.setText(nf.format(totalAmount));
        tvHighTxn.setText(nf.format(highestTxn));
        tvDailyAvg.setText(nf.format(dailyAvg));
        
        if (highDayTs > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
            tvHighDay.setText(sdf.format(new java.util.Date(highDayTs)) + " (" + nf.format(highDayAmt) + ")");
        } else {
            tvHighDay.setText("None");
        }
        
        if (account.isBudgetMode()) {
            tvBudgetPct.setVisibility(View.VISIBLE);
            // Budget percentage (we don't have a specific budget limit field in Account yet, but we have Remaining Purse?)
            // Wait, noteCalc uses 'Records' as transactions. Does Account have a purse limit?
            // "if a list/account has budget, then user should be able to see the percentage"
            // Wait, NoteCalc doesn't have a "budget limit" set per account. It calculates Remaining Purse by subtracting expenses from the first positive entry?
            // Actually, in Budget mode, the first item is the Budget, and rest are expenses.
            if (allRecords.size() > 0) {
                double budget = Double.parseDouble(allRecords.get(0).getAmount());
                if (budget > 0) {
                    // Total expenses exclude the first record
                    double expenses = 0;
                    for (int i = 1; i < filtered.size(); i++) {
                        expenses += Double.parseDouble(filtered.get(i).getAmount());
                    }
                    double pct = (expenses / budget) * 100.0;
                    tvBudgetPct.setText(String.format(java.util.Locale.getDefault(), "%.1f%% of budget spent", pct));
                } else {
                    tvBudgetPct.setText("0% of budget spent");
                }
            }
        } else {
            tvBudgetPct.setVisibility(View.GONE);
        }

        // Populate Chart
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
        dataSet.setColor(ThemeManager.getPrimaryAccentColor(this));
        dataSet.setValueTextColor(ThemeManager.getSecondaryAccentColor(this));
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
'''

if "private void showAnalytics(Account account)" not in content:
    # Add it before the end of class
    content = content.replace('    // =============== ACTIVITY LIFECYCLE ===============', analytics_method + '\n    // =============== ACTIVITY LIFECYCLE ===============')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Added Analytics logic to MainActivity.java")
