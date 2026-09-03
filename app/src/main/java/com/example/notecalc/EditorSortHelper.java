package com.example.notecalc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditorSortHelper {
    public static void applySorting(MainActivity activity) {
        if (StateHelper.getActiveRecords(activity) == null || StateHelper.getActiveRecords(activity).isEmpty()) {
            return;
        }

        StateHelper.getActiveRecords(activity).sort(new java.util.Comparator<>() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

            @Override
            public int compare(Record r1, Record r2) {
                int c = 0;
                switch (StateHelper.getSortColumn(activity)) {
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
                return StateHelper.getSortAscending(activity) ? c : -c;
            }
        });
    }

    public static void updateHeaderLabels(MainActivity activity) {
        if (activity.thSnoField != null) {
            activity.thSnoField.setText(String.format(Locale.getDefault(), "%s%s", activity.getString(R.string.th_sno), (StateHelper.getSortColumn(activity) == 0 ? (StateHelper.getSortAscending(activity) ? "  ▲" : "  ▼") : "")));
        }
        if (activity.thDescField != null) {
            activity.thDescField.setText(String.format(Locale.getDefault(), "%s%s", activity.getString(R.string.th_desc), (StateHelper.getSortColumn(activity) == 1 ? (StateHelper.getSortAscending(activity) ? "  ▲" : "  ▼") : "")));
        }
        if (activity.thDateField != null) {
            activity.thDateField.setText(String.format(Locale.getDefault(), "%s%s", activity.getString(R.string.th_date), (StateHelper.getSortColumn(activity) == 2 ? (StateHelper.getSortAscending(activity) ? "  ▲" : "  ▼") : "")));
        }
        if (activity.thAmountField != null) {
            activity.thAmountField.setText(String.format(Locale.getDefault(), "%s%s", activity.getString(R.string.th_amount), (StateHelper.getSortColumn(activity) == 3 ? (StateHelper.getSortAscending(activity) ? "  ▲" : "  ▼") : "")));
        }
    }

    public static void onHeaderClicked(MainActivity activity, int col) {
        if (StateHelper.getSortColumn(activity) == col) {
            StateHelper.setSortAscending(activity, !StateHelper.getSortAscending(activity));
        } else {
            StateHelper.setSortColumn(activity, col);
            StateHelper.setSortAscending(activity, true);
        }

        applySorting(activity);
        EditorUIHelper.populateRecordsList(activity);
        updateHeaderLabels(activity);
    }

    public static void updateDateHeaderIndicator(MainActivity activity) {
        if (activity.thDateField == null) return;
        boolean active = (StateHelper.getFilterDateFrom(activity) != null || StateHelper.getFilterDateTo(activity) != null);
        activity.thDateField.setTextColor(active ? ThemeManager.getPrimaryAccentColor(activity) : ThemeManager.getSecondaryAccentColor(activity));
    }

    public static void updateAmountHeaderIndicator(MainActivity activity) {
        if (activity.thAmountField == null) return;
        boolean active = (StateHelper.getFilterAmountFrom(activity) != null || StateHelper.getFilterAmountTo(activity) != null);
        activity.thAmountField.setTextColor(active ? ThemeManager.getPrimaryAccentColor(activity) : ThemeManager.getSecondaryAccentColor(activity));
    }

    public static void setupHeaderSortListeners(MainActivity activity) {
        activity.expenseSortColumn = 0;
        activity.expenseSortAscending = false;
        activity.budgetSortColumn = 0;
        activity.budgetSortAscending = false;

        if (activity.thSnoField != null) activity.thSnoField.setBackground(ResponsiveUI.createButtonSelector(activity, android.graphics.Color.parseColor("#15FFFFFF"), 4.0f));
        if (activity.thDescField != null) activity.thDescField.setBackground(ResponsiveUI.createButtonSelector(activity, android.graphics.Color.parseColor("#15FFFFFF"), 4.0f));
        if (activity.thDateField != null) activity.thDateField.setBackground(ResponsiveUI.createButtonSelector(activity, android.graphics.Color.parseColor("#15FFFFFF"), 4.0f));
        if (activity.thAmountField != null) activity.thAmountField.setBackground(ResponsiveUI.createButtonSelector(activity, android.graphics.Color.parseColor("#15FFFFFF"), 4.0f));

        if (activity.thSnoField != null) ResponsiveUI.setupClickable(activity.thSnoField, false, () -> EditorSortHelper.onHeaderClicked(activity, 0));
        if (activity.thDescField != null) ResponsiveUI.setupClickable(activity.thDescField, false, () -> EditorSortHelper.onHeaderClicked(activity, 1));

        if (activity.thDateField != null) {
            activity.thDateField.setOnClickListener(v -> EditorSortHelper.onHeaderClicked(activity, 2));
            activity.thDateField.setOnLongClickListener(v -> {
                FilterHelper.showDateRangeFilterDialog(activity);
                return true;
            });
        }

        if (activity.thAmountField != null) {
            activity.thAmountField.setOnClickListener(v -> EditorSortHelper.onHeaderClicked(activity, 3));
            activity.thAmountField.setOnLongClickListener(v -> {
                FilterHelper.showAmountRangeFilterDialog(activity);
                return true;
            });
        }
        updateHeaderLabels(activity);
    }
}
