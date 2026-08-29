package com.example.notecalc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.app.DatePickerDialog;
import androidx.appcompat.app.AlertDialog;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class DialogHelper {

    public static void showTipsDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_tips, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Setup expandable sections
        int[] headerIds = {R.id.header_section_1, R.id.header_section_2, R.id.header_section_3, R.id.header_section_4, R.id.header_section_5};
        int[] contentIds = {R.id.content_section_1, R.id.content_section_2, R.id.content_section_3, R.id.content_section_4, R.id.content_section_5};
        int[] chevronIds = {R.id.tv_chevron_1, R.id.tv_chevron_2, R.id.tv_chevron_3, R.id.tv_chevron_4, R.id.tv_chevron_5};

        for (int i = 0; i < 5; i++) {
            View header = view.findViewById(headerIds[i]);
            View content = view.findViewById(contentIds[i]);
            TextView chevron = view.findViewById(chevronIds[i]);
            
            if (header != null && content != null && chevron != null) {
                // Apply curved bordered background to header
                header.setBackground(ResponsiveUI.createRoundedBg(
                        context,
                        ThemeManager.getBgSecondaryColor(context),
                        ThemeManager.getBorderColor(context),
                        1.0f,
                        10.0f
                ));
                
                // Add some margin below the header so the border isn't cramped
                android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) header.getLayoutParams();
                params.bottomMargin = 24;
                header.setLayoutParams(params);

                header.setOnClickListener(v -> {
                    if (content.getVisibility() == View.VISIBLE) {
                        content.setVisibility(View.GONE);
                        chevron.setText("▼"); // down chevron
                    } else {
                        content.setVisibility(View.VISIBLE);
                        chevron.setText("▲"); // up chevron
                    }
                });
            }
        }
        
        // Round the dialog box corners
        view.setBackground(ResponsiveUI.createRoundedBg(
                context,
                ThemeManager.getBgPrimaryColor(context),
                android.graphics.Color.TRANSPARENT,
                0f,
                16f
        ));
        
        // Round the Got it! button corners
        View btnClose = view.findViewById(R.id.btn_tips_close);
        btnClose.setBackground(ResponsiveUI.createRoundedBg(
                context,
                ThemeManager.getSecondaryAccentColor(context),
                android.graphics.Color.TRANSPARENT,
                0f,
                12f
        ));

        ResponsiveUI.setupClickable(btnClose, true, dialog::dismiss);
        dialog.show();
    }

    public interface OnDateSelectedListener {
        void onDateSelected(String date);
    }

    public static void showDatePicker(Context context, String initialDate, TextView dateTextWidget, OnDateSelectedListener listener) {
        Calendar cal = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            if (initialDate != null && !initialDate.isEmpty()) {
                Date date = sdf.parse(initialDate);
                if (date != null) {
                    cal.setTime(date);
                }
            }
        } catch (Exception ignored) {}

        DatePickerDialog picker = new DatePickerDialog(
                context,
                (view1, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    String formattedDate = sdf.format(selected.getTime());
                    if (dateTextWidget != null) {
                        dateTextWidget.setText(formattedDate);
                    }
                    if (listener != null) {
                        listener.onDateSelected(formattedDate);
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }
}
