import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace the block from private void showAnalytics to the end
pattern = r'private void showAnalytics\(Account account\) \{.*'
match = re.search(pattern, content, re.DOTALL)
if match:
    content = content[:match.start()]

new_methods = '''private void showAnalytics(Account account) {
        View analyticsRoot = getLayoutInflater().inflate(R.layout.layout_analytics, mainContainer, false);
        mainContainer.removeAllViews();
        mainContainer.addView(analyticsRoot);

        android.widget.ImageButton btnBack = analyticsRoot.findViewById(R.id.btn_analytics_back);
        setupClickable(btnBack, true, () -> openEditor(account));

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
        
        java.util.TreeMap<Long, Double> dailyTotals = new java.util.TreeMap<>();

        for (Record r : allRecords) {
            long rTs = 0;
            try {
                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                rTs = format.parse(r.getDate()).getTime();
            } catch(Exception ex) {}

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
                
                dailyTotals.put(dayStart, dailyTotals.getOrDefault(dayStart, 0.0) + amt);
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
            tvHighDay.setText(sdf.format(new java.util.Date(highDayTs)) + " (" + nf.format(highDayAmt) + ")");
        } else {
            tvHighDay.setText("None");
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
                tvBudgetPct.setText("0% of budget spent");
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
}
'''
content = content + new_methods

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated perfectly")
