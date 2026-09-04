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
import java.text.ParseException;

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

        /** Rebuilds the displayRecords list from tempRecords using the given query filter. */
        void setFilter(String query) {
            displayRecords.clear();
            String q = (query == null ? "" : query.trim().toLowerCase(Locale.getDefault()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            for (Record r : StateHelper.getActiveRecords(activity)) {
                // Category filter
                if (!filterCategories.isEmpty()) {
                    if (!filterCategories.contains(r.getCategory())) continue;
                }
                // Text search filter
                if (!q.isEmpty()) {
                    boolean matchDesc = r.getDescription().toLowerCase(Locale.getDefault()).contains(q);
                    boolean matchRem = r.getRemarks() != null && r.getRemarks().toLowerCase(Locale.getDefault()).contains(q);
                    if (!matchDesc && !matchRem) {
                        continue;
                    }
                }
                // Date range filter
                if (StateHelper.getFilterDateFrom(activity) != null || StateHelper.getFilterDateTo(activity) != null) {
                    try {
                        Date recordDate = sdf.parse(r.getDate());
                        if (StateHelper.getFilterDateFrom(activity) != null) {
                            Date from = sdf.parse(StateHelper.getFilterDateFrom(activity));
                            if (recordDate != null && recordDate.before(from)) continue;
                        }
                        if (StateHelper.getFilterDateTo(activity) != null) {
                            Date to = sdf.parse(StateHelper.getFilterDateTo(activity));
                            if (recordDate != null && recordDate.after(to)) continue;
                        }
                    } catch (ParseException e) {
                        android.util.Log.e("NoteCalc", "Date parse error", e);
                    }
                }
                // Amount range filter
                if (StateHelper.getFilterAmountFrom(activity) != null && r.getAmount() < StateHelper.getFilterAmountFrom(activity)) continue;
                if (StateHelper.getFilterAmountTo(activity) != null && r.getAmount() > StateHelper.getFilterAmountTo(activity)) continue;

                displayRecords.add(r);
            }
            notifyDataSetChanged();
            BulkActionsHelper.updateBulkActionsState(activity);
        }

        /** Call this whenever tempRecords changes (add/edit/delete/sort) to refresh display. */
        void refreshDisplay() {
            // Preserve the current filter text if any — re-filter from scratch
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

            // Bind remarks (show only if non-empty)
            String remarks = record.getRemarks();
            if (holder.tvRemarks != null) {
                if (remarks != null && !remarks.isEmpty()) {
                    holder.tvRemarks.setText(remarks);
                    holder.tvRemarks.setVisibility(View.VISIBLE);
                } else {
                    holder.tvRemarks.setVisibility(View.GONE);
                }
            }
            
            // Bind category
            String category = record.getCategory();
            if (holder.tvCategory != null) {
                if (category != null && !category.isEmpty()) {
                    holder.tvCategory.setText(category);
                    holder.tvCategory.setVisibility(View.VISIBLE);
                } else {
                    holder.tvCategory.setVisibility(View.GONE);
                }
            }

            // Bind attachments
            if (holder.attachmentSummary != null && holder.attachmentsScroll != null && holder.attachmentsContainer != null) {
                if (record.getAttachments() != null && !record.getAttachments().isEmpty()) {
                    java.util.List<String> atts = record.getAttachments();
                    String firstPath = atts.get(0);
                    java.io.File f = new java.io.File(firstPath);
                    String name = f.getName();
                    if (name.length() > 15) name = name.substring(0, 15) + "...";
                    String icon = (firstPath.toLowerCase().endsWith(".pdf") || firstPath.toLowerCase().endsWith(".doc") || firstPath.toLowerCase().endsWith(".docx")) ? "\uD83D\uDCC4 " : "\uD83D\uDDBC ";
                    
                    if (atts.size() == 1) {
                        holder.attachmentSummary.setText(icon + name);
                    } else {
                        holder.attachmentSummary.setText(icon + name + " ▾"); // ?
                    }
                    
                    holder.attachmentSummary.setVisibility(View.VISIBLE);
                    holder.attachmentsScroll.setVisibility(View.GONE);
                    
                    holder.attachmentsScroll.setOnTouchListener((v, event) -> {
                        int action = event.getActionMasked();
                        if (action == android.view.MotionEvent.ACTION_DOWN || action == android.view.MotionEvent.ACTION_MOVE) {
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                        } else if (action == android.view.MotionEvent.ACTION_UP || action == android.view.MotionEvent.ACTION_CANCEL) {
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            if (action == android.view.MotionEvent.ACTION_UP) {
                                v.performClick();
                            }
                        }
                        return false;
                    });
                    
                    holder.attachmentsContainer.removeAllViews();
                    for (int i = 0; i < atts.size(); i++) {
                        String path = atts.get(i);
                        java.io.File file = new java.io.File(path);
                        String fname = file.getName();
                        if (fname.length() > 15) fname = fname.substring(0, 15) + "...";
                        String ficon = (path.toLowerCase().endsWith(".pdf") || path.toLowerCase().endsWith(".doc") || path.toLowerCase().endsWith(".docx")) ? "\uD83D\uDCC4 " : "\uD83D\uDDBC ";
                        
                        android.widget.TextView chip = new android.widget.TextView(activity);
                        chip.setText(ficon + fname);
                        chip.setTextSize(11);
                        chip.setTextColor(activity.getColor(R.color.text_primary));
                        chip.setBackground(ResponsiveUI.createButtonSelector(activity, ThemeManager.getBgSecondaryColor(activity), 6.0f));
                        chip.setPadding(12, 6, 12, 6);
                        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 0, 12, 0);
                        chip.setLayoutParams(lp);
                        
                        chip.setOnTouchListener((v, event) -> {
                            int action = event.getActionMasked();
                            if (action == android.view.MotionEvent.ACTION_DOWN || action == android.view.MotionEvent.ACTION_MOVE) {
                                v.getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
                            } else if (action == android.view.MotionEvent.ACTION_UP || action == android.view.MotionEvent.ACTION_CANCEL) {
                                v.getParent().getParent().getParent().requestDisallowInterceptTouchEvent(false);
                            }
                            return false;
                        });
                        
                        chip.setOnClickListener(_unused_v -> {
                            try {
                                android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", file);
                                android.content.Intent viewIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                                viewIntent.setDataAndType(uri, activity.getContentResolver().getType(uri));
                                viewIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                activity.startActivity(viewIntent);
                            } catch (Exception e) {
                                android.widget.Toast.makeText(activity, "Cannot open file", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
                        holder.attachmentsContainer.addView(chip);
                    }
                    
                    ResponsiveUI.setupClickable(holder.attachmentSummary, false, () -> {
                        if (atts.size() == 1) {
                            try {
                                android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", f);
                                android.content.Intent viewIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                                viewIntent.setDataAndType(uri, activity.getContentResolver().getType(uri));
                                viewIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                activity.startActivity(viewIntent);
                            } catch (Exception e) {
                                android.widget.Toast.makeText(activity, "Cannot open file", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            holder.attachmentSummary.setVisibility(View.GONE);
                            holder.attachmentsScroll.setVisibility(View.VISIBLE);
                        }
                    });
                    
                } else {
                    holder.attachmentSummary.setVisibility(View.GONE);
                    holder.attachmentsScroll.setVisibility(View.GONE);
                }
            }
            
            // Bind selection checkbox without triggering the listener
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

            // Highlight row if actively being edited
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

