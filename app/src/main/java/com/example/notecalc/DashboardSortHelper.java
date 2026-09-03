package com.example.notecalc;

public class DashboardSortHelper {

    @android.annotation.SuppressLint("SetTextI18n")
    public static void updateDashboardSortUI(MainActivity activity) {
        if (activity.btnSortTitle != null) {
            activity.btnSortTitle.setTextColor(StateHelper.getDashboardSortColumn(activity) == 0 ? ThemeManager.getSecondaryAccentColor(activity) : activity.getColor(R.color.text_tertiary));
            activity.btnSortTitle.setText(StateHelper.getDashboardSortColumn(activity) == 0 ? "Title " + (StateHelper.getDashboardSortAscending(activity) ? "▲" : "▼") : "Title");
        }
        if (activity.btnSortTotal != null) {
            activity.btnSortTotal.setTextColor(StateHelper.getDashboardSortColumn(activity) == 1 ? ThemeManager.getSecondaryAccentColor(activity) : activity.getColor(R.color.text_tertiary));
            activity.btnSortTotal.setText(StateHelper.getDashboardSortColumn(activity) == 1 ? "Total " + (StateHelper.getDashboardSortAscending(activity) ? "▲" : "▼") : "Total");
        }
        if (activity.btnSortLatest != null) {
            activity.btnSortLatest.setTextColor(StateHelper.getDashboardSortColumn(activity) == 2 ? ThemeManager.getSecondaryAccentColor(activity) : activity.getColor(R.color.text_tertiary));
            activity.btnSortLatest.setText(StateHelper.getDashboardSortColumn(activity) == 2 ? "Latest " + (StateHelper.getDashboardSortAscending(activity) ? "▲" : "▼") : "Latest");
        }

        activity.btnSortGroupTitle = activity.findViewById(R.id.btn_sort_group_title);
        if (activity.btnSortGroupTitle != null) {
            activity.btnSortGroupTitle.setText("Title " + (StateHelper.getGroupSortAscending(activity) ? "▲" : "▼"));
        }
    }

    public static void setupSortListeners(MainActivity activity) {
        if (activity.btnSortGroupTitle != null) {
            ResponsiveUI.setupClickable(activity.btnSortGroupTitle, false, () -> {
                StateHelper.setGroupSortAscending(activity, !StateHelper.getGroupSortAscending(activity));
                updateDashboardSortUI(activity);
                DashboardHelper.refreshDashboardList(activity);
            });
        }

        if (activity.btnSortTitle != null) ResponsiveUI.setupClickable(activity.btnSortTitle, false, () -> {
            if (StateHelper.getDashboardSortColumn(activity) == 0) StateHelper.setDashboardSortAscending(activity, !StateHelper.getDashboardSortAscending(activity));
            else { StateHelper.setDashboardSortColumn(activity, 0); StateHelper.setDashboardSortAscending(activity, true); }
            StorageHelper.saveAppStorage(activity, activity.appStorage);
            updateDashboardSortUI(activity);
            DashboardHelper.refreshDashboardList(activity);
        });
        if (activity.btnSortTotal != null) ResponsiveUI.setupClickable(activity.btnSortTotal, false, () -> {
            if (StateHelper.getDashboardSortColumn(activity) == 1) StateHelper.setDashboardSortAscending(activity, !StateHelper.getDashboardSortAscending(activity));
            else { StateHelper.setDashboardSortColumn(activity, 1); StateHelper.setDashboardSortAscending(activity, false); }
            StorageHelper.saveAppStorage(activity, activity.appStorage);
            updateDashboardSortUI(activity);
            DashboardHelper.refreshDashboardList(activity);
        });
        if (activity.btnSortLatest != null) ResponsiveUI.setupClickable(activity.btnSortLatest, false, () -> {
            if (StateHelper.getDashboardSortColumn(activity) == 2) StateHelper.setDashboardSortAscending(activity, !StateHelper.getDashboardSortAscending(activity));
            else { StateHelper.setDashboardSortColumn(activity, 2); StateHelper.setDashboardSortAscending(activity, false); }
            StorageHelper.saveAppStorage(activity, activity.appStorage);
            updateDashboardSortUI(activity);
            DashboardHelper.refreshDashboardList(activity);
        });
    }
}
