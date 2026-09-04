package com.example.notecalc;

import android.widget.EditText;

public class EditorSaveHelper {
    public static void setupSaveActions(MainActivity activity, EditText editTitle, EditText editDesc, EditText editAmount, android.widget.TextView btnAdd, android.widget.TextView btnSave) {
        ResponsiveUI.setupClickable(btnAdd, () -> {
            String desc = editDesc.getText().toString().trim();
            String amountStr = editAmount.getText().toString().trim();
            String remarks = activity.editRemarksField != null ? activity.editRemarksField.getText().toString().trim() : "";
            String category = activity.editCategoryField != null ? activity.editCategoryField.getText().toString().trim() : "";

            Double amount = EditorValidationHelper.validateRecordInput(activity, desc, amountStr);
            if (amount == null) return;

            if (activity.editingRecordIndex != -1) {
                Record record = StateHelper.getActiveRecords(activity).get(activity.editingRecordIndex);
                record.setDescription(desc);
                record.setAmount(amount);
                record.setDate(activity.selectedRecordDate);
                record.setRemarks(remarks);
                record.setCategory(category);
                record.setAttachments(new java.util.ArrayList<>(activity.tempAttachments));
                record.setTimestampMillis(System.currentTimeMillis());
                EditorSortHelper.applySorting(activity);
                EditorModeHelper.cancelEditRecordMode(activity);
            } else {
                Record newRecord = new Record(desc, amount, activity.selectedRecordDate);
                newRecord.setRemarks(remarks);
                newRecord.setCategory(category);
                newRecord.setAttachments(new java.util.ArrayList<>(activity.tempAttachments));
                newRecord.setOriginalIndex(EditorUIHelper.getNewOriginalIndex(activity));
                StateHelper.getActiveRecords(activity).add(newRecord);

                editDesc.setText("");
                editAmount.setText("");
                if (activity.editRemarksField != null) activity.editRemarksField.setText("");
                if (activity.editCategoryField != null) activity.editCategoryField.setText("");
                EditorSortHelper.applySorting(activity);
                EditorUIHelper.populateRecordsList(activity);
            }
        });

        ResponsiveUI.setupClickable(btnSave, () -> {
            String title = editTitle.getText().toString().trim();

            if (!EditorValidationHelper.validateAccountTitle(activity, title)) return;

            RecordUtils.resequentializeRecords(activity.tempRecords);
            RecordUtils.resequentializeRecords(activity.tempBudgetRecords);

            if (activity.currentEditingAccount == null) {
                Account newAccount = new Account(title, activity.tempRecords, System.currentTimeMillis());
                newAccount.setBudgetRecords(activity.tempBudgetRecords);
                newAccount.setHasBudget(!activity.tempBudgetRecords.isEmpty());
                
                if (activity.currentViewGroup != null) {
                    activity.currentViewGroup.getAccounts().add(newAccount);
                    activity.currentViewGroup.updateLastModified();
                } else {
                    activity.appStorage.standaloneAccounts.add(newAccount);
                }
            } else {
                activity.currentEditingAccount.setTitle(title);
                activity.currentEditingAccount.setRecords(activity.tempRecords);
                activity.currentEditingAccount.setBudgetRecords(activity.tempBudgetRecords);
                activity.currentEditingAccount.setHasBudget(!activity.tempBudgetRecords.isEmpty());
                activity.currentEditingAccount.updateLastModified();
            }

            StorageHelper.saveAppStorage(activity, activity.appStorage);
            
            activity.currentEditingAccount = null;
            activity.tempRecords = null;
            activity.tempBudgetRecords = null;
            DashboardHelper.showDashboard(activity);
        });
    }
}
