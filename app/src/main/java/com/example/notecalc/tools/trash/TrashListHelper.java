package com.example.notecalc.tools.trash;

import com.example.notecalc.MainActivity;
import com.example.notecalc.R;
import com.example.notecalc.accounts.models.Account;
import com.example.notecalc.core.ui.ResponsiveUI;
import com.example.notecalc.dashboard.DashboardHelper;
import com.example.notecalc.editor.core.EditorModeHelper;
import com.example.notecalc.editor.core.EditorSortHelper;
import com.example.notecalc.core.utils.StateHelper;
import java.util.List;
import com.example.notecalc.editor.ui.EditorUIHelper;
import com.example.notecalc.records.adapters.RecordsAdapter;
import com.example.notecalc.records.models.Record;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class TrashListHelper {

    public static void showTrashList(MainActivity activity, Account trashAccount) {
        if (activity.currentSnackbar != null) {
            activity.currentSnackbar.dismiss();
            activity.currentSnackbar = null;
        }
        
        LayoutInflater inflater = activity.getLayoutInflater();
        View trashListView = inflater.inflate(R.layout.layout_trash_list, activity.mainContainer, false);
        trashListView.setVisibility(View.VISIBLE);

        activity.currentEditingAccount = trashAccount;
        activity.tempRecords = new ArrayList<>();
        activity.tempBudgetRecords = new ArrayList<>();
        activity.isBudgetMode = false;
        
        if (trashAccount.getRecords() != null) {
            activity.tempRecords.addAll(trashAccount.getRecords());
        }
        if (trashAccount.getBudgetRecords() != null) {
            activity.tempBudgetRecords.addAll(trashAccount.getBudgetRecords());
        }

        ImageView btnBack = trashListView.findViewById(R.id.btn_trash_list_back);
        TextView textTitle = trashListView.findViewById(R.id.text_trash_list_title);
        ImageView btnRestore = trashListView.findViewById(R.id.btn_trash_list_restore);

        textTitle.setText(trashAccount.getTitle());

        ResponsiveUI.setupClickable(btnBack, false, () -> {
            activity.currentEditingAccount = null;
            TrashDashboardHelper.showTrashDashboard(activity);
        });
        
        ResponsiveUI.setupClickable(btnRestore, false, () -> {
            List<Record> selectedRecords = new ArrayList<>();
            for (Record r : StateHelper.getActiveRecords(activity)) {
                if (r.isSelected()) selectedRecords.add(r);
            }
            
            if (!selectedRecords.isEmpty()) {
                if (selectedRecords.size() > 2) {
                    com.example.notecalc.records.dialogs.RecordDialogHelper.showRestoreMultipleConfirmationDialog(activity, trashAccount, selectedRecords);
                } else {
                    TrashActionEngine.restoreRecords(activity, trashAccount, selectedRecords, activity.isBudgetMode);
                    EditorUIHelper.populateRecordsList(activity);
                }
            } else {
                List<Record> all = new ArrayList<>(StateHelper.getActiveRecords(activity));
                if (all.isEmpty()) return;
                
                if (all.size() > 2) {
                    com.example.notecalc.records.dialogs.RecordDialogHelper.showRestoreMultipleConfirmationDialog(activity, trashAccount, all);
                } else {
                    TrashActionEngine.restoreRecords(activity, trashAccount, all, activity.isBudgetMode);
                    EditorUIHelper.populateRecordsList(activity);
                    if (StateHelper.getActiveRecords(activity).isEmpty()) {
                        activity.currentEditingAccount = null;
                        TrashDashboardHelper.showTrashDashboard(activity);
                    }
                }
            }
        });

        TextView btnModeExpenses = trashListView.findViewById(R.id.btn_trash_mode_expenses);
        TextView btnModeBudget = trashListView.findViewById(R.id.btn_trash_mode_budget);
        
        activity.cbSelectAllHeader = trashListView.findViewById(R.id.trash_cb_select_all);
        activity.containerBulkActions = trashListView.findViewById(R.id.trash_container_bulk_actions);
        activity.textSelectedTotal = trashListView.findViewById(R.id.trash_text_selected_total);
        activity.editorEmptyState = trashListView.findViewById(R.id.trash_list_empty_state);
        activity.rowSearchAndBulk = trashListView.findViewById(R.id.trash_container_bulk_actions); // We reuse this for bulk actions visibility
        activity.tableHeaderField = trashListView.findViewById(R.id.trash_table_header);
        
        activity.textTotalValField = null; // Removed from trash list
        activity.textTotalLabelField = null; // Removed from trash list
        
        RecyclerView listRecordsRecyclerView = trashListView.findViewById(R.id.trash_list_records);
        listRecordsRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        activity.recordsAdapter = new RecordsAdapter(activity);
        activity.recordsAdapter.isTrashMode = true; // Enables custom click handling in the adapter
        listRecordsRecyclerView.setAdapter(activity.recordsAdapter);
        
        activity.thSnoField = trashListView.findViewById(R.id.trash_th_sno);
        activity.thDescField = trashListView.findViewById(R.id.trash_th_desc);
        activity.thDateField = trashListView.findViewById(R.id.trash_th_date);
        activity.thAmountField = trashListView.findViewById(R.id.trash_th_amount);

        EditorModeHelper.setupModeToggleUI(activity, btnModeExpenses, btnModeBudget);
        EditorSortHelper.setupHeaderSortListeners(activity);
        EditorSortHelper.applySorting(activity);
        EditorUIHelper.populateRecordsList(activity);

        ResponsiveUI.applyResponsiveness(trashListView);
        
        activity.mainContainer.removeAllViews();
        activity.mainContainer.addView(trashListView);
    }
}
