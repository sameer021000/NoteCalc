package com.example.notecalc;

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
            ArchiveHelper.isShowingArchive = !ArchiveHelper.isShowingArchive;
            DashboardSortHelper.updateDashboardSortUI(activity);
            DashboardHelper.refreshDashboardList(activity);
        });
        
        if(btnTips != null) btnTips.setOnClickListener(v -> AppDialogHelper.showTipsDialog(activity));
    }

    public static void applyDashboardStyling(MainActivity activity, View btnCreateAccount, View btnCreateGroup, View cardEmptyState) {
        if (btnCreateAccount != null) {
            btnCreateAccount.setBackground(ResponsiveUI.createRoundedBg(
                    activity,
                    ThemeManager.getBgSecondaryColor(activity),
                    ThemeManager.getBorderColor(activity),
                    1.0f,
                    8.0f
            ));
        }
        if (btnCreateGroup != null) {
            btnCreateGroup.setBackground(ResponsiveUI.createRoundedBg(
                    activity,
                    ThemeManager.getBgSecondaryColor(activity),
                    ThemeManager.getBorderColor(activity),
                    1.0f,
                    8.0f
            ));
        }

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
}
