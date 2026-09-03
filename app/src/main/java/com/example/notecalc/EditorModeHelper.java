package com.example.notecalc;

import android.view.View;

public class EditorModeHelper {
    public static void enterEditRecordMode(MainActivity activity, int index, Record record) {
        activity.editingRecordIndex = index;
        activity.selectedRecordDate = record.getDate();

        activity.editDescField.setText(record.getDescription());
        activity.editAmountField.setText(String.format(java.util.Locale.getDefault(), "%.2f", record.getAmount()));
        activity.editRemarksField.setText(record.getRemarks());
        activity.btnRecordDateField.setText(activity.selectedRecordDate);
        if (activity.editCategoryField != null) activity.editCategoryField.setText(record.getCategory() == null ? "" : record.getCategory());

        // Load attachments
        activity.tempAttachments.clear();
        if (record.getAttachments() != null) activity.tempAttachments.addAll(record.getAttachments());
        AttachmentHelper.renderEditorAttachments(activity);

        // Auto-expand form
        if (activity.formInputsContainer != null && activity.btnToggleForm != null) {
            activity.isFormInputsCollapsed = false;
            activity.formInputsContainer.setVisibility(android.view.View.VISIBLE);
            activity.btnToggleForm.setText(activity.getString(R.string.auto_minimize_21));
        }

        if (activity.isBudgetMode) {
            activity.labelAddRecordField.setText(activity.getString(R.string.auto_edit_budget_22));
            activity.btnAddRecordField.setText(activity.getString(R.string.auto_edit_budget_23));
            activity.editDescField.setHint(activity.getString(R.string.auto_description_32));
            activity.editRemarksField.setHint(activity.getString(R.string.auto_remarks_optional_33));
        } else {
            activity.labelAddRecordField.setText(R.string.label_edit_record);
            activity.btnAddRecordField.setText(R.string.btn_edit_record);
            activity.editDescField.setHint(R.string.hint_record_desc);
            activity.editRemarksField.setHint(activity.getString(R.string.auto_remarks_e_g_bought_a_34));
        }
        activity.btnCancelEditField.setVisibility(View.VISIBLE);
        EditorUIHelper.populateRecordsList(activity);
    }

    public static void cancelEditRecordMode(MainActivity activity) {
        activity.editingRecordIndex = -1;
        activity.selectedRecordDate = AppUtils.getCurrentDateString();

        activity.editDescField.setText("");
        activity.editAmountField.setText("");
        activity.editRemarksField.setText("");
        activity.btnRecordDateField.setText(activity.selectedRecordDate);
        
        activity.tempAttachments.clear();
        AttachmentHelper.renderEditorAttachments(activity);

        if (activity.isBudgetMode) {
            activity.labelAddRecordField.setText(activity.getString(R.string.auto_add_budget_24));
            activity.btnAddRecordField.setText(activity.getString(R.string.auto_add_budget_25));
            activity.editDescField.setHint(activity.getString(R.string.auto_description_35));
            activity.editRemarksField.setHint(activity.getString(R.string.auto_remarks_optional_36));
        } else {
            activity.labelAddRecordField.setText(R.string.label_add_record);
            activity.btnAddRecordField.setText(R.string.btn_add_record);
            activity.editDescField.setHint(R.string.hint_record_desc);
            activity.editRemarksField.setHint(activity.getString(R.string.auto_remarks_e_g_bought_a_37));
        }

        activity.btnCancelEditField.setVisibility(View.GONE);

        EditorUIHelper.populateRecordsList(activity);
    }

    public static void setupModeToggleUI(MainActivity activity, android.widget.TextView btnModeExpenses, android.widget.TextView btnModeBudget, android.widget.TextView textRemainingPurse) {
        Runnable updateModeToggleUI = () -> {
            if (btnModeExpenses != null && btnModeBudget != null) {
                btnModeExpenses.setBackgroundColor(activity.isBudgetMode ? ThemeManager.getBgSecondaryColor(activity) : ThemeManager.getPrimaryAccentColor(activity));
                btnModeExpenses.setTextColor(activity.getColor(activity.isBudgetMode ? R.color.text_tertiary : R.color.text_on_accent));
                
                btnModeBudget.setBackgroundColor(activity.isBudgetMode ? ThemeManager.getPrimaryAccentColor(activity) : ThemeManager.getBgSecondaryColor(activity));
                btnModeBudget.setTextColor(activity.getColor(activity.isBudgetMode ? R.color.text_on_accent : R.color.text_tertiary));
            }
            EditorSortHelper.applySorting(activity);
            if (activity.recordsAdapter != null) activity.recordsAdapter.setFilter(activity.currentRecordSearchQuery);
            EditorSortHelper.updateHeaderLabels(activity);
            EditorSortHelper.updateDateHeaderIndicator(activity);
            EditorSortHelper.updateAmountHeaderIndicator(activity);
            
            if (textRemainingPurse != null) {
                if (activity.tempBudgetRecords.isEmpty() && !activity.isBudgetMode) {
                    textRemainingPurse.setVisibility(android.view.View.GONE);
                } else {
                    textRemainingPurse.setVisibility(android.view.View.VISIBLE);
                    double totalBudget = 0;
                    for (Record r : activity.tempBudgetRecords) totalBudget += r.getAmount();
                    double totalExpenses = 0;
                    for (Record r : activity.tempRecords) totalExpenses += r.getAmount();
                    double remaining = totalBudget - totalExpenses;
                    textRemainingPurse.setText(String.format(java.util.Locale.getDefault(), "Balance : %.2f", remaining));
                }
            }
        };
        
        if (btnModeExpenses != null) ResponsiveUI.setupClickable(btnModeExpenses, false, () -> {
            if (activity.isBudgetMode) {
                activity.isBudgetMode = false;
                EditorModeHelper.cancelEditRecordMode(activity);
                updateModeToggleUI.run();
            }
        });
        if (btnModeBudget != null) ResponsiveUI.setupClickable(btnModeBudget, false, () -> {
            if (!activity.isBudgetMode) {
                activity.isBudgetMode = true;
                EditorModeHelper.cancelEditRecordMode(activity);
                updateModeToggleUI.run();
            }
        });
        updateModeToggleUI.run();
    }
}
