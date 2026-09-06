package com.example.notecalc;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.CheckBox;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

    @android.annotation.SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RecordViewHolder> {
    private final MainActivity activity;

    public RecordsAdapter(MainActivity activity) {
        this.activity = activity;
    }

        private boolean isSelectionMode = false;

        public void setSelectionMode(boolean mode) {
            if (this.isSelectionMode != mode) {
                this.isSelectionMode = mode;
                notifyDataSetChanged();
            }
        }

        // Filtered view of tempRecords, rebuilt on every setFilter() call
        final List<Record> displayRecords = new ArrayList<>();
        public java.util.Set<String> filterCategories = new java.util.HashSet<>();

        public void setFilterCategories(java.util.Set<String> cats) {
            filterCategories.clear();
            if (cats != null) filterCategories.addAll(cats);
            refreshDisplay();
        }

        void setFilter(String query) {
            displayRecords.clear();
            displayRecords.addAll(RecordsFilterEngine.filterRecords(activity, StateHelper.getActiveRecords(activity), query, filterCategories));
            notifyDataSetChanged();
            BulkActionsHelper.updateBulkActionsState(activity);
        }

        void refreshDisplay() {
            setFilter(activity.currentRecordSearchQuery);
        }

        @androidx.annotation.NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            View rowView = LayoutInflater.from(activity).inflate(R.layout.item_record, parent, false);
            ResponsiveUI.applyResponsiveness(rowView);
            return new RecordViewHolder(rowView);
        }

        @android.annotation.SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull RecordViewHolder holder, int position) {
            Record record = displayRecords.get(position);
            // Find the true index in tempRecords (or budget records) so that edit/delete work correctly
            int trueIndex = StateHelper.getActiveRecords(activity).indexOf(record);

            holder.tvSno.setText(String.valueOf(record.getOriginalIndex() + 1));
            holder.tvDesc.setText(record.getDescription());
            
            // Reset date view state to avoid recycling bugs
            if (holder.revertDateTask != null) {
                holder.tvDate.removeCallbacks(holder.revertDateTask);
                holder.revertDateTask = null;
            }
            holder.isShowingDay = false;
            holder.tvDate.setText(DateUtils.formatDateCompact(record.getDate()));
            
            holder.tvDate.setOnClickListener(v -> {
                if (holder.isShowingDay) {
                    if (holder.revertDateTask != null) {
                        holder.tvDate.removeCallbacks(holder.revertDateTask);
                        holder.revertDateTask = null;
                    }
                    holder.isShowingDay = false;
                    holder.tvDate.setText(DateUtils.formatDateCompact(record.getDate()));
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
                                holder.tvDate.setText(DateUtils.formatDateCompact(record.getDate()));
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

            String remarks = record.getRemarks();
            if (holder.tvRemarks != null) {
                if (remarks != null && !remarks.isEmpty()) {
                    holder.tvRemarks.setText(remarks);
                    holder.tvRemarks.setVisibility(View.VISIBLE);
                } else {
                    holder.tvRemarks.setVisibility(View.GONE);
                }
            }
            
            String category = record.getCategory();
            if (holder.tvCategory != null) {
                if (category != null && !category.isEmpty()) {
                    holder.tvCategory.setText(category);
                    holder.tvCategory.setVisibility(View.VISIBLE);
                } else {
                    holder.tvCategory.setVisibility(View.GONE);
                }
            }

            RecordAttachmentBinder.bindAttachments(activity, record, holder);
            
            if (holder.cbSelect != null) {
                holder.cbSelect.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
                holder.cbSelect.setOnCheckedChangeListener(null);
                holder.cbSelect.setChecked(record.isSelected());
                holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    record.setSelected(isChecked);
                    BulkActionsHelper.updateSelectAllHeaderState(activity);
                    BulkActionsHelper.updateBulkActionsState(activity);
                });
            }

            int rowBgColor = (position % 2 == 0) ? ThemeManager.getBgSecondaryColor(activity) : ThemeManager.getBgTertiaryColor(activity);
            if (trueIndex == activity.editingRecordIndex) {
                holder.itemView.setBackground(ResponsiveUI.createRoundedBg(
                        activity,
                        rowBgColor,
                        ThemeManager.getSecondaryAccentColor(activity),
                        1.5f,
                        4.0f
                ));
            } else {
                holder.itemView.setBackground(ResponsiveUI.createRoundedBg(
                        activity,
                        rowBgColor,
                        0,
                        0,
                        4.0f
                ));
            }

            ResponsiveUI.setupClickable(holder.itemView, true, () -> {
                if (activity.currentEditingAccount != null && activity.currentEditingAccount.isArchived()) return;
                EditorModeHelper.enterEditRecordMode(activity, trueIndex, record);
            }, () -> {
                if (!record.isSelected()) {
                    record.setSelected(true);
                    BulkActionsHelper.updateSelectAllHeaderState(activity);
                    BulkActionsHelper.updateBulkActionsState(activity);
                }
            });
        }

        @Override
        public int getItemCount() {
            return displayRecords.size();
        }

        public static class RecordViewHolder extends RecyclerView.ViewHolder {
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