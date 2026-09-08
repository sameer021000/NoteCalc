package com.example.notecalc.core;
import com.example.notecalc.dashboard.*;
import com.example.notecalc.records.Record;
import com.example.notecalc.*;
import androidx.activity.OnBackPressedCallback;

public class NavigationHelper {

    public static void handleBackPress(MainActivity activity, OnBackPressedCallback callback) {
        if (activity.currentEditingAccount != null || (activity.mainContainer.getChildAt(0) != null && activity.mainContainer.getChildAt(0).getId() != R.id.dashboard_root)) {
            if (activity.tempRecords != null) for (Record r : activity.tempRecords) r.setSelected(false);
            if (activity.tempBudgetRecords != null) for (Record r : activity.tempBudgetRecords) r.setSelected(false);
            activity.currentEditingAccount = null;
            activity.tempRecords = null;
            activity.tempBudgetRecords = null;
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