package com.example.notecalc.tools.transfer;
import com.example.notecalc.core.StateHelper;
import com.example.notecalc.records.*;
import com.example.notecalc.accounts.*;
import com.example.notecalc.storage.*;
import com.example.notecalc.records.Record;
import com.example.notecalc.*;
import java.util.List;

public class TransferEngine {

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    public static void executeTransfer(MainActivity activity, List<Record> selectedRecords, Account targetAccount, boolean isCut) {
        java.util.List<Record> targetList = activity.isBudgetMode ? targetAccount.getBudgetRecords() : targetAccount.getRecords();
        int maxIndex = -1;
        for (Record rec : targetList) {
            if (rec.getOriginalIndex() > maxIndex) {
                maxIndex = rec.getOriginalIndex();
            }
        }

        for (Record r : selectedRecords) {
            Record copy = new Record(r.getDescription(), r.getAmount(), r.getDate());
            copy.setRemarks(r.getRemarks());
            copy.setCategory(r.getCategory());
            copy.setTimestampMillis(r.getTimestampMillis());
            if (r.getAttachments() != null) {
                copy.getAttachments().addAll(r.getAttachments());
            }
            maxIndex++;
            copy.setOriginalIndex(maxIndex);

            if (activity.isBudgetMode) {
                targetAccount.getBudgetRecords().add(copy);
            } else {
                targetAccount.getRecords().add(copy);
            }

            if (isCut) {
                if (activity.isBudgetMode) {
                    activity.currentEditingAccount.getBudgetRecords().remove(r);
                } else {
                    activity.currentEditingAccount.getRecords().remove(r);
                }
            }
        }

        StorageHelper.saveAppStorage(activity, activity.appStorage);

        if (isCut) {
            StateHelper.getActiveRecords(activity).removeAll(selectedRecords);
            RecordUtils.resequentializeRecords(StateHelper.getActiveRecords(activity));
            if (activity.recordsAdapter != null) {
                activity.recordsAdapter.setFilter(activity.currentRecordSearchQuery);
            }
        }

        for (Record r : StateHelper.getActiveRecords(activity)) r.setSelected(false);
        if (activity.cbSelectAllHeader != null) {
            activity.cbSelectAllHeader.setOnCheckedChangeListener(null);
            activity.cbSelectAllHeader.setChecked(false);
            activity.cbSelectAllHeader.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (activity.recordsAdapter != null) {
                    for (Record rec : activity.recordsAdapter.displayRecords) {
                        rec.setSelected(isChecked);
                    }
                    activity.recordsAdapter.notifyDataSetChanged();
                    BulkActionsHelper.updateBulkActionsState(activity);
                }
            });
        }
        if (activity.recordsAdapter != null) {
            activity.recordsAdapter.notifyDataSetChanged();
        }
        BulkActionsHelper.updateBulkActionsState(activity);

        String action = isCut ? "Cut" : "Copied";
        android.widget.Toast.makeText(activity, action + " " + selectedRecords.size() + " records to " + targetAccount.getTitle(), android.widget.Toast.LENGTH_SHORT).show();
    }
}