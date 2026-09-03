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
        activity.populateRecordsList();
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

        activity.populateRecordsList();
    }



}
