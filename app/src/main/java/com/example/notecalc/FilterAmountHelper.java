package com.example.notecalc;

import android.view.View;
import android.widget.TextView;
import java.util.Locale;

public class FilterAmountHelper {

    public static void showDialog(MainActivity activity) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_amount_range_dialog, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        android.widget.EditText etFrom = dialogView.findViewById(R.id.dialog_amount_from);
        android.widget.EditText etTo = dialogView.findViewById(R.id.dialog_amount_to);
        TextView btnClear = dialogView.findViewById(R.id.btn_dialog_clear);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnApply = dialogView.findViewById(R.id.btn_dialog_apply);

        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.5f, 12f));
        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 6f));
        etFrom.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        etTo.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        btnClear.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        btnCancel.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        btnApply.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), ThemeManager.getPrimaryAccentColor(activity), 0f, 4f));
        btnApply.setTextColor(activity.getColor(R.color.text_primary));

        if (StateHelper.getFilterAmountFrom(activity) != null) etFrom.setText(String.format(Locale.getDefault(), "%.2f", StateHelper.getFilterAmountFrom(activity)));
        if (StateHelper.getFilterAmountTo(activity) != null) etTo.setText(String.format(Locale.getDefault(), "%.2f", StateHelper.getFilterAmountTo(activity)));

        ResponsiveUI.setupClickable(btnClear, true, () -> {
            StateHelper.setFilterAmountFrom(activity, null);
            StateHelper.setFilterAmountTo(activity, null);
            if (activity.recordsAdapter != null) activity.recordsAdapter.setFilter(activity.currentRecordSearchQuery);
            EditorSortHelper.updateAmountHeaderIndicator(activity);
            dialog.dismiss();
        });
        ResponsiveUI.setupClickable(btnCancel, true, dialog::dismiss);
        ResponsiveUI.setupClickable(btnApply, true, () -> {
            String fromStr = etFrom.getText().toString().trim();
            String toStr = etTo.getText().toString().trim();
            StateHelper.setFilterAmountFrom(activity, fromStr.isEmpty() ? null : Double.parseDouble(fromStr));
            StateHelper.setFilterAmountTo(activity, toStr.isEmpty() ? null : Double.parseDouble(toStr));
            if (activity.recordsAdapter != null) activity.recordsAdapter.setFilter(activity.currentRecordSearchQuery);
            EditorSortHelper.updateAmountHeaderIndicator(activity);
            dialog.dismiss();
        });

        ResponsiveUI.applyResponsiveness(dialogView);
        dialog.show();
    }
}
