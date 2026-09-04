package com.example.notecalc;

public class FilterHelper {

    @android.annotation.SuppressLint("SetTextI18n")

    public static boolean isFilterActive(MainActivity activity) {
        if (activity.recordsAdapter != null && !activity.recordsAdapter.filterCategories.isEmpty()) return true;
        if (activity.currentRecordSearchQuery != null && !activity.currentRecordSearchQuery.trim().isEmpty()) return true;
        if (StateHelper.getFilterDateFrom(activity) != null || StateHelper.getFilterDateTo(activity) != null) return true;
        return StateHelper.getFilterAmountFrom(activity) != null || StateHelper.getFilterAmountTo(activity) != null;
    }

    public static void showCategoryFilterDialog(MainActivity activity, Account account, android.widget.ImageView btnFilterIcon) {
        FilterCategoryHelper.showDialog(activity, account, btnFilterIcon);
    }

    public static void showDateRangeFilterDialog(MainActivity activity) {
        FilterDateHelper.showDialog(activity);
    }

    public static void showAmountRangeFilterDialog(MainActivity activity) {
        FilterAmountHelper.showDialog(activity);
    }
}
