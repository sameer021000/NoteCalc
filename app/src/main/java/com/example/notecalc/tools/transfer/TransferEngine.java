package com.example.notecalc.tools.transfer;
import com.example.notecalc.storage.core.*;
import com.example.notecalc.records.dialogs.*;
import com.example.notecalc.core.utils.*;
import com.example.notecalc.accounts.models.*;
import com.example.notecalc.records.*;
import com.example.notecalc.records.models.Record;
import com.example.notecalc.*;
import java.util.List;

public class TransferEngine {

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    public static void executeTransfer(MainActivity activity, List<Record> selectedRecords, Account targetAccount, boolean isCut) {
        if (isCut) {
            ListMutationEngine.cutRecords(selectedRecords, activity.currentEditingAccount, targetAccount, activity.isBudgetMode);
        } else {
            ListMutationEngine.copyRecords(selectedRecords, targetAccount, activity.isBudgetMode);
        }

        if (isCut) {
            if (activity.recordsAdapter != null) {
                activity.recordsAdapter.setFilter(activity.currentRecordSearchQuery);
            }
        }

        StorageHelper.saveAppStorage(activity, activity.appStorage);

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
        if (isCut) {
            com.example.notecalc.editor.ui.EditorUIHelper.populateRecordsList(activity);
        } else {
            if (activity.recordsAdapter != null) {
                activity.recordsAdapter.notifyDataSetChanged();
            }
            BulkActionsHelper.updateBulkActionsState(activity);
        }

        String action = isCut ? "Cut" : "Copied";
        android.widget.Toast.makeText(activity, action + " " + selectedRecords.size() + " records to " + targetAccount.getTitle(), android.widget.Toast.LENGTH_SHORT).show();
    }
}