package com.example.notecalc.records.adapters;
import com.example.notecalc.core.ui.*;
import com.example.notecalc.records.models.Record;
import com.example.notecalc.*;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;
import java.util.List;

public class RecordAttachmentBinder {

    @android.annotation.SuppressLint("ClickableViewAccessibility")
    public static void bindAttachments(MainActivity activity, Record record, RecordsAdapter.RecordViewHolder holder) {
        if (holder.attachmentSummary == null || holder.attachmentsScroll == null || holder.attachmentsContainer == null) {
            return;
        }

        if (record.getAttachments() != null && !record.getAttachments().isEmpty()) {
            List<String> atts = record.getAttachments();
            String firstPath = atts.get(0);
            File f = new File(firstPath);
            String name = f.getName();
            if (name.length() > 15) name = name.substring(0, 15) + "...";
            String icon = (firstPath.toLowerCase().endsWith(".pdf") || firstPath.toLowerCase().endsWith(".doc") || firstPath.toLowerCase().endsWith(".docx")) ? "\uD83D\uDCC4 " : "\uD83D\uDDBC ";

            if (atts.size() == 1) {
                holder.attachmentSummary.setText(String.format(java.util.Locale.getDefault(), "%s%s", icon, name));
            } else {
                holder.attachmentSummary.setText(String.format(java.util.Locale.getDefault(), "%s%s ▾", icon, name));
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
                File file = new File(path);
                String fname = file.getName();
                if (fname.length() > 15) fname = fname.substring(0, 15) + "...";
                String ficon = (path.toLowerCase().endsWith(".pdf") || path.toLowerCase().endsWith(".doc") || path.toLowerCase().endsWith(".docx")) ? "\uD83D\uDCC4 " : "\uD83D\uDDBC ";

                TextView chip = new TextView(activity);
                chip.setText(String.format(java.util.Locale.getDefault(), "%s%s", ficon, fname));
                chip.setTextSize(11);
                chip.setTextColor(activity.getColor(R.color.text_primary));
                chip.setBackground(ResponsiveUI.createButtonSelector(activity, ThemeManager.getBgSecondaryColor(activity), 6.0f));
                chip.setPadding(12, 6, 12, 6);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
                        Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", file);
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        viewIntent.setDataAndType(uri, activity.getContentResolver().getType(uri));
                        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        activity.startActivity(viewIntent);
                    } catch (Exception e) {
                        Toast.makeText(activity, "Cannot open file", Toast.LENGTH_SHORT).show();
                    }
                });
                holder.attachmentsContainer.addView(chip);
            }

            ResponsiveUI.setupClickable(holder.attachmentSummary, false, () -> {
                if (atts.size() == 1) {
                    try {
                        Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", f);
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        viewIntent.setDataAndType(uri, activity.getContentResolver().getType(uri));
                        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        activity.startActivity(viewIntent);
                    } catch (Exception e) {
                        Toast.makeText(activity, "Cannot open file", Toast.LENGTH_SHORT).show();
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
}