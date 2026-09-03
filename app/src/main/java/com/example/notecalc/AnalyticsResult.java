package com.example.notecalc;

import java.util.List;
import java.util.Map;

public class AnalyticsResult {
    public final double totalAmount;
    public final double highestTxn;
    public final double dailyAvg;
    public final long highDayTs;
    public final double highDayAmt;
    public final double budgetPct;
    public final String dateRangeStringId;
    public final int dateRangeStringResId;
    public final Map<Long, Double> dailyTotals;
    public final List<Record> filteredRecords;

    public AnalyticsResult(double totalAmount, double highestTxn, double dailyAvg, long highDayTs, double highDayAmt, double budgetPct, int dateRangeStringResId, Map<Long, Double> dailyTotals, List<Record> filteredRecords) {
        this.totalAmount = totalAmount;
        this.highestTxn = highestTxn;
        this.dailyAvg = dailyAvg;
        this.highDayTs = highDayTs;
        this.highDayAmt = highDayAmt;
        this.budgetPct = budgetPct;
        this.dateRangeStringResId = dateRangeStringResId;
        this.dailyTotals = dailyTotals;
        this.filteredRecords = filteredRecords;
        this.dateRangeStringId = null;
    }
}
