package com.example.notecalc;

import android.app.DatePickerDialog;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.Calendar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private java.util.List<String> tempAttachments = new java.util.ArrayList<>();
    private static final int REQUEST_CODE_ATTACH = 1001;
    private static final int REQUEST_CODE_CAMERA = 1002;
    private String currentPhotoPath = null;
    private android.widget.LinearLayout attachmentsContainer;
    private android.widget.HorizontalScrollView attachmentsScroll;
    private android.widget.TextView btnAttachFile;

    public enum PdfSortOrder {
        SNO, DESCRIPTION, DATE, AMOUNT
    }

    public interface PdfSortCallback {
        void onSortSelected(PdfSortOrder order);
    }
    
    private void showPdfSortDialog(PdfSortCallback callback) {
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
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnExport.setOnClickListener(v -> {
            dialog.dismiss();
            callback.onSortSelected(selectedOrder[0]);
        });

        android.graphics.drawable.StateListDrawable cancelSelector = new android.graphics.drawable.StateListDrawable();
        cancelSelector.addState(new int[]{android.R.attr.state_pressed}, ResponsiveUI.createRoundedBg(this, ThemeManager.getBorderColor(this), ThemeManager.getBorderColor(this), 1.0f, 8.0f));
        cancelSelector.addState(new int[]{}, ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(this), ThemeManager.getBorderColor(this), 1.0f, 8.0f));
        btnCancel.setBackground(cancelSelector);

        android.graphics.drawable.StateListDrawable exportSelector = new android.graphics.drawable.StateListDrawable();
        exportSelector.addState(new int[]{android.R.attr.state_pressed}, ResponsiveUI.createRoundedBg(this, ThemeManager.getPrimaryAccentColor(this), ThemeManager.getPrimaryAccentColor(this), 1.0f, 8.0f));
        exportSelector.addState(new int[]{}, ResponsiveUI.createRoundedBg(this, ThemeManager.getSecondaryAccentColor(this), ThemeManager.getSecondaryAccentColor(this), 1.0f, 8.0f));
        btnExport.setBackground(exportSelector);

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    
    private androidx.activity.result.ActivityResultLauncher<android.content.Intent> exportJsonLauncher;
    private androidx.activity.result.ActivityResultLauncher<android.content.Intent> importJsonLauncher;

    private FrameLayout mainContainer;
    private AppStorage appStorage;
    private AccountGroup currentViewGroup = null; // null means we are in the Dashboard
    private Account currentEditingAccount;
    
    // Editor state
    private List<Record> tempRecords;
    private List<Record> tempBudgetRecords;
    private boolean isBudgetMode = false; // false = Expenses, true = Budget
    
    private String originalTitle = "";
    private String selectedRecordDate = "";

    private int editingRecordIndex = -1;
    private EditText editDescField;
    private EditText editAmountField;
    private TextView btnRecordDateField;
    private TextView btnAddRecordField;
    private TextView btnCancelEditField;
    private TextView labelAddRecordField;
        private RecordsAdapter recordsAdapter;
    private AccountsAdapter accountsAdapter;
    private AccountsAdapter groupsAdapter;
    private String dashboardSearchQuery = "";
    private boolean groupSortAscending = true;
    private TextView btnSortTitle;
    private TextView btnSortTotal;
    private TextView btnSortLatest;
    private TextView textTotalValField;
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

    // Editor record search query (persists while in editor, reset on openEditor)
    private String currentRecordSearchQuery = "";

    // Fields for collapsible form, remarks, empty state, and bulk delete
    private EditText editRemarksField;
    private android.widget.AutoCompleteTextView editCategoryField;
    private View formInputsContainer;
    private TextView btnToggleForm;
    private CheckBox cbSelectAllHeader;
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

    private String getFilterDateFrom() { return isBudgetMode ? budgetFilterDateFrom : expenseFilterDateFrom; }
    private void setFilterDateFrom(String val) { if (isBudgetMode) budgetFilterDateFrom = val; else expenseFilterDateFrom = val; }
    private String getFilterDateTo() { return isBudgetMode ? budgetFilterDateTo : expenseFilterDateTo; }
    private void setFilterDateTo(String val) { if (isBudgetMode) budgetFilterDateTo = val; else expenseFilterDateTo = val; }
    private Double getFilterAmountFrom() { return isBudgetMode ? budgetFilterAmountFrom : expenseFilterAmountFrom; }
    private void setFilterAmountFrom(Double val) { if (isBudgetMode) budgetFilterAmountFrom = val; else expenseFilterAmountFrom = val; }
    private Double getFilterAmountTo() { return isBudgetMode ? budgetFilterAmountTo : expenseFilterAmountTo; }
    private void setFilterAmountTo(Double val) { if (isBudgetMode) budgetFilterAmountTo = val; else expenseFilterAmountTo = val; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
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
                        .setTitle("Restore Backup")
                        .setMessage("Are you sure? This will completely overwrite your current data.")
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

        // Open the dashboard screen
        showDashboard();
    }

    /**
     * Renders the Dashboard screen containing the list of saved accounts.
     */
    
    
    @android.annotation.SuppressLint("SetTextI18n")
    private void showCategoryFilterDialog(Account account, ImageView btnFilterIcon) {
        java.util.Set<String> uniqueCats = new java.util.HashSet<>();
        for (Record r : account.getRecords()) {
            if (r.getCategory() != null && !r.getCategory().isEmpty()) {
                uniqueCats.add(r.getCategory());
            }
        }
        if (uniqueCats.isEmpty()) {
            Toast.makeText(this, "No categories available to filter.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> catList = new ArrayList<>(uniqueCats);
        java.util.Collections.sort(catList);
        
        boolean[] checkedItems = new boolean[catList.size()];
        for (int i = 0; i < catList.size(); i++) {
            if (recordsAdapter != null && recordsAdapter.filterCategories.contains(catList.get(i))) {
                checkedItems[i] = true;
            }
        }

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(20 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);
        
        TextView title = new TextView(this);
        title.setText("Filter by Category");
        title.setTextSize(20);
        title.setTextColor(getColor(R.color.text_primary));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, pad);
        root.addView(title);
        
        android.widget.ListView listView = new android.widget.ListView(this);
        listView.setDividerHeight(0);
        
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, catList) {
            @androidx.annotation.NonNull
            @Override
            public android.view.View getView(int position, @androidx.annotation.Nullable android.view.View convertView, @androidx.annotation.NonNull android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                if (view instanceof android.widget.CheckedTextView) {
                    android.widget.CheckedTextView ctv = (android.widget.CheckedTextView) view;
                    ctv.setTextColor(getColor(R.color.text_primary));
                    ctv.setCheckMarkTintList(android.content.res.ColorStateList.valueOf(ThemeManager.getPrimaryAccentColor(MainActivity.this)));
                    int ipads = (int)(12 * getResources().getDisplayMetrics().density);
                    ctv.setPadding(ipads, ipads, ipads, ipads);
                }
                view.setBackgroundColor(ThemeManager.getBgPrimaryColor(MainActivity.this));
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setChoiceMode(android.widget.ListView.CHOICE_MODE_MULTIPLE);
        for (int i = 0; i < checkedItems.length; i++) {
            listView.setItemChecked(i, checkedItems[i]);
        }
        
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        root.addView(listView, listParams);
        
        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.setGravity(android.view.Gravity.END);
        btnLayout.setPadding(0, pad, 0, 0);
        
        android.widget.Button btnClear = new android.widget.Button(this);
        btnClear.setText("CLEAR ALL");
        btnClear.setTextColor(getColor(R.color.text_tertiary));
        btnClear.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        
        android.widget.Button btnApply = new android.widget.Button(this);
        btnApply.setText("APPLY");
        btnApply.setTextColor(ThemeManager.getPrimaryAccentColor(this));
        btnApply.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        
        btnLayout.addView(btnClear);
        btnLayout.addView(btnApply);
        root.addView(btnLayout);
        
        dialog.setContentView(root);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(this), ThemeManager.getBorderColor(this), 1.0f, 16.0f));
            // Set max height if needed
            dialog.getWindow().setLayout((int)(300 * getResources().getDisplayMetrics().density), android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        
        btnApply.setOnClickListener(v -> {
            java.util.Set<String> selected = new java.util.HashSet<>();
            android.util.SparseBooleanArray checked = listView.getCheckedItemPositions();
            for (int i = 0; i < catList.size(); i++) {
                if (checked.get(i)) selected.add(catList.get(i));
            }
            if (recordsAdapter != null) {
                recordsAdapter.setFilterCategories(selected);
            }
            if (selected.isEmpty()) {
                btnFilterIcon.setColorFilter(ThemeManager.getSecondaryAccentColor(this));
            } else {
                btnFilterIcon.setColorFilter(ThemeManager.getPrimaryAccentColor(this));
            }
            dialog.dismiss();
        });
        
        btnClear.setOnClickListener(v -> {
            if (recordsAdapter != null) {
                recordsAdapter.setFilterCategories(new java.util.HashSet<>());
            }
            btnFilterIcon.setColorFilter(ThemeManager.getSecondaryAccentColor(this));
            dialog.dismiss();
        });
        
        dialog.show();
    }

    
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
        title.setText("NC Agent");
        title.setTextSize(20);
        title.setTextColor(getColor(R.color.text_primary));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, pad);
        sheet.addView(title);
        
        EditText input = new EditText(this);
        input.setHint("e.g. Bought 2 coffees for 50");
        input.setTextColor(getColor(R.color.text_primary));
        input.setHintTextColor(getColor(R.color.text_secondary));
        input.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgSecondaryColor(this), ThemeManager.getBorderColor(this), 1.0f, 8f));
        input.setPadding(pad, pad, pad, pad);
        input.setLines(4);
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        sheet.addView(input);
        
        android.widget.Button btnAnalyze = new android.widget.Button(this);
        btnAnalyze.setText("Analyze");
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
        btnCancel.setText("Cancel");
        btnCancel.setTextColor(getColor(R.color.text_primary));
        btnCancel.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        
        android.widget.Button btnConfirm = new android.widget.Button(this);
        btnConfirm.setText("Confirm");
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

    @android.annotation.SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void showDashboard() {
        if (currentSnackbar != null) {
            currentSnackbar.dismiss();
            currentSnackbar = null;
        }
                LayoutInflater inflater = getLayoutInflater();
                View dashboardView = inflater.inflate(R.layout.layout_dashboard, mainContainer, false);

        // Find views
        View btnCreateAccount = dashboardView.findViewById(R.id.btn_create_account);
        
        View btnSettings = dashboardView.findViewById(R.id.btn_settings);
        if(btnSettings != null) btnSettings.setOnClickListener(v -> openSettings());
        
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
            setupClickable(btnSortGroupTitle, false, () -> {
                groupSortAscending = !groupSortAscending;
                updateDashboardSortUI();
                refreshDashboardList();
            });
        }

        TextView btnSortTitle = dashboardView.findViewById(R.id.btn_sort_title);
        TextView btnSortTotal = dashboardView.findViewById(R.id.btn_sort_total);
        TextView btnSortLatest = dashboardView.findViewById(R.id.btn_sort_latest);

        if (btnSortTitle != null) setupClickable(btnSortTitle, false, () -> {
            if (getDashboardSortColumn() == 0) setDashboardSortAscending(!getDashboardSortAscending());
            else { setDashboardSortColumn(0); setDashboardSortAscending(true); }
            StorageHelper.saveAppStorage(this, appStorage);
            updateDashboardSortUI();
            refreshDashboardList();
        });
        if (btnSortTotal != null) setupClickable(btnSortTotal, false, () -> {
            if (getDashboardSortColumn() == 1) setDashboardSortAscending(!getDashboardSortAscending());
            else { setDashboardSortColumn(1); setDashboardSortAscending(false); }
            StorageHelper.saveAppStorage(this, appStorage);
            updateDashboardSortUI();
            refreshDashboardList();
        });
        if (btnSortLatest != null) setupClickable(btnSortLatest, false, () -> {
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
        setupClickable(btnCreateAccount, () -> openEditor(null));
        if (btnCreateGroup != null) {
            setupClickable(btnCreateGroup, this::showCreateGroupDialog);
        }
        setupClickable(cardEmptyState, () -> {
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
                setupClickable(btnDashboardBack, false, () -> {
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
            btnSortGroupTitle.setText("Title " + (groupSortAscending ? "▲" : "▼"));
        }
    }

    private void refreshDashboardList() {
        if (accountsAdapter == null) return;
        
        List<Object> combinedGroups = new ArrayList<>();
        List<Object> combinedAccounts = new ArrayList<>();

        if (currentViewGroup != null) {
            combinedAccounts.addAll(applyDashboardSort(currentViewGroup.getAccounts()));
        } else {
            List<AccountGroup> sortedGroups = new ArrayList<>(appStorage.groups);
            sortedGroups.sort((a, b) -> {
                if (a.isPinned() != b.isPinned()) return a.isPinned() ? -1 : 1;
                int titleCompare = a.getTitle().compareToIgnoreCase(b.getTitle());
                return groupSortAscending ? titleCompare : -titleCompare;
            });
            combinedGroups.addAll(sortedGroups);
            combinedAccounts.addAll(applyDashboardSort(appStorage.standaloneAccounts));
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

        if (!hasGroups && !hasAccounts) {
            cardEmptyState.setVisibility(View.VISIBLE);
            contentContainer.setVisibility(View.GONE);
            editDashboardSearch.setVisibility(View.GONE);
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
        
        int mode = currentViewGroup != null ? currentViewGroup.getSortMode() : dashboardSortMode;
        boolean asc = currentViewGroup != null ? currentViewGroup.isSortAscending() : dashboardSortAscending;
        
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
        return currentViewGroup != null ? currentViewGroup.getSortMode() : dashboardSortMode;
    }
    private void setDashboardSortColumn(int mode) {
        if (currentViewGroup != null) currentViewGroup.setSortMode(mode);
        else dashboardSortMode = mode;
    }
    private boolean getDashboardSortAscending() {
        return currentViewGroup != null ? currentViewGroup.isSortAscending() : dashboardSortAscending;
    }
    private void setDashboardSortAscending(boolean asc) {
        if (currentViewGroup != null) currentViewGroup.setSortAscending(asc);
        else dashboardSortAscending = asc;
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
        selectedRecordDate = getCurrentDateString();
        editingRecordIndex = -1;

        // Find views
        ImageView btnBack = editorView.findViewById(R.id.btn_back);
        ImageView btnAnalytics = editorView.findViewById(R.id.btn_analytics);
        if (btnAnalytics != null) {
            setupClickable(btnAnalytics, true, () -> {
                if (currentEditingAccount == null || (currentEditingAccount.getRecords().isEmpty() && currentEditingAccount.getBudgetRecords().isEmpty())) {
                    android.widget.Toast.makeText(this, "Add some records to view analytics", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    showAnalytics(currentEditingAccount);
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
            setupClickable(btnAttachFile, true, () -> {
                if (tempAttachments.size() >= 3) {
                    Toast.makeText(this, "Max 3 files allowed", Toast.LENGTH_SHORT).show();
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
                
                setupClickable(btnTakePhoto, false, () -> {
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
                
                setupClickable(btnChooseFile, false, () -> {
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
        }

        // Setup Swipe-to-Delete and Drag-and-Drop for Records
        androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback recordSwipeCallback = new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            private boolean isDragActive = false;

            @Override
            public int getDragDirs(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder) {
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
        View formHeader = editorView.findViewById(R.id.form_header);
        Runnable toggleForm = () -> {
            isFormInputsCollapsed = !isFormInputsCollapsed;
            formInputsContainer.setVisibility(isFormInputsCollapsed ? View.GONE : View.VISIBLE);
            btnToggleForm.setText(isFormInputsCollapsed ? "Expand [ + ]" : "Minimize [ - ]");
        };
        setupClickable(btnToggleForm, false, toggleForm);
        
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
            btnToggleForm.setText("Minimize [ - ]");
        } else {
            isFormInputsCollapsed = true;
            formInputsContainer.setVisibility(View.GONE);
            btnToggleForm.setText("Expand [ + ]");
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
            btnBulkActionsMenu.setBackground(createButtonSelector(Color.parseColor("#15FFFFFF"), 4.0f));
            setupClickable(btnBulkActionsMenu, true, () -> showBulkActionsMenu(btnBulkActionsMenu));
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
        thSnoField.setBackground(createButtonSelector(Color.parseColor("#15FFFFFF"), 4.0f));
        thDescField.setBackground(createButtonSelector(Color.parseColor("#15FFFFFF"), 4.0f));
        thDateField.setBackground(createButtonSelector(Color.parseColor("#15FFFFFF"), 4.0f));
        thAmountField.setBackground(createButtonSelector(Color.parseColor("#15FFFFFF"), 4.0f));

        setupClickable(thSnoField, false, () -> onHeaderClicked(0));
        setupClickable(thDescField, false, () -> onHeaderClicked(1));

        // Date header: click = sort, long-press (1s) = date range filter
        thDateField.setOnClickListener(v -> onHeaderClicked(2));
        thDateField.setOnLongClickListener(v -> {
            showDateRangeFilterDialog();
            return true;
        });

        // Amount header: click = sort, long-press (1s) = amount range filter
        thAmountField.setOnClickListener(v -> onHeaderClicked(3));
        thAmountField.setOnLongClickListener(v -> {
            showAmountRangeFilterDialog();
            return true;
        });

        updateHeaderLabels();

        // Pre-populate if editing existing account
        if (account != null) {
            
            editTitle.setText(account.getTitle());
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
        
        if (btnModeExpenses != null) setupClickable(btnModeExpenses, false, () -> {
            if (isBudgetMode) {
                isBudgetMode = false;
                cancelEditRecordMode();
                updateModeToggleUI.run();
            }
        });
        if (btnModeBudget != null) setupClickable(btnModeBudget, false, () -> {
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
        setupClickable(btnBack, false, () -> {
            dashboardSearchQuery = "";
            if (tempRecords != null) for (Record r : tempRecords) r.setSelected(false);
            if (tempBudgetRecords != null) for (Record r : tempBudgetRecords) r.setSelected(false);
            showDashboard();
        });

        // Date picker action
        setupClickable(btnDate, () -> showDatePicker(btnDate));

        // Cancel edit action
        setupClickable(btnCancelEdit, this::cancelEditRecordMode);

        // Add/Update item action
        setupClickable(btnAdd, () -> {
            String desc = editDesc.getText().toString().trim();
            String amountStr = editAmount.getText().toString().trim();
            String remarks = editRemarksField.getText().toString().trim();
            String category = editCategoryField != null ? editCategoryField.getText().toString().trim() : "";

            if (desc.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter a description", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(MainActivity.this, "Amount must be positive", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Please enter a valid numeric amount", Toast.LENGTH_SHORT).show();
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
        setupClickable(btnSave, () -> {
            String title = editTitle.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(MainActivity.this, "List title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isDuplicateTitle(title)) {
                Toast.makeText(MainActivity.this, "A list with this title already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            // Re-sequentialize to close any gaps caused by deletions
            resequentializeRecords(tempRecords);
            resequentializeRecords(tempBudgetRecords);

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
        double total = 0;
        for (Record record : getActiveRecords()) {
            total += record.getAmount();
        }
        textTotalValField.setText(String.format(Locale.getDefault(), "%.2f", total));
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
    private class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RecordViewHolder> {

        private boolean isSelectionMode = false;

        public void setSelectionMode(boolean mode) {
            if (this.isSelectionMode != mode) {
                this.isSelectionMode = mode;
                notifyDataSetChanged();
            }
        }

        // Filtered view of tempRecords, rebuilt on every setFilter() call
        private final List<Record> displayRecords = new ArrayList<>();
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
            holder.tvDate.setText(formatDateCompact(record.getDate()));
            
            holder.tvDate.setOnClickListener(v -> {
                if (holder.isShowingDay) {
                    if (holder.revertDateTask != null) {
                        holder.tvDate.removeCallbacks(holder.revertDateTask);
                        holder.revertDateTask = null;
                    }
                    holder.isShowingDay = false;
                    holder.tvDate.setText(formatDateCompact(record.getDate()));
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
                                holder.tvDate.setText(formatDateCompact(record.getDate()));
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
                        holder.attachmentSummary.setText(icon + name + " \u25BE"); // ?
                    }
                    
                    holder.attachmentSummary.setVisibility(View.VISIBLE);
                    holder.attachmentsScroll.setVisibility(View.GONE);
                    
                    holder.attachmentsScroll.setOnTouchListener((v, event) -> {
                        int action = event.getActionMasked();
                        if (action == android.view.MotionEvent.ACTION_DOWN || action == android.view.MotionEvent.ACTION_MOVE) {
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                        } else if (action == android.view.MotionEvent.ACTION_UP || action == android.view.MotionEvent.ACTION_CANCEL) {
                            v.getParent().requestDisallowInterceptTouchEvent(false);
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
                        chip.setBackground(createButtonSelector(ThemeManager.getBgSecondaryColor(MainActivity.this), 6.0f));
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
                    
                    setupClickable(holder.attachmentSummary, false, () -> {
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

            setupClickable(holder.itemView, true, () -> enterEditRecordMode(trueIndex, record), () -> {
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

                accHolder.itemView.setBackground(createCardSelector());



                accHolder.btnPinAccount.setImageResource(account.isPinned() ? R.drawable.ic_pin_filled : R.drawable.ic_pin);
                accHolder.btnPinAccount.setImageTintList(createIconTintSelector(
                        account.isPinned() ? ThemeManager.getSecondaryAccentColor(MainActivity.this) : getColor(R.color.text_tertiary),
                        ThemeManager.getSecondaryAccentColor(MainActivity.this)
                ));
                accHolder.btnPinAccount.setBackground(createButtonSelector(Color.parseColor("#15FFFFFF"), 6.0f));

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

                setupClickable(accHolder.itemView, false, () -> {
                    currentViewGroup = accountParentGroup;
                    openEditor(account);
                }, () -> showAccountPopupMenu(accHolder.itemView, account));

                if (accHolder.btnMoveAccount != null) {
                    if (accountParentGroup != null) {
                        accHolder.btnMoveAccount.setImageResource(R.drawable.ic_move_folder_out);
                    } else {
                        accHolder.btnMoveAccount.setImageResource(R.drawable.ic_move_folder);
                    }
                    accHolder.btnMoveAccount.setBackground(createButtonSelector(Color.parseColor("#15FFFFFF"), 6.0f));
                    accHolder.btnMoveAccount.setImageTintList(createIconTintSelector(
                            getColor(R.color.text_tertiary),
                            ThemeManager.getSecondaryAccentColor(MainActivity.this)
                    ));
                    setupClickable(accHolder.btnMoveAccount, false, () -> {
                        if (accountParentGroup != null) {
                            // Move out of group (back to standalone)
                            accountParentGroup.getAccounts().remove(account);
                            appStorage.standaloneAccounts.add(account);
                            StorageHelper.saveAppStorage(MainActivity.this, appStorage);
                            refreshDashboardList();
                            Toast.makeText(MainActivity.this, "Moved to Dashboard", Toast.LENGTH_SHORT).show();
                        } else {
                            // Move into a group
                            if (appStorage.groups.isEmpty()) {
                                Toast.makeText(MainActivity.this, "No groups available. Create a group first.", Toast.LENGTH_SHORT).show();
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
                            btnCancel.setBackground(createButtonSelector(Color.parseColor("#20EF4444"), 4.0f));
                            btnCancel.setTextColor(getColor(R.color.error_red));
                            
                            for (int i = 0; i < appStorage.groups.size(); i++) {
                                final AccountGroup selectedGroup = appStorage.groups.get(i);
                                TextView tvGroup = new TextView(MainActivity.this);
                                tvGroup.setText(selectedGroup.getTitle());
                                tvGroup.setTextColor(getColor(R.color.text_primary));
                                tvGroup.setTextSize(16f);
                                tvGroup.setPadding(32, 24, 32, 24);
                                tvGroup.setBackground(createButtonSelector(Color.parseColor("#15FFFFFF"), 4.0f));
                                setupClickable(tvGroup, false, () -> {
                                    appStorage.standaloneAccounts.remove(account);
                                    selectedGroup.getAccounts().add(account);
                                    selectedGroup.updateLastModified();
                                    StorageHelper.saveAppStorage(MainActivity.this, appStorage);
                                    refreshDashboardList();
                                    Toast.makeText(MainActivity.this, "Moved to " + selectedGroup.getTitle(), Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                });
                                detailsContainer.addView(tvGroup);
                                
                                if (i < appStorage.groups.size() - 1) {
                                    View divider = new View(MainActivity.this);
                                    divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                                    divider.setBackgroundColor(ThemeManager.getBorderColor(MainActivity.this));
                                    detailsContainer.addView(divider);
                                }
                            }
                            
                            setupClickable(btnCancel, false, dialog::cancel);
                            dialog.show();
                        }
                    });
                }
                accHolder.btnPinAccount.setVisibility(View.VISIBLE);
                accHolder.btnPinAccount.setImageResource(R.drawable.ic_pin); // Use same icon, just tint different? Wait, maybe just keep icon.
                accHolder.btnPinAccount.setImageTintList(createIconTintSelector(
                        account.isPinned() ? ThemeManager.getPrimaryAccentColor(MainActivity.this) : getColor(R.color.text_tertiary),
                        ThemeManager.getSecondaryAccentColor(MainActivity.this)
                ));
                setupClickable(accHolder.btnPinAccount, false, () -> {
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
                
                grpHolder.itemView.setBackground(createCardSelector());
                
                grpHolder.btnDeleteGroup.setBackground(createButtonSelector(Color.parseColor("#20EF4444"), 6.0f));
                grpHolder.btnDeleteGroup.setImageTintList(createIconTintSelector(
                        getColor(R.color.error_red),
                        Color.parseColor("#FF6B6B")
                ));
                
                setupClickable(grpHolder.itemView, false, () -> {
                    currentViewGroup = group;
                    showDashboard(); // Refresh dashboard into group view
                });
                
                setupClickable(grpHolder.btnDeleteGroup, false, () -> showDeleteGroupConfirmation(group));
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
            recordsAdapter.notifyDataSetChanged();
            updateBulkActionsState();
        });
    }

    /**
     * Shows or hides the "Delete Selected" button based on whether any records are selected.
     */
    @android.annotation.SuppressLint("SetTextI18n")
    private void updateBulkActionsState() {
        if (btnBulkActionsMenu == null) return;
        boolean anySelected = false;
        double selectedTotal = 0.0;
        for (Record r : getActiveRecords()) {
            if (r.isSelected()) {
                anySelected = true;
                selectedTotal += r.getAmount();
            }
        }

        if (containerBulkActions != null) {
            containerBulkActions.setVisibility(View.VISIBLE);
        }

        if (textSelectedTotal != null) {
            textSelectedTotal.setVisibility(anySelected ? View.VISIBLE : View.GONE);
            if (anySelected) textSelectedTotal.setText(String.format(Locale.getDefault(), "Total: %.2f", selectedTotal));
        }

        if (cbSelectAllHeader != null) {
            cbSelectAllHeader.setVisibility(anySelected ? View.VISIBLE : View.GONE);
        }

        if (recordsAdapter != null) {
            recordsAdapter.setSelectionMode(anySelected);
        }
    }

    /**
     * Shows a confirmation dialog listing the selected records before performing bulk delete.
     */
    @android.annotation.SuppressLint("SetTextI18n")
    private void showDeleteMultipleConfirmationDialog(List<Record> selectedRecords) {
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
        btnCancel.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgPrimaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                4f
        ));
        btnDelete.setBackground(ResponsiveUI.createRoundedBg(
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
                    + "   " + formatDateCompact(r.getDate())
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
                    remarksView.setText("  \u21B3 " + remarks);
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

        setupClickable(btnCancel, dialog::dismiss);
        setupClickable(btnDelete, () -> {
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
        });

        dialog.show();
    }

    @android.annotation.SuppressLint({"SetTextI18n", "InflateParams"})
    private void showAccountPopupMenu(View anchor, Account account) {
        View popupView = getLayoutInflater().inflate(R.layout.layout_popup_menu, null);
        
        android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(
                popupView,
                (int) (180 * getResources().getDisplayMetrics().density),
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setElevation(8.0f);
        
        View btnDownload = popupView.findViewById(R.id.btn_popup_download);
        View btnDelete = popupView.findViewById(R.id.btn_popup_delete);
        
        setupClickable(btnDownload, false, () -> {
            popupWindow.dismiss();
            showPdfSortDialog(order -> generateAndOpenPdf(account, order));
        });
        
        setupClickable(btnDelete, false, () -> {
            popupWindow.dismiss();
            showDeleteAccountConfirmationDialog(account);
        });
        
        popupWindow.showAsDropDown(anchor, anchor.getWidth() / 2, -anchor.getHeight() / 2);
    }

    @android.annotation.SuppressLint("SetTextI18n")
    private void showDeleteAccountConfirmationDialog(final Account account) {
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
        tvDate.setText(accountDateFormatted + " (" + formatDateCompact(accountDateFormatted) + ")");

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

        btnCancel.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgPrimaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                4f
        ));

        btnDelete.setBackground(ResponsiveUI.createRoundedBg(
                this,
                getColor(R.color.error_red),
                0,
                0,
                4f
        ));

        ResponsiveUI.applyResponsiveness(dialogView);

        setupClickable(btnCancel, dialog::dismiss);
        setupClickable(btnDelete, () -> {
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
            btnToggleForm.setText("Minimize [ - ]");
        }

        if (isBudgetMode) {
            labelAddRecordField.setText("EDIT BUDGET");
            btnAddRecordField.setText("Edit Budget");
            editDescField.setHint("Description");
            editRemarksField.setHint("Remarks (optional)");
        } else {
            labelAddRecordField.setText(R.string.label_edit_record);
            btnAddRecordField.setText(R.string.btn_edit_record);
            editDescField.setHint(R.string.hint_record_desc);
            editRemarksField.setHint("Remarks (e.g. bought at DMart - optional)");
        }

        btnCancelEditField.setVisibility(View.VISIBLE);



        populateRecordsList();
    }

    @android.annotation.SuppressLint("SetTextI18n")
    private void cancelEditRecordMode() {
        editingRecordIndex = -1;
        selectedRecordDate = getCurrentDateString();

        editDescField.setText("");
        editAmountField.setText("");
        editRemarksField.setText("");
        btnRecordDateField.setText(selectedRecordDate);
        
        tempAttachments.clear();
        renderEditorAttachments();

        if (isBudgetMode) {
            labelAddRecordField.setText("ADD BUDGET");
            btnAddRecordField.setText("Add Budget");
            editDescField.setHint("Description");
            editRemarksField.setHint("Remarks (optional)");
        } else {
            labelAddRecordField.setText(R.string.label_add_record);
            btnAddRecordField.setText(R.string.btn_add_record);
            editDescField.setHint(R.string.hint_record_desc);
            editRemarksField.setHint("Remarks (e.g. bought at DMart - optional)");
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

    /**
     * Creates a StateListDrawable for the account item card that instantly switches background on press.
     * Fill color changes to border_color, border lights up in text_secondary (sky-blue).
     */
    private StateListDrawable createCardSelector() {
        Drawable normal = ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgSecondaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                8.0f
        );
        Drawable pressed = ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBorderColor(MainActivity.this), // Fill with slate steel-blue
                ThemeManager.getSecondaryAccentColor(MainActivity.this), // Sky-blue border
                1.5f,
                8.0f
        );
        StateListDrawable selector = new StateListDrawable();
        selector.addState(new int[]{android.R.attr.state_pressed}, pressed);
        selector.addState(new int[]{}, normal);
        return selector;
    }

    /**
     * Creates a StateListDrawable for option buttons with instant fill color changes on press.
     */
    private StateListDrawable createButtonSelector(int pressedColor, float cornerRadiusDp) {
        Drawable normal = ResponsiveUI.createRoundedBg(
                this,
                Color.TRANSPARENT,
                0,
                0,
                cornerRadiusDp
        );
        Drawable pressed = ResponsiveUI.createRoundedBg(
                this,
                pressedColor,
                0,
                0,
                cornerRadiusDp
        );
        StateListDrawable selector = new StateListDrawable();
        selector.addState(new int[]{android.R.attr.state_pressed}, pressed);
        selector.addState(new int[]{}, normal);
        return selector;
    }

    /**
     * Creates a ColorStateList for icon tinting based on press state.
     */
    private ColorStateList createIconTintSelector(int normalColor, int pressedColor) {
        int[][] states = new int[][] {
            new int[] { android.R.attr.state_pressed },
            new int[] {}
        };
        int[] colors = new int[] {
            pressedColor,
            normalColor
        };
        return new ColorStateList(states, colors);
    }

    /**
     * Custom premium touch listener that animates scaling and pressed states to give physical click feedback.
     */
    private void setupClickable(View view, final Runnable onClickAction) {
        setupClickable(view, true, onClickAction);
    }

    /**
     * Custom premium touch listener with optional scale animations.
     */
    private void setupClickable(View view, boolean useScaleAnimation, final Runnable onClickAction) {
        setupClickable(view, useScaleAnimation, onClickAction, null);
    }

    private void setupClickable(View view, boolean useScaleAnimation, final Runnable onClickAction, final Runnable onLongClickAction) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private boolean isInside = false;
            private boolean longPressExecuted = false;
            private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            private final Runnable longPressRunnable = () -> {
                    if (isInside && onLongClickAction != null) {
                        longPressExecuted = true;
                        onLongClickAction.run();
                        // Prevent regular click
                        isInside = false;
                        view.setPressed(false);
                        if (useScaleAnimation) {
                            view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(60).start();
                        }
                    }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isInside = true;
                        longPressExecuted = false;
                        if (useScaleAnimation) {
                            v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(60).start();
                        }
                        v.setPressed(true);
                        if (onLongClickAction != null) {
                            handler.postDelayed(longPressRunnable, 500); // 500 ms standard long press
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float x = event.getX();
                        float y = event.getY();
                        float slop = 40.0f; // generous slop to prevent accidental cancellation
                        boolean nowInside = (x >= -slop && x <= v.getWidth() + slop && y >= -slop && y <= v.getHeight() + slop);
                        if (nowInside != isInside) {
                            isInside = nowInside;
                            if (useScaleAnimation) {
                                float targetScale = isInside ? 0.96f : 1.0f;
                                v.animate().scaleX(targetScale).scaleY(targetScale).setDuration(60).start();
                            }
                            v.setPressed(isInside);
                            if (!isInside && onLongClickAction != null) {
                                handler.removeCallbacks(longPressRunnable);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (onLongClickAction != null) {
                            handler.removeCallbacks(longPressRunnable);
                        }
                        if (useScaleAnimation) {
                            v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(60).start();
                        }
                        v.setPressed(false);
                        if (isInside && !longPressExecuted) {
                            v.performClick();
                            if (onClickAction != null) {
                                onClickAction.run();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        if (onLongClickAction != null) {
                            handler.removeCallbacks(longPressRunnable);
                        }
                        if (useScaleAnimation) {
                            v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(60).start();
                        }
                        v.setPressed(false);
                        break;
                }
                return true;
            }
        });
    }

    /** Opens a dialog to set a date range filter on the records list. */
    private void showDateRangeFilterDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_date_range_dialog, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        TextView tvFrom = dialogView.findViewById(R.id.dialog_date_from);
        TextView tvTo = dialogView.findViewById(R.id.dialog_date_to);
        TextView btnClear = dialogView.findViewById(R.id.btn_dialog_clear);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnApply = dialogView.findViewById(R.id.btn_dialog_apply);

        // Style dialog
        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgSecondaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.5f, 12f));
        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.0f, 6f));
        tvFrom.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.0f, 4f));
        tvTo.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.0f, 4f));
        btnClear.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.0f, 4f));
        btnCancel.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.0f, 4f));
        btnApply.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getPrimaryAccentColor(MainActivity.this), 0, 0, 4f));
        btnApply.setTextColor(getColor(R.color.text_primary));

        // Track temp selections for this dialog session
        final String[] tempFrom = {getFilterDateFrom()};
        final String[] tempTo = {getFilterDateTo()};

        // Populate with current filter values if active
        tvFrom.setText(tempFrom[0] != null ? tempFrom[0] : "Select Date");
        tvTo.setText(tempTo[0] != null ? tempTo[0] : "Select Date");

        // Helper to pick a date and update a TextView
        Runnable pickFrom = () -> {
            Calendar cal = Calendar.getInstance();
            if (tempFrom[0] != null) {
                try {
                    java.util.Date parsed = new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(tempFrom[0]);
                    if (parsed != null) cal.setTime(parsed);
                } catch (Exception ignored) {}
            }
            new android.app.DatePickerDialog(this, (view, year, month, day) -> {
                String picked = String.format(Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year);
                tempFrom[0] = picked;
                tvFrom.setText(picked);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        };
        Runnable pickTo = () -> {
            Calendar cal = Calendar.getInstance();
            if (tempTo[0] != null) {
                try {
                    java.util.Date parsed = new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(tempTo[0]);
                    if (parsed != null) cal.setTime(parsed);
                } catch (Exception ignored) {}
            }
            new android.app.DatePickerDialog(this, (view, year, month, day) -> {
                String picked = String.format(Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year);
                tempTo[0] = picked;
                tvTo.setText(picked);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        };

        tvFrom.setOnClickListener(v -> pickFrom.run());
        tvTo.setOnClickListener(v -> pickTo.run());

        setupClickable(btnClear, () -> {
            setFilterDateFrom(null);
            setFilterDateTo(null);
            if (recordsAdapter != null) recordsAdapter.setFilter(currentRecordSearchQuery);
            updateDateHeaderIndicator();
            dialog.dismiss();
        });
        setupClickable(btnCancel, dialog::dismiss);
        setupClickable(btnApply, () -> {
            setFilterDateFrom(tempFrom[0]);
            setFilterDateTo(tempTo[0]);
            if (recordsAdapter != null) recordsAdapter.setFilter(currentRecordSearchQuery);
            updateDateHeaderIndicator();
            dialog.dismiss();
        });

        ResponsiveUI.applyResponsiveness(dialogView);
        dialog.show();
    }

    /** Visual indicator on Date column header when filter is active. */
    private void updateDateHeaderIndicator() {
        if (thDateField == null) return;
        boolean active = (getFilterDateFrom() != null || getFilterDateTo() != null);
        thDateField.setTextColor(active ? ThemeManager.getPrimaryAccentColor(MainActivity.this) : ThemeManager.getSecondaryAccentColor(MainActivity.this));
    }

    /** Visual indicator on Amount column header when filter is active. */
    private void updateAmountHeaderIndicator() {
        if (thAmountField == null) return;
        boolean active = (getFilterAmountFrom() != null || getFilterAmountTo() != null);
        thAmountField.setTextColor(active ? ThemeManager.getPrimaryAccentColor(MainActivity.this) : ThemeManager.getSecondaryAccentColor(MainActivity.this));
    }

    /** Opens a dialog to set an amount range filter on the records list. */
    private void showAmountRangeFilterDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_amount_range_dialog, null);
        builder.setView(dialogView);

        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        View detailsContainer = dialogView.findViewById(R.id.details_container);
        android.widget.EditText etFrom = dialogView.findViewById(R.id.dialog_amount_from);
        android.widget.EditText etTo = dialogView.findViewById(R.id.dialog_amount_to);
        TextView btnClear = dialogView.findViewById(R.id.btn_dialog_clear);
        TextView btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        TextView btnApply = dialogView.findViewById(R.id.btn_dialog_apply);

        // Style dialog
        dialogRoot.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgSecondaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.5f, 12f));
        detailsContainer.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.0f, 6f));
        etFrom.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.0f, 4f));
        etTo.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.0f, 4f));
        btnClear.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.0f, 4f));
        btnCancel.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(MainActivity.this), ThemeManager.getBorderColor(MainActivity.this), 1.0f, 4f));
        btnApply.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getPrimaryAccentColor(MainActivity.this), 0, 0, 4f));
        btnApply.setTextColor(getColor(R.color.text_primary));

        // Populate with current filter values if active
        if (getFilterAmountFrom() != null) etFrom.setText(String.format(Locale.getDefault(), "%.2f", getFilterAmountFrom()));
        if (getFilterAmountTo() != null) etTo.setText(String.format(Locale.getDefault(), "%.2f", getFilterAmountTo()));

        setupClickable(btnClear, () -> {
            setFilterAmountFrom(null);
            setFilterAmountTo(null);
            if (recordsAdapter != null) recordsAdapter.setFilter(currentRecordSearchQuery);
            updateAmountHeaderIndicator();
            dialog.dismiss();
        });
        setupClickable(btnCancel, dialog::dismiss);
        setupClickable(btnApply, () -> {
            String fromStr = etFrom.getText().toString().trim();
            String toStr = etTo.getText().toString().trim();
            setFilterAmountFrom(fromStr.isEmpty() ? null : Double.parseDouble(fromStr));
            setFilterAmountTo(toStr.isEmpty() ? null : Double.parseDouble(toStr));
            if (recordsAdapter != null) recordsAdapter.setFilter(currentRecordSearchQuery);
            updateAmountHeaderIndicator();
            dialog.dismiss();
        });

        ResponsiveUI.applyResponsiveness(dialogView);
        dialog.show();
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

    /**
     * Launches date picker dialog and updates state on selection.
     */
    private void showDatePicker(TextView dateTextWidget) {
        Calendar cal = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = sdf.parse(selectedRecordDate);
            if (date != null) {
                cal.setTime(date);
            }
        } catch (Exception ignored) {}

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view1, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    selectedRecordDate = sdf.format(selected.getTime());
                    dateTextWidget.setText(selectedRecordDate);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    private String getCurrentDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Generates a multi-page PDF document for the given account and opens it with an external viewer.
     * Supports word-wrapping for long titles and automatic pagination for all record rows.
     */
    
    private void generateAndOpenAllPdf() {
        android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
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
            android.widget.Toast.makeText(this, "No records found to export.", android.widget.Toast.LENGTH_SHORT).show();
            document.close();
            return;
        }

        try {
            java.io.File pdfDir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
            if (pdfDir == null) return;
            if (!pdfDir.exists()) pdfDir.mkdirs();
            java.io.File file = new java.io.File(pdfDir, "All_Accounts_Export.pdf");
            document.writeTo(new java.io.FileOutputStream(file));
            document.close();

            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", file);
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(android.content.Intent.createChooser(intent, "Open PDF with"));
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            document.close();
        }
    }

    private void generateAndOpenPdf(Account account, PdfSortOrder sortOrder) {
        android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
        int[] pageTracker = {0};
        appendAccountToPdf(document, account, pageTracker, sortOrder);
        try {
            java.io.File pdfDir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
            if (pdfDir == null) return;
            if (!pdfDir.exists()) pdfDir.mkdirs();
            java.io.File file = new java.io.File(pdfDir, account.getTitle().replaceAll("[\\\\/:*?\\\"<>|]", "_") + ".pdf");
            document.writeTo(new java.io.FileOutputStream(file));
            document.close();
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", file);
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(android.content.Intent.createChooser(intent, "Open PDF with"));
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            document.close();
        }
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
        java.util.Collections.sort(expRecords, (r1, r2) -> {
            if (sortOrder == PdfSortOrder.DESCRIPTION) return r1.getDescription().compareToIgnoreCase(r2.getDescription());
            if (sortOrder == PdfSortOrder.DATE) {
                try {
                    java.text.SimpleDateFormat sortSdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                    java.util.Date d1 = sortSdf.parse(r1.getDate());
                    java.util.Date d2 = sortSdf.parse(r2.getDate());
                    int c = d1.compareTo(d2);
                    if (c == 0) c = Long.compare(r1.getTimestampMillis(), r2.getTimestampMillis());
                    return c;
                } catch (Exception e) {
                    return r1.getDate().compareTo(r2.getDate());
                }
            }
            if (sortOrder == PdfSortOrder.AMOUNT) return Double.compare(r1.getAmount(), r2.getAmount());
            return Integer.compare(r1.getOriginalIndex(), r2.getOriginalIndex());
        });
        
        List<Record> budRecords = new ArrayList<>(account.getBudgetRecords());
        java.util.Collections.sort(budRecords, (r1, r2) -> {
            if (sortOrder == PdfSortOrder.DESCRIPTION) return r1.getDescription().compareToIgnoreCase(r2.getDescription());
            if (sortOrder == PdfSortOrder.DATE) {
                try {
                    java.text.SimpleDateFormat sortSdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                    java.util.Date d1 = sortSdf.parse(r1.getDate());
                    java.util.Date d2 = sortSdf.parse(r2.getDate());
                    int c = d1.compareTo(d2);
                    if (c == 0) c = Long.compare(r1.getTimestampMillis(), r2.getTimestampMillis());
                    return c;
                } catch (Exception e) {
                    return r1.getDate().compareTo(r2.getDate());
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
        Canvas canvas = null;
        PdfDocument.Page page = null;
        float y = 0;

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
        List<String> titleLines = wrapText(titleText, titlePaint, maxTitleWidth);
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
        float hx = margin;
        canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, tableHeaderBgPaint);
        canvas.drawText("S.No",        hx + 4,                       y + 15f, accentPaint);
        canvas.drawText("Description", hx + colSno + 4,              y + 15f, accentPaint);
        canvas.drawText("Date",        hx + colSno + colDesc + 4,    y + 15f, accentPaint);
        canvas.drawText("Time",        hx + colSno + colDesc + colDate + 4, y + 15f, accentPaint);
        float amountHeaderX = hx + colSno + colDesc + colDate + colTime + colAmount - accentPaint.measureText("Amount") - 4f;
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

            float rx = margin;
            canvas.drawText(String.valueOf(i + 1), rx + 4, y + 15f, cellMutedPaint);

            // Truncate long descriptions to fit column width
            String desc = rec.getDescription();
            while (desc.length() > 1 && cellPaint.measureText(desc) > colDesc - 8f) {
                desc = desc.substring(0, desc.length() - 1);
            }
            if (!desc.equals(rec.getDescription())) desc += "\u2026";
            canvas.drawText(desc, rx + colSno + 4, y + 15f, cellPaint);

            // Draw remarks/category below description if present
            float currentY = y + 27f;
            if (hasRemarks) {
                String truncRemarks = combinedNotes;
                while (truncRemarks.length() > 1 && cellMutedPaint.measureText(truncRemarks) > colDesc - 8f) {
                    truncRemarks = truncRemarks.substring(0, truncRemarks.length() - 1);
                }
                if (!truncRemarks.equals(combinedNotes)) truncRemarks += "\u2026";
                canvas.drawText(truncRemarks, rx + colSno + 4, currentY, cellMutedPaint);
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
                if (!truncFn.equals("\uD83D\uDCCE " + fn)) truncFn += "\u2026";
                canvas.drawText(truncFn, rx + colSno + 4, currentY, cellMutedPaint);
                currentY += 12f;
            }

            canvas.drawText(formatDateCompact(rec.getDate()), rx + colSno + colDesc + 4, y + 15f, cellMutedPaint);
            String timeStr = rec.getTimestampMillis() > 0 ? timeSdf.format(new Date(rec.getTimestampMillis())) : "-";
            canvas.drawText(timeStr, rx + colSno + colDesc + colDate + 4, y + 15f, cellMutedPaint);

            // Right-align amount
            String amtStr = String.format(Locale.getDefault(), "%.2f", rec.getAmount());
            float amtX = rx + colSno + colDesc + colDate + colTime + colAmount - cellPaint.measureText(amtStr) - 4f;
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
            
            if (y + 50f > bottomLimit) {
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
                    if (page != null) document.finishPage(page);
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
                            if (!truncFn.equals(fileName)) truncFn += "\u2026";
                            
                            float fnX = x + (colWidth - subPaint.measureText(truncFn)) / 2f;
                            canvas.drawText(truncFn, fnX, y + drawH + 15f, subPaint);
                            
                            if (drawH + 20f > rowMaxHeight) rowMaxHeight = drawH + 20f;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                y += rowMaxHeight + 15f;
            }
        }
        
        if (page != null) {
            canvas.drawText("Generated by NoteCalc  \u2022  Page " + pageNum, 40f, bottomLimit + 25f, subPaint);
            document.finishPage(page);
        }
        pageTracker[0] = pageNum;
    }

    private List<String> wrapText(String text, Paint paint, float maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        int start = 0;
        int length = text.length();

        while (start < length) {
            // Skip leading spaces for the current line
            while (start < length && text.charAt(start) == ' ') {
                start++;
            }
            if (start >= length) {
                break;
            }

            int count = paint.breakText(text, start, length, true, maxWidth, null);
            if (count <= 0) {
                count = 1;
            }

            if (start + count >= length) {
                // The rest of the string fits
                lines.add(text.substring(start));
                break;
            }

            int end = start + count;
            int lastSpace = text.lastIndexOf(' ', end);

            if (lastSpace > start) {
                // Break at the last space that fits
                lines.add(text.substring(start, lastSpace));
                start = lastSpace + 1;
            } else {
                // No space found, forced character break
                lines.add(text.substring(start, end));
                start = end;
            }
        }

        if (lines.isEmpty()) {
            lines.add("");
        }
        return lines;
    }

    /**
     * Converts a date string from dd-MM-yyyy to compact DDMonthNameYY format.
     * Example: "24-06-2026" -> "24Jun26"
     */
    private String formatDateCompact(String ddMMYYYY) {
        try {
            String[] parts = ddMMYYYY.split("-");
            if (parts.length != 3) return ddMMYYYY;
            String day = parts[0];
            int monthNum = Integer.parseInt(parts[1]);
            String year = parts[2];
            String yy = year.length() >= 2 ? year.substring(year.length() - 2) : year;
            String[] monthAbbr = {"Jan","Feb","Mar","Apr","May","Jun",
                                   "Jul","Aug","Sep","Oct","Nov","Dec"};
            if (monthNum < 1 || monthNum > 12) return ddMMYYYY;
            return day + monthAbbr[monthNum - 1] + yy;
        } catch (Exception e) {
            return ddMMYYYY;
        }
    }

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
        btnCancel.setBackground(createButtonSelector(Color.parseColor("#20EF4444"), 4.0f));
        btnCancel.setTextColor(getColor(R.color.error_red));
        btnApply.setBackground(createButtonSelector(ThemeManager.getPrimaryAccentColor(MainActivity.this), 4.0f));
        btnApply.setTextColor(getColor(R.color.text_primary));

        setupClickable(btnCancel, false, dialog::cancel);
        setupClickable(btnApply, false, () -> {
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

    private void showDeleteGroupConfirmation(AccountGroup group) {
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
        btnCancel.setBackground(createButtonSelector(Color.parseColor("#20EF4444"), 4.0f));
        btnCancel.setTextColor(getColor(R.color.text_primary));
        btnDelete.setBackground(createButtonSelector(Color.parseColor("#20EF4444"), 4.0f));
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

        setupClickable(btnCancel, false, dialog::cancel);
        setupClickable(btnDelete, false, () -> {
            appStorage.groups.remove(group);
            StorageHelper.saveAppStorage(this, appStorage);
            refreshDashboardList();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (currentEditingAccount != null || (mainContainer.getChildAt(0) != null && mainContainer.getChildAt(0).getId() != R.id.dashboard_root)) {
            if (tempRecords != null) for (Record r : tempRecords) r.setSelected(false);
            if (tempBudgetRecords != null) for (Record r : tempBudgetRecords) r.setSelected(false);
            currentEditingAccount = null;
            tempRecords = null;
            tempBudgetRecords = null;
            dashboardSearchQuery = "";
            showDashboard();
            return;
        } else if (currentViewGroup != null) {
            currentViewGroup = null;
            dashboardSearchQuery = "";
            showDashboard();
            return;
        }
        super.onBackPressed();
    }

    private List<Record> getActiveRecords() {
        return isBudgetMode ? tempBudgetRecords : tempRecords;
    }

    private void resequentializeRecords(List<Record> records) {
        if (records == null || records.isEmpty()) return;
        List<Record> copy = new ArrayList<>(records);
        java.util.Collections.sort(copy, new java.util.Comparator<Record>() {
            @Override
            public int compare(Record r1, Record r2) {
                return Integer.compare(r1.getOriginalIndex(), r2.getOriginalIndex());
            }
        });
        for (int i = 0; i < copy.size(); i++) {
            copy.get(i).setOriginalIndex(i);
        }
    }

    private android.view.View settingsView;

    private void initSettings() {
        settingsView = getLayoutInflater().inflate(R.layout.layout_settings, null);
        
        settingsView.findViewById(R.id.btn_settings_back).setOnClickListener(v -> closeSettings());
        
        android.widget.RadioGroup rgTheme = settingsView.findViewById(R.id.rg_theme_mode);
        int currentMode = ThemeManager.getDarkMode(this);
        if (currentMode == ThemeManager.MODE_DARK) rgTheme.check(R.id.rb_theme_dark);
        else if (currentMode == ThemeManager.MODE_LIGHT) rgTheme.check(R.id.rb_theme_light);
        else rgTheme.check(R.id.rb_theme_system);

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int newMode = ThemeManager.MODE_SYSTEM;
            if (checkedId == R.id.rb_theme_dark) newMode = ThemeManager.MODE_DARK;
            else if (checkedId == R.id.rb_theme_light) newMode = ThemeManager.MODE_LIGHT;
            ThemeManager.setDarkMode(this, newMode);
        });

        android.widget.LinearLayout llColors = settingsView.findViewById(R.id.ll_accent_colors);
        String[] colors = {ThemeManager.ACCENT_BLUE, ThemeManager.ACCENT_GREEN, ThemeManager.ACCENT_PURPLE, ThemeManager.ACCENT_YELLOW, ThemeManager.ACCENT_ORANGE, ThemeManager.ACCENT_PINK};
        String[] hexes = {"#0284C7", "#16A34A", "#9333EA", "#CA8A04", "#EA580C", "#DB2777"};
        String active = ThemeManager.getAccentColorName(this);

        for (int i=0; i<colors.length; i++) {
            final String cName = colors[i];
            android.view.View circle = new android.view.View(this);
            android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(100, 100);
            lp.setMargins(16, 16, 16, 16);
            circle.setLayoutParams(lp);
            
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            gd.setColor(android.graphics.Color.parseColor(hexes[i]));
            if (cName.equals(active)) {
                gd.setStroke(8, ThemeManager.getSecondaryAccentColor(MainActivity.this));
            }
            circle.setBackground(gd);
            circle.setOnClickListener(v -> {
                ThemeManager.setAccentColor(this, cName);
                recreate();
            });
            llColors.addView(circle);
        }

        settingsView.findViewById(R.id.btn_export_json).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(android.content.Intent.EXTRA_TITLE, "NoteCalc_Backup.json");
            exportJsonLauncher.launch(intent);
        });

        settingsView.findViewById(R.id.btn_import_json).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            importJsonLauncher.launch(intent);
        });

        settingsView.findViewById(R.id.btn_export_pdf_all).setOnClickListener(v -> {
            generateAndOpenAllPdf();
        });


    }

    private void openSettings() {
        if (settingsView == null) initSettings();
        mainContainer.removeAllViews();
        mainContainer.addView(settingsView);
    }

    private void closeSettings() {
        mainContainer.removeAllViews();
        showDashboard();
    }

    private void showAnalytics(Account account) {
        View analyticsRoot = getLayoutInflater().inflate(R.layout.layout_analytics, mainContainer, false);
        mainContainer.removeAllViews();
        mainContainer.addView(analyticsRoot);

        android.widget.ImageButton btnBack = analyticsRoot.findViewById(R.id.btn_analytics_back);
        setupClickable(btnBack, true, () -> openEditor(account));

        TextView tvTitle = analyticsRoot.findViewById(R.id.tv_analytics_title);
        tvTitle.setText(account.getTitle() + " Analytics");

        TextView tvTotalSpent = analyticsRoot.findViewById(R.id.tv_total_spent);
        TextView tvHighestTxn = analyticsRoot.findViewById(R.id.tv_highest_txn);
        TextView tvDailyAvg = analyticsRoot.findViewById(R.id.tv_daily_avg);
        TextView tvHighestDay = analyticsRoot.findViewById(R.id.tv_highest_day);
        TextView tvBudgetPercent = analyticsRoot.findViewById(R.id.tv_budget_percent);
        TextView tvDateRange = analyticsRoot.findViewById(R.id.tv_date_range);

        Spinner spinnerTimeframe = analyticsRoot.findViewById(R.id.spinner_timeframe);
        String[] options = {"All Time", "Last 7 Days", "Last 30 Days"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeframe.setAdapter(adapter);

        BarChart chart = analyticsRoot.findViewById(R.id.chart_spending);
        setupChartAppearance(chart);

        spinnerTimeframe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateAnalyticsData(account, position, chart, tvTotalSpent, tvHighestTxn, tvDailyAvg, tvHighestDay, tvBudgetPercent, tvDateRange);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Initial load
        updateAnalyticsData(account, 0, chart, tvTotalSpent, tvHighestTxn, tvDailyAvg, tvHighestDay, tvBudgetPercent, tvDateRange);
    }

    private void setupChartAppearance(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);
        
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ThemeManager.getSecondaryAccentColor(this));
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(ThemeManager.getSecondaryAccentColor(this));
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(ThemeManager.getBorderColor(this));
        leftAxis.setAxisMinimum(0f);

        chart.getAxisRight().setEnabled(false);
    }

    private void updateAnalyticsData(Account account, int timeMode, BarChart chart, TextView tvTotal, TextView tvHighTxn, TextView tvDailyAvg, TextView tvHighDay, TextView tvBudgetPct, TextView tvDateRange) {
        List<Record> allRecords = account.getRecords();
        List<Record> filtered = new ArrayList<>();
        
        long now = System.currentTimeMillis();
        long startTime = 0;
        
        if (timeMode == 1) { // Last 7 Days
            startTime = now - (7L * 24 * 60 * 60 * 1000);
            tvDateRange.setText("Last 7 Days");
        } else if (timeMode == 2) { // Last 30 Days
            startTime = now - (30L * 24 * 60 * 60 * 1000);
            tvDateRange.setText("Last 30 Days");
        } else {
            tvDateRange.setText("All Time");
        }

        double totalAmount = 0;
        double highestTxn = 0;
        
        java.util.TreeMap<Long, Double> dailyTotals = new java.util.TreeMap<>();

        for (Record r : allRecords) {
            long rTs = 0;
            try {
                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                rTs = format.parse(r.getDate()).getTime();
            } catch(Exception ex) {}

            if (rTs >= startTime) {
                filtered.add(r);
                double amt = r.getAmount();
                totalAmount += amt;
                if (amt > highestTxn) highestTxn = amt;
                
                Calendar c = Calendar.getInstance();
                if (rTs > 0) {
                    c.setTimeInMillis(rTs);
                } else {
                    c.setTimeInMillis(System.currentTimeMillis());
                }
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                long dayStart = c.getTimeInMillis();
                
                dailyTotals.put(dayStart, dailyTotals.getOrDefault(dayStart, 0.0) + amt);
            }
        }

        long highDayTs = 0;
        double highDayAmt = 0;
        for (java.util.Map.Entry<Long, Double> entry : dailyTotals.entrySet()) {
            if (entry.getValue() > highDayAmt) {
                highDayAmt = entry.getValue();
                highDayTs = entry.getKey();
            }
        }

        int days = dailyTotals.size();
        double dailyAvg = days > 0 ? (totalAmount / days) : 0;

        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance();
        tvTotal.setText(nf.format(totalAmount));
        tvHighTxn.setText(nf.format(highestTxn));
        tvDailyAvg.setText(nf.format(dailyAvg));
        
        if (highDayTs > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
            tvHighDay.setText(sdf.format(new java.util.Date(highDayTs)) + " (" + nf.format(highDayAmt) + ")");
        } else {
            tvHighDay.setText("None");
        }
        
        if (account.hasBudget()) {
            tvBudgetPct.setVisibility(View.VISIBLE);
            double budget = account.calculateTotalBudget();
            if (budget > 0) {
                double expenses = 0;
                for (Record r : filtered) {
                    expenses += r.getAmount();
                }
                double pct = (expenses / budget) * 100.0;
                tvBudgetPct.setText(String.format(java.util.Locale.getDefault(), "%.1f%% of budget spent", pct));
            } else {
                tvBudgetPct.setText("0% of budget spent");
            }
        } else {
            tvBudgetPct.setVisibility(View.GONE);
        }

        List<BarEntry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        int i = 0;
        java.text.SimpleDateFormat sdfShort = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault());
        
        for (java.util.Map.Entry<Long, Double> entry : dailyTotals.entrySet()) {
            entries.add(new BarEntry(i, entry.getValue().floatValue()));
            labels.add(sdfShort.format(new java.util.Date(entry.getKey())));
            i++;
        }

        if (entries.isEmpty()) {
            chart.clear();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Spending");
        dataSet.setColor(ThemeManager.getPrimaryAccentColor(this));
        dataSet.setValueTextColor(ThemeManager.getSecondaryAccentColor(this));
        dataSet.setValueTextSize(10f);
        
        BarData barData = new BarData(dataSet);
        chart.setData(barData);
        
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) return labels.get(index);
                return "";
            }
        });
        
        chart.invalidate();
        chart.animateY(800);
    }

    private void showBulkActionsMenu(View anchor) {
        View popupView = getLayoutInflater().inflate(R.layout.layout_bulk_actions_menu, null);
        
        android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(
                popupView,
                (int) (220 * getResources().getDisplayMetrics().density),
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setElevation(8.0f);
        
        View btnFilter = popupView.findViewById(R.id.btn_popup_filter);
        View btnExport = popupView.findViewById(R.id.btn_popup_export_pdf);
        View btnCut = popupView.findViewById(R.id.btn_popup_cut);
        View btnCopy = popupView.findViewById(R.id.btn_popup_copy);
        View btnDelete = popupView.findViewById(R.id.btn_popup_delete);
        android.widget.ImageView filterIcon = popupView.findViewById(R.id.img_popup_filter_icon);
        if (filterIcon != null && recordsAdapter != null) {
            filterIcon.setColorFilter(null); // Clear any color filter
            if (recordsAdapter.filterCategories.isEmpty()) {
                android.util.TypedValue typedValue = new android.util.TypedValue();
                getTheme().resolveAttribute(R.attr.colorAccentPrimary, typedValue, true);
                filterIcon.setImageTintList(android.content.res.ColorStateList.valueOf(typedValue.data));
            } else {
                filterIcon.setImageTintList(android.content.res.ColorStateList.valueOf(ThemeManager.getSecondaryAccentColor(this)));
            }
        }
        
        List<Record> selectedRecords = new ArrayList<>();
        for (Record r : getActiveRecords()) if (r.isSelected()) selectedRecords.add(r);
        boolean hasSelection = !selectedRecords.isEmpty();
        
        if (btnExport != null) {
            btnExport.setAlpha(hasSelection ? 1.0f : 0.4f);
            btnExport.setEnabled(hasSelection);
        }
        btnCut.setAlpha(hasSelection ? 1.0f : 0.4f);
        btnCut.setEnabled(hasSelection);
        btnCopy.setAlpha(hasSelection ? 1.0f : 0.4f);
        btnCopy.setEnabled(hasSelection);
        btnDelete.setAlpha(hasSelection ? 1.0f : 0.4f);
        btnDelete.setEnabled(hasSelection);
        
        setupClickable(btnFilter, false, () -> {
            popupWindow.dismiss();
            showCategoryFilterDialog(currentEditingAccount, (ImageView) anchor);
        });
        
        if (hasSelection) {
            if (btnExport != null) {
                setupClickable(btnExport, false, () -> {
                    popupWindow.dismiss();
                    showPdfSortDialog(order -> generateAndOpenSelectedPdf(selectedRecords, order));
                });
            }
            setupClickable(btnCut, false, () -> {
                popupWindow.dismiss();
                showTransferDialog(selectedRecords, true);
            });
            setupClickable(btnCopy, false, () -> {
                popupWindow.dismiss();
                showTransferDialog(selectedRecords, false);
            });
            setupClickable(btnDelete, false, () -> {
                popupWindow.dismiss();
                showDeleteMultipleConfirmationDialog(selectedRecords);
            });
        }
        
        popupWindow.showAsDropDown(anchor, 0, 0);
    }

    private void showTransferDialog(List<Record> selectedRecords, boolean isCut) {
        List<Account> targetAccounts = new ArrayList<>();
        for (AccountGroup g : appStorage.groups) targetAccounts.addAll(g.getAccounts());
        targetAccounts.addAll(appStorage.standaloneAccounts);
        
        List<String> names = new ArrayList<>();
        names.add("Create New List");
        for (Account a : targetAccounts) {
            if (a != currentEditingAccount) {
                names.add(a.getTitle());
            }
        }
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_transfer, null);
        builder.setView(dialogView);
        
        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        
        TextView title = dialogView.findViewById(R.id.dialog_title);
        title.setText(isCut ? "Cut to..." : "Copy to...");
        
        android.widget.LinearLayout container = dialogView.findViewById(R.id.transfer_list_container);
        
        for (int i = 0; i < names.size(); i++) {
            final int index = i;
            TextView item = new TextView(this);
            item.setText(names.get(i));
            item.setTextSize(16f);
            item.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            item.setPadding(padding, padding, padding, padding);
            
            // Add a bottom border
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = (int) (1 * getResources().getDisplayMetrics().density);
            item.setLayoutParams(params);
            item.setBackgroundColor(getResources().getColor(R.color.bg_primary_blue, getTheme())); // fallback color, wait... it should use theme color.
            // Actually, setting background to transparent and letting container show it, or a ripple is better.
            
            setupClickable(item, false, () -> {
                dialog.dismiss();
                if (index == 0) { // Create New List
                    showNewListTitleDialog(selectedRecords, isCut);
                } else {
                    Account target = null;
                    String selectedName = names.get(index);
                    for (Account a : targetAccounts) {
                        if (a.getTitle().equals(selectedName)) {
                            target = a;
                            break;
                        }
                    }
                    if (target != null) {
                        executeTransfer(selectedRecords, target, isCut);
                    }
                }
            });
            container.addView(item);
            
            View divider = new View(this);
            divider.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 
                (int) (1 * getResources().getDisplayMetrics().density)
            ));
            divider.setBackgroundColor(android.graphics.Color.parseColor("#15FFFFFF"));
            container.addView(divider);
        }
        
        View btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        btnCancel.setBackground(createButtonSelector(android.graphics.Color.parseColor("#15FFFFFF"), 4.0f));
        setupClickable(btnCancel, false, dialog::dismiss);
        
        dialog.show();
    }

    private void showNewListTitleDialog(List<Record> selectedRecords, boolean isCut) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_new_list, null);
        builder.setView(dialogView);
        
        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        
        final android.widget.EditText input = dialogView.findViewById(R.id.edit_new_list_title);
        
        View btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);
        btnCancel.setBackground(createButtonSelector(android.graphics.Color.parseColor("#15FFFFFF"), 4.0f));
        setupClickable(btnCancel, false, dialog::dismiss);
        
        View btnCreate = dialogView.findViewById(R.id.btn_dialog_create);
        btnCreate.setBackground(createButtonSelector(android.graphics.Color.parseColor("#2034D399"), 4.0f));
        setupClickable(btnCreate, false, () -> {
            String title = input.getText().toString().trim();
            if (title.isEmpty()) {
                android.widget.Toast.makeText(this, "Title cannot be empty", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            // Check if title exists
            for (AccountGroup g : appStorage.groups) {
                for (Account a : g.getAccounts()) {
                    if (a.getTitle().equalsIgnoreCase(title)) {
                        android.widget.Toast.makeText(this, "List with this title already exists", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
            for (Account a : appStorage.standaloneAccounts) {
                if (a.getTitle().equalsIgnoreCase(title)) {
                    android.widget.Toast.makeText(this, "List with this title already exists", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            dialog.dismiss();
            Account newAccount = new Account(title);
            appStorage.standaloneAccounts.add(0, newAccount);
            executeTransfer(selectedRecords, newAccount, isCut);
            showDashboard();
        });
        
        dialog.show();
    }

    private void executeTransfer(List<Record> selectedRecords, Account targetAccount, boolean isCut) {
        java.util.List<Record> targetList = isBudgetMode ? targetAccount.getBudgetRecords() : targetAccount.getRecords();
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
            if (r.getAttachments() != null) {
                copy.getAttachments().addAll(r.getAttachments());
            }
            maxIndex++;
            copy.setOriginalIndex(maxIndex);
            
            if (isBudgetMode) {
                targetAccount.getBudgetRecords().add(copy);
            } else {
                targetAccount.getRecords().add(copy);
            }
            
            if (isCut) {
                if (isBudgetMode) {
                    currentEditingAccount.getBudgetRecords().remove(r);
                } else {
                    currentEditingAccount.getRecords().remove(r);
                }
            }
        }
        
        StorageHelper.saveAppStorage(this, appStorage);
        
        if (isCut) {
            getActiveRecords().removeAll(selectedRecords);
            resequentializeRecords(getActiveRecords());
            if (recordsAdapter != null) {
                recordsAdapter.setFilter(currentRecordSearchQuery);
            }
        }
        
        for (Record r : getActiveRecords()) r.setSelected(false);
        if (cbSelectAllHeader != null) {
            cbSelectAllHeader.setOnCheckedChangeListener(null);
            cbSelectAllHeader.setChecked(false);
            cbSelectAllHeader.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (recordsAdapter != null) {
                    for (Record rec : recordsAdapter.displayRecords) {
                        rec.setSelected(isChecked);
                    }
                    recordsAdapter.notifyDataSetChanged();
                    updateBulkActionsState();
                }
            });
        }
        if (recordsAdapter != null) {
            recordsAdapter.notifyDataSetChanged();
        }
        updateBulkActionsState();
        
        String action = isCut ? "Cut" : "Copied";
        android.widget.Toast.makeText(this, action + " " + selectedRecords.size() + " records to " + targetAccount.getTitle(), android.widget.Toast.LENGTH_SHORT).show();
    }

    private void generateAndOpenSelectedPdf(java.util.List<Record> selectedRecords, PdfSortOrder sortOrder) {
        if (selectedRecords.isEmpty()) return;
        android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
        int[] pageTracker = {0};
        appendSelectedRecordsToPdf(document, currentEditingAccount, selectedRecords, pageTracker, sortOrder);
        try {
            java.io.File pdfDir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
            if (pdfDir == null) return;
            if (!pdfDir.exists()) pdfDir.mkdirs();
            java.io.File file = new java.io.File(pdfDir, currentEditingAccount.getTitle().replaceAll("[\\/:*?\"<>|]", "_") + "_Selected.pdf");
            document.writeTo(new java.io.FileOutputStream(file));
            document.close();
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", file);
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(android.content.Intent.createChooser(intent, "Open PDF with"));
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            document.close();
        }
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
        java.util.Collections.sort(recordsToPrint, (r1, r2) -> {
            if (sortOrder == PdfSortOrder.DESCRIPTION) return r1.getDescription().compareToIgnoreCase(r2.getDescription());
            if (sortOrder == PdfSortOrder.DATE) {
                try {
                    java.text.SimpleDateFormat sortSdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                    java.util.Date d1 = sortSdf.parse(r1.getDate());
                    java.util.Date d2 = sortSdf.parse(r2.getDate());
                    int c = d1.compareTo(d2);
                    if (c == 0) c = Long.compare(r1.getTimestampMillis(), r2.getTimestampMillis());
                    return c;
                } catch (Exception e) {
                    return r1.getDate().compareTo(r2.getDate());
                }
            }
            if (sortOrder == PdfSortOrder.AMOUNT) return Double.compare(r1.getAmount(), r2.getAmount());
            return Integer.compare(r1.getOriginalIndex(), r2.getOriginalIndex());
        });
        
        double totalAmt = 0;
        for (Record r : recordsToPrint) totalAmt += r.getAmount();

        // --- Page tracking ---
        int pageNum = pageTracker[0];
        android.graphics.Canvas canvas = null;
        android.graphics.pdf.PdfDocument.Page page = null;
        float y = 0;

        pageNum++;
        android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();
        canvas.drawRect(0, 0, pageWidth, pageHeight, bgPaint);
        y = margin;

        String titleText = account.getTitle() + " (Selected)";
        float titleLineHeight = 28f;
        float maxTitleWidth = contentWidth - 15f;
        java.util.List<String> titleLines = wrapText(titleText, titlePaint, maxTitleWidth);
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

        float hx = margin;
        canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, tableHeaderBgPaint);
        canvas.drawText("S.No",        hx + 4,                       y + 15f, accentPaint);
        canvas.drawText("Description", hx + colSno + 4,              y + 15f, accentPaint);
        canvas.drawText("Date",        hx + colSno + colDesc + 4,    y + 15f, accentPaint);
        canvas.drawText("Time",        hx + colSno + colDesc + colDate + 4, y + 15f, accentPaint);
        float amountHeaderX = hx + colSno + colDesc + colDate + colTime + colAmount - accentPaint.measureText("Amount") - 4f;
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

            float rx = margin;
            canvas.drawText(String.valueOf(i + 1), rx + 4, y + 15f, cellMutedPaint);

            String desc = rec.getDescription();
            while (desc.length() > 1 && cellPaint.measureText(desc) > colDesc - 8f) {
                desc = desc.substring(0, desc.length() - 1);
            }
            if (!desc.equals(rec.getDescription())) desc += "\u2026";
            canvas.drawText(desc, rx + colSno + 4, y + 15f, cellPaint);

            float currentY = y + 27f;
            if (hasRemarks) {
                String truncRemarks = combinedNotes;
                while (truncRemarks.length() > 1 && cellMutedPaint.measureText(truncRemarks) > colDesc - 8f) {
                    truncRemarks = truncRemarks.substring(0, truncRemarks.length() - 1);
                }
                if (!truncRemarks.equals(combinedNotes)) truncRemarks += "\u2026";
                canvas.drawText(truncRemarks, rx + colSno + 4, currentY, cellMutedPaint);
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
                if (!truncFn.equals("\uD83D\uDCCE " + fn)) truncFn += "\u2026";
                canvas.drawText(truncFn, rx + colSno + 4, currentY, cellMutedPaint);
                currentY += 12f;
            }

            canvas.drawText(formatDateCompact(rec.getDate()), rx + colSno + colDesc + 4, y + 15f, cellMutedPaint);
            String timeStr = rec.getTimestampMillis() > 0 ? timeSdf.format(new java.util.Date(rec.getTimestampMillis())) : "-";
            canvas.drawText(timeStr, rx + colSno + colDesc + colDate + 4, y + 15f, cellMutedPaint);

            String amtStr = String.format(java.util.Locale.getDefault(), "%.2f", rec.getAmount());
            float amtX = rx + colSno + colDesc + colDate + colTime + colAmount - cellPaint.measureText(amtStr) - 4f;
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
                chipContainer.setBackground(createButtonSelector(ThemeManager.getBgSecondaryColor(this), 8.0f));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 16, 0);
                chipContainer.setLayoutParams(lp);

                TextView chip = new TextView(this);
                chip.setText((path.endsWith(".pdf") || path.endsWith(".doc") || path.endsWith(".docx") ? "\uD83D\uDCC4 " : "\uD83D\uDDBC ") + name);
                chip.setTextSize(12);
                chip.setTextColor(getColor(R.color.text_primary));
                chip.setPadding(20, 10, 10, 10);
                
                setupClickable(chip, false, () -> {
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
                        Toast.makeText(MainActivity.this, "Cannot open file", Toast.LENGTH_SHORT).show();
                    }
                });

                TextView closeBtn = new TextView(this);
                closeBtn.setText(" \u2715 ");
                closeBtn.setTextSize(12);
                closeBtn.setTextColor(getColor(R.color.error_red));
                closeBtn.setPadding(10, 10, 20, 10);
                
                setupClickable(closeBtn, false, () -> {
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
                if (f.exists()) f.delete();
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
                        if (index != -1) originalName = cursor.getString(index);
                    }
                }
                
                java.io.File destFile = new java.io.File(attachmentsDir, originalName);
                java.io.InputStream in = getContentResolver().openInputStream(uri);
                java.io.FileOutputStream out = new java.io.FileOutputStream(destFile);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.close();
                
                tempAttachments.add(destFile.getAbsolutePath());
                renderEditorAttachments();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to attach file", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
