package com.example.notecalc;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

public class TouchHelper {

    public static ItemTouchHelper.SimpleCallback getRecordSwipeCallback(MainActivity activity) {
                return new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            private boolean isDragActive = false;

            @Override
            public int getSwipeDirs(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder) {
                if (activity.currentEditingAccount != null && activity.currentEditingAccount.isArchived()) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public int getDragDirs(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder) {
                if (activity.currentEditingAccount != null && activity.currentEditingAccount.isArchived()) return 0;
                boolean isDefaultSort = activity.getSortColumn() == 0 && activity.getSortAscending();
                boolean noSearch = activity.currentRecordSearchQuery == null || activity.currentRecordSearchQuery.trim().isEmpty();
                if (isDefaultSort && noSearch) {
                    return androidx.recyclerview.widget.ItemTouchHelper.UP | androidx.recyclerview.widget.ItemTouchHelper.DOWN;
                }
                return 0; // Disable drag otherwise
            }

            @Override
            public boolean onMove(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, @androidx.annotation.NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getBindingAdapterPosition();
                int toPos = target.getBindingAdapterPosition();
                if (fromPos == RecyclerView.NO_POSITION || toPos == RecyclerView.NO_POSITION || activity.recordsAdapter == null) return false;
                
                Record fromRecord = activity.recordsAdapter.displayRecords.get(fromPos);
                Record toRecord = activity.recordsAdapter.displayRecords.get(toPos);
                
                // Swap originalIndex to permanently swap their S.Nos
                int tempIndex = fromRecord.getOriginalIndex();
                fromRecord.setOriginalIndex(toRecord.getOriginalIndex());
                toRecord.setOriginalIndex(tempIndex);
                
                java.util.Collections.swap(activity.recordsAdapter.displayRecords, fromPos, toPos);
                activity.recordsAdapter.notifyItemMoved(fromPos, toPos);
                isDragActive = true;
                return true;
            }

            @Override
            public void clearView(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if (isDragActive) {
                    isDragActive = false;
                    EditorSortHelper.applySorting(activity);
                    EditorUIHelper.populateRecordsList(activity);
                }
            }

            @Override
            public void onSwiped(@androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION || activity.recordsAdapter == null) return;
                
                Record deletedRecord = activity.recordsAdapter.displayRecords.get(pos);
                int trueIndex = activity.getActiveRecords().indexOf(deletedRecord);
                
                // Temporarily remove
                activity.getActiveRecords().remove(trueIndex);
                activity.recordsAdapter.refreshDisplay();
                BulkActionsHelper.updateBulkActionsState(activity);
                EditorSortHelper.updateHeaderLabels(activity);
                
                SnackbarHelper.showUndoSnackbar(activity, "Record deleted", () -> {
                    activity.getActiveRecords().add(trueIndex, deletedRecord);
                    activity.recordsAdapter.refreshDisplay();
                    BulkActionsHelper.updateBulkActionsState(activity);
                    EditorSortHelper.updateHeaderLabels(activity);
                }, null);
            }
        };

    }
}
