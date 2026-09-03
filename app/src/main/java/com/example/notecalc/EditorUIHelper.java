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
        boolean isEmpty = activity.getActiveRecords().isEmpty();
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


    @android.annotation.SuppressLint("SetTextI18n")


    public static int getNewOriginalIndex(MainActivity activity) {
        int maxIndex = -1;
        for (Record r : activity.getActiveRecords()) {
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
}
