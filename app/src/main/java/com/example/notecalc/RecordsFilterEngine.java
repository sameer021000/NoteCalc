package com.example.notecalc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RecordsFilterEngine {

    public static List<Record> filterRecords(MainActivity activity, List<Record> sourceRecords, String query, Set<String> filterCategories) {
        List<Record> filtered = new ArrayList<>();
        String q = (query == null ? "" : query.trim().toLowerCase(Locale.getDefault()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        
        for (Record r : sourceRecords) {
            if (filterCategories != null && !filterCategories.isEmpty()) {
                if (!filterCategories.contains(r.getCategory())) continue;
            }

            if (!q.isEmpty()) {
                boolean matchDesc = r.getDescription().toLowerCase(Locale.getDefault()).contains(q);
                boolean matchRem = r.getRemarks() != null && r.getRemarks().toLowerCase(Locale.getDefault()).contains(q);
                if (!matchDesc && !matchRem) {
                    continue;
                }
            }

            if (StateHelper.getFilterDateFrom(activity) != null || StateHelper.getFilterDateTo(activity) != null) {
                try {
                    Date recordDate = sdf.parse(r.getDate());
                    if (StateHelper.getFilterDateFrom(activity) != null) {
                        Date from = sdf.parse(StateHelper.getFilterDateFrom(activity));
                        if (recordDate != null && recordDate.before(from)) continue;
                    }
                    if (StateHelper.getFilterDateTo(activity) != null) {
                        Date to = sdf.parse(StateHelper.getFilterDateTo(activity));
                        if (recordDate != null && recordDate.after(to)) continue;
                    }
                } catch (ParseException e) {
                    android.util.Log.e("NoteCalc", "Date parse error", e);
                }
            }

            if (StateHelper.getFilterAmountFrom(activity) != null && r.getAmount() < StateHelper.getFilterAmountFrom(activity)) continue;
            if (StateHelper.getFilterAmountTo(activity) != null && r.getAmount() > StateHelper.getFilterAmountTo(activity)) continue;

            filtered.add(r);
        }
        return filtered;
    }
}
