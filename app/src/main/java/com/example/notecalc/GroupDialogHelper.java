package com.example.notecalc;

import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class GroupDialogHelper {

    public static void showDeleteGroupConfirmation(MainActivity activity, AccountGroup group) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_dialog_confirm_delete_group, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        TextView tvDetails = dialogView.findViewById(R.id.text_group_details);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnDelete = dialogView.findViewById(R.id.btn_dialog_delete);

        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.5f, 12f));
        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 6f));
        btnCancel.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#20EF4444"), 4.0f));
        btnCancel.setTextColor(activity.getColor(R.color.text_primary));
        btnDelete.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#20EF4444"), 4.0f));
        btnDelete.setTextColor(activity.getColor(R.color.error_red));

        StringBuilder details = new StringBuilder();
        int listCount = group.getAccounts().size();
        details.append("This group contains ").append(listCount).append(listCount == 1 ? " list" : " lists").append(".");
        if (listCount > 0) {
            details.append("\n\nLists:");
            for (Account acc : group.getAccounts()) {
                details.append("\n• ").append(acc.getTitle());
            }
        }
        tvDetails.setText(details.toString());

        ResponsiveUI.setupClickable(btnCancel, false, dialog::cancel);
        ResponsiveUI.setupClickable(btnDelete, false, () -> {
            activity.appStorage.groups.remove(group);
            StorageHelper.saveAppStorage(activity, activity.appStorage);
            DashboardHelper.refreshDashboardList(activity);
            dialog.dismiss();
        });

        dialog.show();
    }

    public static void showCreateGroupDialog(MainActivity activity) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_dialog_create_group, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        EditText input = dialogView.findViewById(R.id.edit_group_name);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnApply = dialogView.findViewById(R.id.btn_dialog_apply);

        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.5f, 12f));
        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 6f));
        btnCancel.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#20EF4444"), 4.0f));
        btnCancel.setTextColor(activity.getColor(R.color.error_red));
        btnApply.setBackground(ResponsiveUI.createButtonSelector(activity, ThemeManager.getPrimaryAccentColor(activity), 4.0f));
        btnApply.setTextColor(activity.getColor(R.color.text_primary));

        ResponsiveUI.setupClickable(btnCancel, false, dialog::cancel);
        ResponsiveUI.setupClickable(btnApply, false, () -> {
            String title = input.getText().toString().trim();
            if (!title.isEmpty()) {
                AccountGroup group = new AccountGroup(title);
                activity.appStorage.groups.add(group);
                StorageHelper.saveAppStorage(activity, activity.appStorage);
                DashboardHelper.refreshDashboardList(activity);
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
