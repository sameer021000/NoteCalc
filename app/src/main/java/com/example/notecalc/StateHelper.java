package com.example.notecalc;
import java.util.List;
public class StateHelper {
    public static int getSortColumn(MainActivity activity) { return activity.isBudgetMode ? activity.budgetSortColumn : activity.expenseSortColumn; }

    public static boolean getSortAscending(MainActivity activity) { return activity.isBudgetMode ? activity.budgetSortAscending : activity.expenseSortAscending; }

    public static void setSortColumn(MainActivity activity, int col) { if (activity.isBudgetMode) activity.budgetSortColumn = col; else activity.expenseSortColumn = col; }

    public static void setSortAscending(MainActivity activity, boolean asc) { if (activity.isBudgetMode) activity.budgetSortAscending = asc; else activity.expenseSortAscending = asc; }

    public static String getFilterDateFrom(MainActivity activity) { return activity.isBudgetMode ? activity.budgetFilterDateFrom : activity.expenseFilterDateFrom; }

    public static void setFilterDateFrom(MainActivity activity, String val) { if (activity.isBudgetMode) activity.budgetFilterDateFrom = val; else activity.expenseFilterDateFrom = val; }

    public static String getFilterDateTo(MainActivity activity) { return activity.isBudgetMode ? activity.budgetFilterDateTo : activity.expenseFilterDateTo; }

    public static void setFilterDateTo(MainActivity activity, String val) { if (activity.isBudgetMode) activity.budgetFilterDateTo = val; else activity.expenseFilterDateTo = val; }

    public static Double getFilterAmountFrom(MainActivity activity) { return activity.isBudgetMode ? activity.budgetFilterAmountFrom : activity.expenseFilterAmountFrom; }

    public static void setFilterAmountFrom(MainActivity activity, Double val) { if (activity.isBudgetMode) activity.budgetFilterAmountFrom = val; else activity.expenseFilterAmountFrom = val; }

    public static Double getFilterAmountTo(MainActivity activity) { return activity.isBudgetMode ? activity.budgetFilterAmountTo : activity.expenseFilterAmountTo; }

    public static void setFilterAmountTo(MainActivity activity, Double val) { if (activity.isBudgetMode) activity.budgetFilterAmountTo = val; else activity.expenseFilterAmountTo = val; }

    public static int getDashboardSortColumn(MainActivity activity) {
        if (activity.currentViewGroup != null) return activity.currentViewGroup.getSortMode();
        return ArchiveHelper.isShowingArchive ? activity.archivedDashboardSortMode : activity.dashboardSortMode;
    }

    public static void setDashboardSortColumn(MainActivity activity, int mode) {
        if (activity.currentViewGroup != null) activity.currentViewGroup.setSortMode(mode);
        else if (ArchiveHelper.isShowingArchive) activity.archivedDashboardSortMode = mode;
        else activity.dashboardSortMode = mode;
    }

    public static boolean getDashboardSortAscending(MainActivity activity) {
        if (activity.currentViewGroup != null) return activity.currentViewGroup.isSortAscending();
        return ArchiveHelper.isShowingArchive ? activity.archivedDashboardSortAscending : activity.dashboardSortAscending;
    }

    public static void setDashboardSortAscending(MainActivity activity, boolean asc) {
        if (activity.currentViewGroup != null) activity.currentViewGroup.setSortAscending(asc);
        else if (ArchiveHelper.isShowingArchive) activity.archivedDashboardSortAscending = asc;
        else activity.dashboardSortAscending = asc;
    }

    public static boolean getGroupSortAscending(MainActivity activity) {
        return ArchiveHelper.isShowingArchive ? activity.archivedGroupSortAscending : activity.groupSortAscending;
    }

    public static void setGroupSortAscending(MainActivity activity, boolean asc) {
        if (ArchiveHelper.isShowingArchive) activity.archivedGroupSortAscending = asc;
        else activity.groupSortAscending = asc;
    }

    public static List<Record> getActiveRecords(MainActivity activity) {
        return activity.isBudgetMode ? activity.tempBudgetRecords : activity.tempRecords;
    }

}
