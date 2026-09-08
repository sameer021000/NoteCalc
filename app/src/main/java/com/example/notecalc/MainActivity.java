package com.example.notecalc;
import com.example.notecalc.storage.core.*;
import com.example.notecalc.storage.backup.*;
import com.example.notecalc.accounts.adapters.*;
import com.example.notecalc.core.utils.*;
import com.example.notecalc.core.ui.*;
import com.example.notecalc.records.adapters.*;
import com.example.notecalc.accounts.models.*;
import com.example.notecalc.records.models.Record;
import com.example.notecalc.editor.*;
import com.example.notecalc.dashboard.*;
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
    public SettingsHelper settingsHelper;

    public final java.util.List<String> tempAttachments = new java.util.ArrayList<>();
    public static final int REQUEST_CODE_ATTACH = 1001;
    public static final int REQUEST_CODE_CAMERA = 1002;
    public String currentPhotoPath = null;
    public android.widget.LinearLayout attachmentsContainer;
    public android.widget.HorizontalScrollView attachmentsScroll;
    public android.widget.TextView btnAttachFile;

    public androidx.activity.result.ActivityResultLauncher<android.content.Intent> exportJsonLauncher;
    public androidx.activity.result.ActivityResultLauncher<android.content.Intent> importJsonLauncher;

    public FrameLayout mainContainer;
    public AppStorage appStorage;
    public AccountGroup currentViewGroup = null; // null means we are in the Dashboard
    public Account currentEditingAccount;

    // Editor state
    public List<Record> tempRecords;
    public List<Record> tempBudgetRecords;
    public boolean isBudgetMode = false; // false = Expenses, true = Budget

    public String originalTitle = "";
    public String selectedRecordDate = "";

    public int editingRecordIndex = -1;
    public EditText editDescField;
    public EditText editAmountField;
    public TextView btnRecordDateField;
    public TextView btnAddRecordField;
    public TextView btnCancelEditField;
    public TextView labelAddRecordField;
    public RecordsAdapter recordsAdapter;
    public AccountsAdapter accountsAdapter;
    public AccountsAdapter groupsAdapter;
    public String dashboardSearchQuery = "";
    public boolean groupSortAscending = true;
    public TextView btnSortTitle;
    public TextView btnSortTotal;
    public TextView btnSortLatest;
    public TextView btnSortGroupTitle;
    public TextView textTotalValField;
    public TextView textTotalLabelField;
    public com.google.android.material.snackbar.Snackbar currentSnackbar;

    public TextView thSnoField;
    public TextView thDescField;
    public TextView thDateField;
    public TextView thAmountField;

    public int expenseSortColumn = 0;
    public boolean expenseSortAscending = false;
    public int budgetSortColumn = 0;
    public boolean budgetSortAscending = false;


    // Dashboard sort state: 0 = Title, 1 = Total Spending, 2 = Latest Modified
    public int dashboardSortMode = 0;
    public boolean dashboardSortAscending = true;

    public int archivedDashboardSortMode = 0;
    public boolean archivedDashboardSortAscending = true;
    public boolean archivedGroupSortAscending = true;

    // Editor record search query (persists while in editor, reset on openEditor)
    public String currentRecordSearchQuery = "";

    // Fields for collapsible form, remarks, empty state, and bulk delete
    public EditText editRemarksField;
    public android.widget.AutoCompleteTextView editCategoryField;
    public View formInputsContainer;
    public TextView btnToggleForm;
    public CheckBox cbSelectAllHeader;
    public ImageView btnBulkActionsMenu;
    public View editorEmptyState;
    public View rowSearchAndBulk;
    public View tableHeaderField;
    public boolean isFormInputsCollapsed = false;

    // Bulk action container and selected total display
    public View containerBulkActions;
    public TextView textSelectedTotal;

    // Date range filter state (dd-MM-yyyy strings, null = no filter)
    public String expenseFilterDateFrom = null;
    public String expenseFilterDateTo = null;
    public Double expenseFilterAmountFrom = null;
    public Double expenseFilterAmountTo = null;

    public String budgetFilterDateFrom = null;
    public String budgetFilterDateTo = null;
    public Double budgetFilterAmountFrom = null;
    public Double budgetFilterAmountTo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        settingsHelper = new SettingsHelper(this);
        androidx.activity.EdgeToEdge.enable(this);

        exportJsonLauncher = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> BackupHelper.handleExportResult(this, result));

        importJsonLauncher = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> BackupHelper.handleImportResult(this, result));

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
                NavigationHelper.handleBackPress(MainActivity.this, this);
            }
        });

        // Open the dashboard screen
        DashboardHelper.showDashboard(MainActivity.this);
    }

    public final NCAgent ncAgent = new NCAgent();
    public void openEditor(Account account) {
        EditorHelper.openEditor(this, account);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AttachmentHelper.handleActivityResult(MainActivity.this, requestCode, resultCode, data);
    }
}