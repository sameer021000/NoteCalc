package com.example.notecalc.editor.core;
import com.example.notecalc.storage.core.*;
import com.example.notecalc.core.utils.*;
import com.example.notecalc.core.ui.*;
import com.example.notecalc.editor.ui.*;
import com.example.notecalc.*;
import com.example.notecalc.records.models.Record;
import android.widget.TextView;
import android.widget.EditText;

public class EditorSaveHelper {
    public static void setupSaveActions(MainActivity activity, EditText editDesc, EditText editAmount, TextView btnAdd) {
        ResponsiveUI.setupClickable(btnAdd, () -> {
            String desc = editDesc.getText().toString().trim();
            String amountStr = editAmount.getText().toString().trim();
            String remarks = activity.editRemarksField != null ? activity.editRemarksField.getText().toString().trim() : "";
            String category = activity.editCategoryField != null ? activity.editCategoryField.getText().toString().trim() : "";

            Double amount = EditorValidationHelper.validateRecordInput(activity, desc, amountStr);
            if (amount == null) return;

            if (activity.editingRecordIndex != -1) {
                Record record = StateHelper.getActiveRecords(activity).get(activity.editingRecordIndex);
                boolean contentChanged = !record.getDescription().equals(desc) ||
                                         Double.compare(record.getAmount(), amount) != 0 ||
                                         !record.getDate().equals(activity.selectedRecordDate) ||
                                         !record.getRemarks().equals(remarks) ||
                                         !record.getCategory().equals(category) ||
                                         !record.getAttachments().equals(activity.tempAttachments);

                if (contentChanged) {
                    record.setDescription(desc);
                    record.setAmount(amount);
                    record.setDate(activity.selectedRecordDate);
                    record.setRemarks(remarks);
                    record.setCategory(category);
                    record.setAttachments(new java.util.ArrayList<>(activity.tempAttachments));
                    record.setTimestampMillis(System.currentTimeMillis());
                }
                EditorSortHelper.applySorting(activity);
                EditorModeHelper.cancelEditRecordMode(activity);
                EditorCategoryHelper.setupCategoryDropdown(activity);
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
                activity.tempAttachments.clear();
                com.example.notecalc.core.utils.AttachmentHelper.renderEditorAttachments(activity);
                
                EditorSortHelper.applySorting(activity);
                EditorUIHelper.populateRecordsList(activity);
                EditorCategoryHelper.setupCategoryDropdown(activity);
            }
            
            // Autosave after adding or editing a record
            if (activity.currentEditingAccount != null) {
                activity.currentEditingAccount.setHasBudget(activity.currentEditingAccount.getBudgetRecords() != null && !activity.currentEditingAccount.getBudgetRecords().isEmpty());
                activity.currentEditingAccount.updateLastModified();
                
                if (activity.currentViewGroup != null) {
                    activity.currentViewGroup.updateLastModified();
                }
                
                StorageHelper.saveAppStorage(activity, activity.appStorage);
            }
        });
    }
}