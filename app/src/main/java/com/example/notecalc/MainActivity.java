package com.example.notecalc;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import java.text.ParseException;
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
import android.widget.ScrollView;
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
import com.example.notecalc.pdf.PdfSortCallback;

public class MainActivity extends AppCompatActivity {
    private SettingsHelper settingsHelper;

    private final java.util.List<String> tempAttachments = new java.util.ArrayList<>();
    private static final int REQUEST_CODE_ATTACH = 1001;
    private static final int REQUEST_CODE_CAMERA = 1002;
    private String currentPhotoPath = null;
    private android.widget.LinearLayout attachmentsContainer;
    private android.widget.HorizontalScrollView attachmentsScroll;
    private android.widget.TextView btnAttachFile;

    void showPdfSortDialog(PdfSortCallback callback) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this, R.style.CustomDialogTheme);
        android.view.View view = getLayoutInflater().inflate(R.layout.layout_dialog_pdf_sort, null);
        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();

        TextView optSno = view.findViewById(R.id.option_sort_sno);
        TextView optDesc = view.findViewById(R.id.option_sort_desc);
        TextView optDate = view.findViewById(R.id.option_sort_date);
        TextView optAmount = view.findViewById(R.id.option_sort_amount);
        
        android.graphics.drawable.Drawable unselectedBg = ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(this), ThemeManager.getBorderColor(this), 1.0f, 8.0f);
        android.graphics.drawable.Drawable selectedBg = ResponsiveUI.createRoundedBg(this, ThemeManager.getPrimaryAccentColor(this), ThemeManager.getPrimaryAccentColor(this), 1.0f, 8.0f);
        
        final PdfSortOrder[] selectedOrder = {PdfSortOrder.SNO}; // Default
        
        Runnable updateSelection = () -> {
            optSno.setBackground(selectedOrder[0] == PdfSortOrder.SNO ? selectedBg : unselectedBg);
            optDesc.setBackground(selectedOrder[0] == PdfSortOrder.DESCRIPTION ? selectedBg : unselectedBg);
            optDate.setBackground(selectedOrder[0] == PdfSortOrder.DATE ? selectedBg : unselectedBg);
            optAmount.setBackground(selectedOrder[0] == PdfSortOrder.AMOUNT ? selectedBg : unselectedBg);
            
            optSno.setTextColor(selectedOrder[0] == PdfSortOrder.SNO ? android.graphics.Color.WHITE : getColor(R.color.text_primary));
            optDesc.setTextColor(selectedOrder[0] == PdfSortOrder.DESCRIPTION ? android.graphics.Color.WHITE : getColor(R.color.text_primary));
            optDate.setTextColor(selectedOrder[0] == PdfSortOrder.DATE ? android.graphics.Color.WHITE : getColor(R.color.text_primary));
            optAmount.setTextColor(selectedOrder[0] == PdfSortOrder.AMOUNT ? android.graphics.Color.WHITE : getColor(R.color.text_primary));
        };
        
        optSno.setOnClickListener(v -> { selectedOrder[0] = PdfSortOrder.SNO; updateSelection.run(); });
        optDesc.setOnClickListener(v -> { selectedOrder[0] = PdfSortOrder.DESCRIPTION; updateSelection.run(); });
        optDate.setOnClickListener(v -> { selectedOrder[0] = PdfSortOrder.DATE; updateSelection.run(); });
        optAmount.setOnClickListener(v -> { selectedOrder[0] = PdfSortOrder.AMOUNT; updateSelection.run(); });
        
        updateSelection.run(); // Init

        android.view.View btnCancel = view.findViewById(R.id.btn_dialog_cancel);
        android.view.View btnExport = view.findViewById(R.id.btn_dialog_export);
        
        view.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgSecondaryColor(this), ThemeManager.getBorderColor(this), 1.5f, 12f));

        btnCancel.setBackground(ResponsiveUI.createRippleRoundedBg(this, ThemeManager.getBgPrimaryColor(this), ThemeManager.getBorderColor(this), 1.0f, 8.0f));
        btnExport.setBackground(ResponsiveUI.createRippleRoundedBg(this, ThemeManager.getPrimaryAccentColor(this), ThemeManager.getPrimaryAccentColor(this), 1.0f, 8.0f));

        ResponsiveUI.setupClickable(btnCancel, true, dialog::dismiss);
        ResponsiveUI.setupClickable(btnExport, true, () -> {
            dialog.dismiss();
            callback.onSortSelected(selectedOrder[0]);
        });

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    
    androidx.activity.result.ActivityResultLauncher<android.content.Intent> exportJsonLauncher;
    androidx.activity.result.ActivityResultLauncher<android.content.Intent> importJsonLauncher;

    FrameLayout mainContainer;
    AppStorage appStorage;
    private AccountGroup currentViewGroup = null; // null means we are in the Dashboard
    Account currentEditingAccount;
    
    // Editor state
    private List<Record> tempRecords;
    private List<Record> tempBudgetRecords;
    boolean isBudgetMode = false; // false = Expenses, true = Budget
    
    private String originalTitle = "";
    private String selectedRecordDate = "";

    private int editingRecordIndex = -1;
    private EditText editDescField;
    private EditText editAmountField;
    private TextView btnRecordDateField;
    private TextView btnAddRecordField;
    private TextView btnCancelEditField;
    private TextView labelAddRecordField;
        RecordsAdapter recordsAdapter;
    private AccountsAdapter accountsAdapter;
    private AccountsAdapter groupsAdapter;
    private String dashboardSearchQuery = "";
    private boolean groupSortAscending = true;
    private TextView btnSortTitle;
    private TextView btnSortTotal;
    private TextView btnSortLatest;
    private TextView textTotalValField;
    private TextView textTotalLabelField;
    private com.google.android.material.snackbar.Snackbar currentSnackbar;

    private TextView thSnoField;
    private TextView thDescField;
    private TextView thDateField;
    private TextView thAmountField;

    private int expenseSortColumn = 0;
    private boolean expenseSortAscending = false;
    private int budgetSortColumn = 0;
    private boolean budgetSortAscending = false;
    
    private int getSortColumn() { return isBudgetMode ? budgetSortColumn : expenseSortColumn; }
    private boolean getSortAscending() { return isBudgetMode ? budgetSortAscending : expenseSortAscending; }
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
    private ImageView btnBulkActionsMenu;
    private View editorEmptyState;
    private View rowSearchAndBulk;
    private View tableHeaderField;
    private boolean isFormInputsCollapsed = false;

    // Bulk action container and selected total display
    private View containerBulkActions;
    private TextView textSelectedTotal;

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
                                showDashboard();
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
                    showDashboard();
                } else if (currentViewGroup != null) {
                    currentViewGroup = null;
                    dashboardSearchQuery = "";
                    showDashboard();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });

        // Open the dashboard screen
        showDashboard();
    }

    /**
     * Renders the Dashboard screen containing the list of saved accounts.
     */
    
    
    @android.annotation.SuppressLint("SetTextI18n")
    

    
    private final NCAgent ncAgent = new NCAgent();

    @android.annotation.SuppressLint("SetTextI18n")
    private void showNCAgentBottomSheet() {
        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(android.view.Gravity.BOTTOM);
        root.setBackgroundColor(0x80000000); // dim background
        
        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(this), ThemeManager.getBgPrimaryColor(this), 0f, 16f));
        int pad = (int)(16 * getResources().getDisplayMetrics().density);
        sheet.setPadding(pad, pad, pad, pad);
        
        TextView title = new TextView(this);
        title.setText(getString(R.string.auto_nc_agent_15));
        title.setTextSize(20);
        title.setTextColor(getColor(R.color.text_primary));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, pad);
        sheet.addView(title);
        
        EditText input = new EditText(this);
        input.setHint(getString(R.string.auto_e_g_bought_2_coffees_31));
        input.setTextColor(getColor(R.color.text_primary));
        input.setHintTextColor(getColor(R.color.text_secondary));
        input.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgSecondaryColor(this), ThemeManager.getBorderColor(this), 1.0f, 8f));
        input.setPadding(pad, pad, pad, pad);
        input.setLines(4);
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        sheet.addView(input);
        
        android.widget.Button btnAnalyze = new android.widget.Button(this);
        btnAnalyze.setText(getString(R.string.auto_analyze_16));
        btnAnalyze.setTextColor(getColor(R.color.text_on_accent));
        btnAnalyze.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getPrimaryAccentColor(this), ThemeManager.getPrimaryAccentColor(this), 0f, 8f));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, pad, 0, 0);
        sheet.addView(btnAnalyze, btnParams);
        
        ScrollView previewScroll = new ScrollView(this);
        LinearLayout previewContainer = new LinearLayout(this);
        previewContainer.setOrientation(LinearLayout.VERTICAL);
        previewScroll.addView(previewContainer);
        previewScroll.setVisibility(View.GONE);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        scrollParams.setMargins(0, pad, 0, 0);
        sheet.addView(previewScroll, scrollParams);
        
        LinearLayout actionButtons = new LinearLayout(this);
        actionButtons.setOrientation(LinearLayout.HORIZONTAL);
        actionButtons.setVisibility(View.GONE);
        
        android.widget.Button btnCancel = new android.widget.Button(this);
        btnCancel.setText(getString(R.string.auto_cancel_17));
        btnCancel.setTextColor(getColor(R.color.text_primary));
        btnCancel.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        
        android.widget.Button btnConfirm = new android.widget.Button(this);
        btnConfirm.setText(getString(R.string.auto_confirm_18));
        btnConfirm.setTextColor(ThemeManager.getPrimaryAccentColor(this));
        btnConfirm.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        
        actionButtons.addView(btnCancel, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        actionButtons.addView(btnConfirm, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        sheet.addView(actionButtons, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        
        root.addView(sheet, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        
        root.setOnClickListener(v -> dialog.dismiss());
        sheet.setOnClickListener(v -> {}); // prevent dismiss when clicking sheet
        
        dialog.setContentView(root);
        
        // State variables to hold parsed actions
        List<NCAction> parsedActions = new ArrayList<>();
        
        btnAnalyze.setOnClickListener(v -> {
            String text = input.getText().toString();
            if (text.trim().isEmpty()) return;
            
            parsedActions.clear();
            parsedActions.addAll(ncAgent.process(text, getActiveRecords()));
            
            // Build Preview UI
            previewContainer.removeAllViews();
            input.setVisibility(View.GONE);
            btnAnalyze.setVisibility(View.GONE);
            previewScroll.setVisibility(View.VISIBLE);
            actionButtons.setVisibility(View.VISIBLE);
            
            for (NCAction action : parsedActions) {
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgSecondaryColor(this), ThemeManager.getBorderColor(this), 1.0f, 8f));
                card.setPadding(pad, pad, pad, pad);
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                cardParams.setMargins(0, 0, 0, pad);
                previewContainer.addView(card, cardParams);
                
                TextView intentView = new TextView(this);
                intentView.setText("[" + action.getIntent().name() + "]");
                intentView.setTypeface(null, android.graphics.Typeface.BOLD);
                
                if (action.getIntent() == NCAgentIntent.ADD) intentView.setTextColor(getColor(R.color.accent_green_primary));
                else if (action.getIntent() == NCAgentIntent.UPDATE) intentView.setTextColor(getColor(R.color.accent_blue_primary));
                else if (action.getIntent() == NCAgentIntent.DELETE) intentView.setTextColor(getColor(R.color.error_red));
                else intentView.setTextColor(getColor(R.color.text_tertiary));
                card.addView(intentView);
                
                if (!action.isValid()) {
                    TextView errView = new TextView(this);
                    errView.setText("Error: " + action.getErrorMessage());
                    errView.setTextColor(getColor(R.color.error_red));
                    card.addView(errView);
                } else if (action.isNeedsDisambiguation()) {
                    TextView disambigText = new TextView(this);
                    disambigText.setText("Multiple matches found. Select which to " + action.getIntent().name().toLowerCase() + ":");
                    disambigText.setTextColor(getColor(R.color.text_primary));
                    card.addView(disambigText);
                    
                    for (Record matched : action.getDisambiguationCandidates()) {
                        CheckBox cb = new CheckBox(this);
                        cb.setText(matched.getDescription() + " (?" + matched.getAmount() + ") - " + matched.getDate());
                        cb.setTextColor(getColor(R.color.text_primary));
                        // Save the checkbox view in a tag to retrieve its state on Confirm
                        cb.setTag(matched);
                        card.addView(cb);
                    }
                } else {
                    Record rec = action.getValidatedRecord() != null ? action.getValidatedRecord() : action.getTargetRecord();
                    TextView dataView = new TextView(this);
                    dataView.setText(rec.getDescription() + "  -  ?" + rec.getAmount() + "  (" + rec.getDate() + ")");
                    dataView.setTextColor(getColor(R.color.text_primary));
                    card.addView(dataView);
                }
            }
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnConfirm.setOnClickListener(v -> {
            int added = 0, updated = 0, deleted = 0;
            
            for (int i = 0; i < parsedActions.size(); i++) {
                NCAction action = parsedActions.get(i);
                if (!action.isValid()) continue;
                
                if (action.isNeedsDisambiguation()) {
                    // Find the card view
                    LinearLayout card = (LinearLayout) previewContainer.getChildAt(i);
                    for (int j = 0; j < card.getChildCount(); j++) {
                        View child = card.getChildAt(j);
                        if (child instanceof CheckBox) {
                            CheckBox cb = (CheckBox) child;
                            if (cb.isChecked()) {
                                Record target = (Record) cb.getTag();
                                if (action.getIntent() == NCAgentIntent.DELETE) {
                                    getActiveRecords().remove(target);
                                    deleted++;
                                }
                            }
                        }
                    }
                } else {
                    if (action.getIntent() == NCAgentIntent.ADD) {
                        Record validated = action.getValidatedRecord();
                        validated.setOriginalIndex(getNewOriginalIndex());
                        getActiveRecords().add(validated);
                        added++;
                    } else if (action.getIntent() == NCAgentIntent.UPDATE) {
                        Record target = action.getTargetRecord();
                        Record val = action.getValidatedRecord();
                        target.setDescription(val.getDescription());
                        target.setAmount(val.getAmount());
                        target.setDate(val.getDate());
                        target.setCategory(val.getCategory());
                        target.setRemarks(val.getRemarks());
                        updated++;
                    } else if (action.getIntent() == NCAgentIntent.DELETE) {
                        getActiveRecords().remove(action.getTargetRecord());
                        deleted++;
                    }
                }
            }
            
            String summary = "";
            if (added > 0) summary += "Added " + added + " records\n";
            if (updated > 0) summary += "Updated " + updated + " records\n";
            if (deleted > 0) summary += "Deleted " + deleted + " records\n";
            if (!summary.isEmpty()) Toast.makeText(this, summary, Toast.LENGTH_LONG).show();
            
            applySorting();
            StorageHelper.saveAppStorage(MainActivity.this, appStorage);
            populateRecordsList();
            dialog.dismiss();
        });
        
        dialog.show();
    }
    @android.annotation.SuppressLint("ClickableViewAccessibility")
    void showDashboard() {
        if (currentSnackbar != null) {
            currentSnackbar.dismiss();
            currentSnackbar = null;
        }
                LayoutInflater inflater = getLayoutInflater();
                View dashboardView = inflater.inflate(R.layout.layout_dashboard, mainContainer, false);

        // Find views
        View btnCreateAccount = dashboardView.findViewById(R.id.btn_create_account);
        
        View btnSettings = dashboardView.findViewById(R.id.btn_settings);
        if(btnSettings != null) btnSettings.setOnClickListener(v -> settingsHelper.openSettings());
        
        View btnArchive = dashboardView.findViewById(R.id.btn_archive);
        if(btnArchive != null) btnArchive.setOnClickListener(v -> {
            ArchiveHelper.isShowingArchive = !ArchiveHelper.isShowingArchive;
            updateDashboardSortUI();
            refreshDashboardList();
        });
        
        View btnTips = dashboardView.findViewById(R.id.btn_tips);
        if(btnTips != null) btnTips.setOnClickListener(v -> DialogHelper.showTipsDialog(this));
        
        View btnCreateGroup = dashboardView.findViewById(R.id.btn_create_group);
        View cardEmptyState = dashboardView.findViewById(R.id.card_empty_state);

        RecyclerView listAccountsContainer = dashboardView.findViewById(R.id.list_accounts);
        listAccountsContainer.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        accountsAdapter = new AccountsAdapter();
        listAccountsContainer.setAdapter(accountsAdapter);

        RecyclerView listGroupsContainer = dashboardView.findViewById(R.id.list_groups);
        if (listGroupsContainer != null) {
            listGroupsContainer.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            groupsAdapter = new AccountsAdapter();
            listGroupsContainer.setAdapter(groupsAdapter);
        }

        TextView btnSortGroupTitle = dashboardView.findViewById(R.id.btn_sort_group_title);
        if (btnSortGroupTitle != null) {
            ResponsiveUI.setupClickable(btnSortGroupTitle, false, () -> {
                setGroupSortAscending(!getGroupSortAscending());
                updateDashboardSortUI();
                refreshDashboardList();
            });
        }

        TextView btnSortTitle = dashboardView.findViewById(R.id.btn_sort_title);
        TextView btnSortTotal = dashboardView.findViewById(R.id.btn_sort_total);
        TextView btnSortLatest = dashboardView.findViewById(R.id.btn_sort_latest);

        if (btnSortTitle != null) ResponsiveUI.setupClickable(btnSortTitle, false, () -> {
            if (getDashboardSortColumn() == 0) setDashboardSortAscending(!getDashboardSortAscending());
            else { setDashboardSortColumn(0); setDashboardSortAscending(true); }
            StorageHelper.saveAppStorage(this, appStorage);
            updateDashboardSortUI();
            refreshDashboardList();
        });
        if (btnSortTotal != null) ResponsiveUI.setupClickable(btnSortTotal, false, () -> {
            if (getDashboardSortColumn() == 1) setDashboardSortAscending(!getDashboardSortAscending());
            else { setDashboardSortColumn(1); setDashboardSortAscending(false); }
            StorageHelper.saveAppStorage(this, appStorage);
            updateDashboardSortUI();
            refreshDashboardList();
        });
        if (btnSortLatest != null) ResponsiveUI.setupClickable(btnSortLatest, false, () -> {
            if (getDashboardSortColumn() == 2) setDashboardSortAscending(!getDashboardSortAscending());
            else { setDashboardSortColumn(2); setDashboardSortAscending(false); }
            StorageHelper.saveAppStorage(this, appStorage);
            updateDashboardSortUI();
            refreshDashboardList();
        });

        this.btnSortTitle = btnSortTitle;
        this.btnSortTotal = btnSortTotal;
        this.btnSortLatest = btnSortLatest;

        // Apply responsive styling to the main layout elements
        ResponsiveUI.applyResponsiveness(dashboardView);

        // Dynamic background borders & colors styling
        if (btnCreateAccount != null) {
            btnCreateAccount.setBackground(ResponsiveUI.createRoundedBg(
                    this,
                    ThemeManager.getBgSecondaryColor(MainActivity.this),
                    ThemeManager.getBorderColor(MainActivity.this),
                    1.0f,
                    8.0f
            ));
        }
        if (btnCreateGroup != null) {
            btnCreateGroup.setBackground(ResponsiveUI.createRoundedBg(
                    this,
                    ThemeManager.getBgSecondaryColor(MainActivity.this),
                    ThemeManager.getBorderColor(MainActivity.this),
                    1.0f,
                    8.0f
            ));
        }

        cardEmptyState.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgSecondaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.5f,
                12f
        ));

        // Set up click actions
        ResponsiveUI.setupClickable(btnCreateAccount, () -> openEditor(null));
        if (btnCreateGroup != null) {
            ResponsiveUI.setupClickable(btnCreateGroup, this::showCreateGroupDialog);
        }
        ResponsiveUI.setupClickable(cardEmptyState, () -> {
            if (currentViewGroup != null) {
                openEditor(null);
            }
        });
        currentEditingAccount = null;
        
        TextView textAppTitle = dashboardView.findViewById(R.id.text_app_title);
        TextView textAppSubtitle = dashboardView.findViewById(R.id.text_app_subtitle);
        ImageView btnDashboardBack = dashboardView.findViewById(R.id.btn_dashboard_back);
        
        if (currentViewGroup != null) {
            if (textAppTitle != null) textAppTitle.setText(currentViewGroup.getTitle());
            if (textAppSubtitle != null) textAppSubtitle.setVisibility(View.GONE);
            if (btnCreateGroup != null) btnCreateGroup.setVisibility(View.GONE);
            if (btnDashboardBack != null) {
                btnDashboardBack.setVisibility(View.VISIBLE);
                ResponsiveUI.setupClickable(btnDashboardBack, false, () -> {
                    currentViewGroup = null;
                    showDashboard();
                });
            }
        } else {
            if (textAppTitle != null) textAppTitle.setText(getString(R.string.app_name));
            if (textAppSubtitle != null) textAppSubtitle.setVisibility(View.VISIBLE);
            if (btnCreateGroup != null) btnCreateGroup.setVisibility(View.VISIBLE);
            if (btnDashboardBack != null) btnDashboardBack.setVisibility(View.GONE);
        }

        EditText editDashboardSearch = dashboardView.findViewById(R.id.edit_dashboard_search);
        editDashboardSearch.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgSecondaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                8.0f
        ));

        editDashboardSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (editDashboardSearch.getCompoundDrawablesRelative()[2] != null) {
                    if (event.getRawX() >= (editDashboardSearch.getRight() - editDashboardSearch.getCompoundDrawablesRelative()[2].getBounds().width() - editDashboardSearch.getPaddingRight())) {
                        editDashboardSearch.setText("");
                        return true;
                    }
                }
                v.performClick();
            }
            return false;
        });



        // Search bar watcher
        editDashboardSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dashboardSearchQuery = s.toString();
                refreshDashboardList();
            }
        });

        // Mount to main container first so findViewById works in refreshDashboardList
        mainContainer.removeAllViews();
        mainContainer.addView(dashboardView);

        // Populate accounts list
        refreshDashboardList();
    }

    @android.annotation.SuppressLint("SetTextI18n")
    private void updateDashboardSortUI() {
        if (btnSortTitle != null) {
            btnSortTitle.setTextColor(getDashboardSortColumn() == 0 ? ThemeManager.getSecondaryAccentColor(MainActivity.this) : getColor(R.color.text_tertiary));
            btnSortTitle.setText(getDashboardSortColumn() == 0 ? "Title " + (getDashboardSortAscending() ? "▲" : "▼") : "Title");
        }
        if (btnSortTotal != null) {
            btnSortTotal.setTextColor(getDashboardSortColumn() == 1 ? ThemeManager.getSecondaryAccentColor(MainActivity.this) : getColor(R.color.text_tertiary));
            btnSortTotal.setText(getDashboardSortColumn() == 1 ? "Total " + (getDashboardSortAscending() ? "▲" : "▼") : "Total");
        }
        if (btnSortLatest != null) {
            btnSortLatest.setTextColor(getDashboardSortColumn() == 2 ? ThemeManager.getSecondaryAccentColor(MainActivity.this) : getColor(R.color.text_tertiary));
            btnSortLatest.setText(getDashboardSortColumn() == 2 ? "Latest " + (getDashboardSortAscending() ? "▲" : "▼") : "Latest");
        }

        TextView btnSortGroupTitle = findViewById(R.id.btn_sort_group_title);
        if (btnSortGroupTitle != null) {
            btnSortGroupTitle.setText("Title " + (getGroupSortAscending() ? "▲" : "▼"));
        }
    }

    void refreshDashboardList() {
        if (accountsAdapter == null) return;
        
        List<Object> combinedGroups = new ArrayList<>();
        List<Object> combinedAccounts = new ArrayList<>();

        TextView textAppTitle = findViewById(R.id.text_app_title);
        TextView textAppSubtitle = findViewById(R.id.text_app_subtitle);
        if (textAppTitle != null) textAppTitle.setText(ArchiveHelper.isShowingArchive ? "Archive" : getString(R.string.app_name));
        if (textAppSubtitle != null) textAppSubtitle.setText(ArchiveHelper.isShowingArchive ? "Read-only history" : getString(R.string.app_subtitle));

        View btnCreateAccount = findViewById(R.id.btn_create_account);
        View btnCreateGroup = findViewById(R.id.btn_create_group);
        android.widget.ImageView btnArchive = findViewById(R.id.btn_archive);
        if (btnCreateAccount != null) btnCreateAccount.setVisibility(ArchiveHelper.isShowingArchive ? View.GONE : View.VISIBLE);
        if (btnCreateGroup != null) btnCreateGroup.setVisibility(ArchiveHelper.isShowingArchive ? View.GONE : View.VISIBLE);
        if (btnArchive != null) btnArchive.setImageResource(ArchiveHelper.isShowingArchive ? R.drawable.ic_archive : R.drawable.ic_archive_outline);

        if (currentViewGroup != null) {
            combinedAccounts.addAll(applyDashboardSort(ArchiveHelper.getVisibleAccounts(currentViewGroup.getAccounts())));
        } else {
            List<AccountGroup> sortedGroups = new ArrayList<>(ArchiveHelper.getVisibleGroups(appStorage.groups));
            sortedGroups.sort((a, b) -> {
                if (a.isPinned() != b.isPinned()) return a.isPinned() ? -1 : 1;
                int titleCompare = a.getTitle().compareToIgnoreCase(b.getTitle());
                return getGroupSortAscending() ? titleCompare : -titleCompare;
            });
            combinedGroups.addAll(sortedGroups);
            combinedAccounts.addAll(applyDashboardSort(ArchiveHelper.getVisibleAccounts(appStorage.standaloneAccounts)));
        }
        
        String query = dashboardSearchQuery.trim().toLowerCase(Locale.getDefault());
        accountsAdapter.setFilter(combinedAccounts, query);
        if (groupsAdapter != null) groupsAdapter.setFilter(combinedGroups, query);
        
        View cardEmptyState = findViewById(R.id.card_empty_state);
        View contentContainer = findViewById(R.id.dashboard_content_container);
        View sectionGroups = findViewById(R.id.section_groups);
        View sectionAccounts = findViewById(R.id.section_accounts);
        EditText editDashboardSearch = findViewById(R.id.edit_dashboard_search);
        
        if (cardEmptyState == null) return;

        boolean hasGroups = groupsAdapter != null && groupsAdapter.getItemCount() > 0;
        boolean hasAccounts = accountsAdapter.getItemCount() > 0;
        boolean isListEmpty = appStorage.groups.isEmpty() && appStorage.standaloneAccounts.isEmpty();

        if (isListEmpty) {
            cardEmptyState.setVisibility(View.VISIBLE);
            contentContainer.setVisibility(View.GONE);
            editDashboardSearch.setVisibility(View.GONE);
        } else if (!hasGroups && !hasAccounts) {
            cardEmptyState.setVisibility(View.VISIBLE);
            contentContainer.setVisibility(View.GONE);
            editDashboardSearch.setVisibility(View.VISIBLE);
        } else {
            cardEmptyState.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
            editDashboardSearch.setVisibility(View.VISIBLE);
            
            sectionGroups.setVisibility(hasGroups ? View.VISIBLE : View.GONE);
            sectionAccounts.setVisibility(hasAccounts ? View.VISIBLE : View.GONE);
            
            updateDashboardSortUI();
        }
    }

    /**
     * Returns a sorted copy of the accounts list based on current dashboardSortMode and dashboardSortAscending.
     * Pinned accounts are always placed at the top first.
     */
    private List<Account> applyDashboardSort(List<Account> source) {
        List<Account> sorted = new ArrayList<>(source);
        
        int mode = getDashboardSortColumn();
        boolean asc = getDashboardSortAscending();
        
        sorted.sort((a, b) -> {
            // First check pin status
            if (a.isPinned() != b.isPinned()) {
                return a.isPinned() ? -1 : 1;
            }
            
            int result;
            if (mode == 0) { // Title
                result = a.getTitle().compareToIgnoreCase(b.getTitle());
            } else if (mode == 1) { // Total amount
                result = Double.compare(a.calculateTotal(), b.calculateTotal());
            } else { // Latest modified
                result = Long.compare(a.getLastModified(), b.getLastModified());
            }
            return asc ? result : -result;
        });
        return sorted;
    }



    private int getDashboardSortColumn() {
        if (currentViewGroup != null) return currentViewGroup.getSortMode();
        return ArchiveHelper.isShowingArchive ? archivedDashboardSortMode : dashboardSortMode;
    }
    private void setDashboardSortColumn(int mode) {
        if (currentViewGroup != null) currentViewGroup.setSortMode(mode);
        else if (ArchiveHelper.isShowingArchive) archivedDashboardSortMode = mode;
        else dashboardSortMode = mode;
    }
    private boolean getDashboardSortAscending() {
        if (currentViewGroup != null) return currentViewGroup.isSortAscending();
        return ArchiveHelper.isShowingArchive ? archivedDashboardSortAscending : dashboardSortAscending;
    }
    private void setDashboardSortAscending(boolean asc) {
        if (currentViewGroup != null) currentViewGroup.setSortAscending(asc);
        else if (ArchiveHelper.isShowingArchive) archivedDashboardSortAscending = asc;
        else dashboardSortAscending = asc;
    }
    
    private boolean getGroupSortAscending() {
        return ArchiveHelper.isShowingArchive ? archivedGroupSortAscending : groupSortAscending;
    }
    
    private void setGroupSortAscending(boolean asc) {
        if (ArchiveHelper.isShowingArchive) archivedGroupSortAscending = asc;
        else groupSortAscending = asc;
    }

    /**
     * Renders the Account Editor screen.
     *
     * @param account The account to edit. If null, a new account is initialized.
     */
    @android.annotation.SuppressLint({"SetTextI18n", "ClickableViewAccessibility", "NotifyDataSetChanged"})
    private void openEditor(Account account) {
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
        recordsAdapter = new RecordsAdapter();
        listRecordsRecyclerView.setAdapter(recordsAdapter);

        // Bind NC Agent FAB
        android.widget.ImageView btnNCAgent = editorView.findViewById(R.id.btn_nc_agent);
        if (btnNCAgent != null) {
            btnNCAgent.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getPrimaryAccentColor(MainActivity.this), ThemeManager.getPrimaryAccentColor(MainActivity.this), 0f, 100f));
            btnNCAgent.setColorFilter(getColor(R.color.text_on_accent));
            btnNCAgent.setImageResource(android.R.drawable.ic_btn_speak_now); // Unique microphone/voice icon representing natural language
            btnNCAgent.setOnClickListener(v -> showNCAgentBottomSheet());
            if (account != null && account.isArchived()) {
                btnNCAgent.setVisibility(View.GONE);
            }
        }
        
        // Setup Swipe-to-Delete and Drag-and-Drop for Records
        androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback recordSwipeCallback = new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            private boolean isDragActive = false;

            @Override
            public int getSwipeDirs(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder) {
                if (currentEditingAccount != null && currentEditingAccount.isArchived()) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public int getDragDirs(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder) {
                if (currentEditingAccount != null && currentEditingAccount.isArchived()) return 0;
                boolean isDefaultSort = getSortColumn() == 0 && getSortAscending();
                boolean noSearch = currentRecordSearchQuery == null || currentRecordSearchQuery.trim().isEmpty();
                if (isDefaultSort && noSearch) {
                    return androidx.recyclerview.widget.ItemTouchHelper.UP | androidx.recyclerview.widget.ItemTouchHelper.DOWN;
                }
                return 0; // Disable drag otherwise
            }

            @Override
            public boolean onMove(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, @androidx.annotation.NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getBindingAdapterPosition();
                int toPos = target.getBindingAdapterPosition();
                if (fromPos == RecyclerView.NO_POSITION || toPos == RecyclerView.NO_POSITION || recordsAdapter == null) return false;
                
                Record fromRecord = recordsAdapter.displayRecords.get(fromPos);
                Record toRecord = recordsAdapter.displayRecords.get(toPos);
                
                // Swap originalIndex to permanently swap their S.Nos
                int tempIndex = fromRecord.getOriginalIndex();
                fromRecord.setOriginalIndex(toRecord.getOriginalIndex());
                toRecord.setOriginalIndex(tempIndex);
                
                java.util.Collections.swap(recordsAdapter.displayRecords, fromPos, toPos);
                recordsAdapter.notifyItemMoved(fromPos, toPos);
                isDragActive = true;
                return true;
            }

            @Override
            public void clearView(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if (isDragActive) {
                    isDragActive = false;
                    applySorting();
                    populateRecordsList();
                }
            }

            @Override
            public void onSwiped(@androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION || recordsAdapter == null) return;
                
                Record deletedRecord = recordsAdapter.displayRecords.get(pos);
                int trueIndex = getActiveRecords().indexOf(deletedRecord);
                
                // Temporarily remove
                getActiveRecords().remove(trueIndex);
                recordsAdapter.refreshDisplay();
                updateBulkActionsState();
                updateHeaderLabels();
                
                showUndoSnackbar("Record deleted", () -> {
                    getActiveRecords().add(trueIndex, deletedRecord);
                    recordsAdapter.refreshDisplay();
                    updateBulkActionsState();
                    updateHeaderLabels();
                }, null);
            }
        };
        new androidx.recyclerview.widget.ItemTouchHelper(recordSwipeCallback).attachToRecyclerView(listRecordsRecyclerView);
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
                updateBulkActionsState();
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
            showDashboard();
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
            showDashboard();
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
    private void populateRecordsList() {
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
        updateSelectAllHeaderState();
        updateBulkActionsState();
    }

    private void applySorting() {
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
    private void updateHeaderLabels() {
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

    private int getNewOriginalIndex() {
        int maxIndex = -1;
        for (Record r : getActiveRecords()) {
            if (r.getOriginalIndex() > maxIndex) {
                maxIndex = r.getOriginalIndex();
            }
        }
        return maxIndex + 1;
    }


    @android.annotation.SuppressLint("NotifyDataSetChanged")
    class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RecordViewHolder> {

        private boolean isSelectionMode = false;

        public void setSelectionMode(boolean mode) {
            if (this.isSelectionMode != mode) {
                this.isSelectionMode = mode;
                notifyDataSetChanged();
            }
        }

        // Filtered view of tempRecords, rebuilt on every setFilter() call
        final List<Record> displayRecords = new ArrayList<>();
        public java.util.Set<String> filterCategories = new java.util.HashSet<>();

        public void setFilterCategories(java.util.Set<String> cats) {
            filterCategories.clear();
            if (cats != null) filterCategories.addAll(cats);
            refreshDisplay();
        }

        /** Rebuilds the displayRecords list from tempRecords using the given query filter. */
        void setFilter(String query) {
            displayRecords.clear();
            String q = (query == null ? "" : query.trim().toLowerCase(Locale.getDefault()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            for (Record r : getActiveRecords()) {
                // Category filter
                if (!filterCategories.isEmpty()) {
                    if (!filterCategories.contains(r.getCategory())) continue;
                }
                // Text search filter
                if (!q.isEmpty()) {
                    boolean matchDesc = r.getDescription().toLowerCase(Locale.getDefault()).contains(q);
                    boolean matchRem = r.getRemarks() != null && r.getRemarks().toLowerCase(Locale.getDefault()).contains(q);
                    if (!matchDesc && !matchRem) {
                        continue;
                    }
                }
                // Date range filter
                if (getFilterDateFrom() != null || getFilterDateTo() != null) {
                    try {
                        Date recordDate = sdf.parse(r.getDate());
                        if (getFilterDateFrom() != null) {
                            Date from = sdf.parse(getFilterDateFrom());
                            if (recordDate != null && recordDate.before(from)) continue;
                        }
                        if (getFilterDateTo() != null) {
                            Date to = sdf.parse(getFilterDateTo());
                            if (recordDate != null && recordDate.after(to)) continue;
                        }
                    } catch (ParseException e) {
                        android.util.Log.e("NoteCalc", "Date parse error", e);
                    }
                }
                // Amount range filter
                if (getFilterAmountFrom() != null && r.getAmount() < getFilterAmountFrom()) continue;
                if (getFilterAmountTo() != null && r.getAmount() > getFilterAmountTo()) continue;

                displayRecords.add(r);
            }
            notifyDataSetChanged();
            updateBulkActionsState();
        }

        /** Call this whenever tempRecords changes (add/edit/delete/sort) to refresh display. */
        void refreshDisplay() {
            // Preserve the current filter text if any — re-filter from scratch
            setFilter(currentRecordSearchQuery);
        }

        @androidx.annotation.NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            View rowView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_record, parent, false);
            ResponsiveUI.applyResponsiveness(rowView);
            return new RecordViewHolder(rowView);
        }

        @android.annotation.SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull RecordViewHolder holder, int position) {
            Record record = displayRecords.get(position);
            // Find the true index in tempRecords (or budget records) so that edit/delete work correctly
            int trueIndex = getActiveRecords().indexOf(record);

            holder.tvSno.setText(String.valueOf(record.getOriginalIndex() + 1));
            holder.tvDesc.setText(record.getDescription());
            
            // Reset date view state to avoid recycling bugs
            if (holder.revertDateTask != null) {
                holder.tvDate.removeCallbacks(holder.revertDateTask);
                holder.revertDateTask = null;
            }
            holder.isShowingDay = false;
            holder.tvDate.setText(AppUtils.formatDateCompact(record.getDate()));
            
            holder.tvDate.setOnClickListener(v -> {
                if (holder.isShowingDay) {
                    if (holder.revertDateTask != null) {
                        holder.tvDate.removeCallbacks(holder.revertDateTask);
                        holder.revertDateTask = null;
                    }
                    holder.isShowingDay = false;
                    holder.tvDate.setText(AppUtils.formatDateCompact(record.getDate()));
                } else {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        Date d = sdf.parse(record.getDate());
                        if (d != null) {
                            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                            holder.tvDate.setText(dayFormat.format(d));
                            holder.isShowingDay = true;
                            
                            if (holder.revertDateTask != null) {
                                holder.tvDate.removeCallbacks(holder.revertDateTask);
                            }
                            holder.revertDateTask = () -> {
                                holder.isShowingDay = false;
                                holder.tvDate.setText(AppUtils.formatDateCompact(record.getDate()));
                                holder.revertDateTask = null;
                            };
                            holder.tvDate.postDelayed(holder.revertDateTask, 5000);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("NoteCalc", "Error resetting date", e);
                    }
                }
            });

            holder.tvAmount.setText(String.format(Locale.getDefault(), "%.2f", record.getAmount()));

            // Bind remarks (show only if non-empty)
            String remarks = record.getRemarks();
            if (holder.tvRemarks != null) {
                if (remarks != null && !remarks.isEmpty()) {
                    holder.tvRemarks.setText(remarks);
                    holder.tvRemarks.setVisibility(View.VISIBLE);
                } else {
                    holder.tvRemarks.setVisibility(View.GONE);
                }
            }
            
            // Bind category
            String category = record.getCategory();
            if (holder.tvCategory != null) {
                if (category != null && !category.isEmpty()) {
                    holder.tvCategory.setText(category);
                    holder.tvCategory.setVisibility(View.VISIBLE);
                } else {
                    holder.tvCategory.setVisibility(View.GONE);
                }
            }

            // Bind attachments
            if (holder.attachmentSummary != null && holder.attachmentsScroll != null && holder.attachmentsContainer != null) {
                if (record.getAttachments() != null && !record.getAttachments().isEmpty()) {
                    java.util.List<String> atts = record.getAttachments();
                    String firstPath = atts.get(0);
                    java.io.File f = new java.io.File(firstPath);
                    String name = f.getName();
                    if (name.length() > 15) name = name.substring(0, 15) + "...";
                    String icon = (firstPath.toLowerCase().endsWith(".pdf") || firstPath.toLowerCase().endsWith(".doc") || firstPath.toLowerCase().endsWith(".docx")) ? "\uD83D\uDCC4 " : "\uD83D\uDDBC ";
                    
                    if (atts.size() == 1) {
                        holder.attachmentSummary.setText(icon + name);
                    } else {
                        holder.attachmentSummary.setText(icon + name + " ▾"); // ?
                    }
                    
                    holder.attachmentSummary.setVisibility(View.VISIBLE);
                    holder.attachmentsScroll.setVisibility(View.GONE);
                    
                    holder.attachmentsScroll.setOnTouchListener((v, event) -> {
                        int action = event.getActionMasked();
                        if (action == android.view.MotionEvent.ACTION_DOWN || action == android.view.MotionEvent.ACTION_MOVE) {
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                        } else if (action == android.view.MotionEvent.ACTION_UP || action == android.view.MotionEvent.ACTION_CANCEL) {
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            if (action == android.view.MotionEvent.ACTION_UP) {
                                v.performClick();
                            }
                        }
                        return false;
                    });
                    
                    holder.attachmentsContainer.removeAllViews();
                    for (int i = 0; i < atts.size(); i++) {
                        String path = atts.get(i);
                        java.io.File file = new java.io.File(path);
                        String fname = file.getName();
                        if (fname.length() > 15) fname = fname.substring(0, 15) + "...";
                        String ficon = (path.toLowerCase().endsWith(".pdf") || path.toLowerCase().endsWith(".doc") || path.toLowerCase().endsWith(".docx")) ? "\uD83D\uDCC4 " : "\uD83D\uDDBC ";
                        
                        android.widget.TextView chip = new android.widget.TextView(MainActivity.this);
                        chip.setText(ficon + fname);
                        chip.setTextSize(11);
                        chip.setTextColor(getColor(R.color.text_primary));
                        chip.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, ThemeManager.getBgSecondaryColor(MainActivity.this), 6.0f));
                        chip.setPadding(12, 6, 12, 6);
                        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 0, 12, 0);
                        chip.setLayoutParams(lp);
                        
                        chip.setOnTouchListener((v, event) -> {
                            int action = event.getActionMasked();
                            if (action == android.view.MotionEvent.ACTION_DOWN || action == android.view.MotionEvent.ACTION_MOVE) {
                                v.getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
                            } else if (action == android.view.MotionEvent.ACTION_UP || action == android.view.MotionEvent.ACTION_CANCEL) {
                                v.getParent().getParent().getParent().requestDisallowInterceptTouchEvent(false);
                            }
                            return false;
                        });
                        
                        chip.setOnClickListener(_unused_v -> {
                            try {
                                android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".fileprovider", file);
                                android.content.Intent viewIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                                viewIntent.setDataAndType(uri, getContentResolver().getType(uri));
                                viewIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(viewIntent);
                            } catch (Exception e) {
                                android.widget.Toast.makeText(MainActivity.this, "Cannot open file", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
                        holder.attachmentsContainer.addView(chip);
                    }
                    
                    ResponsiveUI.setupClickable(holder.attachmentSummary, false, () -> {
                        if (atts.size() == 1) {
                            try {
                                android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".fileprovider", f);
                                android.content.Intent viewIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                                viewIntent.setDataAndType(uri, getContentResolver().getType(uri));
                                viewIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(viewIntent);
                            } catch (Exception e) {
                                android.widget.Toast.makeText(MainActivity.this, "Cannot open file", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            holder.attachmentSummary.setVisibility(View.GONE);
                            holder.attachmentsScroll.setVisibility(View.VISIBLE);
                        }
                    });
                    
                } else {
                    holder.attachmentSummary.setVisibility(View.GONE);
                    holder.attachmentsScroll.setVisibility(View.GONE);
                }
            }
            
            // Bind selection checkbox without triggering the listener
            if (holder.cbSelect != null) {
                holder.cbSelect.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
                holder.cbSelect.setOnCheckedChangeListener(null);
                holder.cbSelect.setChecked(record.isSelected());
                holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    record.setSelected(isChecked);
                    updateSelectAllHeaderState();
                    updateBulkActionsState();
                });
            }

            // Highlight row if actively being edited
            int rowBgColor = (position % 2 == 0) ? ThemeManager.getBgSecondaryColor(MainActivity.this) : ThemeManager.getBgTertiaryColor(MainActivity.this);
            if (trueIndex == editingRecordIndex) {
                holder.itemView.setBackground(ResponsiveUI.createRoundedBg(
                        MainActivity.this,
                        rowBgColor,
                        ThemeManager.getSecondaryAccentColor(MainActivity.this),
                        1.5f,
                        4.0f
                ));
            } else {
                holder.itemView.setBackground(ResponsiveUI.createRoundedBg(
                        MainActivity.this,
                        rowBgColor,
                        0,
                        0,
                        4.0f
                ));
            }

            ResponsiveUI.setupClickable(holder.itemView, true, () -> {
                if (currentEditingAccount != null && currentEditingAccount.isArchived()) return;
                enterEditRecordMode(trueIndex, record);
            }, () -> {
                if (!record.isSelected()) {
                    record.setSelected(true);
                    updateSelectAllHeaderState();
                    updateBulkActionsState();
                }
            });
        }

        @Override
        public int getItemCount() {
            return displayRecords.size();
        }

        class RecordViewHolder extends RecyclerView.ViewHolder {
            TextView tvSno;
            TextView tvDesc;
            TextView tvDate;
            TextView tvAmount;
            TextView tvRemarks;
            TextView tvCategory;
            CheckBox cbSelect;
            TextView attachmentSummary;
            android.widget.HorizontalScrollView attachmentsScroll;
            LinearLayout attachmentsContainer;
            Runnable revertDateTask;
            boolean isShowingDay = false;

            RecordViewHolder(View itemView) {
                super(itemView);
                tvSno = itemView.findViewById(R.id.text_record_sno);
                tvDesc = itemView.findViewById(R.id.text_record_desc);
                tvDate = itemView.findViewById(R.id.text_record_date);
                tvAmount = itemView.findViewById(R.id.text_record_amount);
                tvRemarks = itemView.findViewById(R.id.text_record_remarks);
                tvCategory = itemView.findViewById(R.id.text_record_category);
                cbSelect = itemView.findViewById(R.id.cb_record_select);
                attachmentSummary = itemView.findViewById(R.id.text_record_attachment_summary);
                attachmentsScroll = itemView.findViewById(R.id.record_attachments_scroll);
                attachmentsContainer = itemView.findViewById(R.id.record_attachments_container);
            }
        }
    }

    @android.annotation.SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private class AccountsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_ACCOUNT = 0;
        private static final int TYPE_GROUP = 1;

        private final List<Object> displayItems = new ArrayList<>();

        void setFilter(List<Object> sortedSource, String query) {
            displayItems.clear();
            for (Object item : sortedSource) {
                if (item instanceof Account) {
                    Account account = (Account) item;
                    if (!query.isEmpty() && !account.getTitle().toLowerCase(Locale.getDefault()).contains(query)) {
                        continue;
                    }
                    displayItems.add(account);
                } else if (item instanceof AccountGroup) {
                    AccountGroup group = (AccountGroup) item;
                    if (!query.isEmpty()) {
                        boolean matchesGroupTitle = group.getTitle().toLowerCase(Locale.getDefault()).contains(query);
                        if (matchesGroupTitle) {
                            displayItems.add(group);
                        }
                        
                        for (Account acc : group.getAccounts()) {
                            if (acc.getTitle().toLowerCase(Locale.getDefault()).contains(query)) {
                                displayItems.add(acc);
                            }
                        }
                    } else {
                        displayItems.add(group);
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (displayItems.get(position) instanceof AccountGroup) return TYPE_GROUP;
            return TYPE_ACCOUNT;
        }

        @androidx.annotation.NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            if (viewType == TYPE_GROUP) {
                View rowView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_group, parent, false);
                ResponsiveUI.applyResponsiveness(rowView);
                return new GroupViewHolder(rowView);
            } else {
                View rowView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_account, parent, false);
                ResponsiveUI.applyResponsiveness(rowView);
                return new AccountViewHolder(rowView);
            }
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull RecyclerView.ViewHolder holder, int position) {
            Object item = displayItems.get(position);
            
            if (holder instanceof AccountViewHolder) {
                AccountViewHolder accHolder = (AccountViewHolder) holder;
                Account account = (Account) item;

                accHolder.tvTitle.setText(account.getTitle());
                accHolder.tvTotal.setText(String.format(Locale.getDefault(), "%.2f", account.calculateTotal()));

                if (accHolder.tvPurse != null) {
                    if (account.hasBudget()) {
                        accHolder.tvPurse.setVisibility(View.VISIBLE);
                        accHolder.tvPurse.setText(String.format(Locale.getDefault(), "Bal : %.2f", account.calculateRemainingPurse()));
                    } else {
                        accHolder.tvPurse.setVisibility(View.GONE);
                    }
                }

                int itemsSize = account.getRecords().size();
                accHolder.tvItemsCount.setText(String.format(Locale.getDefault(), "%d %s", itemsSize, itemsSize == 1 ? "item" : "items"));

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                accHolder.tvDate.setText(sdf.format(new Date(account.getLastModified())));

                accHolder.itemView.setBackground(ResponsiveUI.createCardSelector(MainActivity.this));



                accHolder.btnPinAccount.setImageResource(account.isPinned() ? R.drawable.ic_pin_filled : R.drawable.ic_pin);
                accHolder.btnPinAccount.setImageTintList(ResponsiveUI.createIconTintSelector(
                        account.isPinned() ? ThemeManager.getSecondaryAccentColor(MainActivity.this) : getColor(R.color.text_tertiary),
                        ThemeManager.getSecondaryAccentColor(MainActivity.this)
                ));
                accHolder.btnPinAccount.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, Color.parseColor("#15FFFFFF"), 6.0f));

                final AccountGroup accountParentGroup;
                if (currentViewGroup != null) {
                    accountParentGroup = currentViewGroup;
                } else {
                    AccountGroup temp = null;
                    for (AccountGroup g : appStorage.groups) {
                        if (g.getAccounts().contains(account)) {
                            temp = g;
                            break;
                        }
                    }
                    accountParentGroup = temp;
                }

                ResponsiveUI.setupClickable(accHolder.itemView, false, () -> {
                    currentViewGroup = accountParentGroup;
                    openEditor(account);
                }, () -> MenuHelper.showAccountPopupMenu(MainActivity.this, accHolder.itemView, account));

                if (accHolder.btnMoveAccount != null) {
                    if (accountParentGroup != null) {
                        accHolder.btnMoveAccount.setImageResource(R.drawable.ic_move_folder_out);
                    } else {
                        accHolder.btnMoveAccount.setImageResource(R.drawable.ic_move_folder);
                    }
                    accHolder.btnMoveAccount.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, Color.parseColor("#15FFFFFF"), 6.0f));
                    accHolder.btnMoveAccount.setImageTintList(ResponsiveUI.createIconTintSelector(
                            getColor(R.color.text_tertiary),
                            ThemeManager.getSecondaryAccentColor(MainActivity.this)
                    ));
                    ResponsiveUI.setupClickable(accHolder.btnMoveAccount, false, () -> {
                        if (accountParentGroup != null) {
                            // Move out of group (back to standalone)
                            accountParentGroup.getAccounts().remove(account);
                            appStorage.standaloneAccounts.add(account);
                            StorageHelper.saveAppStorage(MainActivity.this, appStorage);
                            refreshDashboardList();
                            Toast.makeText(MainActivity.this, getString(R.string.auto_moved_to_dashboard_8), Toast.LENGTH_SHORT).show();
                        } else {
                            // Move into a group
                            if (appStorage.groups.isEmpty()) {
                                Toast.makeText(MainActivity.this, getString(R.string.auto_no_groups_available__9), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            

                            
                            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
                            View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_move_group, null);
                            builder.setView(dialogView);
                            
                            final androidx.appcompat.app.AlertDialog dialog = builder.create();
                            if (dialog.getWindow() != null) {
                                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                            }
                            
                            View dialogRoot = dialogView.findViewById(R.id.dialog_root);
                            LinearLayout detailsContainer = dialogView.findViewById(R.id.details_container);
                            TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
                            
                            dialogRoot.setBackground(ResponsiveUI.createRoundedBg(MainActivity.this, ThemeManager.getBgSecondaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.5f, 12f));
                            detailsContainer.setBackground(ResponsiveUI.createRoundedBg(MainActivity.this, ThemeManager.getBgPrimaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.0f, 6f));
                            btnCancel.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, Color.parseColor("#20EF4444"), 4.0f));
                            btnCancel.setTextColor(getColor(R.color.error_red));
                            
                            List<AccountGroup> targetGroups = new ArrayList<>();
                            for (AccountGroup g : appStorage.groups) {
                                if (g.isArchived() == account.isArchived()) targetGroups.add(g);
                            }
                            
                            if (targetGroups.isEmpty()) {
                                Toast.makeText(MainActivity.this, getString(R.string.auto_no_groups_available__9), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                            
                            for (int i = 0; i < targetGroups.size(); i++) {
                                final AccountGroup selectedGroup = targetGroups.get(i);
                                TextView tvGroup = new TextView(MainActivity.this);
                                tvGroup.setText(selectedGroup.getTitle());
                                tvGroup.setTextColor(getColor(R.color.text_primary));
                                tvGroup.setTextSize(16f);
                                tvGroup.setPadding(32, 24, 32, 24);
                                tvGroup.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, Color.parseColor("#15FFFFFF"), 4.0f));
                                ResponsiveUI.setupClickable(tvGroup, false, () -> {
                                    appStorage.standaloneAccounts.remove(account);
                                    selectedGroup.getAccounts().add(account);
                                    selectedGroup.updateLastModified();
                                    StorageHelper.saveAppStorage(MainActivity.this, appStorage);
                                    refreshDashboardList();
                                    Toast.makeText(MainActivity.this, "Moved to " + selectedGroup.getTitle(), Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                });
                                detailsContainer.addView(tvGroup);
                                
                                if (i < targetGroups.size() - 1) {
                                    View divider = new View(MainActivity.this);
                                    divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                                    divider.setBackgroundColor(ThemeManager.getBorderColor(MainActivity.this));
                                    detailsContainer.addView(divider);
                                }
                            }
                            
                            ResponsiveUI.setupClickable(btnCancel, false, dialog::cancel);
                            dialog.show();
                        }
                    });
                }
                accHolder.btnPinAccount.setVisibility(View.VISIBLE);
                accHolder.btnPinAccount.setImageResource(R.drawable.ic_pin); // Use same icon, just tint different? Wait, maybe just keep icon.
                accHolder.btnPinAccount.setImageTintList(ResponsiveUI.createIconTintSelector(
                        account.isPinned() ? ThemeManager.getPrimaryAccentColor(MainActivity.this) : getColor(R.color.text_tertiary),
                        ThemeManager.getSecondaryAccentColor(MainActivity.this)
                ));
                ResponsiveUI.setupClickable(accHolder.btnPinAccount, false, () -> {
                    account.setPinned(!account.isPinned());
                    StorageHelper.saveAppStorage(MainActivity.this, appStorage);
                    refreshDashboardList();
                });
            } else if (holder instanceof GroupViewHolder) {
                GroupViewHolder grpHolder = (GroupViewHolder) holder;
                AccountGroup group = (AccountGroup) item;
                
                grpHolder.tvTitle.setText(group.getTitle());
                long latestDate = group.getLastModified();
                for (Account a : group.getAccounts()) {
                    if (a.getLastModified() > latestDate) {
                        latestDate = a.getLastModified();
                    }
                }
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                grpHolder.tvDate.setText(sdf.format(new Date(latestDate)));
                
                int listCount = group.getAccounts().size();
                grpHolder.tvAccounts.setText(listCount + (listCount == 1 ? " List" : " Lists"));
                
                grpHolder.itemView.setBackground(ResponsiveUI.createCardSelector(MainActivity.this));
                
                grpHolder.btnDeleteGroup.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, Color.parseColor("#20EF4444"), 6.0f));
                grpHolder.btnDeleteGroup.setImageTintList(ResponsiveUI.createIconTintSelector(
                        getColor(R.color.error_red),
                        Color.parseColor("#FF6B6B")
                ));
                
                ResponsiveUI.setupClickable(grpHolder.itemView, false, () -> {
                    currentViewGroup = group;
                    showDashboard(); // Refresh dashboard into group view
                }, () -> MenuHelper.showGroupPopupMenu(MainActivity.this, grpHolder.itemView, group));
                
                ResponsiveUI.setupClickable(grpHolder.btnDeleteGroup, false, () -> showDeleteGroupConfirmation(group));
            }
        }

        @Override
        public int getItemCount() {
            return displayItems.size();
        }

        class AccountViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvTotal, tvDate, tvItemsCount, tvPurse;
            ImageView btnPinAccount, btnMoveAccount;

            AccountViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.text_account_title);
                tvTotal = itemView.findViewById(R.id.text_account_total);
                tvPurse = itemView.findViewById(R.id.text_account_purse);
                tvDate = itemView.findViewById(R.id.text_account_date);
                tvItemsCount = itemView.findViewById(R.id.text_account_items_count);

                btnPinAccount = itemView.findViewById(R.id.btn_pin_account);
                btnMoveAccount = itemView.findViewById(R.id.btn_move_account);
            }
        }
        
        class GroupViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvAccounts, tvDate;
            ImageView btnDeleteGroup, btnPinGroup;
            
            GroupViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.text_group_title);
                tvAccounts = itemView.findViewById(R.id.text_group_accounts);
                tvDate = itemView.findViewById(R.id.text_group_date);
                btnDeleteGroup = itemView.findViewById(R.id.btn_delete_group);
                btnPinGroup = itemView.findViewById(R.id.btn_pin_group);
            }
        }
    }


    /**
     * Syncs the "select all" header checkbox state based on visible displayRecords selection.
     * States: unchecked (none selected), checked (all selected), indeterminate (partial).
     */
    @android.annotation.SuppressLint("SetTextI18n")
    private void updateSelectAllHeaderState() {
        if (cbSelectAllHeader == null || recordsAdapter == null) return;
        List<Record> displayed = recordsAdapter.displayRecords;
        if (displayed.isEmpty()) {
            cbSelectAllHeader.setOnCheckedChangeListener(null);
            cbSelectAllHeader.setChecked(false);
            return;
        }
        int selectedCount = 0;
        for (Record r : displayed) {
            if (r.isSelected()) selectedCount++;
        }
        cbSelectAllHeader.setOnCheckedChangeListener(null);
        cbSelectAllHeader.setChecked(selectedCount == displayed.size());
        cbSelectAllHeader.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (Record r : recordsAdapter.displayRecords) {
                r.setSelected(isChecked);
            }
            recordsAdapter.notifyItemRangeChanged(0, recordsAdapter.getItemCount());
            updateBulkActionsState();
        });
    }

    boolean isFilterActive() {
        if (recordsAdapter != null && !recordsAdapter.filterCategories.isEmpty()) return true;
        if (currentRecordSearchQuery != null && !currentRecordSearchQuery.trim().isEmpty()) return true;
        if (getFilterDateFrom() != null || getFilterDateTo() != null) return true;
        return getFilterAmountFrom() != null || getFilterAmountTo() != null;
    }

    /**
     * Shows or hides the "Delete Selected" button based on whether any records are selected.
     */
    @android.annotation.SuppressLint("SetTextI18n")
    void updateBulkActionsState() {
        if (btnBulkActionsMenu == null) return;
        
        int filterCount = recordsAdapter != null ? recordsAdapter.displayRecords.size() : 0;
        
        boolean anySelected = EditorUIHelper.updateTotalsAndBulkActions(
                getActiveRecords(),
                filterCount,
                isFilterActive(),
                containerBulkActions,
                textSelectedTotal,
                textTotalValField,
                textTotalLabelField,
                cbSelectAllHeader
        );

        if (recordsAdapter != null) {
            recordsAdapter.setSelectionMode(anySelected);
        }
    }

    /**
     * Shows a confirmation dialog listing the selected records before performing bulk delete.
     */
    @android.annotation.SuppressLint("SetTextI18n")
    void showDeleteMultipleConfirmationDialog(List<Record> selectedRecords) {
        if (selectedRecords.size() <= 2) {
            for (Record r : selectedRecords) {
                int idx = getActiveRecords().indexOf(r);
                if (idx != -1) {
                    if (editingRecordIndex == idx) {
                        cancelEditRecordMode();
                    } else if (editingRecordIndex > idx) {
                        editingRecordIndex--;
                    }
                }
            }
            getActiveRecords().removeAll(selectedRecords);
            populateRecordsList();
            updateBulkActionsState();
            updateHeaderLabels();
            return;
        }

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_delete_multiple_dialog, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        LinearLayout selectedItemsList = dialogView.findViewById(R.id.selected_items_list);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnDelete = dialogView.findViewById(R.id.btn_dialog_delete);

        // Style dialog
        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgSecondaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.5f,
                12f
        ));
        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgPrimaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                6f
        ));
        btnCancel.setBackground(ResponsiveUI.createRippleRoundedBg(
                this,
                ThemeManager.getBgPrimaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                4f
        ));
        btnDelete.setBackground(ResponsiveUI.createRippleRoundedBg(
                this,
                getColor(R.color.error_red),
                0,
                0,
                4f
        ));

        // Populate selected items list inside the dialog
                for (Record r : selectedRecords) {
            // Build a simple text row for each selected item
            TextView rowView = new TextView(this);
            String lineText = "• " + r.getDescription()
                    + "   " + AppUtils.formatDateCompact(r.getDate())
                    + "   " + String.format(Locale.getDefault(), "%.2f", r.getAmount());
            rowView.setText(lineText);
            rowView.setTextColor(getColor(R.color.text_primary));
            rowView.setTextSize(13f);
            int padPx = (int) (6 * getResources().getDisplayMetrics().density);
            rowView.setPadding(0, padPx, 0, padPx);

            // Show remarks below if present
            String remarks = r.getRemarks();
            boolean hasRemarks = (remarks != null && !remarks.isEmpty());
            boolean hasAttachments = (r.getAttachments() != null && !r.getAttachments().isEmpty());
            
            if (hasRemarks || hasAttachments) {
                LinearLayout rowContainer = new LinearLayout(this);
                rowContainer.setOrientation(LinearLayout.VERTICAL);
                rowContainer.addView(rowView);
                
                if (hasRemarks) {
                    TextView remarksView = new TextView(this);
                    remarksView.setText("  ↳ " + remarks);
                    remarksView.setTextColor(getColor(R.color.text_tertiary));
                    remarksView.setTextSize(11f);
                    remarksView.setTypeface(null, android.graphics.Typeface.ITALIC);
                    remarksView.setPadding(0, 0, 0, hasAttachments ? 0 : padPx);
                    rowContainer.addView(remarksView);
                }
                
                if (hasAttachments) {
                    TextView attachView = new TextView(this);
                    attachView.setText("  \uD83D\uDCCE " + r.getAttachments().size() + " attached file(s)");
                    attachView.setTextColor(ThemeManager.getSecondaryAccentColor(MainActivity.this));
                    attachView.setTextSize(11f);
                    attachView.setPadding(0, hasRemarks ? (padPx / 2) : 0, 0, padPx);
                    rowContainer.addView(attachView);
                }
                
                selectedItemsList.addView(rowContainer);
            } else {
                selectedItemsList.addView(rowView);
            }

            // Add a thin divider between items (except last)
            if (selectedRecords.indexOf(r) < selectedRecords.size() - 1) {
                View divider = new View(this);
                divider.setBackgroundColor(ThemeManager.getBorderColor(MainActivity.this));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(lp);
                selectedItemsList.addView(divider);
            }
        }

        ResponsiveUI.applyResponsiveness(dialogView);

        ResponsiveUI.setupClickable(btnCancel, dialog::dismiss);
        ResponsiveUI.setupClickable(btnDelete, () -> {
            dialog.dismiss();
            // Deselect and adjust editingRecordIndex before removal
            for (Record r : selectedRecords) {
                int idx = getActiveRecords().indexOf(r);
                if (idx != -1) {
                    if (editingRecordIndex == idx) {
                        cancelEditRecordMode();
                    } else if (editingRecordIndex > idx) {
                        editingRecordIndex--;
                    }
                }
            }
            getActiveRecords().removeAll(selectedRecords);
            populateRecordsList();
            updateBulkActionsState();
            updateHeaderLabels();
        });

        dialog.show();
    }


    


    


    @android.annotation.SuppressLint("SetTextI18n")
    void showDeleteAccountConfirmationDialog(final Account account) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_delete_account_dialog, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        TextView tvAccountTitle = dialogView.findViewById(R.id.dialog_account_title);
        TextView tvItemsCount = dialogView.findViewById(R.id.dialog_account_items_count);
        TextView tvAmount = dialogView.findViewById(R.id.dialog_account_amount);
        TextView tvDate = dialogView.findViewById(R.id.dialog_account_date);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnDelete = dialogView.findViewById(R.id.btn_dialog_delete);

        // Populate account details
        tvAccountTitle.setText(account.getTitle());
        tvItemsCount.setText(String.valueOf(account.getRecords().size()));
        tvAmount.setText(String.format(Locale.getDefault(), "%.2f", account.calculateTotal()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String accountDateFormatted = sdf.format(new Date(account.getLastModified()));
        tvDate.setText(accountDateFormatted + " (" + AppUtils.formatDateCompact(accountDateFormatted) + ")");

        // Apply premium styling
        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgSecondaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.5f,
                12f
        ));

        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgPrimaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                6f
        ));

        btnCancel.setBackground(ResponsiveUI.createRippleRoundedBg(
                this,
                ThemeManager.getBgPrimaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                4f
        ));

        btnDelete.setBackground(ResponsiveUI.createRippleRoundedBg(
                this,
                getColor(R.color.error_red),
                getColor(R.color.error_red),
                0f,
                4f
        ));

        ResponsiveUI.applyResponsiveness(dialogView);

        ResponsiveUI.setupClickable(btnCancel, true, dialog::dismiss);
        ResponsiveUI.setupClickable(btnDelete, true, () -> {
            dialog.dismiss();
            if (currentViewGroup != null) {
                currentViewGroup.getAccounts().remove(account);
            } else {
                appStorage.standaloneAccounts.remove(account);
            }
            StorageHelper.saveAppStorage(MainActivity.this, appStorage);
            refreshDashboardList();
        });

        dialog.show();
    }

    @android.annotation.SuppressLint("SetTextI18n")
    private void enterEditRecordMode(int index, Record record) {
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
    private void cancelEditRecordMode() {
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


    

    /**
     * Shows a Snackbar with an Undo action. 
     * @param message The text to display.
     * @param onUndo Action to perform if Undo is clicked.
     * @param onCommit Action to perform if the Snackbar is dismissed without undo.
     */
    @SuppressWarnings("SameParameterValue")
    private void showUndoSnackbar(String message, final Runnable onUndo, final Runnable onCommit) {
        if (currentSnackbar != null) {
            currentSnackbar.dismiss();
            currentSnackbar = null;
        }
        
        View targetView = findViewById(android.R.id.content);
        if (targetView == null) targetView = mainContainer;
        
        com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(targetView, message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
        snackbar.setDuration(5000); // 5 seconds
        
        snackbar.setAction("UNDO", v -> {
            if (onUndo != null) onUndo.run();
        });
        
        snackbar.setActionTextColor(getColor(R.color.error_red)); 
        snackbar.setTextColor(getColor(R.color.text_primary));
        snackbar.setBackgroundTint(ThemeManager.getBgTertiaryColor(MainActivity.this));
        
        View sbView = snackbar.getView();
        sbView.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgTertiaryColor(MainActivity.this), 0, 0, 8f));
        
        snackbar.addCallback(new com.google.android.material.snackbar.Snackbar.Callback() {
            @Override
            public void onDismissed(com.google.android.material.snackbar.Snackbar transientBottomBar, int event) {
                if (currentSnackbar == transientBottomBar) {
                    currentSnackbar = null;
                }
                if (event != DISMISS_EVENT_ACTION) {
                    if (onCommit != null) onCommit.run();
                }
            }
        });
        
        currentSnackbar = snackbar;
        snackbar.show();
    }


    private android.app.Dialog showProgressDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setPadding(60, 60, 60, 60);
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        layout.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgSecondaryColor(this), ThemeManager.getBorderColor(this), 1.0f, 16.0f));

        android.widget.ProgressBar progressBar = new android.widget.ProgressBar(this);
        progressBar.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(ThemeManager.getPrimaryAccentColor(this)));

        android.widget.TextView tvMessage = new android.widget.TextView(this);
        tvMessage.setText(getString(R.string.msg_generating_pdf));
        tvMessage.setTextColor(getColor(R.color.text_primary));
        tvMessage.setTextSize(16f);
        tvMessage.setPadding(40, 0, 0, 0);

        layout.addView(progressBar);
        layout.addView(tvMessage);

        dialog.setContentView(layout);
        dialog.show();

        return dialog;
    }

    void generateAndOpenAllPdf() {
        android.app.Dialog progressDialog = showProgressDialog();
        
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
        android.app.Dialog progressDialog = showProgressDialog();
        
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
        android.app.Dialog progressDialog = showProgressDialog();
        
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
    

    private void showCreateGroupDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_create_group, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        EditText input = dialogView.findViewById(R.id.edit_group_name);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnApply = dialogView.findViewById(R.id.btn_dialog_apply);

        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgSecondaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.5f, 12f));
        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.0f, 6f));
        btnCancel.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, Color.parseColor("#20EF4444"), 4.0f));
        btnCancel.setTextColor(getColor(R.color.error_red));
        btnApply.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, ThemeManager.getPrimaryAccentColor(MainActivity.this), 4.0f));
        btnApply.setTextColor(getColor(R.color.text_primary));

        ResponsiveUI.setupClickable(btnCancel, false, dialog::cancel);
        ResponsiveUI.setupClickable(btnApply, false, () -> {
            String title = input.getText().toString().trim();
            if (!title.isEmpty()) {
                AccountGroup group = new AccountGroup(title);
                appStorage.groups.add(group);
                StorageHelper.saveAppStorage(this, appStorage);
                refreshDashboardList();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    void showDeleteGroupConfirmation(AccountGroup group) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_confirm_delete_group, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        TextView tvDetails = dialogView.findViewById(R.id.text_group_details);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnDelete = dialogView.findViewById(R.id.btn_dialog_delete);

        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgSecondaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.5f, 12f));
        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.0f, 6f));
        btnCancel.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, Color.parseColor("#20EF4444"), 4.0f));
        btnCancel.setTextColor(getColor(R.color.text_primary));
        btnDelete.setBackground(ResponsiveUI.createButtonSelector(MainActivity.this, Color.parseColor("#20EF4444"), 4.0f));
        btnDelete.setTextColor(getColor(R.color.error_red));

        StringBuilder details = new StringBuilder();
        int listCount = group.getAccounts().size();
        details.append("This group contains ").append(listCount).append(listCount == 1 ? " list" : " lists").append(".");
        if (listCount > 0) {
            details.append("\n\nLists:");
            for (Account acc : group.getAccounts()) {
                details.append("\n• ").append(acc.getTitle());
            }
        }
        tvDetails.setText(details.toString());

        ResponsiveUI.setupClickable(btnCancel, false, dialog::cancel);
        ResponsiveUI.setupClickable(btnDelete, false, () -> {
            appStorage.groups.remove(group);
            StorageHelper.saveAppStorage(this, appStorage);
            refreshDashboardList();
            dialog.dismiss();
        });

        dialog.show();
    }


    List<Record> getActiveRecords() {
        return isBudgetMode ? tempBudgetRecords : tempRecords;
    }

    


    


    

    


    

    void generateAndOpenSelectedPdf(java.util.List<Record> selectedRecords, PdfSortOrder sortOrder) {
        if (selectedRecords.isEmpty()) return;
        
        android.app.Dialog progressDialog = showProgressDialog();
        
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
