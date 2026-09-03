package com.example.notecalc;

import android.view.LayoutInflater;
import android.view.View;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;
import android.text.TextWatcher;
import android.text.Editable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardHelper {

        @android.annotation.SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    public static void showDashboard(MainActivity activity) {
        if (activity.currentSnackbar != null) {
            activity.currentSnackbar.dismiss();
            activity.currentSnackbar = null;
        }
                LayoutInflater inflater = activity.getLayoutInflater();
                View dashboardView = inflater.inflate(R.layout.layout_dashboard, activity.mainContainer, false);

        // Find views
        View btnCreateAccount = dashboardView.findViewById(R.id.btn_create_account);
        
        View btnSettings = dashboardView.findViewById(R.id.btn_settings);
        if(btnSettings != null) btnSettings.setOnClickListener(v -> activity.settingsHelper.openSettings());
        
        View btnArchive = dashboardView.findViewById(R.id.btn_archive);
        if(btnArchive != null) btnArchive.setOnClickListener(v -> {
            ArchiveHelper.isShowingArchive = !ArchiveHelper.isShowingArchive;
            updateDashboardSortUI(activity);
            refreshDashboardList(activity);
        });
        
        View btnTips = dashboardView.findViewById(R.id.btn_tips);
        if(btnTips != null) btnTips.setOnClickListener(v -> DialogHelper.showTipsDialog(activity));
        
        View btnCreateGroup = dashboardView.findViewById(R.id.btn_create_group);
        View cardEmptyState = dashboardView.findViewById(R.id.card_empty_state);

        RecyclerView listAccountsContainer = dashboardView.findViewById(R.id.list_accounts);
        listAccountsContainer.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(activity));
        activity.accountsAdapter = new AccountsAdapter(activity);
        listAccountsContainer.setAdapter(activity.accountsAdapter);

        RecyclerView listGroupsContainer = dashboardView.findViewById(R.id.list_groups);
        if (listGroupsContainer != null) {
            listGroupsContainer.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(activity));
            activity.groupsAdapter = new AccountsAdapter(activity);
            listGroupsContainer.setAdapter(activity.groupsAdapter);
        }

        activity.btnSortGroupTitle = dashboardView.findViewById(R.id.btn_sort_group_title);
        if (activity.btnSortGroupTitle != null) {
            ResponsiveUI.setupClickable(activity.btnSortGroupTitle, false, () -> {
                StateHelper.setGroupSortAscending(activity, !StateHelper.getGroupSortAscending(activity));
                updateDashboardSortUI(activity);
                refreshDashboardList(activity);
            });
        }

        activity.btnSortTitle = dashboardView.findViewById(R.id.btn_sort_title);
        activity.btnSortTotal = dashboardView.findViewById(R.id.btn_sort_total);
        activity.btnSortLatest = dashboardView.findViewById(R.id.btn_sort_latest);

        if (activity.btnSortTitle != null) ResponsiveUI.setupClickable(activity.btnSortTitle, false, () -> {
            if (StateHelper.getDashboardSortColumn(activity) == 0) StateHelper.setDashboardSortAscending(activity, !StateHelper.getDashboardSortAscending(activity));
            else { StateHelper.setDashboardSortColumn(activity, 0); StateHelper.setDashboardSortAscending(activity, true); }
            StorageHelper.saveAppStorage(activity, activity.appStorage);
            updateDashboardSortUI(activity);
            refreshDashboardList(activity);
        });
        if (activity.btnSortTotal != null) ResponsiveUI.setupClickable(activity.btnSortTotal, false, () -> {
            if (StateHelper.getDashboardSortColumn(activity) == 1) StateHelper.setDashboardSortAscending(activity, !StateHelper.getDashboardSortAscending(activity));
            else { StateHelper.setDashboardSortColumn(activity, 1); StateHelper.setDashboardSortAscending(activity, false); }
            StorageHelper.saveAppStorage(activity, activity.appStorage);
            updateDashboardSortUI(activity);
            refreshDashboardList(activity);
        });
        if (activity.btnSortLatest != null) ResponsiveUI.setupClickable(activity.btnSortLatest, false, () -> {
            if (StateHelper.getDashboardSortColumn(activity) == 2) StateHelper.setDashboardSortAscending(activity, !StateHelper.getDashboardSortAscending(activity));
            else { StateHelper.setDashboardSortColumn(activity, 2); StateHelper.setDashboardSortAscending(activity, false); }
            StorageHelper.saveAppStorage(activity, activity.appStorage);
            updateDashboardSortUI(activity);
            refreshDashboardList(activity);
        });
        // Apply responsive styling to the main layout elements
        ResponsiveUI.applyResponsiveness(dashboardView);

        // Dynamic background borders & colors styling
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

        cardEmptyState.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgSecondaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.5f,
                12f
        ));

        // Set up click actions
        ResponsiveUI.setupClickable(btnCreateAccount, () -> activity.openEditor(null));
        if (btnCreateGroup != null) {
            ResponsiveUI.setupClickable(btnCreateGroup, () -> DialogHelper.showCreateGroupDialog(activity));
        }
        ResponsiveUI.setupClickable(cardEmptyState, () -> {
            if (activity.currentViewGroup != null) {
                activity.openEditor(null);
            }
        });
        activity.currentEditingAccount = null;
        
        TextView textAppTitle = dashboardView.findViewById(R.id.text_app_title);
        TextView textAppSubtitle = dashboardView.findViewById(R.id.text_app_subtitle);
        ImageView btnDashboardBack = dashboardView.findViewById(R.id.btn_dashboard_back);
        
        if (activity.currentViewGroup != null) {
            if (textAppTitle != null) textAppTitle.setText(activity.currentViewGroup.getTitle());
            if (textAppSubtitle != null) textAppSubtitle.setVisibility(View.GONE);
            if (btnCreateGroup != null) btnCreateGroup.setVisibility(View.GONE);
            if (btnDashboardBack != null) {
                btnDashboardBack.setVisibility(View.VISIBLE);
                ResponsiveUI.setupClickable(btnDashboardBack, false, () -> {
                    activity.currentViewGroup = null;
                    showDashboard(activity);
                });
            }
        } else {
            if (textAppTitle != null) textAppTitle.setText(activity.getString(R.string.app_name));
            if (textAppSubtitle != null) textAppSubtitle.setVisibility(View.VISIBLE);
            if (btnCreateGroup != null) btnCreateGroup.setVisibility(View.VISIBLE);
            if (btnDashboardBack != null) btnDashboardBack.setVisibility(View.GONE);
        }

        EditText editDashboardSearch = dashboardView.findViewById(R.id.edit_dashboard_search);
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



        // Search bar watcher
        editDashboardSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                activity.dashboardSearchQuery = s.toString();
                refreshDashboardList(activity);
            }
        });

        // Mount to main container first so findViewById works in refreshDashboardList
        activity.mainContainer.removeAllViews();
        activity.mainContainer.addView(dashboardView);

        // Populate accounts list
        refreshDashboardList(activity);
    }


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


    public static void refreshDashboardList(MainActivity activity) {
        if (activity.accountsAdapter == null) return;
        
        List<Object> combinedGroups = new ArrayList<>();
        List<Object> combinedAccounts = new ArrayList<>();

        TextView textAppTitle = activity.findViewById(R.id.text_app_title);
        TextView textAppSubtitle = activity.findViewById(R.id.text_app_subtitle);
        if (textAppTitle != null) textAppTitle.setText(ArchiveHelper.isShowingArchive ? "Archive" : activity.getString(R.string.app_name));
        if (textAppSubtitle != null) textAppSubtitle.setText(ArchiveHelper.isShowingArchive ? "Read-only history" : activity.getString(R.string.app_subtitle));

        View btnCreateAccount = activity.findViewById(R.id.btn_create_account);
        View btnCreateGroup = activity.findViewById(R.id.btn_create_group);
        android.widget.ImageView btnArchive = activity.findViewById(R.id.btn_archive);
        if (btnCreateAccount != null) btnCreateAccount.setVisibility(ArchiveHelper.isShowingArchive ? View.GONE : View.VISIBLE);
        if (btnCreateGroup != null) btnCreateGroup.setVisibility(ArchiveHelper.isShowingArchive ? View.GONE : View.VISIBLE);
        if (btnArchive != null) btnArchive.setImageResource(ArchiveHelper.isShowingArchive ? R.drawable.ic_archive : R.drawable.ic_archive_outline);

        if (activity.currentViewGroup != null) {
            combinedAccounts.addAll(applyDashboardSort(activity, ArchiveHelper.getVisibleAccounts(activity.currentViewGroup.getAccounts())));
        } else {
            List<AccountGroup> sortedGroups = new ArrayList<>(ArchiveHelper.getVisibleGroups(activity.appStorage.groups));
            sortedGroups.sort((a, b) -> {
                if (a.isPinned() != b.isPinned()) return a.isPinned() ? -1 : 1;
                int titleCompare = a.getTitle().compareToIgnoreCase(b.getTitle());
                return StateHelper.getGroupSortAscending(activity) ? titleCompare : -titleCompare;
            });
            combinedGroups.addAll(sortedGroups);
            combinedAccounts.addAll(applyDashboardSort(activity, ArchiveHelper.getVisibleAccounts(activity.appStorage.standaloneAccounts)));
        }
        
        String query = activity.dashboardSearchQuery.trim().toLowerCase(Locale.getDefault());
        activity.accountsAdapter.setFilter(combinedAccounts, query);
        if (activity.groupsAdapter != null) activity.groupsAdapter.setFilter(combinedGroups, query);
        
        View cardEmptyState = activity.findViewById(R.id.card_empty_state);
        View contentContainer = activity.findViewById(R.id.dashboard_content_container);
        View sectionGroups = activity.findViewById(R.id.section_groups);
        View sectionAccounts = activity.findViewById(R.id.section_accounts);
        EditText editDashboardSearch = activity.findViewById(R.id.edit_dashboard_search);
        
        if (cardEmptyState == null) return;

        boolean hasGroups = activity.groupsAdapter != null && activity.groupsAdapter.getItemCount() > 0;
        boolean hasAccounts = activity.accountsAdapter.getItemCount() > 0;
        boolean isListEmpty = activity.appStorage.groups.isEmpty() && activity.appStorage.standaloneAccounts.isEmpty();

        if (isListEmpty) {
            cardEmptyState.setVisibility(View.VISIBLE);
            contentContainer.setVisibility(View.GONE);
            editDashboardSearch.setVisibility(View.GONE);
        } else if (!hasGroups && !hasAccounts) {
            cardEmptyState.setVisibility(View.VISIBLE);
            contentContainer.setVisibility(View.GONE);
            editDashboardSearch.setVisibility(View.VISIBLE);
        } else {
            cardEmptyState.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
            editDashboardSearch.setVisibility(View.VISIBLE);
            
            sectionGroups.setVisibility(hasGroups ? View.VISIBLE : View.GONE);
            sectionAccounts.setVisibility(hasAccounts ? View.VISIBLE : View.GONE);
            
            updateDashboardSortUI(activity);
        }
    }


    public static List<Account> applyDashboardSort(MainActivity activity, List<Account> source) {
        List<Account> sorted = new ArrayList<>(source);
        
        int mode = StateHelper.getDashboardSortColumn(activity);
        boolean asc = StateHelper.getDashboardSortAscending(activity);
        
        sorted.sort((a, b) -> {
            // First check pin status
            if (a.isPinned() != b.isPinned()) {
                return a.isPinned() ? -1 : 1;
            }
            
            int result;
            if (mode == 0) { // Title
                result = a.getTitle().compareToIgnoreCase(b.getTitle());
            } else if (mode == 1) { // Total amount
                result = Double.compare(a.calculateTotal(), b.calculateTotal());
            } else { // Latest modified
                result = Long.compare(a.getLastModified(), b.getLastModified());
            }
            return asc ? result : -result;
        });
        return sorted;
    }

}
