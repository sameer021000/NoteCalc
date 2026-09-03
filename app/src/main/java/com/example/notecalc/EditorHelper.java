package com.example.notecalc;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import android.view.MotionEvent;
import android.text.TextWatcher;
import android.text.Editable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.graphics.Color;
import java.util.Locale;

public class EditorHelper {

    /**
     * Renders the Account Editor screen.
     *
     * @param account The account to edit. If null, a new account is initialized.
     */
    @SuppressWarnings("deprecation")
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
        activity.selectedRecordDate = AppUtils.getCurrentDateString();
        activity.editingRecordIndex = -1;

        ImageView btnBack = editorView.findViewById(R.id.btn_back);
        ImageView btnAnalytics = editorView.findViewById(R.id.btn_analytics);
        if (btnAnalytics != null) {
            ResponsiveUI.setupClickable(btnAnalytics, true, () -> {
                if (activity.currentEditingAccount == null || (activity.currentEditingAccount.getRecords().isEmpty() && activity.currentEditingAccount.getBudgetRecords().isEmpty())) {
                    android.widget.Toast.makeText(activity, "Add some records to view analytics", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    AnalyticsHelper.showAnalytics(activity, activity.currentEditingAccount, activity.mainContainer, () -> openEditor(activity, activity.currentEditingAccount));
                }
            });
        }

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

        android.widget.ImageView btnNCAgent = editorView.findViewById(R.id.btn_nc_agent);
        if (btnNCAgent != null) {
            btnNCAgent.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), ThemeManager.getPrimaryAccentColor(activity), 0f, 100f));
            btnNCAgent.setColorFilter(activity.getColor(R.color.text_on_accent));
            btnNCAgent.setImageResource(android.R.drawable.ic_btn_speak_now); // Unique microphone/voice icon representing natural language
            btnNCAgent.setOnClickListener(v -> NCAgentHelper.showNCAgentBottomSheet(activity, activity.ncAgent));
            if (account != null && account.isArchived()) {
                btnNCAgent.setVisibility(View.GONE);
            }
        }
        
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
        activity.isFormInputsCollapsed = true;

        Runnable toggleForm = () -> {
            activity.isFormInputsCollapsed = !activity.isFormInputsCollapsed;
            activity.formInputsContainer.setVisibility(activity.isFormInputsCollapsed ? View.GONE : View.VISIBLE);
            activity.btnToggleForm.setText(activity.isFormInputsCollapsed ? "Expand [ + ]" : "Minimize [ - ]");
        };
        ResponsiveUI.setupClickable(activity.btnToggleForm, false, toggleForm);
        
        android.graphics.drawable.StateListDrawable toggleSelector = new android.graphics.drawable.StateListDrawable();
        toggleSelector.addState(new int[]{android.R.attr.state_pressed}, new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        toggleSelector.addState(new int[]{}, ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 12.0f));
        activity.btnToggleForm.setBackground(toggleSelector);
        
        int pLR = (int) (12 * activity.getResources().getDisplayMetrics().density);
        int pTB = (int) (6 * activity.getResources().getDisplayMetrics().density);
        activity.btnToggleForm.setPadding(pLR, pTB, pLR, pTB);

        if (account == null) {
            activity.isFormInputsCollapsed = false;
            activity.formInputsContainer.setVisibility(View.VISIBLE);
            activity.btnToggleForm.setText(activity.getString(R.string.auto_minimize_19));
        } else {
            activity.isFormInputsCollapsed = true;
            activity.formInputsContainer.setVisibility(View.GONE);
            activity.btnToggleForm.setText(activity.getString(R.string.auto_expand_20));
        }

        if (account != null && account.isArchived()) {
            if (formContainer != null) {
                formContainer.setVisibility(View.GONE);
            }
        }

        EditText editRecordsSearch = editorView.findViewById(R.id.edit_records_search);
        editRecordsSearch.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgSecondaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                8.0f
        ));

        editRecordsSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (editRecordsSearch.getCompoundDrawablesRelative()[2] != null) {
                    if (event.getRawX() >= (editRecordsSearch.getRight() - editRecordsSearch.getCompoundDrawablesRelative()[2].getBounds().width() - editRecordsSearch.getPaddingRight())) {
                        editRecordsSearch.setText("");
                        return true;
                    }
                }
                v.performClick();
            }
            return false;
        });
        editRecordsSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                activity.currentRecordSearchQuery = s.toString();
                activity.recordsAdapter.setFilter(activity.currentRecordSearchQuery);
            }
        });

        activity.cbSelectAllHeader.setOnCheckedChangeListener(null);
        activity.cbSelectAllHeader.setChecked(false);
        activity.cbSelectAllHeader.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (activity.recordsAdapter != null) {
                for (Record r : activity.recordsAdapter.displayRecords) {
                    r.setSelected(isChecked);
                }
                activity.recordsAdapter.notifyDataSetChanged();
                BulkActionsHelper.updateBulkActionsState(activity);
            }
        });

        if (activity.btnBulkActionsMenu != null) {
            activity.btnBulkActionsMenu.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#15FFFFFF"), 4.0f));
            ResponsiveUI.setupClickable(activity.btnBulkActionsMenu, true, () -> MenuHelper.showBulkActionsMenu(activity, activity.btnBulkActionsMenu));
        }

        activity.thSnoField = editorView.findViewById(R.id.th_sno);
        activity.thDescField = editorView.findViewById(R.id.th_desc);
        activity.thDateField = editorView.findViewById(R.id.th_date);
        activity.thAmountField = editorView.findViewById(R.id.th_amount);

        activity.expenseSortColumn = 0;
        activity.expenseSortAscending = false;
        activity.budgetSortColumn = 0;
        activity.budgetSortAscending = false;

        activity.thSnoField.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#15FFFFFF"), 4.0f));
        activity.thDescField.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#15FFFFFF"), 4.0f));
        activity.thDateField.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#15FFFFFF"), 4.0f));
        activity.thAmountField.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#15FFFFFF"), 4.0f));

        ResponsiveUI.setupClickable(activity.thSnoField, false, () -> EditorSortHelper.onHeaderClicked(activity, 0));
        ResponsiveUI.setupClickable(activity.thDescField, false, () -> EditorSortHelper.onHeaderClicked(activity, 1));

        activity.thDateField.setOnClickListener(v -> EditorSortHelper.onHeaderClicked(activity, 2));
        activity.thDateField.setOnLongClickListener(v -> {
            FilterHelper.showDateRangeFilterDialog(activity);
            return true;
        });

        activity.thAmountField.setOnClickListener(v -> EditorSortHelper.onHeaderClicked(activity, 3));
        activity.thAmountField.setOnLongClickListener(v -> {
            FilterHelper.showAmountRangeFilterDialog(activity);
            return true;
        });

        EditorSortHelper.updateHeaderLabels(activity);

        if (account != null) {
            
            editTitle.setText(account.getTitle());
            if (account.isArchived()) {
                editTitle.setEnabled(false);
                editTitle.setAlpha(0.7f);
            }
            activity.originalTitle = account.getTitle();
            activity.tempRecords.addAll(account.getRecords());
            
            boolean needsMigration = false;
            for (Record r : activity.tempRecords) { if (r.getOriginalIndex() == -1) { needsMigration = true; break; } }
            if (needsMigration) {
                for (int i = 0; i < activity.tempRecords.size(); i++) {
                    activity.tempRecords.get(i).setOriginalIndex(i);
                }
            }

            if (account.getBudgetRecords() != null) {
                activity.tempBudgetRecords.addAll(account.getBudgetRecords());
                boolean needsBudgetMigration = false;
                for (Record r : activity.tempBudgetRecords) { if (r.getOriginalIndex() == -1) { needsBudgetMigration = true; break; } }
                if (needsBudgetMigration) {
                    for (int i = 0; i < activity.tempBudgetRecords.size(); i++) {
                        activity.tempBudgetRecords.get(i).setOriginalIndex(i);
                    }
                }
            }
        } else {
            
            activity.originalTitle = "";
        }
        
        Runnable updateModeToggleUI = () -> {
            if (btnModeExpenses != null && btnModeBudget != null) {
                btnModeExpenses.setBackgroundColor(activity.isBudgetMode ? ThemeManager.getBgSecondaryColor(activity) : ThemeManager.getPrimaryAccentColor(activity));
                btnModeExpenses.setTextColor(activity.getColor(activity.isBudgetMode ? R.color.text_tertiary : R.color.text_on_accent));
                
                btnModeBudget.setBackgroundColor(activity.isBudgetMode ? ThemeManager.getPrimaryAccentColor(activity) : ThemeManager.getBgSecondaryColor(activity));
                btnModeBudget.setTextColor(activity.getColor(activity.isBudgetMode ? R.color.text_on_accent : R.color.text_tertiary));
            }
            EditorSortHelper.applySorting(activity);
              activity.recordsAdapter.setFilter(activity.currentRecordSearchQuery);
            EditorSortHelper.updateHeaderLabels(activity);
            EditorSortHelper.updateDateHeaderIndicator(activity);
            EditorSortHelper.updateAmountHeaderIndicator(activity);
            
            if (textRemainingPurse != null) {
                if (activity.tempBudgetRecords.isEmpty() && !activity.isBudgetMode) {
                    textRemainingPurse.setVisibility(View.GONE);
                } else {
                    textRemainingPurse.setVisibility(View.VISIBLE);
                    double totalBudget = 0;
                    for (Record r : activity.tempBudgetRecords) totalBudget += r.getAmount();
                    double totalExpenses = 0;
                    for (Record r : activity.tempRecords) totalExpenses += r.getAmount();
                    double remaining = totalBudget - totalExpenses;
                    textRemainingPurse.setText(String.format(Locale.getDefault(), "Balance : %.2f", remaining));
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
        EditorSortHelper.applySorting(activity);
        EditorUIHelper.populateRecordsList(activity);

        ResponsiveUI.applyResponsiveness(editorView);

        if (account != null && account.isArchived()) {
            btnSave.setVisibility(View.GONE);
        }
        EditorThemeHelper.applyEditorTheme(activity, formContainer, tableHeader, editTitle, editDesc, editAmount, btnDate, btnCancelEdit, btnAdd, btnSave);

        btnDate.setText(activity.selectedRecordDate);

        editTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                if (EditorUIHelper.isDuplicateTitle(activity, input)) {
                    textTitleError.setVisibility(View.VISIBLE);
                    editTitle.setBackground(ResponsiveUI.createRoundedBg(
                            activity,
                            ThemeManager.getBgSecondaryColor(activity),
                            activity.getColor(R.color.error_red),
                            1.5f,
                            6.0f
                    ));
                } else {
                    textTitleError.setVisibility(View.GONE);
                    editTitle.setBackground(ResponsiveUI.createRoundedBg(
                            activity,
                            ThemeManager.getBgSecondaryColor(activity),
                            ThemeManager.getBorderColor(activity),
                            1.0f,
                            6.0f
                    ));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ResponsiveUI.setupClickable(btnBack, false, () -> {
            activity.dashboardSearchQuery = "";
            if (activity.tempRecords != null) for (Record r : activity.tempRecords) r.setSelected(false);
            if (activity.tempBudgetRecords != null) for (Record r : activity.tempBudgetRecords) r.setSelected(false);
            DashboardHelper.showDashboard(activity);
        });

        ResponsiveUI.setupClickable(btnDate, () -> DialogHelper.showDatePicker(activity, activity.selectedRecordDate, btnDate, newDate -> activity.selectedRecordDate = newDate));

        ResponsiveUI.setupClickable(btnCancelEdit, () -> EditorModeHelper.cancelEditRecordMode(activity));

        EditorSaveHelper.setupSaveActions(activity, editTitle, editDesc, editAmount, btnAdd, btnSave);

        EditorUIHelper.populateRecordsList(activity);

        activity.mainContainer.removeAllViews();
        activity.mainContainer.addView(editorView);
    }

}
