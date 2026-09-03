package com.example.notecalc;

import android.content.Context;
import android.view.LayoutInflater;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.EditText;
import java.util.List;
import com.example.notecalc.pdf.PdfSortOrder;
import com.example.notecalc.pdf.PdfSortCallback;
import android.view.View;
import android.widget.TextView;
import android.app.DatePickerDialog;
import androidx.appcompat.app.AlertDialog;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class DialogHelper {

    public static void showTipsDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_tips, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Setup expandable sections
        int[] headerIds = {R.id.header_section_1, R.id.header_section_2, R.id.header_section_3, R.id.header_section_4, R.id.header_section_5};
        int[] contentIds = {R.id.content_section_1, R.id.content_section_2, R.id.content_section_3, R.id.content_section_4, R.id.content_section_5};
        int[] chevronIds = {R.id.tv_chevron_1, R.id.tv_chevron_2, R.id.tv_chevron_3, R.id.tv_chevron_4, R.id.tv_chevron_5};

        for (int i = 0; i < 5; i++) {
            View header = view.findViewById(headerIds[i]);
            View content = view.findViewById(contentIds[i]);
            TextView chevron = view.findViewById(chevronIds[i]);
            
            if (header != null && content != null && chevron != null) {
                // Apply curved bordered background to header
                header.setBackground(ResponsiveUI.createRoundedBg(
                        context,
                        ThemeManager.getBgSecondaryColor(context),
                        ThemeManager.getBorderColor(context),
                        1.0f,
                        10.0f
                ));
                
                // Add some margin below the header so the border isn't cramped
                android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) header.getLayoutParams();
                params.bottomMargin = 24;
                header.setLayoutParams(params);

                header.setOnClickListener(v -> {
                    if (content.getVisibility() == View.VISIBLE) {
                        content.setVisibility(View.GONE);
                        chevron.setText("▼"); // down chevron
                    } else {
                        content.setVisibility(View.VISIBLE);
                        chevron.setText("▲"); // up chevron
                    }
                });
            }
        }
        
        // Round the dialog box corners
        view.setBackground(ResponsiveUI.createRoundedBg(
                context,
                ThemeManager.getBgPrimaryColor(context),
                android.graphics.Color.TRANSPARENT,
                0f,
                16f
        ));
        
        // Round the Got it! button corners
        View btnClose = view.findViewById(R.id.btn_tips_close);
        btnClose.setBackground(ResponsiveUI.createRoundedBg(
                context,
                ThemeManager.getSecondaryAccentColor(context),
                android.graphics.Color.TRANSPARENT,
                0f,
                12f
        ));

        ResponsiveUI.setupClickable(btnClose, true, dialog::dismiss);
        dialog.show();
    }

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
                    + "   " + AppUtils.formatDateCompact(r.getDate())
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
public static void showPdfSortDialog(MainActivity activity, PdfSortCallback callback) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity, R.style.CustomDialogTheme);
        android.view.View view = activity.getLayoutInflater().inflate(R.layout.layout_dialog_pdf_sort, null);
        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();

        TextView optSno = view.findViewById(R.id.option_sort_sno);
        TextView optDesc = view.findViewById(R.id.option_sort_desc);
        TextView optDate = view.findViewById(R.id.option_sort_date);
        TextView optAmount = view.findViewById(R.id.option_sort_amount);
        
        android.graphics.drawable.Drawable unselectedBg = ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 8.0f);
        android.graphics.drawable.Drawable selectedBg = ResponsiveUI.createRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), ThemeManager.getPrimaryAccentColor(activity), 1.0f, 8.0f);
        
        final PdfSortOrder[] selectedOrder = {PdfSortOrder.SNO}; // Default
        
        Runnable updateSelection = () -> {
            optSno.setBackground(selectedOrder[0] == PdfSortOrder.SNO ? selectedBg : unselectedBg);
            optDesc.setBackground(selectedOrder[0] == PdfSortOrder.DESCRIPTION ? selectedBg : unselectedBg);
            optDate.setBackground(selectedOrder[0] == PdfSortOrder.DATE ? selectedBg : unselectedBg);
            optAmount.setBackground(selectedOrder[0] == PdfSortOrder.AMOUNT ? selectedBg : unselectedBg);
            
            optSno.setTextColor(selectedOrder[0] == PdfSortOrder.SNO ? android.graphics.Color.WHITE : activity.getColor(R.color.text_primary));
            optDesc.setTextColor(selectedOrder[0] == PdfSortOrder.DESCRIPTION ? android.graphics.Color.WHITE : activity.getColor(R.color.text_primary));
            optDate.setTextColor(selectedOrder[0] == PdfSortOrder.DATE ? android.graphics.Color.WHITE : activity.getColor(R.color.text_primary));
            optAmount.setTextColor(selectedOrder[0] == PdfSortOrder.AMOUNT ? android.graphics.Color.WHITE : activity.getColor(R.color.text_primary));
        };
        
        optSno.setOnClickListener(v -> { selectedOrder[0] = PdfSortOrder.SNO; updateSelection.run(); });
        optDesc.setOnClickListener(v -> { selectedOrder[0] = PdfSortOrder.DESCRIPTION; updateSelection.run(); });
        optDate.setOnClickListener(v -> { selectedOrder[0] = PdfSortOrder.DATE; updateSelection.run(); });
        optAmount.setOnClickListener(v -> { selectedOrder[0] = PdfSortOrder.AMOUNT; updateSelection.run(); });
        
        updateSelection.run(); // Init

        android.view.View btnCancel = view.findViewById(R.id.btn_dialog_cancel);
        android.view.View btnExport = view.findViewById(R.id.btn_dialog_export);
        
        view.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.5f, 12f));

        btnCancel.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 8.0f));
        btnExport.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), ThemeManager.getPrimaryAccentColor(activity), 1.0f, 8.0f));

        ResponsiveUI.setupClickable(btnCancel, true, dialog::dismiss);
        ResponsiveUI.setupClickable(btnExport, true, () -> {
            dialog.dismiss();
            callback.onSortSelected(selectedOrder[0]);
        });

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    public static android.app.Dialog showProgressDialog(MainActivity activity) {
        android.app.Dialog dialog = new android.app.Dialog(activity);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        android.widget.LinearLayout layout = new android.widget.LinearLayout(activity);
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setPadding(60, 60, 60, 60);
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        layout.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 16.0f));

        android.widget.ProgressBar progressBar = new android.widget.ProgressBar(activity);
        progressBar.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(ThemeManager.getPrimaryAccentColor(activity)));

        android.widget.TextView tvMessage = new android.widget.TextView(activity);
        tvMessage.setText(activity.getString(R.string.msg_generating_pdf));
        tvMessage.setTextColor(activity.getColor(R.color.text_primary));
        tvMessage.setTextSize(16f);
        tvMessage.setPadding(40, 0, 0, 0);

        layout.addView(progressBar);
        layout.addView(tvMessage);

        dialog.setContentView(layout);
        dialog.show();

        return dialog;
    }

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
        tvDate.setText(accountDateFormatted + " (" + AppUtils.formatDateCompact(accountDateFormatted) + ")");

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
}
