package com.example.notecalc;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.example.notecalc.pdf.PdfSortOrder;

public class MainActivity extends AppCompatActivity {
    SettingsHelper settingsHelper;

    private final java.util.List<String> tempAttachments = new java.util.ArrayList<>();
    private static final int REQUEST_CODE_ATTACH = 1001;
    private static final int REQUEST_CODE_CAMERA = 1002;
    private String currentPhotoPath = null;
    private android.widget.LinearLayout attachmentsContainer;
    private android.widget.HorizontalScrollView attachmentsScroll;
    private android.widget.TextView btnAttachFile;

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
    
    private String originalTitle = "";
    private String selectedRecordDate = "";

    int editingRecordIndex = -1;
    private EditText editDescField;
    private EditText editAmountField;
    private TextView btnRecordDateField;
    private TextView btnAddRecordField;
    private TextView btnCancelEditField;
    private TextView labelAddRecordField;
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

    private TextView thSnoField;
    private TextView thDescField;
    private TextView thDateField;
    private TextView thAmountField;

    private int expenseSortColumn = 0;
    private boolean expenseSortAscending = false;
    private int budgetSortColumn = 0;
    private boolean budgetSortAscending = false;
    
    int getSortColumn() { return isBudgetMode ? budgetSortColumn : expenseSortColumn; }
    boolean getSortAscending() { return isBudgetMode ? budgetSortAscending : expenseSortAscending; }
    private void setSortColumn(int col) { if (isBudgetMode) budgetSortColumn = col; else expenseSortColumn = col; }
    private void setSortAscending(boolean asc) { if (isBudgetMode) budgetSortAscending = asc; else expenseSortAscending = asc; }

    // Dashboard sort state: 0 = Title, 1 = Total Spending, 2 = Latest Modified
    private int dashboardSortMode = 0;
    private boolean dashboardSortAscending = true;
    
    private int archivedDashboardSortMode = 0;
    private boolean archivedDashboardSortAscending = true;
    private boolean archivedGroupSortAscending = true;

    // Editor record search query (persists while in editor, reset on openEditor)
    String currentRecordSearchQuery = "";

    // Fields for collapsible form, remarks, empty state, and bulk delete
    private EditText editRemarksField;
    private android.widget.AutoCompleteTextView editCategoryField;
    private View formInputsContainer;
    private TextView btnToggleForm;
    CheckBox cbSelectAllHeader;
    ImageView btnBulkActionsMenu;
    private View editorEmptyState;
    private View rowSearchAndBulk;
    private View tableHeaderField;
    private boolean isFormInputsCollapsed = false;

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
        renderEditorAttachments();

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

        ResponsiveUI.setupClickable(thSnoField, false, () -> onHeaderClicked(0));
        ResponsiveUI.setupClickable(thDescField, false, () -> onHeaderClicked(1));

        // Date header: click = sort, long-press (1s) = date range filter
        thDateField.setOnClickListener(v -> onHeaderClicked(2));
        thDateField.setOnLongClickListener(v -> {
            FilterHelper.showDateRangeFilterDialog(MainActivity.this);
            return true;
        });

        // Amount header: click = sort, long-press (1s) = amount range filter
        thAmountField.setOnClickListener(v -> onHeaderClicked(3));
        thAmountField.setOnLongClickListener(v -> {
            FilterHelper.showAmountRangeFilterDialog(MainActivity.this);
            return true;
        });

