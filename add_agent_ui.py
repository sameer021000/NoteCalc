import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add field
field_search = '''    private ImageView btnCreateAccount;
    private ImageView btnCreateGroup;'''
field_replace = '''    private ImageView btnCreateAccount;
    private ImageView btnCreateGroup;
    private ImageView btnNCAgent;'''
content = content.replace(field_search, field_replace)

# 2. Bind and style
bind_search = '''        btnRecordDateField = findViewById(R.id.btn_date);
        btnCancelEditField = findViewById(R.id.btn_cancel_edit_record);'''
bind_replace = '''        btnRecordDateField = findViewById(R.id.btn_date);
        btnCancelEditField = findViewById(R.id.btn_cancel_edit_record);
        btnNCAgent = findViewById(R.id.btn_nc_agent);'''
content = content.replace(bind_search, bind_replace)

style_search = '''        btnAddRecordField.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getPrimaryAccentColor(MainActivity.this), ThemeManager.getPrimaryAccentColor(MainActivity.this), 0f, 4.0f));'''
style_replace = '''        btnAddRecordField.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getPrimaryAccentColor(MainActivity.this), ThemeManager.getPrimaryAccentColor(MainActivity.this), 0f, 4.0f));
        if (btnNCAgent != null) {
            btnNCAgent.setBackground(ResponsiveUI.createRoundedBg(this, ThemeManager.getPrimaryAccentColor(MainActivity.this), ThemeManager.getPrimaryAccentColor(MainActivity.this), 0f, 28f));
            btnNCAgent.setColorFilter(getColor(R.color.text_on_accent));
            btnNCAgent.setOnClickListener(v -> showNCAgentBottomSheet());
        }'''
content = content.replace(style_search, style_replace)

# 3. Add showNCAgentBottomSheet method
import_search = '''import java.util.UUID;'''
import_replace = '''import java.util.UUID;
import com.example.notecalc.ncagent.*;
import com.example.notecalc.ncagent.parser.*;
import android.widget.CheckBox;
import android.widget.ScrollView;'''
content = content.replace(import_search, import_replace)

method_code = '''
    private NCAgent ncAgent = new NCAgent();

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
        title.setTextColor(ThemeManager.getTextPrimaryColor(this));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, pad);
        sheet.addView(title);
        
        EditText input = new EditText(this);
        input.setHint("e.g. Bought 2 coffees for 50");
        input.setTextColor(ThemeManager.getTextPrimaryColor(this));
        input.setHintTextColor(ThemeManager.getTextSecondaryColor(this));
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
        btnCancel.setTextColor(ThemeManager.getTextPrimaryColor(this));
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
                    disambigText.setTextColor(ThemeManager.getTextPrimaryColor(this));
                    card.addView(disambigText);
                    
                    for (Record matched : action.getDisambiguationCandidates()) {
                        CheckBox cb = new CheckBox(this);
                        cb.setText(matched.getDescription() + " (?" + matched.getAmount() + ") - " + matched.getDate());
                        cb.setTextColor(ThemeManager.getTextPrimaryColor(this));
                        // Save the checkbox view in a tag to retrieve its state on Confirm
                        cb.setTag(matched);
                        card.addView(cb);
                    }
                } else {
                    Record rec = action.getValidatedRecord() != null ? action.getValidatedRecord() : action.getTargetRecord();
                    TextView dataView = new TextView(this);
                    dataView.setText(rec.getDescription() + "  -  ?" + rec.getAmount() + "  (" + rec.getDate() + ")");
                    dataView.setTextColor(ThemeManager.getTextPrimaryColor(this));
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
                                } else if (action.getIntent() == NCAgentIntent.UPDATE) {
                                    // In a real flow, the user selects one, and we apply the update to it.
                                    // This is a simplified application
                                }
                            }
                        }
                    }
                } else {
                    if (action.getIntent() == NCAgentIntent.ADD) {
                        getActiveRecords().add(action.getValidatedRecord());
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
            if (added > 0) summary += "Added " + added + " records\\n";
            if (updated > 0) summary += "Updated " + updated + " records\\n";
            if (deleted > 0) summary += "Deleted " + deleted + " records\\n";
            if (!summary.isEmpty()) Toast.makeText(this, summary, Toast.LENGTH_LONG).show();
            
            saveCurrentAccount();
            updateDashboardAccounts();
            refreshRecordsList();
            updateTotal();
            dialog.dismiss();
        });
        
        dialog.show();
    }
'''

# Find a good place to insert the method. Before private void showDashboard() is good.
content = content.replace('private void showDashboard() {', method_code + '\n    private void showDashboard() {')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Agent UI added to Java")
