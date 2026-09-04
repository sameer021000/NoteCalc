package com.example.notecalc;

import android.view.View;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Locale;

public class FilterDateHelper {

    public static void showDialog(MainActivity activity) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_date_range_dialog, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        TextView tvFrom = dialogView.findViewById(R.id.dialog_date_from);
        TextView tvTo = dialogView.findViewById(R.id.dialog_date_to);
        TextView btnClear = dialogView.findViewById(R.id.btn_dialog_clear);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnApply = dialogView.findViewById(R.id.btn_dialog_apply);

        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.5f, 12f));
        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 6f));
        tvFrom.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        tvTo.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        btnClear.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        btnCancel.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 4f));
        btnApply.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), ThemeManager.getPrimaryAccentColor(activity), 0f, 4f));
        btnApply.setTextColor(activity.getColor(R.color.text_primary));

        final String[] tempFrom = {StateHelper.getFilterDateFrom(activity)};
        final String[] tempTo = {StateHelper.getFilterDateTo(activity)};

        tvFrom.setText(tempFrom[0] != null ? tempFrom[0] : "Select Date");
        tvTo.setText(tempTo[0] != null ? tempTo[0] : "Select Date");

        Runnable pickFrom = () -> {
            Calendar cal = Calendar.getInstance();
            if (tempFrom[0] != null) {
                try {
                    java.util.Date parsed = new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(tempFrom[0]);
                    if (parsed != null) cal.setTime(parsed);
                } catch (Exception ignored) {}
            }
            new android.app.DatePickerDialog(activity, (view, year, month, day) -> {
                String picked = String.format(Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year);
                tempFrom[0] = picked;
                tvFrom.setText(picked);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        };
        Runnable pickTo = () -> {
            Calendar cal = Calendar.getInstance();
            if (tempTo[0] != null) {
                try {
                    java.util.Date parsed = new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(tempTo[0]);
                    if (parsed != null) cal.setTime(parsed);
                } catch (Exception ignored) {}
            }
            new android.app.DatePickerDialog(activity, (view, year, month, day) -> {
                String picked = String.format(Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year);
                tempTo[0] = picked;
                tvTo.setText(picked);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        };

        tvFrom.setOnClickListener(v -> pickFrom.run());
        tvTo.setOnClickListener(v -> pickTo.run());

        ResponsiveUI.setupClickable(btnClear, true, () -> {
            StateHelper.setFilterDateFrom(activity, null);
            StateHelper.setFilterDateTo(activity, null);
            if (activity.recordsAdapter != null) activity.recordsAdapter.setFilter(activity.currentRecordSearchQuery);
            EditorSortHelper.updateDateHeaderIndicator(activity);
            dialog.dismiss();
        });
        ResponsiveUI.setupClickable(btnCancel, true, dialog::dismiss);
        ResponsiveUI.setupClickable(btnApply, true, () -> {
            StateHelper.setFilterDateFrom(activity, tempFrom[0]);
            StateHelper.setFilterDateTo(activity, tempTo[0]);
            if (activity.recordsAdapter != null) activity.recordsAdapter.setFilter(activity.currentRecordSearchQuery);
            EditorSortHelper.updateDateHeaderIndicator(activity);
            dialog.dismiss();
        });

        ResponsiveUI.applyResponsiveness(dialogView);
        dialog.show();
    }
}
