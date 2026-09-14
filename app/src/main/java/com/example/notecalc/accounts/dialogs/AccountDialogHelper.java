package com.example.notecalc.accounts.dialogs;
import com.example.notecalc.storage.core.*;
import com.example.notecalc.core.utils.*;
import com.example.notecalc.core.ui.*;
import com.example.notecalc.accounts.models.*;
import com.example.notecalc.*;
import com.example.notecalc.dashboard.*;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AccountDialogHelper {

    @android.annotation.SuppressLint("SetTextI18n")
    public static void showDeleteAccountConfirmationDialog(MainActivity activity, final Account account) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_delete_account_dialog, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        TextView tvAccountTitle = dialogView.findViewById(R.id.dialog_account_title);
        TextView tvItemsCount = dialogView.findViewById(R.id.dialog_account_items_count);
        TextView tvAmount = dialogView.findViewById(R.id.dialog_account_amount);
        TextView tvDate = dialogView.findViewById(R.id.dialog_account_date);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnDelete = dialogView.findViewById(R.id.btn_dialog_delete);

        // Populate account details
        tvAccountTitle.setText(account.getTitle());
        tvItemsCount.setText(String.valueOf(account.getRecords().size()));
        tvAmount.setText(String.format(Locale.getDefault(), "%.2f", account.calculateTotal()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String accountDateFormatted = sdf.format(new Date(account.getLastModified()));
        tvDate.setText(accountDateFormatted + " (" + DateUtils.formatDateCompact(accountDateFormatted) + ")");

        // Apply premium styling
        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgSecondaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.5f,
                12f
        ));

        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgPrimaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                6f
        ));

        btnCancel.setBackground(ResponsiveUI.createRippleRoundedBg(
                activity,
                ThemeManager.getBgPrimaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                4f
        ));

        btnDelete.setBackground(ResponsiveUI.createRippleRoundedBg(
                activity,
                activity.getColor(R.color.error_red),
                activity.getColor(R.color.error_red),
                0f,
                4f
        ));

        ResponsiveUI.applyResponsiveness(dialogView);

        ResponsiveUI.setupClickable(btnCancel, true, dialog::dismiss);
        ResponsiveUI.setupClickable(btnDelete, true, () -> {
            dialog.dismiss();
            if (activity.currentViewGroup != null) {
                activity.currentViewGroup.getAccounts().remove(account);
            } else {
                activity.appStorage.standaloneAccounts.remove(account);
            }
            StorageHelper.saveAppStorage(activity, activity.appStorage);
            DashboardHelper.refreshDashboardList(activity);
        });

        dialog.show();
    }

    public static void showMoveAccountDialog(MainActivity activity, Account account) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_dialog_move_group, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        LinearLayout detailsContainer = dialogView.findViewById(R.id.details_container);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);

        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.5f, 12f));
        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 6f));
        btnCancel.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#20EF4444"), 4.0f));
        btnCancel.setTextColor(activity.getColor(R.color.error_red));

        List<AccountGroup> targetGroups = new ArrayList<>();
        for (AccountGroup g : activity.appStorage.groups) {
            if (g.isArchived() == account.isArchived()) targetGroups.add(g);
        }

        if (targetGroups.isEmpty()) {
            Toast.makeText(activity, activity.getString(R.string.auto_no_groups_available__9), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }

        for (int i = 0; i < targetGroups.size(); i++) {
            final AccountGroup selectedGroup = targetGroups.get(i);
            TextView tvGroup = new TextView(activity);
            tvGroup.setText(selectedGroup.getTitle());
            tvGroup.setTextColor(activity.getColor(R.color.text_primary));
            tvGroup.setTextSize(16f);
            tvGroup.setPadding(32, 24, 32, 24);
            tvGroup.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#15FFFFFF"), 4.0f));
            ResponsiveUI.setupClickable(tvGroup, false, () -> {
                activity.appStorage.standaloneAccounts.remove(account);
                selectedGroup.getAccounts().add(account);
                selectedGroup.updateLastModified();
                StorageHelper.saveAppStorage(activity, activity.appStorage);
                DashboardHelper.refreshDashboardList(activity);
                Toast.makeText(activity, "Moved to " + selectedGroup.getTitle(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
            detailsContainer.addView(tvGroup);

            if (i < targetGroups.size() - 1) {
                View divider = new View(activity);
                divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(ThemeManager.getBorderColor(activity));
                detailsContainer.addView(divider);
            }
        }

        ResponsiveUI.setupClickable(btnCancel, false, dialog::cancel);
        dialog.show();
    }

    public static void showCreateAccountDialog(MainActivity activity, AccountGroup targetGroup) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_dialog_create_group, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        android.widget.EditText input = dialogView.findViewById(R.id.edit_group_name);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnApply = dialogView.findViewById(R.id.btn_dialog_apply);

        titleView.setText(R.string.create_list_title);
        input.setHint(R.string.create_list_name_hint);
        btnApply.setText(R.string.create_group_btn_create);

        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.5f, 12f));
        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 6f));
        btnCancel.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#20EF4444"), 4.0f));
        btnCancel.setTextColor(activity.getColor(R.color.error_red));
        btnApply.setBackground(ResponsiveUI.createButtonSelector(activity, ThemeManager.getPrimaryAccentColor(activity), 4.0f));
        btnApply.setTextColor(activity.getColor(R.color.text_primary));

        ResponsiveUI.setupClickable(btnCancel, false, dialog::cancel);
        ResponsiveUI.setupClickable(btnApply, false, () -> {
            String title = input.getText().toString().trim();
            activity.currentEditingAccount = null; // Ensure validation treats it as new
            if (!com.example.notecalc.editor.core.EditorValidationHelper.validateAccountTitle(activity, title)) {
                return;
            }

            Account newAccount = new Account(title);
            if (targetGroup != null) {
                targetGroup.getAccounts().add(newAccount);
                targetGroup.updateLastModified();
            } else {
                activity.appStorage.standaloneAccounts.add(newAccount);
            }
            StorageHelper.saveAppStorage(activity, activity.appStorage);
            DashboardHelper.refreshDashboardList(activity);
            dialog.dismiss();
            
            // Open editor for the newly created account
            activity.currentViewGroup = targetGroup;
            com.example.notecalc.editor.EditorHelper.openEditor(activity, newAccount);
        });

        dialog.show();
    }

    public static void showRenameAccountDialog(MainActivity activity, Account account) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_dialog_create_group, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        android.widget.EditText input = dialogView.findViewById(R.id.edit_group_name);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnApply = dialogView.findViewById(R.id.btn_dialog_apply);

        titleView.setText(R.string.rename_list_title);
        btnApply.setText(R.string.rename_group_btn);
        
        input.setText(account.getTitle());
        input.setSelection(input.getText().length());

        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.5f, 12f));
        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 6f));
        btnCancel.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#20EF4444"), 4.0f));
        btnCancel.setTextColor(activity.getColor(R.color.error_red));
        btnApply.setBackground(ResponsiveUI.createButtonSelector(activity, ThemeManager.getPrimaryAccentColor(activity), 4.0f));
        btnApply.setTextColor(activity.getColor(R.color.text_primary));

        ResponsiveUI.setupClickable(btnCancel, false, dialog::cancel);
        ResponsiveUI.setupClickable(btnApply, false, () -> {
            String newTitle = input.getText().toString().trim();
            if (newTitle.equalsIgnoreCase(account.getTitle())) {
                dialog.dismiss();
                return;
            }
            
            activity.currentEditingAccount = account;
            activity.originalTitle = account.getTitle();
            if (!com.example.notecalc.editor.core.EditorValidationHelper.validateAccountTitle(activity, newTitle)) {
                return;
            }
            
            account.setTitle(newTitle);
            StorageHelper.saveAppStorage(activity, activity.appStorage);
            DashboardHelper.refreshDashboardList(activity);
            dialog.dismiss();
        });

        dialog.show();
    }
}