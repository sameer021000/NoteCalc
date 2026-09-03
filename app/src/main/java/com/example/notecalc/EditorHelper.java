package com.example.notecalc;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
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

        // Find views
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

        if (activity.btnAttachFile != null) {
            ResponsiveUI.setupClickable(activity.btnAttachFile, true, () -> {
                if (activity.tempAttachments.size() >= 3) {
                    Toast.makeText(activity, activity.getString(R.string.auto_max_3_files_allowed_2), Toast.LENGTH_SHORT).show();
                    return;
                }
                
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
                android.view.View dialogView = activity.getLayoutInflater().inflate(R.layout.layout_dialog_attach_file, null);
                builder.setView(dialogView);
                
                final androidx.appcompat.app.AlertDialog dialog = builder.create();
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                }
                
                android.view.View dialogRoot = dialogView.findViewById(R.id.dialog_root);
                dialogRoot.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), 0, 0, 16.0f));
                
                android.widget.TextView btnTakePhoto = dialogView.findViewById(R.id.btn_take_photo);
                android.widget.TextView btnChooseFile = dialogView.findViewById(R.id.btn_choose_file);
                
                btnTakePhoto.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), 0, 0, 8.0f));
                btnChooseFile.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), 0, 0, 8.0f));
                
                ResponsiveUI.setupClickable(btnTakePhoto, false, () -> {
                    dialog.dismiss();
                    try {
                        java.io.File attachmentsDir = new java.io.File(activity.getFilesDir(), "attachments");
                        if (!attachmentsDir.exists()) { boolean ignored = attachmentsDir.mkdirs(); }
                        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new java.util.Date());
                        java.io.File imageFile = new java.io.File(attachmentsDir, "IMG_" + timeStamp + ".jpg");
                        activity.currentPhotoPath = imageFile.getAbsolutePath();
                        android.net.Uri photoURI = androidx.core.content.FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", imageFile);
                        android.content.Intent takePictureIntent = new android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI);
                        activity.startActivityForResult(takePictureIntent, MainActivity.REQUEST_CODE_CAMERA);
                    } catch (Exception e) {
                        android.widget.Toast.makeText(activity, "Could not start camera", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
                
                ResponsiveUI.setupClickable(btnChooseFile, false, () -> {
                    dialog.dismiss();
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    String[] mimeTypes = {"image/jpeg", "image/png", "image/jpg", "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
                    intent.putExtra(android.content.Intent.EXTRA_MIME_TYPES, mimeTypes);
                    activity.startActivityForResult(intent, MainActivity.REQUEST_CODE_ATTACH);
                });
                
                dialog.show();
            });
        }
        if (activity.editCategoryField != null) {
            java.util.Set<String> catSet = new java.util.HashSet<>();
            if (activity.currentEditingAccount != null) {
                for (Record r : activity.currentEditingAccount.getRecords()) {
                    if (!r.getCategory().isEmpty()) catSet.add(r.getCategory());
                }
            }
            java.util.List<String> catList = new java.util.ArrayList<>(catSet);
            java.util.Collections.sort(catList);
            android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<>(activity, android.R.layout.simple_dropdown_item_1line, catList) {
                @androidx.annotation.NonNull
                @Override
                public android.view.View getView(int position, @androidx.annotation.Nullable android.view.View convertView, @androidx.annotation.NonNull android.view.ViewGroup parent) {
                    // Pass null to avoid ClassCastException since we wrap the view in a LinearLayout
                    android.view.View coreView = super.getView(position, null, parent);
                    
                    LinearLayout container = new LinearLayout(activity);
                    container.setOrientation(LinearLayout.VERTICAL);
                    
                    if (coreView instanceof TextView) {
                        ((TextView) coreView).setTextColor(ThemeManager.getSecondaryAccentColor(activity));
                        int hPad = (int)(12 * activity.getResources().getDisplayMetrics().density);
                        int vPad = (int)(8 * activity.getResources().getDisplayMetrics().density); // Slightly larger padding since we have lines
                        coreView.setPadding(hPad, vPad, hPad, vPad);
                        coreView.setMinimumHeight(0);
                        android.view.ViewGroup.LayoutParams params = coreView.getLayoutParams();
                        if (params != null) {
                            params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
                            coreView.setLayoutParams(params);
                        }
                    }
                    
                    // The text view
                    container.addView(coreView);
                    
                    // The thin line separator (only if not the last item, though it's easier to just add it to all)
                    if (position < getCount() - 1) {
                        View divider = new View(activity);
                        divider.setBackgroundColor(ThemeManager.getBorderColor(activity));
                        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1); // 1px thin line
                        container.addView(divider, divParams);
                    }
                    
                    container.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                    return container;
                }
            };
            activity.editCategoryField.setAdapter(catAdapter);
            
            // Set rounded corners for autocomplete drop down box
            activity.editCategoryField.setDropDownBackgroundDrawable(ResponsiveUI.createRoundedBg(
                    activity,
                    ThemeManager.getBgSecondaryColor(activity),
                    ThemeManager.getBorderColor(activity),
                    1.0f,
                    8.0f // nice curve
            ));
            // Limit drop-down height so it doesn't get fully covered by keyboard and becomes scrollable
            activity.editCategoryField.setDropDownHeight((int) (180 * activity.getResources().getDisplayMetrics().density));
            
            activity.editCategoryField.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    activity.editCategoryField.showDropDown();
                }
            });
            activity.editCategoryField.setOnClickListener(v -> activity.editCategoryField.showDropDown());
        }

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
        
        // Assign fields
        activity.editDescField = editDesc;
        activity.editAmountField = editAmount;
        activity.btnRecordDateField = btnDate;
        activity.btnAddRecordField = btnAdd;
        activity.btnCancelEditField = btnCancelEdit;
        activity.labelAddRecordField = editorView.findViewById(R.id.label_add_record);
                listRecordsRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        activity.recordsAdapter = new RecordsAdapter(activity);
        listRecordsRecyclerView.setAdapter(activity.recordsAdapter);

        // Bind NC Agent FAB
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
        
        // Setup Swipe-to-Delete and Drag-and-Drop for Records
        new androidx.recyclerview.widget.ItemTouchHelper(TouchHelper.getRecordSwipeCallback(activity)).attachToRecyclerView(listRecordsRecyclerView);
        activity.textTotalValField = textTotalVal;
        activity.textTotalLabelField = textTotalLabel;

        // Collapsible form, remarks, empty state, and bulk delete view mappings
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

        // Reset editor search query, selections, and filters
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

        // Wire the collapsible form header click action
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
        
        // Add some padding to make the pill look good
        int pLR = (int) (12 * activity.getResources().getDisplayMetrics().density);
        int pTB = (int) (6 * activity.getResources().getDisplayMetrics().density);
        activity.btnToggleForm.setPadding(pLR, pTB, pLR, pTB);

        // Apply default minimized state
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

        // Wire the record search bar
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

        // Wire bulk delete and headers check sync
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

        // Assign header fields and reset sort state
        activity.thSnoField = editorView.findViewById(R.id.th_sno);
        activity.thDescField = editorView.findViewById(R.id.th_desc);
        activity.thDateField = editorView.findViewById(R.id.th_date);
        activity.thAmountField = editorView.findViewById(R.id.th_amount);

        activity.expenseSortColumn = 0;
        activity.expenseSortAscending = false;
        activity.budgetSortColumn = 0;
        activity.budgetSortAscending = false;

        // Set up header touch backgrounds and click listeners
        activity.thSnoField.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#15FFFFFF"), 4.0f));
        activity.thDescField.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#15FFFFFF"), 4.0f));
        activity.thDateField.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#15FFFFFF"), 4.0f));
        activity.thAmountField.setBackground(ResponsiveUI.createButtonSelector(activity, Color.parseColor("#15FFFFFF"), 4.0f));

        ResponsiveUI.setupClickable(activity.thSnoField, false, () -> EditorSortHelper.onHeaderClicked(activity, 0));
        ResponsiveUI.setupClickable(activity.thDescField, false, () -> EditorSortHelper.onHeaderClicked(activity, 1));

        // Date header: click = sort, long-press (1s) = date range filter
        activity.thDateField.setOnClickListener(v -> EditorSortHelper.onHeaderClicked(activity, 2));
        activity.thDateField.setOnLongClickListener(v -> {
            FilterHelper.showDateRangeFilterDialog(activity);
            return true;
        });

        // Amount header: click = sort, long-press (1s) = amount range filter
        activity.thAmountField.setOnClickListener(v -> EditorSortHelper.onHeaderClicked(activity, 3));
        activity.thAmountField.setOnLongClickListener(v -> {
            FilterHelper.showAmountRangeFilterDialog(activity);
            return true;
        });

        EditorSortHelper.updateHeaderLabels(activity);

        // Pre-populate if editing existing account
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
        
        // Mode toggle logic
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

        // Apply responsive dimensions
        ResponsiveUI.applyResponsiveness(editorView);

        // Programmatic monochrome styling
        editTitle.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgSecondaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                6.0f
        ));

        if (formContainer != null) {
            formContainer.setBackground(ResponsiveUI.createRoundedBg(
                    activity,
                    ThemeManager.getBgSecondaryColor(activity),
                    ThemeManager.getBorderColor(activity),
                    1.0f,
                    8.0f
            ));
        }

        tableHeader.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgSecondaryColor(activity),
                0,
                0,
                4.0f
        ));

        editDesc.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgPrimaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                4.0f
        ));

        editAmount.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgPrimaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                4.0f
        ));

        activity.editRemarksField.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgPrimaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                4.0f
        ));
        
        if (activity.editCategoryField != null) {
            activity.editCategoryField.setBackground(ResponsiveUI.createRoundedBg(
                    activity,
                    ThemeManager.getBgPrimaryColor(activity),
                    ThemeManager.getBorderColor(activity),
                    1.0f,
                    4.0f
            ));
        }

        btnDate.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgPrimaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                4.0f
        ));

        btnCancelEdit.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                activity.getColor(R.color.error_red),
                activity.getColor(R.color.error_red),
                0f,
                4.0f
        ));
        btnCancelEdit.setTextColor(activity.getColor(R.color.text_on_accent));
        btnCancelEdit.setTypeface(null, android.graphics.Typeface.BOLD);

        btnAdd.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getPrimaryAccentColor(activity),
                0,
                0,
                4.0f
        ));

        if (account != null && account.isArchived()) {
            btnSave.setVisibility(View.GONE);
        }
        btnSave.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getPrimaryAccentColor(activity),
                0,
                0,
                6.0f
        ));

        // Date Display
        btnDate.setText(activity.selectedRecordDate);

        // Title text validation watcher
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

        // Set up click listeners with touch styling
        ResponsiveUI.setupClickable(btnBack, false, () -> {
            activity.dashboardSearchQuery = "";
            if (activity.tempRecords != null) for (Record r : activity.tempRecords) r.setSelected(false);
            if (activity.tempBudgetRecords != null) for (Record r : activity.tempBudgetRecords) r.setSelected(false);
            DashboardHelper.showDashboard(activity);
        });

        // Date picker action
        ResponsiveUI.setupClickable(btnDate, () -> DialogHelper.showDatePicker(activity, activity.selectedRecordDate, btnDate, newDate -> activity.selectedRecordDate = newDate));

        // Cancel edit action
        ResponsiveUI.setupClickable(btnCancelEdit, () -> EditorModeHelper.cancelEditRecordMode(activity));

        // Add/Update item action
        ResponsiveUI.setupClickable(btnAdd, () -> {
            String desc = editDesc.getText().toString().trim();
            String amountStr = editAmount.getText().toString().trim();
            String remarks = activity.editRemarksField.getText().toString().trim();
            String category = activity.editCategoryField != null ? activity.editCategoryField.getText().toString().trim() : "";

            if (desc.isEmpty()) {
                Toast.makeText(activity, activity.getString(R.string.auto_please_enter_a_descr_3), Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(activity, activity.getString(R.string.auto_amount_must_be_posit_4), Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(activity, activity.getString(R.string.auto_please_enter_a_valid_5), Toast.LENGTH_SHORT).show();
                return;
            }

            if (activity.editingRecordIndex != -1) {
                // Update mode
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
                // Add mode
                Record newRecord = new Record(desc, amount, activity.selectedRecordDate);
                newRecord.setRemarks(remarks);
                newRecord.setCategory(category);
                  newRecord.setAttachments(new java.util.ArrayList<>(activity.tempAttachments));
                newRecord.setOriginalIndex(EditorUIHelper.getNewOriginalIndex(activity));
                StateHelper.getActiveRecords(activity).add(newRecord);

                // Update UI elements
                editDesc.setText("");
                editAmount.setText("");
                activity.editRemarksField.setText("");
                if (activity.editCategoryField != null) activity.editCategoryField.setText("");
                EditorSortHelper.applySorting(activity);
                EditorUIHelper.populateRecordsList(activity);
            }
        });

        // Save Account action
        ResponsiveUI.setupClickable(btnSave, () -> {
            String title = editTitle.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(activity, activity.getString(R.string.auto_list_title_cannot_be_6), Toast.LENGTH_SHORT).show();
                return;
            }

            if (EditorUIHelper.isDuplicateTitle(activity, title)) {
                Toast.makeText(activity, activity.getString(R.string.auto_a_list_with_this_tit_7), Toast.LENGTH_SHORT).show();
                return;
            }

            // Re-sequentialize to close any gaps caused by deletions
            AppUtils.resequentializeRecords(activity.tempRecords);
            AppUtils.resequentializeRecords(activity.tempBudgetRecords);

            // Save record values
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

            // Write to storage
            StorageHelper.saveAppStorage(activity, activity.appStorage);
            
            // Clean up memory
            activity.currentEditingAccount = null;
            activity.tempRecords = null;
            activity.tempBudgetRecords = null;
            DashboardHelper.showDashboard(activity);
        });

        // Initial populate of record lists
        EditorUIHelper.populateRecordsList(activity);

        // Mount to main container
        activity.mainContainer.removeAllViews();
        activity.mainContainer.addView(editorView);
    }

}
