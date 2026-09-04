package com.example.notecalc;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

public class EditorHelper {


    @android.annotation.SuppressLint({"SetTextI18n", "ClickableViewAccessibility", "NotifyDataSetChanged"})
    public static void openEditor(MainActivity activity, Account account) {
        if (activity.currentSnackbar != null) {
            activity.currentSnackbar.dismiss();
            activity.currentSnackbar = null;
        }
        LayoutInflater inflater = activity.getLayoutInflater();
        View editorView = inflater.inflate(R.layout.layout_editor, activity.mainContainer, false);

        activity.currentEditingAccount = account;
        activity.tempRecords = new ArrayList<>();
        activity.tempBudgetRecords = new ArrayList<>();
        activity.isBudgetMode = false;
        activity.selectedRecordDate = DateUtils.getCurrentDateString();
        activity.editingRecordIndex = -1;

        ImageView btnBack = editorView.findViewById(R.id.btn_back);
        ImageView btnAnalytics = editorView.findViewById(R.id.btn_analytics);
        AnalyticsHelper.setupAnalyticsButton(activity, btnAnalytics);

        EditText editTitle = editorView.findViewById(R.id.edit_account_title);
        TextView textTitleError = editorView.findViewById(R.id.text_title_error);
        EditText editDesc = editorView.findViewById(R.id.edit_record_desc);
        EditText editAmount = editorView.findViewById(R.id.edit_record_amount);
        TextView btnDate = editorView.findViewById(R.id.btn_record_date);
        TextView btnAdd = editorView.findViewById(R.id.btn_add_record);
        activity.editCategoryField = editorView.findViewById(R.id.edit_record_category);
        activity.btnAttachFile = editorView.findViewById(R.id.btn_attach_file);
        activity.attachmentsScroll = editorView.findViewById(R.id.attachments_scroll);
        activity.attachmentsContainer = editorView.findViewById(R.id.attachments_container);
        
        activity.tempAttachments.clear();
        AttachmentHelper.renderEditorAttachments(activity);

        EditorAttachmentsDialogHelper.setupAttachmentsDialog(activity);
        EditorCategoryHelper.setupCategoryDropdown(activity);

        TextView btnCancelEdit = editorView.findViewById(R.id.btn_cancel_edit_record);
        RecyclerView listRecordsRecyclerView = editorView.findViewById(R.id.list_records);
        TextView textTotalVal = editorView.findViewById(R.id.text_total_value);
        TextView textTotalLabel = editorView.findViewById(R.id.text_total_label);
        TextView btnSave = editorView.findViewById(R.id.btn_save_account);
        View formContainer = editorView.findViewById(R.id.form_container);
        View tableHeader = editorView.findViewById(R.id.table_header);

        TextView btnModeExpenses = editorView.findViewById(R.id.btn_mode_expenses);
        TextView btnModeBudget = editorView.findViewById(R.id.btn_mode_budget);
        TextView textRemainingPurse = editorView.findViewById(R.id.text_remaining_purse);
        
        activity.editDescField = editDesc;
        activity.editAmountField = editAmount;
        activity.btnRecordDateField = btnDate;
        activity.btnAddRecordField = btnAdd;
        activity.btnCancelEditField = btnCancelEdit;
        activity.labelAddRecordField = editorView.findViewById(R.id.label_add_record);
                listRecordsRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        activity.recordsAdapter = new RecordsAdapter(activity);
        listRecordsRecyclerView.setAdapter(activity.recordsAdapter);

        ImageView btnNCAgent = editorView.findViewById(R.id.btn_nc_agent);
        NCAgentHelper.setupNCAgentButton(activity, btnNCAgent, account);
        
        new androidx.recyclerview.widget.ItemTouchHelper(TouchHelper.getRecordSwipeCallback(activity)).attachToRecyclerView(listRecordsRecyclerView);
        activity.textTotalValField = textTotalVal;
        activity.textTotalLabelField = textTotalLabel;

        activity.editRemarksField = editorView.findViewById(R.id.edit_record_remarks);
        activity.formInputsContainer = editorView.findViewById(R.id.form_inputs_container);
        activity.btnToggleForm = editorView.findViewById(R.id.btn_toggle_form);
        activity.cbSelectAllHeader = editorView.findViewById(R.id.cb_select_all);
        activity.btnBulkActionsMenu = editorView.findViewById(R.id.btn_bulk_actions_menu);
        activity.containerBulkActions = editorView.findViewById(R.id.container_bulk_actions);
        activity.textSelectedTotal = editorView.findViewById(R.id.text_selected_total);
        activity.editorEmptyState = editorView.findViewById(R.id.editor_empty_state);
        activity.rowSearchAndBulk = editorView.findViewById(R.id.row_search_and_bulk);
        activity.tableHeaderField = tableHeader;

        activity.currentRecordSearchQuery = "";
        activity.expenseFilterDateFrom = null;
        activity.expenseFilterDateTo = null;
        activity.expenseFilterAmountFrom = null;
        activity.expenseFilterAmountTo = null;
        activity.budgetFilterDateFrom = null;
        activity.budgetFilterDateTo = null;
        activity.budgetFilterAmountFrom = null;
        activity.budgetFilterAmountTo = null;
        EditorUIHelper.setupFormToggle(activity, account);

        if (account != null && account.isArchived()) {
            if (formContainer != null) {
                formContainer.setVisibility(View.GONE);
            }
        }

        EditText editRecordsSearch = editorView.findViewById(R.id.edit_records_search);
        EditorUIHelper.setupSearchBar(activity, editRecordsSearch);

        EditorUIHelper.setupBulkActions(activity);

        activity.thSnoField = editorView.findViewById(R.id.th_sno);
        activity.thDescField = editorView.findViewById(R.id.th_desc);
        activity.thDateField = editorView.findViewById(R.id.th_date);
        activity.thAmountField = editorView.findViewById(R.id.th_amount);
        
        EditorSortHelper.setupHeaderSortListeners(activity);

        if (account != null) {
            
            editTitle.setText(account.getTitle());
            if (account.isArchived()) {
                editTitle.setEnabled(false);
                editTitle.setAlpha(0.7f);
            }
            activity.originalTitle = account.getTitle();
            activity.tempRecords.addAll(account.getRecords());
            
            EditorUIHelper.migrateLegacyIndices(activity, account);
        } else {
            
            activity.originalTitle = "";
        }
        
        EditorModeHelper.setupModeToggleUI(activity, btnModeExpenses, btnModeBudget, textRemainingPurse);
        EditorSortHelper.applySorting(activity);
        EditorUIHelper.populateRecordsList(activity);

        ResponsiveUI.applyResponsiveness(editorView);

        if (account != null && account.isArchived()) {
            btnSave.setVisibility(View.GONE);
        }
        EditorThemeHelper.applyEditorTheme(activity, formContainer, tableHeader, editTitle, editDesc, editAmount, btnDate, btnCancelEdit, btnAdd, btnSave);

        btnDate.setText(activity.selectedRecordDate);

        EditorUIHelper.setupTitleWatcher(activity, editTitle, textTitleError);

        DashboardHelper.setupBackButton(activity, btnBack);

        EditorUIHelper.setupFormListeners(activity, btnDate, btnCancelEdit);

        EditorSaveHelper.setupSaveActions(activity, editTitle, editDesc, editAmount, btnAdd, btnSave);

        EditorUIHelper.populateRecordsList(activity);

        activity.mainContainer.removeAllViews();
        activity.mainContainer.addView(editorView);
    }
}
