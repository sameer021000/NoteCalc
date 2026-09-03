package com.example.notecalc;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.notecalc.ncagent.*;
import android.widget.CheckBox;
import java.util.List;
import java.util.ArrayList;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    SettingsHelper settingsHelper;

    final java.util.List<String> tempAttachments = new java.util.ArrayList<>();
    static final int REQUEST_CODE_ATTACH = 1001;
    static final int REQUEST_CODE_CAMERA = 1002;
    String currentPhotoPath = null;
    android.widget.LinearLayout attachmentsContainer;
    android.widget.HorizontalScrollView attachmentsScroll;
    android.widget.TextView btnAttachFile;

    androidx.activity.result.ActivityResultLauncher<android.content.Intent> exportJsonLauncher;
    androidx.activity.result.ActivityResultLauncher<android.content.Intent> importJsonLauncher;

    FrameLayout mainContainer;
    AppStorage appStorage;
    AccountGroup currentViewGroup = null; // null means we are in the Dashboard
    Account currentEditingAccount;
    
    // Editor state
    private List<Record> tempRecords;
    private List<Record> tempBudgetRecords;
    boolean isBudgetMode = false; // false = Expenses, true = Budget
    
    String originalTitle = "";
    String selectedRecordDate = "";

    int editingRecordIndex = -1;
    EditText editDescField;
    EditText editAmountField;
    TextView btnRecordDateField;
    TextView btnAddRecordField;
    TextView btnCancelEditField;
    TextView labelAddRecordField;
    RecordsAdapter recordsAdapter;
    AccountsAdapter accountsAdapter;
    AccountsAdapter groupsAdapter;
    String dashboardSearchQuery = "";
    private boolean groupSortAscending = true;
    TextView btnSortTitle;
    TextView btnSortTotal;
    TextView btnSortLatest;
    TextView btnSortGroupTitle;
    TextView textTotalValField;
    TextView textTotalLabelField;
    com.google.android.material.snackbar.Snackbar currentSnackbar;

    TextView thSnoField;
    TextView thDescField;
    TextView thDateField;
    TextView thAmountField;

    private int expenseSortColumn = 0;
    private boolean expenseSortAscending = false;
    private int budgetSortColumn = 0;
    private boolean budgetSortAscending = false;
    
    int getSortColumn() { return isBudgetMode ? budgetSortColumn : expenseSortColumn; }
    boolean getSortAscending() { return isBudgetMode ? budgetSortAscending : expenseSortAscending; }
    void setSortColumn(int col) { if (isBudgetMode) budgetSortColumn = col; else expenseSortColumn = col; }
    void setSortAscending(boolean asc) { if (isBudgetMode) budgetSortAscending = asc; else expenseSortAscending = asc; }

    // Dashboard sort state: 0 = Title, 1 = Total Spending, 2 = Latest Modified
    private int dashboardSortMode = 0;
    private boolean dashboardSortAscending = true;
    
    private int archivedDashboardSortMode = 0;
    private boolean archivedDashboardSortAscending = true;
    private boolean archivedGroupSortAscending = true;

    // Editor record search query (persists while in editor, reset on openEditor)
    String currentRecordSearchQuery = "";

    // Fields for collapsible form, remarks, empty state, and bulk delete
    EditText editRemarksField;
    android.widget.AutoCompleteTextView editCategoryField;
    View formInputsContainer;
    TextView btnToggleForm;
    CheckBox cbSelectAllHeader;
    ImageView btnBulkActionsMenu;
    View editorEmptyState;
    View rowSearchAndBulk;
    View tableHeaderField;
    boolean isFormInputsCollapsed = false;

    // Bulk action container and selected total display
    View containerBulkActions;
    TextView textSelectedTotal;

    // Date range filter state (dd-MM-yyyy strings, null = no filter)
    private String expenseFilterDateFrom = null;
    private String expenseFilterDateTo = null;
    private Double expenseFilterAmountFrom = null;
    private Double expenseFilterAmountTo = null;

    private String budgetFilterDateFrom = null;
    private String budgetFilterDateTo = null;
    private Double budgetFilterAmountFrom = null;
    private Double budgetFilterAmountTo = null;

    String getFilterDateFrom() { return isBudgetMode ? budgetFilterDateFrom : expenseFilterDateFrom; }
    void setFilterDateFrom(String val) { if (isBudgetMode) budgetFilterDateFrom = val; else expenseFilterDateFrom = val; }
    String getFilterDateTo() { return isBudgetMode ? budgetFilterDateTo : expenseFilterDateTo; }
    void setFilterDateTo(String val) { if (isBudgetMode) budgetFilterDateTo = val; else expenseFilterDateTo = val; }
    Double getFilterAmountFrom() { return isBudgetMode ? budgetFilterAmountFrom : expenseFilterAmountFrom; }
    void setFilterAmountFrom(Double val) { if (isBudgetMode) budgetFilterAmountFrom = val; else expenseFilterAmountFrom = val; }
    Double getFilterAmountTo() { return isBudgetMode ? budgetFilterAmountTo : expenseFilterAmountTo; }
    void setFilterAmountTo(Double val) { if (isBudgetMode) budgetFilterAmountTo = val; else expenseFilterAmountTo = val; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        settingsHelper = new SettingsHelper(this);
        androidx.activity.EdgeToEdge.enable(this);
        
        exportJsonLauncher = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                android.net.Uri uri = result.getData().getData();
                if (uri != null) {
                    try {
                        java.io.OutputStream os = getContentResolver().openOutputStream(uri);
                        if (os != null) {
                            String json = appStorage.toJSONObject().toString(4);
                            os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                            os.close();
                            android.widget.Toast.makeText(this, "Backup Exported Successfully", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("NoteCalc", "Error exporting JSON", e);
                        android.widget.Toast.makeText(this, "Export failed", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        importJsonLauncher = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                android.net.Uri uri = result.getData().getData();
                if (uri != null) {
                    new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
                        .setTitle(getString(R.string.auto_restore_backup_38))
                        .setMessage(getString(R.string.auto_are_you_sure_this_wi_39))
                        .setPositiveButton("Overwrite", (d, w) -> {
                            try {
                                java.io.InputStream is = getContentResolver().openInputStream(uri);
                                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) sb.append(line);
                                if (is != null) is.close();
                                
                                appStorage = AppStorage.fromJSONObject(new org.json.JSONObject(sb.toString()));
                                StorageHelper.saveAppStorage(this, appStorage);
                                DashboardHelper.showDashboard(MainActivity.this);
                                android.widget.Toast.makeText(this, "Backup Restored!", android.widget.Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                android.util.Log.e("NoteCalc", "Error restoring JSON", e);
                                android.widget.Toast.makeText(this, "Invalid backup file", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                }
            }
        });

        setContentView(R.layout.activity_main);

        // Reference the root frame container
        mainContainer = findViewById(R.id.main_container);

        // Apply edge-to-edge window insets to main container
        ViewCompat.setOnApplyWindowInsetsListener(mainContainer, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load existing saved storage (groups and standalone accounts)
        appStorage = StorageHelper.loadAppStorage(this);

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentEditingAccount != null || (mainContainer.getChildAt(0) != null && mainContainer.getChildAt(0).getId() != R.id.dashboard_root)) {
                    if (tempRecords != null) for (Record r : tempRecords) r.setSelected(false);
                    if (tempBudgetRecords != null) for (Record r : tempBudgetRecords) r.setSelected(false);
                    currentEditingAccount = null;
                    tempRecords = null;
                    tempBudgetRecords = null;
                    dashboardSearchQuery = "";
                    DashboardHelper.showDashboard(MainActivity.this);
                } else if (currentViewGroup != null) {
                    currentViewGroup = null;
                    dashboardSearchQuery = "";
                    DashboardHelper.showDashboard(MainActivity.this);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });

        // Open the dashboard screen
        DashboardHelper.showDashboard(MainActivity.this);
    }

    /**
     * Renders the Dashboard screen containing the list of saved accounts.
     */

    @android.annotation.SuppressLint("SetTextI18n")
    final NCAgent ncAgent = new NCAgent();
    @android.annotation.SuppressLint("SetTextI18n")
    int getDashboardSortColumn() {
        if (currentViewGroup != null) return currentViewGroup.getSortMode();
        return ArchiveHelper.isShowingArchive ? archivedDashboardSortMode : dashboardSortMode;
    }
    void setDashboardSortColumn(int mode) {
        if (currentViewGroup != null) currentViewGroup.setSortMode(mode);
        else if (ArchiveHelper.isShowingArchive) archivedDashboardSortMode = mode;
        else dashboardSortMode = mode;
    }
    boolean getDashboardSortAscending() {
        if (currentViewGroup != null) return currentViewGroup.isSortAscending();
        return ArchiveHelper.isShowingArchive ? archivedDashboardSortAscending : dashboardSortAscending;
    }
    void setDashboardSortAscending(boolean asc) {
        if (currentViewGroup != null) currentViewGroup.setSortAscending(asc);
        else if (ArchiveHelper.isShowingArchive) archivedDashboardSortAscending = asc;
        else dashboardSortAscending = asc;
    }
    
    boolean getGroupSortAscending() {
        return ArchiveHelper.isShowingArchive ? archivedGroupSortAscending : groupSortAscending;
    }
    
    void setGroupSortAscending(boolean asc) {
        if (ArchiveHelper.isShowingArchive) archivedGroupSortAscending = asc;
        else groupSortAscending = asc;
    }

    /**
     * Renders the Account Editor screen.
     *
     * @param account The account to edit. If null, a new account is initialized.
     */
    @android.annotation.SuppressLint({"SetTextI18n", "ClickableViewAccessibility", "NotifyDataSetChanged"})
    void openEditor(Account account) {
        if (currentSnackbar != null) {
            currentSnackbar.dismiss();
            currentSnackbar = null;
        }
                LayoutInflater inflater = getLayoutInflater();
                View editorView = inflater.inflate(R.layout.layout_editor, mainContainer, false);

        currentEditingAccount = account;
        tempRecords = new ArrayList<>();
        tempBudgetRecords = new ArrayList<>();
        isBudgetMode = false;
        selectedRecordDate = AppUtils.getCurrentDateString();
        editingRecordIndex = -1;

        // Find views
        ImageView btnBack = editorView.findViewById(R.id.btn_back);
        ImageView btnAnalytics = editorView.findViewById(R.id.btn_analytics);
        if (btnAnalytics != null) {
            ResponsiveUI.setupClickable(btnAnalytics, true, () -> {
                if (currentEditingAccount == null || (currentEditingAccount.getRecords().isEmpty() && currentEditingAccount.getBudgetRecords().isEmpty())) {
                    android.widget.Toast.makeText(this, "Add some records to view analytics", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    AnalyticsHelper.showAnalytics(this, currentEditingAccount, mainContainer, () -> openEditor(currentEditingAccount));
                }
            });
        }

        EditText editTitle = editorView.findViewById(R.id.edit_account_title);
        TextView textTitleError = editorView.findViewById(R.id.text_title_error);
        EditText editDesc = editorView.findViewById(R.id.edit_record_desc);
        EditText editAmount = editorView.findViewById(R.id.edit_record_amount);
        TextView btnDate = editorView.findViewById(R.id.btn_record_date);
        TextView btnAdd = editorView.findViewById(R.id.btn_add_record);
        editCategoryField = editorView.findViewById(R.id.edit_record_category);
        btnAttachFile = editorView.findViewById(R.id.btn_attach_file);
        attachmentsScroll = editorView.findViewById(R.id.attachments_scroll);
        attachmentsContainer = editorView.findViewById(R.id.attachments_container);
        
        tempAttachments.clear();
        AttachmentHelper.renderEditorAttachments(MainActivity.this);

        if (btnAttachFile != null) {
            ResponsiveUI.setupClickable(btnAttachFile, true, () -> {
                if (tempAttachments.size() >= 3) {
                    Toast.makeText(this, getString(R.string.auto_max_3_files_allowed_2), Toast.LENGTH_SHORT).show();
                    return;
                }
                
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                android.view.View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_attach_file, null);
                builder.setView(dialogView);
                
                final androidx.appcompat.app.AlertDialog dialog = builder.create();
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                }
                
                android.view.View dialogRoot = dialogView.findViewById(R.id.dialog_root);
                dialogRoot.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgSecondaryColor(this), 0, 0, 16.0f));
                
                android.widget.TextView btnTakePhoto = dialogView.findViewById(R.id.btn_take_photo);
                android.widget.TextView btnChooseFile = dialogView.findViewById(R.id.btn_choose_file);
                
                btnTakePhoto.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(this), 0, 0, 8.0f));
                btnChooseFile.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(this), 0, 0, 8.0f));
                
                ResponsiveUI.setupClickable(btnTakePhoto, false, () -> {
                    dialog.dismiss();
                    try {
                        java.io.File attachmentsDir = new java.io.File(getFilesDir(), "attachments");
                        if (!attachmentsDir.exists()) attachmentsDir.mkdirs();
                        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new java.util.Date());
                        java.io.File imageFile = new java.io.File(attachmentsDir, "IMG_" + timeStamp + ".jpg");
                        currentPhotoPath = imageFile.getAbsolutePath();
                        android.net.Uri photoURI = androidx.core.content.FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".fileprovider", imageFile);
                        android.content.Intent takePictureIntent = new android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA);
                    } catch (Exception e) {
                        android.widget.Toast.makeText(this, "Could not start camera", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
                
                ResponsiveUI.setupClickable(btnChooseFile, false, () -> {
                    dialog.dismiss();
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    String[] mimeTypes = {"image/jpeg", "image/png", "image/jpg", "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
                    intent.putExtra(android.content.Intent.EXTRA_MIME_TYPES, mimeTypes);
                    startActivityForResult(intent, REQUEST_CODE_ATTACH);
                });
                
                dialog.show();
            });
        }
        if (editCategoryField != null) {
            java.util.Set<String> catSet = new java.util.HashSet<>();
            if (currentEditingAccount != null) {
                for (Record r : currentEditingAccount.getRecords()) {
                    if (!r.getCategory().isEmpty()) catSet.add(r.getCategory());
                }
            }
            java.util.List<String> catList = new java.util.ArrayList<>(catSet);
            java.util.Collections.sort(catList);
            android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, catList) {
                @androidx.annotation.NonNull
                @Override
                public android.view.View getView(int position, @androidx.annotation.Nullable android.view.View convertView, @androidx.annotation.NonNull android.view.ViewGroup parent) {
                    // Pass null to avoid ClassCastException since we wrap the view in a LinearLayout
                    android.view.View coreView = super.getView(position, null, parent);
                    
                    LinearLayout container = new LinearLayout(MainActivity.this);
                    container.setOrientation(LinearLayout.VERTICAL);
                    
                    if (coreView instanceof TextView) {
                        ((TextView) coreView).setTextColor(ThemeManager.getSecondaryAccentColor(MainActivity.this));
                        int hPad = (int)(12 * getResources().getDisplayMetrics().density);
                        int vPad = (int)(8 * getResources().getDisplayMetrics().density); // Slightly larger padding since we have lines
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
                        View divider = new View(MainActivity.this);
                        divider.setBackgroundColor(ThemeManager.getBorderColor(MainActivity.this));
                        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1); // 1px thin line
                        container.addView(divider, divParams);
                    }
                    
                    container.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                    return container;
                }
            };
            editCategoryField.setAdapter(catAdapter);
            
            // Set rounded corners for autocomplete drop down box
            editCategoryField.setDropDownBackgroundDrawable(ResponsiveUI.createRoundedBg(
                    this,
                    ThemeManager.getBgSecondaryColor(this),
                    ThemeManager.getBorderColor(this),
                    1.0f,
                    8.0f // nice curve
            ));
            // Limit drop-down height so it doesn't get fully covered by keyboard and becomes scrollable
            editCategoryField.setDropDownHeight((int) (180 * getResources().getDisplayMetrics().density));
            
            editCategoryField.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    editCategoryField.showDropDown();
                }
            });
            editCategoryField.setOnClickListener(v -> editCategoryField.showDropDown());
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
        editDescField = editDesc;
        editAmountField = editAmount;
        btnRecordDateField = btnDate;
        btnAddRecordField = btnAdd;
        btnCancelEditField = btnCancelEdit;
        labelAddRecordField = editorView.findViewById(R.id.label_add_record);
                listRecordsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recordsAdapter = new RecordsAdapter(MainActivity.this);
        listRecordsRecyclerView.setAdapter(recordsAdapter);

        // Bind NC Agent FAB
        android.widget.ImageView btnNCAgent = editorView.findViewById(R.id.btn_nc_agent);
        if (btnNCAgent != null) {
            btnNCAgent.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getPrimaryAccentColor(MainActivity.this), ThemeManager.getPrimaryAccentColor(MainActivity.this), 0f, 100f));
            btnNCAgent.setColorFilter(getColor(R.color.text_on_accent));
            btnNCAgent.setImageResource(android.R.drawable.ic_btn_speak_now); // Unique microphone/voice icon representing natural language
            btnNCAgent.setOnClickListener(v -> NCAgentHelper.showNCAgentBottomSheet(MainActivity.this, ncAgent));
            if (account != null && account.isArchived()) {
                btnNCAgent.setVisibility(View.GONE);
            }
        }
        
        // Setup Swipe-to-Delete and Drag-and-Drop for Records
        new androidx.recyclerview.widget.ItemTouchHelper(TouchHelper.getRecordSwipeCallback(this)).attachToRecyclerView(listRecordsRecyclerView);
        textTotalValField = textTotalVal;
        textTotalLabelField = textTotalLabel;

        // Collapsible form, remarks, empty state, and bulk delete view mappings
        editRemarksField = editorView.findViewById(R.id.edit_record_remarks);
        formInputsContainer = editorView.findViewById(R.id.form_inputs_container);
        btnToggleForm = editorView.findViewById(R.id.btn_toggle_form);
        cbSelectAllHeader = editorView.findViewById(R.id.cb_select_all);
        btnBulkActionsMenu = editorView.findViewById(R.id.btn_bulk_actions_menu);
        containerBulkActions = editorView.findViewById(R.id.container_bulk_actions);
        textSelectedTotal = editorView.findViewById(R.id.text_selected_total);
        editorEmptyState = editorView.findViewById(R.id.editor_empty_state);
        rowSearchAndBulk = editorView.findViewById(R.id.row_search_and_bulk);
        tableHeaderField = tableHeader;

        // Reset editor search query, selections, and filters
        currentRecordSearchQuery = "";
        expenseFilterDateFrom = null;
        expenseFilterDateTo = null;
        expenseFilterAmountFrom = null;
        expenseFilterAmountTo = null;
        budgetFilterDateFrom = null;
        budgetFilterDateTo = null;
        budgetFilterAmountFrom = null;
        budgetFilterAmountTo = null;
        isFormInputsCollapsed = true;

        // Wire the collapsible form header click action
        Runnable toggleForm = () -> {
            isFormInputsCollapsed = !isFormInputsCollapsed;
            formInputsContainer.setVisibility(isFormInputsCollapsed ? View.GONE : View.VISIBLE);
            btnToggleForm.setText(isFormInputsCollapsed ? "Expand [ + ]" : "Minimize [ - ]");
        };
        ResponsiveUI.setupClickable(btnToggleForm, false, toggleForm);
        
        android.graphics.drawable.StateListDrawable toggleSelector = new android.graphics.drawable.StateListDrawable();
        toggleSelector.addState(new int[]{android.R.attr.state_pressed}, new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        toggleSelector.addState(new int[]{}, ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(this), ThemeManager.getBorderColor(this), 1.0f, 12.0f));
        btnToggleForm.setBackground(toggleSelector);
        
        // Add some padding to make the pill look good
        int pLR = (int) (12 * getResources().getDisplayMetrics().density);
        int pTB = (int) (6 * getResources().getDisplayMetrics().density);
        btnToggleForm.setPadding(pLR, pTB, pLR, pTB);

        // Apply default minimized state
        if (account == null) {
            isFormInputsCollapsed = false;
            formInputsContainer.setVisibility(View.VISIBLE);
            btnToggleForm.setText(getString(R.string.auto_minimize_19));
        } else {
            isFormInputsCollapsed = true;
            formInputsContainer.setVisibility(View.GONE);
            btnToggleForm.setText(getString(R.string.auto_expand_20));
        }

        if (account != null && account.isArchived()) {
            if (formContainer != null) {
                formContainer.setVisibility(View.GONE);
            }
        }

        // Wire the record search bar
        EditText editRecordsSearch = editorView.findViewById(R.id.edit_records_search);
        editRecordsSearch.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgSecondaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
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
                currentRecordSearchQuery = s.toString();
                recordsAdapter.setFilter(currentRecordSearchQuery);
            }
        });

        // Wire bulk delete and headers check sync
        cbSelectAllHeader.setOnCheckedChangeListener(null);
        cbSelectAllHeader.setChecked(false);
        cbSelectAllHeader.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (recordsAdapter != null) {
                for (Record r : recordsAdapter.displayRecords) {
                    r.setSelected(isChecked);
                }
                recordsAdapter.notifyDataSetChanged();
                BulkActionsHelper.updateBulkActionsState(MainActivity.this);
            }
        });

        if (btnBulkActionsMenu != null) {
            btnBulkActionsMenu.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, Color.parseColor("#15FFFFFF"), 4.0f));
            ResponsiveUI.setupClickable(btnBulkActionsMenu, true, () -> MenuHelper.showBulkActionsMenu(MainActivity.this, btnBulkActionsMenu));
        }

        // Assign header fields and reset sort state
        thSnoField = editorView.findViewById(R.id.th_sno);
        thDescField = editorView.findViewById(R.id.th_desc);
        thDateField = editorView.findViewById(R.id.th_date);
        thAmountField = editorView.findViewById(R.id.th_amount);

        expenseSortColumn = 0;
        expenseSortAscending = false;
        budgetSortColumn = 0;
        budgetSortAscending = false;

        // Set up header touch backgrounds and click listeners
        thSnoField.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, Color.parseColor("#15FFFFFF"), 4.0f));
        thDescField.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, Color.parseColor("#15FFFFFF"), 4.0f));
        thDateField.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, Color.parseColor("#15FFFFFF"), 4.0f));
        thAmountField.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, Color.parseColor("#15FFFFFF"), 4.0f));

        ResponsiveUI.setupClickable(thSnoField, false, () -> EditorSortHelper.onHeaderClicked(MainActivity.this, 0));
        ResponsiveUI.setupClickable(thDescField, false, () -> EditorSortHelper.onHeaderClicked(MainActivity.this, 1));

        // Date header: click = sort, long-press (1s) = date range filter
        thDateField.setOnClickListener(v -> EditorSortHelper.onHeaderClicked(MainActivity.this, 2));
        thDateField.setOnLongClickListener(v -> {
            FilterHelper.showDateRangeFilterDialog(MainActivity.this);
            return true;
        });

        // Amount header: click = sort, long-press (1s) = amount range filter
        thAmountField.setOnClickListener(v -> EditorSortHelper.onHeaderClicked(MainActivity.this, 3));
        thAmountField.setOnLongClickListener(v -> {
            FilterHelper.showAmountRangeFilterDialog(MainActivity.this);
            return true;
        });

        EditorSortHelper.updateHeaderLabels(MainActivity.this);

        // Pre-populate if editing existing account
        if (account != null) {
            
            editTitle.setText(account.getTitle());
            if (account.isArchived()) {
                editTitle.setEnabled(false);
                editTitle.setAlpha(0.7f);
            }
            originalTitle = account.getTitle();
            tempRecords.addAll(account.getRecords());
            
            boolean needsMigration = false;
            for (Record r : tempRecords) { if (r.getOriginalIndex() == -1) { needsMigration = true; break; } }
            if (needsMigration) {
                for (int i = 0; i < tempRecords.size(); i++) {
                    tempRecords.get(i).setOriginalIndex(i);
                }
            }

            if (account.getBudgetRecords() != null) {
                tempBudgetRecords.addAll(account.getBudgetRecords());
                boolean needsBudgetMigration = false;
                for (Record r : tempBudgetRecords) { if (r.getOriginalIndex() == -1) { needsBudgetMigration = true; break; } }
                if (needsBudgetMigration) {
                    for (int i = 0; i < tempBudgetRecords.size(); i++) {
                        tempBudgetRecords.get(i).setOriginalIndex(i);
                    }
                }
            }
        } else {
            
            originalTitle = "";
        }
        
        // Mode toggle logic
        Runnable updateModeToggleUI = () -> {
            if (btnModeExpenses != null && btnModeBudget != null) {
                btnModeExpenses.setBackgroundColor(isBudgetMode ? ThemeManager.getBgSecondaryColor(MainActivity.this) : ThemeManager.getPrimaryAccentColor(MainActivity.this));
                btnModeExpenses.setTextColor(getColor(isBudgetMode ? R.color.text_tertiary : R.color.text_on_accent));
                
                btnModeBudget.setBackgroundColor(isBudgetMode ? ThemeManager.getPrimaryAccentColor(MainActivity.this) : ThemeManager.getBgSecondaryColor(MainActivity.this));
                btnModeBudget.setTextColor(getColor(isBudgetMode ? R.color.text_on_accent : R.color.text_tertiary));
            }
            EditorSortHelper.applySorting(MainActivity.this);
              recordsAdapter.setFilter(currentRecordSearchQuery);
            EditorSortHelper.updateHeaderLabels(MainActivity.this);
            EditorSortHelper.updateDateHeaderIndicator(MainActivity.this);
            EditorSortHelper.updateAmountHeaderIndicator(MainActivity.this);
            
            if (textRemainingPurse != null) {
                if (tempBudgetRecords.isEmpty() && !isBudgetMode) {
                    textRemainingPurse.setVisibility(View.GONE);
                } else {
                    textRemainingPurse.setVisibility(View.VISIBLE);
                    double totalBudget = 0;
                    for (Record r : tempBudgetRecords) totalBudget += r.getAmount();
                    double totalExpenses = 0;
                    for (Record r : tempRecords) totalExpenses += r.getAmount();
                    double remaining = totalBudget - totalExpenses;
                    textRemainingPurse.setText(String.format(Locale.getDefault(), "Balance : %.2f", remaining));
                }
            }
        };
        
        if (btnModeExpenses != null) ResponsiveUI.setupClickable(btnModeExpenses, false, () -> {
            if (isBudgetMode) {
                isBudgetMode = false;
                EditorModeHelper.cancelEditRecordMode(MainActivity.this);
                updateModeToggleUI.run();
            }
        });
        if (btnModeBudget != null) ResponsiveUI.setupClickable(btnModeBudget, false, () -> {
            if (!isBudgetMode) {
                isBudgetMode = true;
                EditorModeHelper.cancelEditRecordMode(MainActivity.this);
                updateModeToggleUI.run();
            }
        });
        updateModeToggleUI.run();
        EditorSortHelper.applySorting(MainActivity.this);
        EditorUIHelper.populateRecordsList(MainActivity.this);

        // Apply responsive dimensions
        ResponsiveUI.applyResponsiveness(editorView);

        // Programmatic monochrome styling
        editTitle.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgSecondaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                6.0f
        ));

        if (formContainer != null) {
            formContainer.setBackground(ResponsiveUI.createRoundedBg(
                    this,
                    ThemeManager.getBgSecondaryColor(MainActivity.this),
                    ThemeManager.getBorderColor(MainActivity.this),
                    1.0f,
                    8.0f
            ));
        }

        tableHeader.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgSecondaryColor(MainActivity.this),
                0,
                0,
                4.0f
        ));

        editDesc.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgPrimaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                4.0f
        ));

        editAmount.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgPrimaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                4.0f
        ));

        editRemarksField.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgPrimaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                4.0f
        ));
        
        if (editCategoryField != null) {
            editCategoryField.setBackground(ResponsiveUI.createRoundedBg(
                    this,
                    ThemeManager.getBgPrimaryColor(MainActivity.this),
                    ThemeManager.getBorderColor(MainActivity.this),
                    1.0f,
                    4.0f
            ));
        }

        btnDate.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgPrimaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                4.0f
        ));

        btnCancelEdit.setBackground(ResponsiveUI.createRoundedBg(
                this,
                getColor(R.color.error_red),
                getColor(R.color.error_red),
                0f,
                4.0f
        ));
        btnCancelEdit.setTextColor(getColor(R.color.text_on_accent));
        btnCancelEdit.setTypeface(null, android.graphics.Typeface.BOLD);

        btnAdd.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getPrimaryAccentColor(MainActivity.this),
                0,
                0,
                4.0f
        ));

        if (account != null && account.isArchived()) {
            btnSave.setVisibility(View.GONE);
        }
        btnSave.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getPrimaryAccentColor(MainActivity.this),
                0,
                0,
                6.0f
        ));

        // Date Display
        btnDate.setText(selectedRecordDate);

        // Title text validation watcher
        editTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                if (EditorUIHelper.isDuplicateTitle(MainActivity.this, input)) {
                    textTitleError.setVisibility(View.VISIBLE);
                    editTitle.setBackground(ResponsiveUI.createRoundedBg(
                            MainActivity.this,
                            ThemeManager.getBgSecondaryColor(MainActivity.this),
                            getColor(R.color.error_red),
                            1.5f,
                            6.0f
                    ));
                } else {
                    textTitleError.setVisibility(View.GONE);
                    editTitle.setBackground(ResponsiveUI.createRoundedBg(
                            MainActivity.this,
                            ThemeManager.getBgSecondaryColor(MainActivity.this),
                            ThemeManager.getBorderColor(MainActivity.this),
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
            dashboardSearchQuery = "";
            if (tempRecords != null) for (Record r : tempRecords) r.setSelected(false);
            if (tempBudgetRecords != null) for (Record r : tempBudgetRecords) r.setSelected(false);
            DashboardHelper.showDashboard(MainActivity.this);
        });

        // Date picker action
        ResponsiveUI.setupClickable(btnDate, () -> DialogHelper.showDatePicker(this, selectedRecordDate, btnDate, newDate -> selectedRecordDate = newDate));

        // Cancel edit action
        ResponsiveUI.setupClickable(btnCancelEdit, () -> EditorModeHelper.cancelEditRecordMode(MainActivity.this));

        // Add/Update item action
        ResponsiveUI.setupClickable(btnAdd, () -> {
            String desc = editDesc.getText().toString().trim();
            String amountStr = editAmount.getText().toString().trim();
            String remarks = editRemarksField.getText().toString().trim();
            String category = editCategoryField != null ? editCategoryField.getText().toString().trim() : "";

            if (desc.isEmpty()) {
                Toast.makeText(MainActivity.this, getString(R.string.auto_please_enter_a_descr_3), Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(MainActivity.this, getString(R.string.auto_amount_must_be_posit_4), Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, getString(R.string.auto_please_enter_a_valid_5), Toast.LENGTH_SHORT).show();
                return;
            }

            if (editingRecordIndex != -1) {
                // Update mode
                Record record = getActiveRecords().get(editingRecordIndex);
                record.setDescription(desc);
                record.setAmount(amount);
                record.setDate(selectedRecordDate);
                record.setRemarks(remarks);
                record.setCategory(category);
                record.setAttachments(new java.util.ArrayList<>(tempAttachments));
                record.setTimestampMillis(System.currentTimeMillis());
                EditorSortHelper.applySorting(MainActivity.this);
                EditorModeHelper.cancelEditRecordMode(MainActivity.this);
            } else {
                // Add mode
                Record newRecord = new Record(desc, amount, selectedRecordDate);
                newRecord.setRemarks(remarks);
                newRecord.setCategory(category);
                  newRecord.setAttachments(new java.util.ArrayList<>(tempAttachments));
                newRecord.setOriginalIndex(EditorUIHelper.getNewOriginalIndex(MainActivity.this));
                getActiveRecords().add(newRecord);

                // Update UI elements
                editDesc.setText("");
                editAmount.setText("");
                editRemarksField.setText("");
                if (editCategoryField != null) editCategoryField.setText("");
                EditorSortHelper.applySorting(MainActivity.this);
                EditorUIHelper.populateRecordsList(MainActivity.this);
            }
        });

        // Save Account action
        ResponsiveUI.setupClickable(btnSave, () -> {
            String title = editTitle.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(MainActivity.this, getString(R.string.auto_list_title_cannot_be_6), Toast.LENGTH_SHORT).show();
                return;
            }

            if (EditorUIHelper.isDuplicateTitle(MainActivity.this, title)) {
                Toast.makeText(MainActivity.this, getString(R.string.auto_a_list_with_this_tit_7), Toast.LENGTH_SHORT).show();
                return;
            }

            // Re-sequentialize to close any gaps caused by deletions
            AppUtils.resequentializeRecords(tempRecords);
            AppUtils.resequentializeRecords(tempBudgetRecords);

            // Save record values
            if (currentEditingAccount == null) {
                Account newAccount = new Account(title, tempRecords, System.currentTimeMillis());
                newAccount.setBudgetRecords(tempBudgetRecords);
                newAccount.setHasBudget(!tempBudgetRecords.isEmpty());
                
                if (currentViewGroup != null) {
                    currentViewGroup.getAccounts().add(newAccount);
                    currentViewGroup.updateLastModified();
                } else {
                    appStorage.standaloneAccounts.add(newAccount);
                }
            } else {
                currentEditingAccount.setTitle(title);
                currentEditingAccount.setRecords(tempRecords);
                currentEditingAccount.setBudgetRecords(tempBudgetRecords);
                currentEditingAccount.setHasBudget(!tempBudgetRecords.isEmpty());
                currentEditingAccount.updateLastModified();
            }

            // Write to storage
            StorageHelper.saveAppStorage(MainActivity.this, appStorage);
            
            // Clean up memory
            currentEditingAccount = null;
            tempRecords = null;
            tempBudgetRecords = null;
            DashboardHelper.showDashboard(MainActivity.this);
        });

        // Initial populate of record lists
        EditorUIHelper.populateRecordsList(MainActivity.this);

        // Mount to main container
        mainContainer.removeAllViews();
        mainContainer.addView(editorView);
    }

    /**
     * Converts a date string from dd-MM-yyyy to compact DDMonthNameYY format.
     * Example: "24-06-2026" -> "24Jun26"
     */

    List<Record> getActiveRecords() {
        return isBudgetMode ? tempBudgetRecords : tempRecords;
    }

    @android.annotation.SuppressLint("SetTextI18n")

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AttachmentHelper.handleActivityResult(MainActivity.this, requestCode, resultCode, data);
    }
}
