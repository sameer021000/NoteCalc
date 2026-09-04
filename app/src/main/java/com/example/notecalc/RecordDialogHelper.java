package com.example.notecalc;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordDialogHelper {

    public interface OnDateSelectedListener {
        void onDateSelected(String date);
    }

    public static void showDatePicker(Context context, String initialDate, TextView dateTextWidget, OnDateSelectedListener listener) {
        Calendar cal = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            if (initialDate != null && !initialDate.isEmpty()) {
                Date date = sdf.parse(initialDate);
                if (date != null) {
                    cal.setTime(date);
                }
            }
        } catch (Exception ignored) {}

        DatePickerDialog picker = new DatePickerDialog(
                context,
                (view1, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    String formattedDate = sdf.format(selected.getTime());
                    if (dateTextWidget != null) {
                        dateTextWidget.setText(formattedDate);
                    }
                    if (listener != null) {
                        listener.onDateSelected(formattedDate);
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    @android.annotation.SuppressLint("SetTextI18n")
    public static void showDeleteMultipleConfirmationDialog(MainActivity activity, List<Record> selectedRecords) {
        if (selectedRecords.size() <= 2) {
            for (Record r : selectedRecords) {
                int idx = StateHelper.getActiveRecords(activity).indexOf(r);
                if (idx != -1) {
                    if (activity.editingRecordIndex == idx) {
                        EditorModeHelper.cancelEditRecordMode(activity);
                    } else if (activity.editingRecordIndex > idx) {
                        activity.editingRecordIndex--;
                    }
                }
            }
            StateHelper.getActiveRecords(activity).removeAll(selectedRecords);
            EditorUIHelper.populateRecordsList(activity);
            BulkActionsHelper.updateBulkActionsState(activity);
            EditorSortHelper.updateHeaderLabels(activity);
            return;
        }

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_delete_multiple_dialog, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        LinearLayout selectedItemsList = dialogView.findViewById(R.id.selected_items_list);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnDelete = dialogView.findViewById(R.id.btn_dialog_delete);

        // Style dialog
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
                0,
                0,
                4f
        ));

        // Populate selected items list inside the dialog
        for (Record r : selectedRecords) {
            // Build a simple text row for each selected item
            TextView rowView = new TextView(activity);
            String lineText = "• " + r.getDescription()
                    + "   " + DateUtils.formatDateCompact(r.getDate())
                    + "   " + String.format(Locale.getDefault(), "%.2f", r.getAmount());
            rowView.setText(lineText);
            rowView.setTextColor(activity.getColor(R.color.text_primary));
            rowView.setTextSize(13f);
            int padPx = (int) (6 * activity.getResources().getDisplayMetrics().density);
            rowView.setPadding(0, padPx, 0, padPx);

            // Show remarks below if present
            String remarks = r.getRemarks();
            boolean hasRemarks = (remarks != null && !remarks.isEmpty());
            boolean hasAttachments = (r.getAttachments() != null && !r.getAttachments().isEmpty());
            
            if (hasRemarks || hasAttachments) {
                LinearLayout rowContainer = new LinearLayout(activity);
                rowContainer.setOrientation(LinearLayout.VERTICAL);
                rowContainer.addView(rowView);
                
                if (hasRemarks) {
                    TextView remarksView = new TextView(activity);
                    remarksView.setText("  ↳ " + remarks);
                    remarksView.setTextColor(activity.getColor(R.color.text_tertiary));
                    remarksView.setTextSize(11f);
                    remarksView.setTypeface(null, android.graphics.Typeface.ITALIC);
                    remarksView.setPadding(0, 0, 0, hasAttachments ? 0 : padPx);
                    rowContainer.addView(remarksView);
                }
                
                if (hasAttachments) {
                    TextView attachView = new TextView(activity);
                    attachView.setText("  \uD83D\uDCCE " + r.getAttachments().size() + " attached file(s)");
                    attachView.setTextColor(ThemeManager.getSecondaryAccentColor(activity));
                    attachView.setTextSize(11f);
                    attachView.setPadding(0, hasRemarks ? (padPx / 2) : 0, 0, padPx);
                    rowContainer.addView(attachView);
                }
                
                selectedItemsList.addView(rowContainer);
            } else {
                selectedItemsList.addView(rowView);
            }

            // Add a thin divider between items (except last)
            if (selectedRecords.indexOf(r) < selectedRecords.size() - 1) {
                View divider = new View(activity);
                divider.setBackgroundColor(ThemeManager.getBorderColor(activity));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(lp);
                selectedItemsList.addView(divider);
            }
        }

        ResponsiveUI.applyResponsiveness(dialogView);

        ResponsiveUI.setupClickable(btnCancel, dialog::dismiss);
        ResponsiveUI.setupClickable(btnDelete, () -> {
            dialog.dismiss();
            // Deselect and adjust activity.editingRecordIndex before removal
            for (Record r : selectedRecords) {
                int idx = StateHelper.getActiveRecords(activity).indexOf(r);
                if (idx != -1) {
                    if (activity.editingRecordIndex == idx) {
                        EditorModeHelper.cancelEditRecordMode(activity);
                    } else if (activity.editingRecordIndex > idx) {
                        activity.editingRecordIndex--;
                    }
                }
            }
            StateHelper.getActiveRecords(activity).removeAll(selectedRecords);
            EditorUIHelper.populateRecordsList(activity);
            BulkActionsHelper.updateBulkActionsState(activity);
            EditorSortHelper.updateHeaderLabels(activity);
        });

        dialog.show();
    }
}
