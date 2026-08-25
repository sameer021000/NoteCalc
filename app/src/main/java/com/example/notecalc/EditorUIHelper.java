package com.example.notecalc;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;

public class EditorUIHelper {

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
