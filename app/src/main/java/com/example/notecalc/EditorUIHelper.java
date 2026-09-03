package com.example.notecalc;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;

public class EditorUIHelper {

    /**
     * Helper to render the records in the table format.
     */
    public static void populateRecordsList(MainActivity activity) {
        if (activity.recordsAdapter != null) {
            activity.recordsAdapter.refreshDisplay();
        }

        // Toggle empty state and table rows visibility
        boolean isEmpty = StateHelper.getActiveRecords(activity).isEmpty();
        if (activity.editorEmptyState != null) {
            activity.editorEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (activity.rowSearchAndBulk != null) {
            activity.rowSearchAndBulk.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
        if (activity.tableHeaderField != null) {
            activity.tableHeaderField.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }

        // Sync select-all header checkbox after any list change
        BulkActionsHelper.updateSelectAllHeaderState(activity);
        BulkActionsHelper.updateBulkActionsState(activity);
    }

    public static int getNewOriginalIndex(MainActivity activity) {
        int maxIndex = -1;
        for (Record r : StateHelper.getActiveRecords(activity)) {
            if (r.getOriginalIndex() > maxIndex) {
                maxIndex = r.getOriginalIndex();
            }
        }
        return maxIndex + 1;
    }

    /**
     * Evaluates if the entered title already exists in saved accounts.
     */
    public static boolean isDuplicateTitle(MainActivity activity, String title) {
        for (Account acc : activity.appStorage.standaloneAccounts) {
            if (activity.currentEditingAccount != null && acc.getTitle().equalsIgnoreCase(activity.originalTitle)) {
                continue;
            }
            if (acc.getTitle().equalsIgnoreCase(title.trim())) {
                return true;
            }
        }
        for (AccountGroup group : activity.appStorage.groups) {
            for (Account acc : group.getAccounts()) {
                if (activity.currentEditingAccount != null && acc.getTitle().equalsIgnoreCase(activity.originalTitle)) {
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
     * Updates the UI state of the editor's totals and bulk action views.
     * @return true if any records are currently selected, false otherwise.
     */
    public static boolean updateTotalsAndBulkActions(
            List<Record> activeRecords,
            int filteredRecordCount,
            boolean isFilterActive,
            View containerBulkActions,
            TextView textSelectedTotal,
            TextView textTotalValField,
            TextView textTotalLabel,
            CheckBox cbSelectAllHeader) {
            
        boolean anySelected = false;
        double selectedTotal = 0.0;
        double overallTotal = 0.0;

        for (Record r : activeRecords) {
            if (r.isSelected()) {
                anySelected = true;
                selectedTotal += r.getAmount();
            }
            overallTotal += r.getAmount();
        }

        if (containerBulkActions != null) {
            containerBulkActions.setVisibility(View.VISIBLE);
        }

        if (textTotalValField != null) {
            if (anySelected) {
                textTotalValField.setText(String.format(Locale.getDefault(), "%.2f", selectedTotal));
                if (textTotalLabel != null) textTotalLabel.setText(textTotalLabel.getContext().getString(R.string.total_of_selection));
            } else {
                textTotalValField.setText(String.format(Locale.getDefault(), "%.2f", overallTotal));
                if (textTotalLabel != null) textTotalLabel.setText(textTotalLabel.getContext().getString(R.string.total_spendings_label));
            }
        }

        if (textSelectedTotal != null) {
            if (isFilterActive) {
                textSelectedTotal.setVisibility(View.VISIBLE);
                textSelectedTotal.setText(textSelectedTotal.getContext().getString(R.string.records_count, filteredRecordCount));
            } else {
                textSelectedTotal.setVisibility(View.GONE);
            }
        }

        if (cbSelectAllHeader != null) {
            cbSelectAllHeader.setVisibility(anySelected ? View.VISIBLE : View.GONE);
        }
        
        return anySelected;
    }

    public static void setupFormToggle(MainActivity activity, Account account) {
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
    }

    @android.annotation.SuppressLint("ClickableViewAccessibility")
    public static void setupSearchBar(MainActivity activity, android.widget.EditText editRecordsSearch) {
        editRecordsSearch.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getBgSecondaryColor(activity),
                ThemeManager.getBorderColor(activity),
                1.0f,
                8.0f
        ));

        editRecordsSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
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
        editRecordsSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                activity.currentRecordSearchQuery = s.toString();
                activity.recordsAdapter.setFilter(activity.currentRecordSearchQuery);
            }
        });
    }

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    public static void setupBulkActions(MainActivity activity) {
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
            activity.btnBulkActionsMenu.setBackground(ResponsiveUI.createButtonSelector(activity, android.graphics.Color.parseColor("#15FFFFFF"), 4.0f));
            ResponsiveUI.setupClickable(activity.btnBulkActionsMenu, true, () -> MenuHelper.showBulkActionsMenu(activity, activity.btnBulkActionsMenu));
        }
    }

    public static void setupTitleWatcher(MainActivity activity, android.widget.EditText editTitle, TextView textTitleError) {
        editTitle.addTextChangedListener(new android.text.TextWatcher() {
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
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    public static void migrateLegacyIndices(MainActivity activity, Account account) {
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
    }

    public static void setupFormListeners(MainActivity activity, android.widget.TextView btnDate, android.widget.TextView btnCancelEdit) {
        ResponsiveUI.setupClickable(btnDate, () -> DialogHelper.showDatePicker(activity, activity.selectedRecordDate, btnDate, newDate -> activity.selectedRecordDate = newDate));
        ResponsiveUI.setupClickable(btnCancelEdit, () -> EditorModeHelper.cancelEditRecordMode(activity));
    }
}
