package com.example.notecalc.core.utils;
import com.example.notecalc.dashboard.*;
import com.example.notecalc.records.models.Record;
import com.example.notecalc.*;
import androidx.activity.OnBackPressedCallback;

public class NavigationHelper {

    public static void handleBackPress(MainActivity activity, OnBackPressedCallback callback) {
        if (activity.currentEditingAccount != null || (activity.mainContainer.getChildAt(0) != null && activity.mainContainer.getChildAt(0).getId() != R.id.dashboard_root)) {
            if (activity.currentEditingAccount != null) {
                if (activity.currentEditingAccount.getRecords() != null) {
                    for (Record r : activity.currentEditingAccount.getRecords()) r.setSelected(false);
                }
                if (activity.currentEditingAccount.getBudgetRecords() != null) {
                    for (Record r : activity.currentEditingAccount.getBudgetRecords()) r.setSelected(false);
                }
            }
            activity.currentEditingAccount = null;
            activity.dashboardSearchQuery = "";
            DashboardHelper.showDashboard(activity);
        } else if (activity.currentViewGroup != null) {
            activity.currentViewGroup = null;
            activity.dashboardSearchQuery = "";
            DashboardHelper.showDashboard(activity);
        } else {
            callback.setEnabled(false);
            activity.getOnBackPressedDispatcher().onBackPressed();
            callback.setEnabled(true);
        }
    }
}