        updateHeaderLabels();

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
            applySorting();
              recordsAdapter.setFilter(currentRecordSearchQuery);
            updateHeaderLabels();
            updateDateHeaderIndicator();
            updateAmountHeaderIndicator();
            
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
                cancelEditRecordMode();
                updateModeToggleUI.run();
            }
        });
        if (btnModeBudget != null) ResponsiveUI.setupClickable(btnModeBudget, false, () -> {
            if (!isBudgetMode) {
                isBudgetMode = true;
                cancelEditRecordMode();
                updateModeToggleUI.run();
            }
        });
        updateModeToggleUI.run();
        applySorting();
        populateRecordsList();

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
                if (isDuplicateTitle(input)) {
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
        ResponsiveUI.setupClickable(btnCancelEdit, this::cancelEditRecordMode);

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
                applySorting();
                cancelEditRecordMode();
            } else {
                // Add mode
                Record newRecord = new Record(desc, amount, selectedRecordDate);
                newRecord.setRemarks(remarks);
                newRecord.setCategory(category);
                  newRecord.setAttachments(new java.util.ArrayList<>(tempAttachments));
                newRecord.setOriginalIndex(getNewOriginalIndex());
                getActiveRecords().add(newRecord);

                // Update UI elements
                editDesc.setText("");
                editAmount.setText("");
                editRemarksField.setText("");
                if (editCategoryField != null) editCategoryField.setText("");
                applySorting();
                populateRecordsList();
            }
        });

        // Save Account action
        ResponsiveUI.setupClickable(btnSave, () -> {
            String title = editTitle.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(MainActivity.this, getString(R.string.auto_list_title_cannot_be_6), Toast.LENGTH_SHORT).show();
                return;
            }

            if (isDuplicateTitle(title)) {
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
        populateRecordsList();

        // Mount to main container
        mainContainer.removeAllViews();
        mainContainer.addView(editorView);
    }

    /**
     * Helper to render the records in the table format.
     */
    void populateRecordsList() {
        if (recordsAdapter != null) {
            recordsAdapter.refreshDisplay();
        }

        // Toggle empty state and table rows visibility
        boolean isEmpty = getActiveRecords().isEmpty();
        if (editorEmptyState != null) {
            editorEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (rowSearchAndBulk != null) {
            rowSearchAndBulk.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
        if (tableHeaderField != null) {
            tableHeaderField.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }

        // Sync select-all header checkbox after any list change
        BulkActionsHelper.updateSelectAllHeaderState(MainActivity.this);
        BulkActionsHelper.updateBulkActionsState(MainActivity.this);
    }

    void applySorting() {
        if (getActiveRecords() == null || getActiveRecords().isEmpty()) {
            return;
        }

        getActiveRecords().sort(new java.util.Comparator<>() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

            @Override
            public int compare(Record r1, Record r2) {
                int c = 0;
                switch (getSortColumn()) {
                    case 0: // S.No
                        c = Integer.compare(r1.getOriginalIndex(), r2.getOriginalIndex());
                        break;
                    case 1: // Description
                        c = r1.getDescription().compareToIgnoreCase(r2.getDescription());
                        break;
                    case 2: // Date
                        try {
                            Date d1 = sdf.parse(r1.getDate());
                            Date d2 = sdf.parse(r2.getDate());
                            if (d1 != null && d2 != null) {
                                c = d1.compareTo(d2);
                                if (c == 0) {
                                    c = Long.compare(r1.getTimestampMillis(), r2.getTimestampMillis());
                                }
                            }
                        } catch (Exception ignored) {}
                        break;
                    case 3: // Amount
                        c = Double.compare(r1.getAmount(), r2.getAmount());
                        break;
                }
                return getSortAscending() ? c : -c;
            }
        });
    }

    @android.annotation.SuppressLint("SetTextI18n")
    void updateHeaderLabels() {
        if (thSnoField != null) {
            thSnoField.setText(getString(R.string.th_sno) + (getSortColumn() == 0 ? (getSortAscending() ? "  ▲" : "  ▼") : ""));
        }
        if (thDescField != null) {
            thDescField.setText(getString(R.string.th_desc) + (getSortColumn() == 1 ? (getSortAscending() ? "  ▲" : "  ▼") : ""));
        }
        if (thDateField != null) {
            thDateField.setText(getString(R.string.th_date) + (getSortColumn() == 2 ? (getSortAscending() ? "  ▲" : "  ▼") : ""));
        }
        if (thAmountField != null) {
            thAmountField.setText(getString(R.string.th_amount) + (getSortColumn() == 3 ? (getSortAscending() ? "  ▲" : "  ▼") : ""));
        }
    }

    private void onHeaderClicked(int col) {
        if (getSortColumn() == col) {
            setSortAscending(!getSortAscending());
        } else {
            setSortColumn(col);
            setSortAscending(true);
        }

        applySorting();
        populateRecordsList();
        updateHeaderLabels();
    }

    int getNewOriginalIndex() {
        int maxIndex = -1;
        for (Record r : getActiveRecords()) {
            if (r.getOriginalIndex() > maxIndex) {
                maxIndex = r.getOriginalIndex();
            }
        }
        return maxIndex + 1;
    }

    /**
     * Syncs the "select all" header checkbox state based on visible displayRecords selection.
     * States: unchecked (none selected), checked (all selected), indeterminate (partial).
     */
    @android.annotation.SuppressLint("SetTextI18n")

    boolean isFilterActive() {
        if (recordsAdapter != null && !recordsAdapter.filterCategories.isEmpty()) return true;
        if (currentRecordSearchQuery != null && !currentRecordSearchQuery.trim().isEmpty()) return true;
        if (getFilterDateFrom() != null || getFilterDateTo() != null) return true;
        return getFilterAmountFrom() != null || getFilterAmountTo() != null;
    }

    void enterEditRecordMode(int index, Record record) {
        editingRecordIndex = index;
        selectedRecordDate = record.getDate();

        editDescField.setText(record.getDescription());
        editAmountField.setText(String.format(Locale.getDefault(), "%.2f", record.getAmount()));
        editRemarksField.setText(record.getRemarks());
        btnRecordDateField.setText(selectedRecordDate);
        if (editCategoryField != null) editCategoryField.setText(record.getCategory() == null ? "" : record.getCategory());

        // Load attachments
        tempAttachments.clear();
        if (record.getAttachments() != null) tempAttachments.addAll(record.getAttachments());
        renderEditorAttachments();

        // Auto-expand form
        if (formInputsContainer != null && btnToggleForm != null) {
            isFormInputsCollapsed = false;
            formInputsContainer.setVisibility(android.view.View.VISIBLE);
            btnToggleForm.setText(getString(R.string.auto_minimize_21));
        }

        if (isBudgetMode) {
            labelAddRecordField.setText(getString(R.string.auto_edit_budget_22));
            btnAddRecordField.setText(getString(R.string.auto_edit_budget_23));
            editDescField.setHint(getString(R.string.auto_description_32));
            editRemarksField.setHint(getString(R.string.auto_remarks_optional_33));
        } else {
            labelAddRecordField.setText(R.string.label_edit_record);
            btnAddRecordField.setText(R.string.btn_edit_record);
            editDescField.setHint(R.string.hint_record_desc);
            editRemarksField.setHint(getString(R.string.auto_remarks_e_g_bought_a_34));
        }
        btnCancelEditField.setVisibility(View.VISIBLE);
        populateRecordsList();
    }

    @android.annotation.SuppressLint("SetTextI18n")
    void cancelEditRecordMode() {
        editingRecordIndex = -1;
        selectedRecordDate = AppUtils.getCurrentDateString();

        editDescField.setText("");
        editAmountField.setText("");
        editRemarksField.setText("");
        btnRecordDateField.setText(selectedRecordDate);
        
        tempAttachments.clear();
        renderEditorAttachments();

        if (isBudgetMode) {
            labelAddRecordField.setText(getString(R.string.auto_add_budget_24));
            btnAddRecordField.setText(getString(R.string.auto_add_budget_25));
            editDescField.setHint(getString(R.string.auto_description_35));
            editRemarksField.setHint(getString(R.string.auto_remarks_optional_36));
        } else {
            labelAddRecordField.setText(R.string.label_add_record);
            btnAddRecordField.setText(R.string.btn_add_record);
            editDescField.setHint(R.string.hint_record_desc);
            editRemarksField.setHint(getString(R.string.auto_remarks_e_g_bought_a_37));
        }

        btnCancelEditField.setVisibility(View.GONE);

        populateRecordsList();
    }

    /**
     * Evaluates if the entered title already exists in saved accounts.
     */
    private boolean isDuplicateTitle(String title) {
        for (Account acc : appStorage.standaloneAccounts) {
            if (currentEditingAccount != null && acc.getTitle().equalsIgnoreCase(originalTitle)) {
                continue;
            }
            if (acc.getTitle().equalsIgnoreCase(title.trim())) {
                return true;
            }
        }
        for (AccountGroup group : appStorage.groups) {
            for (Account acc : group.getAccounts()) {
                if (currentEditingAccount != null && acc.getTitle().equalsIgnoreCase(originalTitle)) {
                    continue;
                }
                if (acc.getTitle().equalsIgnoreCase(title.trim())) {
                    return true;
                }
            }
        }
        return false;
    }



    

    /** Visual indicator on Date column header when filter is active. */
    void updateDateHeaderIndicator() {
        if (thDateField == null) return;
        boolean active = (getFilterDateFrom() != null || getFilterDateTo() != null);
        thDateField.setTextColor(active ? ThemeManager.getPrimaryAccentColor(MainActivity.this) : ThemeManager.getSecondaryAccentColor(MainActivity.this));
    }

    /** Visual indicator on Amount column header when filter is active. */
    void updateAmountHeaderIndicator() {
        if (thAmountField == null) return;
        boolean active = (getFilterAmountFrom() != null || getFilterAmountTo() != null);
        thAmountField.setTextColor(active ? ThemeManager.getPrimaryAccentColor(MainActivity.this) : ThemeManager.getSecondaryAccentColor(MainActivity.this));
    }


    void generateAndOpenAllPdf() {
        android.app.Dialog progressDialog = DialogHelper.showProgressDialog(MainActivity.this);
        
        new Thread(() -> {
            android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
            try {
                int[] pageTracker = {0};
                boolean hasRecords = false;
                
                for (AccountGroup group : appStorage.groups) {
                    for (Account account : group.getAccounts()) {
                        if (!account.getRecords().isEmpty()) {
                            appendAccountToPdf(document, account, pageTracker, PdfSortOrder.SNO);
                            hasRecords = true;
                        }
                    }
                }
                for (Account account : appStorage.standaloneAccounts) {
                    if (!account.getRecords().isEmpty()) {
                        appendAccountToPdf(document, account, pageTracker, PdfSortOrder.SNO);
                        hasRecords = true;
                    }
                }
                
                if (!hasRecords) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(MainActivity.this, "No records found to export.", android.widget.Toast.LENGTH_SHORT).show();
                    });
                    document.close();
                    return;
                }

                java.io.File pdfDir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
                if (pdfDir == null) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(MainActivity.this, "Cannot access documents directory.", android.widget.Toast.LENGTH_LONG).show();
                    });
                    document.close();
                    return;
                }
                if (!pdfDir.exists() && !pdfDir.mkdirs()) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(MainActivity.this, "Failed to create directory.", android.widget.Toast.LENGTH_LONG).show();
                    });
                    document.close();
                    return;
                }
                java.io.File file = new java.io.File(pdfDir, "All_Accounts_Export.pdf");
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    document.writeTo(fos);
                }
                try { document.close(); } catch (Exception ignored) {}

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    try {
                        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".fileprovider", file);
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "application/pdf");
                        intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                        // Check if any app can handle the PDF intent
                        if (intent.resolveActivity(getPackageManager()) == null) {
                            android.widget.Toast.makeText(MainActivity.this, "No PDF viewer installed. Please install one from the Play Store.", android.widget.Toast.LENGTH_LONG).show();
                            return;
                        }

                        startActivity(intent);
                    } catch (Exception ex) {
                        android.widget.Toast.makeText(MainActivity.this, "Failed to open PDF: " + ex.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("NoteCalc", "Failed to generate PDF", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    android.widget.Toast.makeText(MainActivity.this, "Failed to generate PDF: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                });
                try { document.close(); } catch (Exception ignored) {}
            }
        }).start();
    }

    void generateAndOpenGroupPdf(AccountGroup group, PdfSortOrder sortOrder) {
        android.app.Dialog progressDialog = DialogHelper.showProgressDialog(MainActivity.this);
        
        new Thread(() -> {
            android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
            try {
                int[] pageTracker = {0};
                boolean hasRecords = false;
                
                for (Account account : group.getAccounts()) {
                    if (!account.getRecords().isEmpty()) {
                        appendAccountToPdf(document, account, pageTracker, sortOrder);
                        hasRecords = true;
                    }
                }
                
                if (!hasRecords) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(MainActivity.this, "No records found to export in this group.", android.widget.Toast.LENGTH_SHORT).show();
                    });
                    document.close();
                    return;
                }

                java.io.File pdfDir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
                if (pdfDir == null) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(MainActivity.this, "Cannot access documents directory.", android.widget.Toast.LENGTH_LONG).show();
                    });
                    document.close();
                    return;
                }
                if (!pdfDir.exists() && !pdfDir.mkdirs()) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(MainActivity.this, "Failed to create directory.", android.widget.Toast.LENGTH_LONG).show();
                    });
                    document.close();
                    return;
                }
                java.io.File file = new java.io.File(pdfDir, group.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") + "_Export.pdf");
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    document.writeTo(fos);
                }
                try { document.close(); } catch (Exception ignored) {}

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    try {
                        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".fileprovider", file);
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "application/pdf");
                        intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                        // Check if any app can handle the PDF intent
                        if (intent.resolveActivity(getPackageManager()) == null) {
                            android.widget.Toast.makeText(MainActivity.this, "No PDF viewer installed. Please install one from the Play Store.", android.widget.Toast.LENGTH_LONG).show();
                            return;
                        }

                        startActivity(intent);
                    } catch (Exception ex) {
                        android.widget.Toast.makeText(MainActivity.this, "Failed to open PDF: " + ex.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("NoteCalc", "Failed to generate PDF", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    android.widget.Toast.makeText(MainActivity.this, "Failed to generate PDF: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                });
                try { document.close(); } catch (Exception ignored) {}
            }
        }).start();
    }

    void generateAndOpenPdf(Account account, PdfSortOrder sortOrder) {
        android.app.Dialog progressDialog = DialogHelper.showProgressDialog(MainActivity.this);
        
        new Thread(() -> {
            android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
            try {
                int[] pageTracker = {0};
                appendAccountToPdf(document, account, pageTracker, sortOrder);
                java.io.File pdfDir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
                if (pdfDir == null) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(MainActivity.this, "Cannot access documents directory.", android.widget.Toast.LENGTH_LONG).show();
                    });
                    document.close();
                    return;
                }
                if (!pdfDir.exists() && !pdfDir.mkdirs()) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(MainActivity.this, "Failed to create directory.", android.widget.Toast.LENGTH_LONG).show();
                    });
                    document.close();
                    return;
                }
                java.io.File file = new java.io.File(pdfDir, account.getTitle().replaceAll("[\\\\/:*?\"<>|]", "_") + ".pdf");
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    document.writeTo(fos);
                }
                try { document.close(); } catch (Exception ignored) {}
                
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    try {
                        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".fileprovider", file);
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "application/pdf");
                        intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                        // Check if any app can handle the PDF intent
                        if (intent.resolveActivity(getPackageManager()) == null) {
                            android.widget.Toast.makeText(MainActivity.this, "No PDF viewer installed. Please install one from the Play Store.", android.widget.Toast.LENGTH_LONG).show();
                            return;
                        }

                        startActivity(intent);
                    } catch (Exception ex) {
                        android.widget.Toast.makeText(MainActivity.this, "Failed to open PDF: " + ex.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("NoteCalc", "Failed to generate PDF", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    android.widget.Toast.makeText(MainActivity.this, "Failed to generate PDF: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                });
                try { document.close(); } catch (Exception ignored) {}
            }
        }).start();
    }

    private void appendAccountToPdf(android.graphics.pdf.PdfDocument document, Account account, int[] pageTracker, PdfSortOrder sortOrder) {
        // --- Page dimensions (A4 at 72 dpi approx) ---
        int pageWidth  = 595;
        int pageHeight = 842;
        int margin     = 40;

        int contentWidth = pageWidth - margin * 2;
        float bottomLimit = pageHeight - margin;

        // --- Paints (reusable across pages) ---
        Paint bgPaint = new Paint();
        bgPaint.setColor(ThemeManager.getBgPrimaryColor(MainActivity.this));

        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(getColor(R.color.text_primary));
        titlePaint.setTextSize(22f);
        titlePaint.setFakeBoldText(true);

        Paint subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subPaint.setColor(getColor(R.color.text_tertiary));
        subPaint.setTextSize(11f);

        Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        accentPaint.setColor(ThemeManager.getSecondaryAccentColor(MainActivity.this));
        accentPaint.setTextSize(11f);
        accentPaint.setFakeBoldText(true);

        Paint cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellPaint.setColor(getColor(R.color.text_primary));
        cellPaint.setTextSize(10f);

        Paint cellMutedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellMutedPaint.setColor(getColor(R.color.text_tertiary));
        cellMutedPaint.setTextSize(10f);

        Paint dividerPaint = new Paint();
        dividerPaint.setColor(ThemeManager.getBorderColor(MainActivity.this));
        dividerPaint.setStrokeWidth(0.8f);

        Paint rowEvenPaint = new Paint();
        rowEvenPaint.setColor(ThemeManager.getBgSecondaryColor(MainActivity.this));

        Paint rowOddPaint = new Paint();
        rowOddPaint.setColor(ThemeManager.getBgTertiaryColor(MainActivity.this));

        Paint totalBgPaint = new Paint();
        totalBgPaint.setColor(ThemeManager.getPrimaryAccentColor(MainActivity.this));

        Paint totalTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        totalTextPaint.setColor(Color.WHITE);
        totalTextPaint.setTextSize(11f);
        totalTextPaint.setFakeBoldText(true);

        Paint tableHeaderBgPaint = new Paint();
        tableHeaderBgPaint.setColor(ThemeManager.getBgSecondaryColor(MainActivity.this));

        // --- Column widths ---
        float colSno    = 38f;
        float colDate   = 70f;
        float colTime   = 55f;
        float colAmount = 60f;
        float colDesc   = contentWidth - colSno - colDate - colTime - colAmount;
        float rowHeight = 22f;

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String lastMod = sdf.format(new Date(account.getLastModified()));
        
        // Copy and sort the records chronologically to prevent jumbled PDFs
        List<Record> expRecords = new ArrayList<>(account.getRecords());
        expRecords.sort((r1, r2) -> {
            if (sortOrder == PdfSortOrder.DESCRIPTION) {
                String d1 = r1.getDescription() == null ? "" : r1.getDescription();
                String d2 = r2.getDescription() == null ? "" : r2.getDescription();
                return d1.compareToIgnoreCase(d2);
            }
            if (sortOrder == PdfSortOrder.DATE) {
                try {
                    java.text.SimpleDateFormat sortSdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                    java.util.Date d1 = r1.getDate() == null ? null : sortSdf.parse(r1.getDate());
                    java.util.Date d2 = r2.getDate() == null ? null : sortSdf.parse(r2.getDate());
                    int c = (d1 != null && d2 != null) ? d1.compareTo(d2) : 0;
                    if (c == 0) c = Long.compare(r1.getTimestampMillis(), r2.getTimestampMillis());
                    return c;
                } catch (Exception e) {
                    String d1 = r1.getDate() == null ? "" : r1.getDate();
                    String d2 = r2.getDate() == null ? "" : r2.getDate();
                    return d1.compareTo(d2);
                }
            }
            if (sortOrder == PdfSortOrder.AMOUNT) return Double.compare(r1.getAmount(), r2.getAmount());
            return Integer.compare(r1.getOriginalIndex(), r2.getOriginalIndex());
        });
        
        List<Record> budRecords = new ArrayList<>(account.getBudgetRecords());
        budRecords.sort((r1, r2) -> {
            if (sortOrder == PdfSortOrder.DESCRIPTION) {
                String d1 = r1.getDescription() == null ? "" : r1.getDescription();
                String d2 = r2.getDescription() == null ? "" : r2.getDescription();
                return d1.compareToIgnoreCase(d2);
            }
            if (sortOrder == PdfSortOrder.DATE) {
                try {
                    java.text.SimpleDateFormat sortSdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                    java.util.Date d1 = r1.getDate() == null ? null : sortSdf.parse(r1.getDate());
                    java.util.Date d2 = r2.getDate() == null ? null : sortSdf.parse(r2.getDate());
                    int c = (d1 != null && d2 != null) ? d1.compareTo(d2) : 0;
                    if (c == 0) c = Long.compare(r1.getTimestampMillis(), r2.getTimestampMillis());
                    return c;
                } catch (Exception e) {
                    String d1 = r1.getDate() == null ? "" : r1.getDate();
                    String d2 = r2.getDate() == null ? "" : r2.getDate();
                    return d1.compareTo(d2);
                }
            }
            if (sortOrder == PdfSortOrder.AMOUNT) return Double.compare(r1.getAmount(), r2.getAmount());
            return Integer.compare(r1.getOriginalIndex(), r2.getOriginalIndex());
        });
        
        List<List<Record>> allRecordLists = new ArrayList<>();
        List<String> listNames = new ArrayList<>();
        List<Double> listTotals = new ArrayList<>();
        
        if (!budRecords.isEmpty()) {
            allRecordLists.add(budRecords);
            listNames.add("Budgets");
            double bt = 0; for(Record r: budRecords) bt += r.getAmount();
            listTotals.add(bt);
        }
        if (!expRecords.isEmpty() || budRecords.isEmpty()) {
            allRecordLists.add(expRecords);
            listNames.add(budRecords.isEmpty() ? null : "Expenses");
            listTotals.add(account.calculateTotal());
        }

        // --- Page tracking ---
        int pageNum = pageTracker[0];
        Canvas canvas;
        PdfDocument.Page page;
        float y;

        // ===== PAGE 1: Header + Table Header + Rows =====
        pageNum++;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();
        canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
        y = margin;

        // --- Title with word-wrapping ---
        String titleText = account.getTitle();
        float titleLineHeight = 28f;
        float maxTitleWidth = contentWidth - 15f;
        List<String> titleLines = AppUtils.wrapText(titleText, titlePaint, maxTitleWidth);
        for (String line : titleLines) {
            canvas.drawText(line, margin, y + 22f, titlePaint);
            y += titleLineHeight;
        }
        y += 4f;

        // --- Subtitle: last modified + item count ---
        String subtitle = "Last modified: " + lastMod + "  |  Items: " + (expRecords.size() + budRecords.size());
        double budget = account.calculateTotalBudget();
        if (budget > 0) {
            subtitle += "  |  Budget: " + String.format(Locale.getDefault(), "%.2f", budget);
        }
        canvas.drawText(subtitle, margin, y + 13f, subPaint);
        y += 20f;

        // Divider under header
        canvas.drawLine(margin, y + 4f, pageWidth - margin, y + 4f, dividerPaint);
        y += 16f;

        for (int listIdx = 0; listIdx < allRecordLists.size(); listIdx++) {
            List<Record> records = allRecordLists.get(listIdx);
            String tableName = listNames.get(listIdx);
            double totalAmt = listTotals.get(listIdx);
            
            if (tableName != null) {
                if (y + 50f > bottomLimit) {
                    document.finishPage(page);
                    pageNum++;
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
                    y = margin;
                }
                y += 10f;
                canvas.drawText(tableName, margin, y + 15f, titlePaint);
                y += 25f;
            }

        // --- Draw table header row ---
        canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, tableHeaderBgPaint);
        canvas.drawText("S.No",        margin + 4,                       y + 15f, accentPaint);
        canvas.drawText("Description", margin + colSno + 4,              y + 15f, accentPaint);
        canvas.drawText("Date",        margin + colSno + colDesc + 4,    y + 15f, accentPaint);
        canvas.drawText("Time",        margin + colSno + colDesc + colDate + 4, y + 15f, accentPaint);
        float amountHeaderX = margin + colSno + colDesc + colDate + colTime + colAmount - accentPaint.measureText("Amount") - 4f;
        canvas.drawText("Amount",      amountHeaderX,                y + 15f, accentPaint);
        y += rowHeight;
        canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint);

        // --- Record rows (paginated) ---
        for (int i = 0; i < records.size(); i++) {
            Record tmpRec = records.get(i);
            String tRem = tmpRec.getRemarks();
            String tCat = tmpRec.getCategory();
            String tCombined = "";
            if (tCat != null && !tCat.isEmpty()) tCombined += "[" + tCat + "] ";
            if (tRem != null && !tRem.isEmpty()) tCombined += tRem;
            boolean tHasRem = !tCombined.isEmpty();
            java.util.List<String> tAtt = tmpRec.getAttachments();
            int numFiles = (tAtt != null) ? tAtt.size() : 0;
            float actualRowHeight = rowHeight;
            if (tHasRem) actualRowHeight += 14f;
            actualRowHeight += (12f * numFiles);

            // Check if we need a new page (need space for row + potential total row + footer)
            if (y + actualRowHeight > bottomLimit - 10f) {
                // Finish current page
                document.finishPage(page);

                // Start new page
                pageNum++;
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
                y = margin;

                // Page continuation header
                canvas.drawText(account.getTitle() + " (contd.)", margin, y + 13f, subPaint);
                y += 20f;
                canvas.drawLine(margin, y + 2f, pageWidth - margin, y + 2f, dividerPaint);
                y += 10f;

                // Redraw table header on new page
                canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, tableHeaderBgPaint);
                canvas.drawText("S.No",        margin + 4,                       y + 15f, accentPaint);
                canvas.drawText("Description", margin + colSno + 4,              y + 15f, accentPaint);
                canvas.drawText("Date",        margin + colSno + colDesc + 4,    y + 15f, accentPaint);
                canvas.drawText("Time",        margin + colSno + colDesc + colDate + 4, y + 15f, accentPaint);
                float ahx = margin + colSno + colDesc + colDate + colTime + colAmount - accentPaint.measureText("Amount") - 4f;
                canvas.drawText("Amount",      ahx,                              y + 15f, accentPaint);
                y += rowHeight;
                canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint);
            }

            Record rec = records.get(i);
            Paint rowBg = (i % 2 == 0) ? rowEvenPaint : rowOddPaint;

            // Check if remarks or category exist; add extra height if so
            String recRemarks = rec.getRemarks();
            String cat = rec.getCategory();
            String combinedNotes = "";
            if (cat != null && !cat.isEmpty()) combinedNotes += "[" + cat + "] ";
            if (recRemarks != null && !recRemarks.isEmpty()) combinedNotes += recRemarks;
            boolean hasRemarks = !combinedNotes.isEmpty();
            
            java.util.List<String> attachments = rec.getAttachments();
            java.util.List<String> fileNames = new java.util.ArrayList<>();
            if (attachments != null && !attachments.isEmpty()) {
                for (int j = 0; j < attachments.size(); j++) {
                    String path = attachments.get(j);
                    String fileName = path;
                    int lastSlash = path.lastIndexOf('/');
                    if (lastSlash != -1 && lastSlash < path.length() - 1) fileName = path.substring(lastSlash + 1);
                    fileNames.add(fileName);
                }
            }
            float actualRowHeightCalc = rowHeight;
            if (hasRemarks) actualRowHeightCalc += 14f;
            actualRowHeightCalc += (12f * fileNames.size());

            canvas.drawRect(margin, y, pageWidth - margin, y + actualRowHeightCalc, rowBg);

            canvas.drawText(String.valueOf(i + 1), margin + 4, y + 15f, cellMutedPaint);

            // Truncate long descriptions to fit column width
            String desc = rec.getDescription();
            while (desc.length() > 1 && cellPaint.measureText(desc) > colDesc - 8f) {
                desc = desc.substring(0, desc.length() - 1);
            }
            if (!desc.equals(rec.getDescription())) desc += "…";
            canvas.drawText(desc, margin + colSno + 4, y + 15f, cellPaint);

            // Draw remarks/category below description if present
            float currentY = y + 27f;
            if (hasRemarks) {
                String truncRemarks = combinedNotes;
                while (truncRemarks.length() > 1 && cellMutedPaint.measureText(truncRemarks) > colDesc - 8f) {
                    truncRemarks = truncRemarks.substring(0, truncRemarks.length() - 1);
                }
                if (!truncRemarks.equals(combinedNotes)) truncRemarks += "…";
                canvas.drawText(truncRemarks, margin + colSno + 4, currentY, cellMutedPaint);
                currentY += 12f;
            } else {
                currentY -= 14f;
                currentY += 12f;
            }
            for (String fn : fileNames) {
                String truncFn = "\uD83D\uDCCE " + fn;
                while (truncFn.length() > 1 && cellMutedPaint.measureText(truncFn) > colDesc - 8f) {
                    truncFn = truncFn.substring(0, truncFn.length() - 1);
                }
                if (!truncFn.equals("\uD83D\uDCCE " + fn)) truncFn += "…";
                canvas.drawText(truncFn, margin + colSno + 4, currentY, cellMutedPaint);
                currentY += 12f;
            }

            canvas.drawText(AppUtils.formatDateCompact(rec.getDate()), margin + colSno + colDesc + 4, y + 15f, cellMutedPaint);
            String timeStr = rec.getTimestampMillis() > 0 ? timeSdf.format(new Date(rec.getTimestampMillis())) : "-";
            canvas.drawText(timeStr, margin + colSno + colDesc + colDate + 4, y + 15f, cellMutedPaint);

            // Right-align amount
            String amtStr = String.format(Locale.getDefault(), "%.2f", rec.getAmount());
            float amtX = margin + colSno + colDesc + colDate + colTime + colAmount - cellPaint.measureText(amtStr) - 4f;
            canvas.drawText(amtStr, amtX, y + 15f, cellPaint);

            y += actualRowHeightCalc;
            canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint);
        }

        // --- Total row (check if it fits, otherwise new page) ---
        if (y + rowHeight + 30f > bottomLimit) {
            document.finishPage(page);
            pageNum++;
            pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
            y = margin;
        }

        y += 4f;
        canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, totalBgPaint);
        canvas.drawText("TOTAL", margin + 4, y + 15f, totalTextPaint);
        String totalStr = String.format(Locale.getDefault(), "%.2f", totalAmt);
        float totalX = margin + contentWidth - totalTextPaint.measureText(totalStr) - 4f;
        canvas.drawText(totalStr, totalX, y + 15f, totalTextPaint);
        y += rowHeight + 16f;

        } // End of tables loop

        // --- Footer ---
        if (y + 20f > bottomLimit) {
            document.finishPage(page);
            pageNum++;
            PdfDocument.PageInfo pi = new PdfDocument.PageInfo.Builder(595, 842, pageNum).create();
            page = document.startPage(pi);
            canvas = page.getCanvas();
            Paint bg = new Paint();
            bg.setColor(ThemeManager.getBgPrimaryColor(MainActivity.this));
            canvas.drawRect(0, 0, 595, 842, bg);
            y = 40f;
        }
        canvas.drawText("Generated by NoteCalc  •  " + lastMod + "  •  Page " + pageNum, 40f, y + 12f, subPaint);

        document.finishPage(page);
        pageTracker[0] = pageNum;

        java.util.LinkedHashMap<Record, String> recordLabels = new java.util.LinkedHashMap<>();
        for (int listIdx = 0; listIdx < allRecordLists.size(); listIdx++) {
            java.util.List<Record> list = allRecordLists.get(listIdx);
            String prefix = listNames.get(listIdx) != null ? listNames.get(listIdx) + " - " : "";
            for (int j = 0; j < list.size(); j++) {
                recordLabels.put(list.get(j), prefix + "S.No " + (j + 1) + ": " + list.get(j).getDescription());
            }
        }
        appendAttachmentsAppendixToPdf(document, recordLabels, pageTracker);
    }

    /**
     * Wraps text into multiple lines that fit within the given maxWidth using the given paint.
     * Uses breakText for precise measurement and supports character-level wrapping if a word exceeds maxWidth.
     */
    private void appendAttachmentsAppendixToPdf(android.graphics.pdf.PdfDocument document, java.util.LinkedHashMap<Record, String> recordLabels, int[] pageTracker) {
        java.util.List<Record> recordsWithImages = new java.util.ArrayList<>();
        for (Record r : recordLabels.keySet()) {
            java.util.List<String> atts = r.getAttachments();
            if (atts != null) {
                boolean hasImg = false;
                for (String path : atts) {
                    String lower = path.toLowerCase();
                    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp")) {
                        hasImg = true;
                        break;
                    } else if (path.startsWith("content://")) {
                        String mime = getContentResolver().getType(android.net.Uri.parse(path));
                        if (mime != null && mime.startsWith("image/")) {
                            hasImg = true;
                            break;
                        }
                    }
                }
                if (hasImg) recordsWithImages.add(r);
            }
        }
        
        if (recordsWithImages.isEmpty()) return;
        
        int pageWidth = 595;
        int pageHeight = 842;
        int margin = 40;
        float bottomLimit = pageHeight - margin;
        
        android.graphics.Paint bgPaint = new android.graphics.Paint();
        bgPaint.setColor(ThemeManager.getBgPrimaryColor(MainActivity.this));
        
        android.graphics.Paint titlePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(getColor(R.color.text_primary));
        titlePaint.setTextSize(20f);
        titlePaint.setFakeBoldText(true);
        
        android.graphics.Paint subPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        subPaint.setColor(getColor(R.color.text_tertiary));
        subPaint.setTextSize(12f);
        
        android.graphics.Paint dividerPaint = new android.graphics.Paint();
        dividerPaint.setColor(ThemeManager.getBorderColor(MainActivity.this));
        dividerPaint.setStrokeWidth(0.8f);
        
        int pageNum = pageTracker[0];
        android.graphics.Canvas canvas = null;
        android.graphics.pdf.PdfDocument.Page page = null;
        float y = bottomLimit + 100f; 
        
        int colWidth = (pageWidth - (margin * 2) - 15) / 2;
        int maxImgHeight = 350;
        
        for (Record r : recordsWithImages) {
            java.util.List<String> imgPaths = new java.util.ArrayList<>();
            for (String path : r.getAttachments()) {
                String lower = path.toLowerCase();
                boolean isImg = lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp");
                if (!isImg && path.startsWith("content://")) {
                    String mime = getContentResolver().getType(android.net.Uri.parse(path));
                    if (mime != null && mime.startsWith("image/")) isImg = true;
                }
                if (isImg) imgPaths.add(path);
            }
            if (imgPaths.isEmpty()) continue;
            
            if (canvas == null || y + 50f > bottomLimit) {
                if (page != null) document.finishPage(page);
                pageNum++;
                android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
                y = margin;
                canvas.drawText("Attachments Appendix", margin, y + 15f, titlePaint);
                y += 30f;
                canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint);
                y += 20f;
            } else {
                y += 20f;
            }
            
            String recTitle = recordLabels.get(r);
            if (recTitle == null) recTitle = "Record: " + r.getDescription();
            canvas.drawText(recTitle, margin, y + 12f, subPaint);
            y += 20f;
            
            for (int i = 0; i < imgPaths.size(); i += 2) {
                if (y + 100f > bottomLimit) {
                    document.finishPage(page);
                    pageNum++;
                    android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
                    y = margin;
                    canvas.drawText(recTitle + " (contd.)", margin, y + 12f, subPaint);
                    y += 20f;
                }
                
                float rowMaxHeight = 0;
                for (int c = 0; c < 2 && i + c < imgPaths.size(); c++) {
                    String path = imgPaths.get(i + c);
                    try {
                        android.graphics.Bitmap bitmap = null;
                        if (path.startsWith("content://")) {
                            java.io.InputStream is = getContentResolver().openInputStream(android.net.Uri.parse(path));
                            if (is != null) {
                                bitmap = android.graphics.BitmapFactory.decodeStream(is);
                                is.close();
                            }
                        } else {
                            bitmap = android.graphics.BitmapFactory.decodeFile(path);
                        }
                        
                        if (bitmap != null) {
                            float scale = Math.min((float) colWidth / bitmap.getWidth(), (float) maxImgHeight / bitmap.getHeight());
                            int drawW = (int) (bitmap.getWidth() * scale);
                            int drawH = (int) (bitmap.getHeight() * scale);
                            float x = margin + (c * (colWidth + 15));
                            
                            float drawX = x + (colWidth - drawW) / 2f;
                            
                            float spaceLeft = bottomLimit - y - 20f; // 20f extra for text
                            if (drawH > spaceLeft && spaceLeft > 100f) {
                                float newScale = spaceLeft / bitmap.getHeight();
                                if(newScale < scale) {
                                    scale = newScale;
                                    drawW = (int) (bitmap.getWidth() * scale);
                                    drawH = (int) (bitmap.getHeight() * scale);
                                    drawX = x + (colWidth - drawW) / 2f;
                                }
                            }

                            android.graphics.Rect destRect = new android.graphics.Rect((int) drawX, (int) y, (int) (drawX + drawW), (int) (y + drawH));
                            canvas.drawBitmap(bitmap, null, destRect, null);
                            bitmap.recycle();
                            
                            // Draw filename
                            String fileName = path;
                            int lastSlash = path.lastIndexOf('/');
                            if (lastSlash != -1 && lastSlash < path.length() - 1) fileName = path.substring(lastSlash + 1);
                            
                            String truncFn = fileName;
                            while (truncFn.length() > 1 && subPaint.measureText(truncFn) > colWidth - 8f) {
                                truncFn = truncFn.substring(0, truncFn.length() - 1);
                            }
                            if (!truncFn.equals(fileName)) truncFn += "…";
                            
                            float fnX = x + (colWidth - subPaint.measureText(truncFn)) / 2f;
                            canvas.drawText(truncFn, fnX, y + drawH + 15f, subPaint);
                            
                            if (drawH + 20f > rowMaxHeight) rowMaxHeight = drawH + 20f;
                        }
                    } catch (Exception e) {
                        android.util.Log.e("NoteCalc", "Error appending attachment", e);
                    }
                }
                y += rowMaxHeight + 15f;
            }
        }
        
        if (page != null) {
            canvas.drawText("Generated by NoteCalc  •  Page " + pageNum, 40f, bottomLimit + 25f, subPaint);
            document.finishPage(page);
        }
        pageTracker[0] = pageNum;
    }

    /**
     * Converts a date string from dd-MM-yyyy to compact DDMonthNameYY format.
     * Example: "24-06-2026" -> "24Jun26"
     */

    List<Record> getActiveRecords() {
        return isBudgetMode ? tempBudgetRecords : tempRecords;
    }
    void generateAndOpenSelectedPdf(java.util.List<Record> selectedRecords, PdfSortOrder sortOrder) {
        if (selectedRecords.isEmpty()) return;
        
        android.app.Dialog progressDialog = DialogHelper.showProgressDialog(MainActivity.this);
        
        new Thread(() -> {
            android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
            try {
                int[] pageTracker = {0};
                appendSelectedRecordsToPdf(document, currentEditingAccount, selectedRecords, pageTracker, sortOrder);
                java.io.File pdfDir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
                if (pdfDir == null) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        android.widget.Toast.makeText(MainActivity.this, "Cannot access documents directory.", android.widget.Toast.LENGTH_LONG).show();
                    });
                    document.close();
                    return;
                }
                if (!pdfDir.exists()) pdfDir.mkdirs();
                java.io.File file = new java.io.File(pdfDir, "Selected_Export.pdf");
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    document.writeTo(fos);
                }
                try { document.close(); } catch (Exception ignored) {}
                
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    try {
                        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".fileprovider", file);
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "application/pdf");
                        intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                        // Check if any app can handle the PDF intent
                        if (intent.resolveActivity(getPackageManager()) == null) {
                            android.widget.Toast.makeText(MainActivity.this, "No PDF viewer installed. Please install one from the Play Store.", android.widget.Toast.LENGTH_LONG).show();
                            return;
                        }

                        startActivity(intent);
                    } catch (Exception ex) {
                        android.widget.Toast.makeText(MainActivity.this, "Failed to open PDF: " + ex.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("NoteCalc", "Failed to generate PDF", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    android.widget.Toast.makeText(MainActivity.this, "Failed to generate PDF: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                });
                try { document.close(); } catch (Exception ignored) {}
            }
        }).start();
    }
    private void appendSelectedRecordsToPdf(android.graphics.pdf.PdfDocument document, Account account, java.util.List<Record> selectedRecords, int[] pageTracker, PdfSortOrder sortOrder) {
        // --- Page dimensions (A4 at 72 dpi approx) ---
        int pageWidth  = 595;
        int pageHeight = 842;
        int margin     = 40;
        int contentWidth = pageWidth - margin * 2;
        float bottomLimit = pageHeight - margin;

        // --- Paints (reusable across pages) ---
        android.graphics.Paint bgPaint = new android.graphics.Paint();
        bgPaint.setColor(ThemeManager.getBgPrimaryColor(MainActivity.this));

        android.graphics.Paint titlePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(getColor(R.color.text_primary));
        titlePaint.setTextSize(22f);
        titlePaint.setFakeBoldText(true);

        android.graphics.Paint subPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        subPaint.setColor(getColor(R.color.text_tertiary));
        subPaint.setTextSize(11f);

        android.graphics.Paint accentPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        accentPaint.setColor(ThemeManager.getSecondaryAccentColor(MainActivity.this));
        accentPaint.setTextSize(11f);
        accentPaint.setFakeBoldText(true);

        android.graphics.Paint cellPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        cellPaint.setColor(getColor(R.color.text_primary));
        cellPaint.setTextSize(10f);

        android.graphics.Paint cellMutedPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        cellMutedPaint.setColor(getColor(R.color.text_tertiary));
        cellMutedPaint.setTextSize(10f);

        android.graphics.Paint dividerPaint = new android.graphics.Paint();
        dividerPaint.setColor(ThemeManager.getBorderColor(MainActivity.this));
        dividerPaint.setStrokeWidth(0.8f);

        android.graphics.Paint rowEvenPaint = new android.graphics.Paint();
        rowEvenPaint.setColor(ThemeManager.getBgSecondaryColor(MainActivity.this));

        android.graphics.Paint rowOddPaint = new android.graphics.Paint();
        rowOddPaint.setColor(ThemeManager.getBgTertiaryColor(MainActivity.this));

        android.graphics.Paint totalBgPaint = new android.graphics.Paint();
        totalBgPaint.setColor(ThemeManager.getPrimaryAccentColor(MainActivity.this));

        android.graphics.Paint totalTextPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        totalTextPaint.setColor(android.graphics.Color.WHITE);
        totalTextPaint.setTextSize(11f);
        totalTextPaint.setFakeBoldText(true);

        android.graphics.Paint tableHeaderBgPaint = new android.graphics.Paint();
        tableHeaderBgPaint.setColor(ThemeManager.getBgSecondaryColor(MainActivity.this));

        // --- Column widths ---
        float colSno    = 38f;
        float colDate   = 70f;
        float colTime   = 55f;
        float colAmount = 60f;
        float colDesc   = contentWidth - colSno - colDate - colTime - colAmount;
        float rowHeight = 22f;

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
        java.text.SimpleDateFormat timeSdf = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault());
        String lastMod = sdf.format(new java.util.Date(account.getLastModified()));
        
        java.util.List<Record> recordsToPrint = new java.util.ArrayList<>(selectedRecords);
        recordsToPrint.sort((r1, r2) -> {
            if (sortOrder == PdfSortOrder.DESCRIPTION) {
                String d1 = r1.getDescription() == null ? "" : r1.getDescription();
                String d2 = r2.getDescription() == null ? "" : r2.getDescription();
                return d1.compareToIgnoreCase(d2);
            }
            if (sortOrder == PdfSortOrder.DATE) {
                try {
                    java.text.SimpleDateFormat sortSdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                    java.util.Date d1 = r1.getDate() == null ? null : sortSdf.parse(r1.getDate());
                    java.util.Date d2 = r2.getDate() == null ? null : sortSdf.parse(r2.getDate());
                    int c = (d1 != null && d2 != null) ? d1.compareTo(d2) : 0;
                    if (c == 0) c = Long.compare(r1.getTimestampMillis(), r2.getTimestampMillis());
                    return c;
                } catch (Exception e) {
                    String d1 = r1.getDate() == null ? "" : r1.getDate();
                    String d2 = r2.getDate() == null ? "" : r2.getDate();
                    return d1.compareTo(d2);
                }
            }
            if (sortOrder == PdfSortOrder.AMOUNT) return Double.compare(r1.getAmount(), r2.getAmount());
            return Integer.compare(r1.getOriginalIndex(), r2.getOriginalIndex());
        });
        
        double totalAmt = 0;
        for (Record r : recordsToPrint) totalAmt += r.getAmount();

        // --- Page tracking ---
        int pageNum = pageTracker[0];
        android.graphics.Canvas canvas;
        android.graphics.pdf.PdfDocument.Page page;
        float y;

        pageNum++;
        android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();
        canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
        y = margin;

        String titleText = account.getTitle() + " (Selected)";
        float titleLineHeight = 28f;
        float maxTitleWidth = contentWidth - 15f;
        java.util.List<String> titleLines = AppUtils.wrapText(titleText, titlePaint, maxTitleWidth);
        for (String line : titleLines) {
            canvas.drawText(line, margin, y + 22f, titlePaint);
            y += titleLineHeight;
        }
        y += 4f;

        String subtitle = "Exported: " + sdf.format(new java.util.Date()) + "  |  Items: " + recordsToPrint.size();
        canvas.drawText(subtitle, margin, y + 13f, subPaint);
        y += 20f;

        canvas.drawLine(margin, y + 4f, pageWidth - margin, y + 4f, dividerPaint);
        y += 16f;

        canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, tableHeaderBgPaint);
        canvas.drawText("S.No",        margin + 4,                       y + 15f, accentPaint);
        canvas.drawText("Description", margin + colSno + 4,              y + 15f, accentPaint);
        canvas.drawText("Date",        margin + colSno + colDesc + 4,    y + 15f, accentPaint);
        canvas.drawText("Time",        margin + colSno + colDesc + colDate + 4, y + 15f, accentPaint);
        float amountHeaderX = margin + colSno + colDesc + colDate + colTime + colAmount - accentPaint.measureText("Amount") - 4f;
        canvas.drawText("Amount",      amountHeaderX,                y + 15f, accentPaint);
        y += rowHeight;
        canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint);

        for (int i = 0; i < recordsToPrint.size(); i++) {
            Record tmpRec = recordsToPrint.get(i);
            String tRem = tmpRec.getRemarks();
            String tCat = tmpRec.getCategory();
            String tCombined = "";
            if (tCat != null && !tCat.isEmpty()) tCombined += "[" + tCat + "] ";
            if (tRem != null && !tRem.isEmpty()) tCombined += tRem;
            boolean tHasRem = !tCombined.isEmpty();
            java.util.List<String> tAtt = tmpRec.getAttachments();
            int numFiles = (tAtt != null) ? tAtt.size() : 0;
            float actualRowHeight = rowHeight;
            if (tHasRem) actualRowHeight += 14f;
            actualRowHeight += (12f * numFiles);

            if (y + actualRowHeight > bottomLimit - 10f) {
                document.finishPage(page);
                pageNum++;
                pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
                y = margin;

                canvas.drawText(account.getTitle() + " (contd.)", margin, y + 13f, subPaint);
                y += 20f;
                canvas.drawLine(margin, y + 2f, pageWidth - margin, y + 2f, dividerPaint);
                y += 10f;

                canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, tableHeaderBgPaint);
                canvas.drawText("S.No",        margin + 4,                       y + 15f, accentPaint);
                canvas.drawText("Description", margin + colSno + 4,              y + 15f, accentPaint);
                canvas.drawText("Date",        margin + colSno + colDesc + 4,    y + 15f, accentPaint);
                canvas.drawText("Time",        margin + colSno + colDesc + colDate + 4, y + 15f, accentPaint);
                float ahx = margin + colSno + colDesc + colDate + colTime + colAmount - accentPaint.measureText("Amount") - 4f;
                canvas.drawText("Amount",      ahx,                              y + 15f, accentPaint);
                y += rowHeight;
                canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint);
            }

            Record rec = recordsToPrint.get(i);
            android.graphics.Paint rowBg = (i % 2 == 0) ? rowEvenPaint : rowOddPaint;

            String recRemarks = rec.getRemarks();
            String cat = rec.getCategory();
            String combinedNotes = "";
            if (cat != null && !cat.isEmpty()) combinedNotes += "[" + cat + "] ";
            if (recRemarks != null && !recRemarks.isEmpty()) combinedNotes += recRemarks;
            boolean hasRemarks = !combinedNotes.isEmpty();
            
            java.util.List<String> attachments = rec.getAttachments();
            java.util.List<String> fileNames = new java.util.ArrayList<>();
            if (attachments != null && !attachments.isEmpty()) {
                for (int j = 0; j < attachments.size(); j++) {
                    String path = attachments.get(j);
                    String fileName = path;
                    int lastSlash = path.lastIndexOf('/');
                    if (lastSlash != -1 && lastSlash < path.length() - 1) fileName = path.substring(lastSlash + 1);
                    fileNames.add(fileName);
                }
            }
            float actualRowHeightCalc = rowHeight;
            if (hasRemarks) actualRowHeightCalc += 14f;
            actualRowHeightCalc += (12f * fileNames.size());

            canvas.drawRect(margin, y, pageWidth - margin, y + actualRowHeightCalc, rowBg);

            canvas.drawText(String.valueOf(i + 1), margin + 4, y + 15f, cellMutedPaint);

            String desc = rec.getDescription();
            while (desc.length() > 1 && cellPaint.measureText(desc) > colDesc - 8f) {
                desc = desc.substring(0, desc.length() - 1);
            }
            if (!desc.equals(rec.getDescription())) desc += "…";
            canvas.drawText(desc, margin + colSno + 4, y + 15f, cellPaint);

            float currentY = y + 27f;
            if (hasRemarks) {
                String truncRemarks = combinedNotes;
                while (truncRemarks.length() > 1 && cellMutedPaint.measureText(truncRemarks) > colDesc - 8f) {
                    truncRemarks = truncRemarks.substring(0, truncRemarks.length() - 1);
                }
                if (!truncRemarks.equals(combinedNotes)) truncRemarks += "…";
                canvas.drawText(truncRemarks, margin + colSno + 4, currentY, cellMutedPaint);
                currentY += 12f;
            } else {
                currentY -= 14f;
                currentY += 12f;
            }
            for (String fn : fileNames) {
                String truncFn = "\uD83D\uDCCE " + fn;
                while (truncFn.length() > 1 && cellMutedPaint.measureText(truncFn) > colDesc - 8f) {
                    truncFn = truncFn.substring(0, truncFn.length() - 1);
                }
                if (!truncFn.equals("\uD83D\uDCCE " + fn)) truncFn += "…";
                canvas.drawText(truncFn, margin + colSno + 4, currentY, cellMutedPaint);
                currentY += 12f;
            }

            canvas.drawText(AppUtils.formatDateCompact(rec.getDate()), margin + colSno + colDesc + 4, y + 15f, cellMutedPaint);
            String timeStr = rec.getTimestampMillis() > 0 ? timeSdf.format(new java.util.Date(rec.getTimestampMillis())) : "-";
            canvas.drawText(timeStr, margin + colSno + colDesc + colDate + 4, y + 15f, cellMutedPaint);

            String amtStr = String.format(java.util.Locale.getDefault(), "%.2f", rec.getAmount());
            float amtX = margin + colSno + colDesc + colDate + colTime + colAmount - cellPaint.measureText(amtStr) - 4f;
            canvas.drawText(amtStr, amtX, y + 15f, cellPaint);

            y += actualRowHeightCalc;
            canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint);
        }

        if (y + rowHeight + 30f > bottomLimit) {
            document.finishPage(page);
            pageNum++;
            pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
            y = margin;
        }

        y += 4f;
        canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, totalBgPaint);
        canvas.drawText("TOTAL SELECTED", margin + 4, y + 15f, totalTextPaint);
        String totalStr = String.format(java.util.Locale.getDefault(), "%.2f", totalAmt);
        float totalX = margin + contentWidth - totalTextPaint.measureText(totalStr) - 4f;
        canvas.drawText(totalStr, totalX, y + 15f, totalTextPaint);
        y += rowHeight + 16f;

        if (y + 20f > bottomLimit) {
            document.finishPage(page);
            pageNum++;
            android.graphics.pdf.PdfDocument.PageInfo pi = new android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pageNum).create();
            page = document.startPage(pi);
            canvas = page.getCanvas();
            android.graphics.Paint bg = new android.graphics.Paint();
            bg.setColor(ThemeManager.getBgPrimaryColor(MainActivity.this));
            canvas.drawRect(0, 0, 595, 842, bg);
            y = 40f;
        }
        canvas.drawText("Generated by NoteCalc  •  " + lastMod + "  •  Page " + pageNum, 40f, y + 12f, subPaint);

        document.finishPage(page);
        pageTracker[0] = pageNum;

        java.util.LinkedHashMap<Record, String> recordLabels = new java.util.LinkedHashMap<>();
        for (int j = 0; j < recordsToPrint.size(); j++) {
            recordLabels.put(recordsToPrint.get(j), "S.No " + (j + 1) + ": " + recordsToPrint.get(j).getDescription());
        }
        appendAttachmentsAppendixToPdf(document, recordLabels, pageTracker);
    }

    @android.annotation.SuppressLint("SetTextI18n")
    private void renderEditorAttachments() {
        if (attachmentsContainer == null || attachmentsScroll == null) return;
        attachmentsContainer.removeAllViews();
        if (tempAttachments.isEmpty()) {
            attachmentsScroll.setVisibility(View.GONE);
            if (btnAttachFile != null) btnAttachFile.setAlpha(1.0f);
        } else {
            attachmentsScroll.setVisibility(View.VISIBLE);
            if (btnAttachFile != null) btnAttachFile.setAlpha(tempAttachments.size() >= 3 ? 0.5f : 1.0f);
            
            for (int i = 0; i < tempAttachments.size(); i++) {
                final int idx = i;
                String path = tempAttachments.get(i);
                java.io.File f = new java.io.File(path);
                String name = f.getName();
                if (name.length() > 15) name = name.substring(0, 15) + "...";
                
                LinearLayout chipContainer = new LinearLayout(this);
                chipContainer.setOrientation(LinearLayout.HORIZONTAL);
                chipContainer.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, ThemeManager.getBgSecondaryColor(this), 8.0f));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 16, 0);
                chipContainer.setLayoutParams(lp);

                TextView chip = new TextView(this);
                chip.setText((path.endsWith(".pdf") || path.endsWith(".doc") || path.endsWith(".docx") ? "\uD83D\uDCC4 " : "\uD83D\uDDBC ") + name);
                chip.setTextSize(12);
                chip.setTextColor(getColor(R.color.text_primary));
                chip.setPadding(20, 10, 10, 10);
                
                ResponsiveUI.setupClickable(chip, false, () -> {
                    try {
                        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".fileprovider", f);
                        android.content.Intent viewIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        viewIntent.setDataAndType(uri, getContentResolver().getType(uri));
                        if (viewIntent.getType() == null) {
                            if (path.toLowerCase().endsWith(".pdf")) viewIntent.setDataAndType(uri, "application/pdf");
                            else if (path.toLowerCase().endsWith(".jpg") || path.toLowerCase().endsWith(".png")) viewIntent.setDataAndType(uri, "image/*");
                            else viewIntent.setDataAndType(uri, "*/*");
                        }
                        viewIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(viewIntent);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, getString(R.string.auto_cannot_open_file_10), Toast.LENGTH_SHORT).show();
                    }
                });

                TextView closeBtn = new TextView(this);
                closeBtn.setText(" ✕ ");
                closeBtn.setTextSize(12);
                closeBtn.setTextColor(getColor(R.color.error_red));
                closeBtn.setPadding(10, 10, 20, 10);
                
                ResponsiveUI.setupClickable(closeBtn, false, () -> {
                    tempAttachments.remove(idx);
                    renderEditorAttachments();
                });
                
                chipContainer.addView(chip);
                chipContainer.addView(closeBtn);
                attachmentsContainer.addView(chipContainer);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (resultCode == RESULT_OK && currentPhotoPath != null) {
                tempAttachments.add(currentPhotoPath);
                renderEditorAttachments();
            } else if (currentPhotoPath != null) {
                java.io.File f = new java.io.File(currentPhotoPath);
                if (f.exists() && !f.delete()) android.util.Log.w("NoteCalc", "Failed to delete temp file");
            }
            currentPhotoPath = null;
            return;
        }
        
        if (requestCode == REQUEST_CODE_ATTACH && resultCode == RESULT_OK && data != null && data.getData() != null) {
            android.net.Uri uri = data.getData();
            try {
                java.io.File attachmentsDir = new java.io.File(getFilesDir(), "attachments");
                if (!attachmentsDir.exists()) attachmentsDir.mkdirs();
                
                String originalName = "attachment_" + System.currentTimeMillis();
                try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                        if (index != -1) {
                            String tempName = cursor.getString(index);
                            if (tempName != null) {
                                int lastSlash = tempName.lastIndexOf('/');
                                String nameOnly = (lastSlash != -1) ? tempName.substring(lastSlash + 1) : tempName;
                                originalName = nameOnly.replaceAll("[/\\\\:*?\"<>|]", "_");
                            }
                        }
                    }
                }
                
                java.io.File destFile = new java.io.File(attachmentsDir, originalName);
                if (destFile.exists() || destFile.createNewFile()) {
                    try (java.io.InputStream in = getContentResolver().openInputStream(uri);
                         java.io.FileOutputStream out = new java.io.FileOutputStream(destFile)) {
                        if (in == null) throw new java.io.IOException("Failed to open input stream");
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                    }
                    tempAttachments.add(destFile.getAbsolutePath());
                    renderEditorAttachments();
                }
            } catch (Exception e) {
                android.util.Log.e("NoteCalc", "Failed to attach file", e);
                Toast.makeText(this, getString(R.string.auto_failed_to_attach_fil_11), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
