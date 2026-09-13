package com.example.notecalc.dashboard.ui;
import com.example.notecalc.dashboard.engine.*;
import com.example.notecalc.dashboard.*;
import com.example.notecalc.core.utils.*;
import com.example.notecalc.core.ui.*;
import com.example.notecalc.*;
import android.view.View;
import android.view.MotionEvent;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
public class DashboardUIHelper {

    @android.annotation.SuppressLint("ClickableViewAccessibility")
    public static void setupSearchBar(MainActivity activity, EditText editDashboardSearch) {
        editDashboardSearch.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgSecondaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                8.0f
        ));

        editDashboardSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (editDashboardSearch.getCompoundDrawablesRelative()[2] != null) {
                    if (event.getRawX() >= (editDashboardSearch.getRight() - editDashboardSearch.getCompoundDrawablesRelative()[2].getBounds().width() - editDashboardSearch.getPaddingRight())) {
                        editDashboardSearch.setText("");
                        return true;
                    }
                }
                v.performClick();
            }
            return false;
        });

        if (activity.dashboardSearchQuery != null && !activity.dashboardSearchQuery.isEmpty()) {
            editDashboardSearch.setText(activity.dashboardSearchQuery);
        }

        editDashboardSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                activity.dashboardSearchQuery = s.toString();
                DashboardHelper.refreshDashboardList(activity);
            }
        });
    }

    public static void setupActionButtons(MainActivity activity, View btnSettings, View btnArchive, View btnTips) {
        if(btnSettings != null) btnSettings.setOnClickListener(v -> activity.settingsHelper.openSettings());

        if(btnArchive != null) btnArchive.setOnClickListener(v -> {
            if (!ArchiveHelper.isShowingArchive) {
                boolean hasArchived = false;
                for (com.example.notecalc.accounts.models.AccountGroup group : activity.appStorage.groups) {
                    if (group.isArchived()) { hasArchived = true; break; }
                }
                if (!hasArchived) {
                    for (com.example.notecalc.accounts.models.Account account : activity.appStorage.standaloneAccounts) {
                        if (account.isArchived()) { hasArchived = true; break; }
                    }
                }
                if (!hasArchived) {
                    android.widget.Toast.makeText(activity, "No archived items", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            ArchiveHelper.isShowingArchive = !ArchiveHelper.isShowingArchive;
            
            activity.dashboardSearchQuery = "";
            android.widget.EditText searchBox = activity.findViewById(R.id.edit_dashboard_search);
            if (searchBox != null) {
                searchBox.setText("");
            }
            
            if (activity.currentViewGroup != null) {
                activity.currentViewGroup = null;
                DashboardHelper.showDashboard(activity);
            } else {
                DashboardSortHelper.updateDashboardSortUI(activity);
                DashboardHelper.refreshDashboardList(activity);
                
                androidx.recyclerview.widget.RecyclerView rvAccounts = activity.findViewById(R.id.list_accounts);
                if (rvAccounts != null) rvAccounts.scrollToPosition(0);
                androidx.recyclerview.widget.RecyclerView rvGroups = activity.findViewById(R.id.list_groups);
                if (rvGroups != null) rvGroups.scrollToPosition(0);
            }
        });

        if(btnTips != null) btnTips.setOnClickListener(v -> AppDialogHelper.showTipsDialog(activity));
    }

    public static void applyDashboardStyling(MainActivity activity, View cardEmptyState) {
        if (cardEmptyState != null) {
            cardEmptyState.setBackground(ResponsiveUI.createRoundedBg(
                    activity,
                    ThemeManager.getBgSecondaryColor(activity),
                    ThemeManager.getBorderColor(activity),
                    1.5f,
                    12f
            ));
        }
    }

    public static void setupAddMenuPopup(MainActivity activity, View btnCreateAdd) {
        if (btnCreateAdd != null) {
            ResponsiveUI.setupClickable(btnCreateAdd, () -> {
                android.view.View popupView = activity.getLayoutInflater().inflate(R.layout.layout_create_menu, null);
                android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(
                        popupView,
                        (int) (180 * activity.getResources().getDisplayMetrics().density),
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        true
                );

                popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                popupWindow.setElevation(8f);

                popupView.measure(android.view.View.MeasureSpec.UNSPECIFIED, android.view.View.MeasureSpec.UNSPECIFIED);

                android.view.View btnCreateList = popupView.findViewById(R.id.btn_popup_create_list);
                android.view.View btnCreateGroupPopup = popupView.findViewById(R.id.btn_popup_create_group);

                ResponsiveUI.setupClickable(btnCreateList, true, () -> {
                    popupWindow.dismiss();
                    activity.openEditor(null);
                });

                ResponsiveUI.setupClickable(btnCreateGroupPopup, true, () -> {
                    popupWindow.dismiss();
                    com.example.notecalc.accounts.dialogs.GroupDialogHelper.showCreateGroupDialog(activity);
                });

                popupWindow.showAsDropDown(btnCreateAdd, 0, 16);
            });
        }
    }
}