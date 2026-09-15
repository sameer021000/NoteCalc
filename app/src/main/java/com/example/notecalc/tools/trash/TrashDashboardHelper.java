package com.example.notecalc.tools.trash;

import com.example.notecalc.MainActivity;
import com.example.notecalc.R;
import com.example.notecalc.accounts.models.Account;
import com.example.notecalc.core.ui.ResponsiveUI;
import com.example.notecalc.core.ui.ThemeManager;
import com.example.notecalc.dashboard.DashboardHelper;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrashDashboardHelper {

    public static void showTrashDashboard(MainActivity activity) {
        if (activity.currentSnackbar != null) {
            activity.currentSnackbar.dismiss();
            activity.currentSnackbar = null;
        }

        LayoutInflater inflater = activity.getLayoutInflater();
        View trashView = inflater.inflate(R.layout.layout_trash_dashboard, activity.mainContainer, false);
        trashView.setVisibility(View.VISIBLE);

        ImageView btnBack = trashView.findViewById(R.id.btn_trash_dashboard_back);
        ResponsiveUI.setupClickable(btnBack, false, () -> {
            DashboardHelper.showDashboard(activity);
        });

        LinearLayout listContainer = trashView.findViewById(R.id.trash_list_container);
        listContainer.removeAllViews();

        if (activity.appStorage.trashAccounts == null || activity.appStorage.trashAccounts.isEmpty()) {
            TextView emptyText = new TextView(activity);
            emptyText.setText("Trash is empty.");
            emptyText.setTextColor(ThemeManager.getSecondaryAccentColor(activity));
            emptyText.setTextSize(16f);
            emptyText.setPadding(32, 64, 32, 32);
            emptyText.setGravity(android.view.Gravity.CENTER);
            listContainer.addView(emptyText);
        } else {
            for (Account account : activity.appStorage.trashAccounts) {
                View card = inflater.inflate(R.layout.layout_item_trash_card, listContainer, false);
                
                TextView titleView = card.findViewById(R.id.trash_card_title);
                TextView itemsCountView = card.findViewById(R.id.trash_card_items_count);
                ImageView btnRestore = card.findViewById(R.id.btn_trash_card_restore);

                titleView.setText(account.getTitle());
                
                int totalItems = 0;
                if (account.getRecords() != null) totalItems += account.getRecords().size();
                if (account.getBudgetRecords() != null) totalItems += account.getBudgetRecords().size();
                
                itemsCountView.setText(totalItems + " items");

                card.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.5f, 12f));

                ResponsiveUI.setupClickable(card, false, () -> {
                    com.example.notecalc.tools.trash.TrashListHelper.showTrashList(activity, account);
                });

                ResponsiveUI.setupClickable(btnRestore, false, () -> {
                    java.util.List<com.example.notecalc.records.models.Record> allRecords = new java.util.ArrayList<>();
                    if (account.getRecords() != null) allRecords.addAll(account.getRecords());
                    if (account.getBudgetRecords() != null) allRecords.addAll(account.getBudgetRecords());
                    
                    if (allRecords.isEmpty()) return;
                    
                    if (allRecords.size() > 2) {
                        com.example.notecalc.records.dialogs.RecordDialogHelper.showRestoreMultipleConfirmationDialog(activity, account, allRecords, () -> {
                            if (account.getRecords() != null && !account.getRecords().isEmpty()) {
                                com.example.notecalc.tools.trash.TrashActionEngine.restoreRecords(activity, account, new java.util.ArrayList<>(account.getRecords()), false);
                            }
                            if (account.getBudgetRecords() != null && !account.getBudgetRecords().isEmpty()) {
                                com.example.notecalc.tools.trash.TrashActionEngine.restoreRecords(activity, account, new java.util.ArrayList<>(account.getBudgetRecords()), true);
                            }
                            showTrashDashboard(activity);
                        });
                    } else {
                        if (account.getRecords() != null && !account.getRecords().isEmpty()) {
                            com.example.notecalc.tools.trash.TrashActionEngine.restoreRecords(activity, account, new java.util.ArrayList<>(account.getRecords()), false);
                        }
                        if (account.getBudgetRecords() != null && !account.getBudgetRecords().isEmpty()) {
                            com.example.notecalc.tools.trash.TrashActionEngine.restoreRecords(activity, account, new java.util.ArrayList<>(account.getBudgetRecords()), true);
                        }
                        showTrashDashboard(activity);
                    }
                });

                listContainer.addView(card);
            }
        }

        ResponsiveUI.applyResponsiveness(trashView);
        
        activity.mainContainer.removeAllViews();
        activity.mainContainer.addView(trashView);
    }
}
