package com.example.notecalc;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.example.notecalc.ncagent.*;
import android.widget.CheckBox;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.ImageView;

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
    public AppStorage appStorage;
    public AccountGroup currentViewGroup = null; // null means we are in the Dashboard
    public Account currentEditingAccount;
    
    // Editor state
    List<Record> tempRecords;
    List<Record> tempBudgetRecords;
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
    boolean groupSortAscending = true;
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

    int expenseSortColumn = 0;
    boolean expenseSortAscending = false;
    int budgetSortColumn = 0;
    boolean budgetSortAscending = false;
    

    // Dashboard sort state: 0 = Title, 1 = Total Spending, 2 = Latest Modified
    int dashboardSortMode = 0;
    boolean dashboardSortAscending = true;
    
    int archivedDashboardSortMode = 0;
    boolean archivedDashboardSortAscending = true;
    boolean archivedGroupSortAscending = true;

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
    String expenseFilterDateFrom = null;
    String expenseFilterDateTo = null;
    Double expenseFilterAmountFrom = null;
    Double expenseFilterAmountTo = null;

    String budgetFilterDateFrom = null;
    String budgetFilterDateTo = null;
    Double budgetFilterAmountFrom = null;
    Double budgetFilterAmountTo = null;

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

    final NCAgent ncAgent = new NCAgent();
    void openEditor(Account account) {
        EditorHelper.openEditor(this, account);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AttachmentHelper.handleActivityResult(MainActivity.this, requestCode, resultCode, data);
    }
}
