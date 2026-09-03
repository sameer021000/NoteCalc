package com.example.notecalc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AnalyticsEngine {

    public static AnalyticsResult calculate(Account account, int timeMode) {
        List<Record> allRecords = account.getRecords();
        List<Record> filtered = new ArrayList<>();
        
        long now = System.currentTimeMillis();
        long startTime = 0;
        int dateRangeStringResId;
        
        if (timeMode == 1) {
            startTime = now - (7L * 24 * 60 * 60 * 1000);
            dateRangeStringResId = R.string.auto_last_7_days_26;
        } else if (timeMode == 2) {
            startTime = now - (30L * 24 * 60 * 60 * 1000);
            dateRangeStringResId = R.string.auto_last_30_days_27;
        } else {
            dateRangeStringResId = R.string.auto_all_time_28;
        }

        double totalAmount = 0;
        double highestTxn = 0;
        
        TreeMap<Long, Double> dailyTotals = new TreeMap<>();

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
        for (Map.Entry<Long, Double> entry : dailyTotals.entrySet()) {
            if (entry.getValue() > highDayAmt) {
                highDayAmt = entry.getValue();
                highDayTs = entry.getKey();
            }
        }

        int days = dailyTotals.size();
        double dailyAvg = days > 0 ? (totalAmount / days) : 0;
        
        double budgetPct = -1.0;
        if (account.hasBudget()) {
            double budget = account.calculateTotalBudget();
            if (budget > 0) {
                double expenses = 0;
                for (Record r : filtered) {
                    expenses += r.getAmount();
                }
                budgetPct = (expenses / budget) * 100.0;
            } else {
                budgetPct = 0.0;
            }
        }

        return new AnalyticsResult(totalAmount, highestTxn, dailyAvg, highDayTs, highDayAmt, budgetPct, dateRangeStringResId, dailyTotals, filtered);
    }
}
