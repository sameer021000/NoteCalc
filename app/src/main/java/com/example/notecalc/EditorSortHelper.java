package com.example.notecalc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditorSortHelper {
    public static void applySorting(MainActivity activity) {
        if (activity.getActiveRecords() == null || activity.getActiveRecords().isEmpty()) {
            return;
        }

        activity.getActiveRecords().sort(new java.util.Comparator<>() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

            @Override
            public int compare(Record r1, Record r2) {
                int c = 0;
                switch (activity.getSortColumn()) {
                    case 0: // S.No
                        c = Integer.compare(r1.getOriginalIndex(), r2.getOriginalIndex());
                        break;
                    case 1: // Description
                        c = r1.getDescription().compareToIgnoreCase(r2.getDescription());
                        break;
                    case 2: // Date
                        try {
                            Date d1 = sdf.parse(r1.getDate());
                            Date d2 = sdf.parse(r2.getDate());
                            if (d1 != null && d2 != null) {
                                c = d1.compareTo(d2);
                                if (c == 0) {
                                    c = Long.compare(r1.getTimestampMillis(), r2.getTimestampMillis());
                                }
                            }
                        } catch (Exception ignored) {}
                        break;
                    case 3: // Amount
                        c = Double.compare(r1.getAmount(), r2.getAmount());
                        break;
                }
                return activity.getSortAscending() ? c : -c;
            }
        });
    }

    public static void updateHeaderLabels(MainActivity activity) {
        if (activity.thSnoField != null) {
            activity.thSnoField.setText(activity.getString(R.string.th_sno) + (activity.getSortColumn() == 0 ? (activity.getSortAscending() ? "  ▲" : "  ▼") : ""));
        }
        if (activity.thDescField != null) {
            activity.thDescField.setText(activity.getString(R.string.th_desc) + (activity.getSortColumn() == 1 ? (activity.getSortAscending() ? "  ▲" : "  ▼") : ""));
        }
        if (activity.thDateField != null) {
            activity.thDateField.setText(activity.getString(R.string.th_date) + (activity.getSortColumn() == 2 ? (activity.getSortAscending() ? "  ▲" : "  ▼") : ""));
        }
        if (activity.thAmountField != null) {
            activity.thAmountField.setText(activity.getString(R.string.th_amount) + (activity.getSortColumn() == 3 ? (activity.getSortAscending() ? "  ▲" : "  ▼") : ""));
        }
    }

    public static void onHeaderClicked(MainActivity activity, int col) {
        if (activity.getSortColumn() == col) {
            activity.setSortAscending(!activity.getSortAscending());
        } else {
            activity.setSortColumn(col);
            activity.setSortAscending(true);
        }

        applySorting(activity);
        EditorUIHelper.populateRecordsList(activity);
        updateHeaderLabels(activity);
    }

    public static void updateDateHeaderIndicator(MainActivity activity) {
        if (activity.thDateField == null) return;
        boolean active = (activity.getFilterDateFrom() != null || activity.getFilterDateTo() != null);
        activity.thDateField.setTextColor(active ? ThemeManager.getPrimaryAccentColor(activity) : ThemeManager.getSecondaryAccentColor(activity));
    }

    public static void updateAmountHeaderIndicator(MainActivity activity) {
        if (activity.thAmountField == null) return;
        boolean active = (activity.getFilterAmountFrom() != null || activity.getFilterAmountTo() != null);
        activity.thAmountField.setTextColor(active ? ThemeManager.getPrimaryAccentColor(activity) : ThemeManager.getSecondaryAccentColor(activity));
    }


}
