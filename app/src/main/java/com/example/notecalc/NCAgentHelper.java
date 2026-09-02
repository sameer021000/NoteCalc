package com.example.notecalc;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import com.example.notecalc.ncagent.*;

public class NCAgentHelper {

        @android.annotation.SuppressLint("SetTextI18n")
    public static void showNCAgentBottomSheet(MainActivity activity, NCAgent ncAgent) {
        android.app.Dialog dialog = new android.app.Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);
        
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(android.view.Gravity.BOTTOM);
        root.setBackgroundColor(0x80000000); // dim background
        
        LinearLayout sheet = new LinearLayout(activity);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBgPrimaryColor(activity), 0f, 16f));
        int pad = (int)(16 * activity.getResources().getDisplayMetrics().density);
        sheet.setPadding(pad, pad, pad, pad);
        
        TextView title = new TextView(activity);
        title.setText(activity.getString(R.string.auto_nc_agent_15));
        title.setTextSize(20);
        title.setTextColor(activity.getColor(R.color.text_primary));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, pad);
        sheet.addView(title);
        
        EditText input = new EditText(activity);
        input.setHint(activity.getString(R.string.auto_e_g_bought_2_coffees_31));
        input.setTextColor(activity.getColor(R.color.text_primary));
        input.setHintTextColor(activity.getColor(R.color.text_secondary));
        input.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 8f));
        input.setPadding(pad, pad, pad, pad);
        input.setLines(4);
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        sheet.addView(input);
        
        android.widget.Button btnAnalyze = new android.widget.Button(activity);
        btnAnalyze.setText(activity.getString(R.string.auto_analyze_16));
        btnAnalyze.setTextColor(activity.getColor(R.color.text_on_accent));
        btnAnalyze.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), ThemeManager.getPrimaryAccentColor(activity), 0f, 8f));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, pad, 0, 0);
        sheet.addView(btnAnalyze, btnParams);
        
        ScrollView previewScroll = new ScrollView(activity);
        LinearLayout previewContainer = new LinearLayout(activity);
        previewContainer.setOrientation(LinearLayout.VERTICAL);
        previewScroll.addView(previewContainer);
        previewScroll.setVisibility(View.GONE);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        scrollParams.setMargins(0, pad, 0, 0);
        sheet.addView(previewScroll, scrollParams);
        
        LinearLayout actionButtons = new LinearLayout(activity);
        actionButtons.setOrientation(LinearLayout.HORIZONTAL);
        actionButtons.setVisibility(View.GONE);
        
        android.widget.Button btnCancel = new android.widget.Button(activity);
        btnCancel.setText(activity.getString(R.string.auto_cancel_17));
        btnCancel.setTextColor(activity.getColor(R.color.text_primary));
        btnCancel.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        
        android.widget.Button btnConfirm = new android.widget.Button(activity);
        btnConfirm.setText(activity.getString(R.string.auto_confirm_18));
        btnConfirm.setTextColor(ThemeManager.getPrimaryAccentColor(activity));
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
            parsedActions.addAll(ncAgent.process(text, activity.getActiveRecords()));
            
            // Build Preview UI
            previewContainer.removeAllViews();
            input.setVisibility(View.GONE);
            btnAnalyze.setVisibility(View.GONE);
            previewScroll.setVisibility(View.VISIBLE);
            actionButtons.setVisibility(View.VISIBLE);
            
            for (NCAction action : parsedActions) {
                LinearLayout card = new LinearLayout(activity);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 8f));
                card.setPadding(pad, pad, pad, pad);
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                cardParams.setMargins(0, 0, 0, pad);
                previewContainer.addView(card, cardParams);
                
                TextView intentView = new TextView(activity);
                intentView.setText("[" + action.getIntent().name() + "]");
                intentView.setTypeface(null, android.graphics.Typeface.BOLD);
                
                if (action.getIntent() == NCAgentIntent.ADD) intentView.setTextColor(activity.getColor(R.color.accent_green_primary));
                else if (action.getIntent() == NCAgentIntent.UPDATE) intentView.setTextColor(activity.getColor(R.color.accent_blue_primary));
                else if (action.getIntent() == NCAgentIntent.DELETE) intentView.setTextColor(activity.getColor(R.color.error_red));
                else intentView.setTextColor(activity.getColor(R.color.text_tertiary));
                card.addView(intentView);
                
                if (!action.isValid()) {
                    TextView errView = new TextView(activity);
                    errView.setText("Error: " + action.getErrorMessage());
                    errView.setTextColor(activity.getColor(R.color.error_red));
                    card.addView(errView);
                } else if (action.isNeedsDisambiguation()) {
                    TextView disambigText = new TextView(activity);
                    disambigText.setText("Multiple matches found. Select which to " + action.getIntent().name().toLowerCase() + ":");
                    disambigText.setTextColor(activity.getColor(R.color.text_primary));
                    card.addView(disambigText);
                    
                    for (Record matched : action.getDisambiguationCandidates()) {
                        CheckBox cb = new CheckBox(activity);
                        cb.setText(matched.getDescription() + " (?" + matched.getAmount() + ") - " + matched.getDate());
                        cb.setTextColor(activity.getColor(R.color.text_primary));
                        // Save the checkbox view in a tag to retrieve its state on Confirm
                        cb.setTag(matched);
                        card.addView(cb);
                    }
                } else {
                    Record rec = action.getValidatedRecord() != null ? action.getValidatedRecord() : action.getTargetRecord();
                    TextView dataView = new TextView(activity);
                    dataView.setText(rec.getDescription() + "  -  ?" + rec.getAmount() + "  (" + rec.getDate() + ")");
                    dataView.setTextColor(activity.getColor(R.color.text_primary));
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
                                    activity.getActiveRecords().remove(target);
                                    deleted++;
                                }
                            }
                        }
                    }
                } else {
                    if (action.getIntent() == NCAgentIntent.ADD) {
                        Record validated = action.getValidatedRecord();
                        validated.setOriginalIndex(activity.getNewOriginalIndex());
                        activity.getActiveRecords().add(validated);
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
                        activity.getActiveRecords().remove(action.getTargetRecord());
                        deleted++;
                    }
                }
            }
            
            String summary = "";
            if (added > 0) summary += "Added " + added + " records\n";
            if (updated > 0) summary += "Updated " + updated + " records\n";
            if (deleted > 0) summary += "Deleted " + deleted + " records\n";
            if (!summary.isEmpty()) Toast.makeText(activity, summary, Toast.LENGTH_LONG).show();
            
            EditorSortHelper.applySorting(activity);
            StorageHelper.saveAppStorage(activity, activity.appStorage);
            activity.populateRecordsList();
            dialog.dismiss();
        });
        
        dialog.show();
    }

}
