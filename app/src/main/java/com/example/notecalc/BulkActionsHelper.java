package com.example.notecalc;

import java.util.List;

public class BulkActionsHelper {
    public static void updateBulkActionsState(MainActivity activity) {
        if (activity.btnBulkActionsMenu == null) return;
        
        int filterCount = activity.recordsAdapter != null ? activity.recordsAdapter.displayRecords.size() : 0;
        
        boolean anySelected = EditorUIHelper.updateTotalsAndBulkActions(
                StateHelper.getActiveRecords(activity),
                filterCount,
                FilterHelper.isFilterActive(activity),
                activity.containerBulkActions,
                activity.textSelectedTotal,
                activity.textTotalValField,
                activity.textTotalLabelField,
                activity.cbSelectAllHeader
        );

        if (activity.recordsAdapter != null) {
            activity.recordsAdapter.setSelectionMode(anySelected);
        }
    }

    public static void updateSelectAllHeaderState(MainActivity activity) {
        if (activity.cbSelectAllHeader == null || activity.recordsAdapter == null) return;
        List<Record> displayed = activity.recordsAdapter.displayRecords;
        if (displayed.isEmpty()) {
            activity.cbSelectAllHeader.setOnCheckedChangeListener(null);
            activity.cbSelectAllHeader.setChecked(false);
            return;
        }
        int selectedCount = 0;
        for (Record r : displayed) {
            if (r.isSelected()) selectedCount++;
        }
        activity.cbSelectAllHeader.setOnCheckedChangeListener(null);
        activity.cbSelectAllHeader.setChecked(selectedCount == displayed.size());
        activity.cbSelectAllHeader.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (Record r : activity.recordsAdapter.displayRecords) {
                r.setSelected(isChecked);
            }
            activity.recordsAdapter.notifyItemRangeChanged(0, activity.recordsAdapter.getItemCount());
            updateBulkActionsState(activity);
        });
    }
}
