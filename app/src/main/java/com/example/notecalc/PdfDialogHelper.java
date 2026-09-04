package com.example.notecalc;

import android.widget.TextView;

import com.example.notecalc.pdf.PdfSortCallback;
import com.example.notecalc.pdf.PdfSortOrder;

public class PdfDialogHelper {

    public static void showPdfSortDialog(MainActivity activity, PdfSortCallback callback) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity, R.style.CustomDialogTheme);
        android.view.View view = activity.getLayoutInflater().inflate(R.layout.layout_dialog_pdf_sort, null);
        builder.setView(view);
        android.app.AlertDialog dialog = builder.create();

        TextView optSno = view.findViewById(R.id.option_sort_sno);
        TextView optDesc = view.findViewById(R.id.option_sort_desc);
        TextView optDate = view.findViewById(R.id.option_sort_date);
        TextView optAmount = view.findViewById(R.id.option_sort_amount);
        
        android.graphics.drawable.Drawable unselectedBg = ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 8.0f);
        android.graphics.drawable.Drawable selectedBg = ResponsiveUI.createRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), ThemeManager.getPrimaryAccentColor(activity), 1.0f, 8.0f);
        
        final PdfSortOrder[] selectedOrder = {PdfSortOrder.SNO}; // Default
        
        Runnable updateSelection = () -> {
            optSno.setBackground(selectedOrder[0] == PdfSortOrder.SNO ? selectedBg : unselectedBg);
            optDesc.setBackground(selectedOrder[0] == PdfSortOrder.DESCRIPTION ? selectedBg : unselectedBg);
            optDate.setBackground(selectedOrder[0] == PdfSortOrder.DATE ? selectedBg : unselectedBg);
            optAmount.setBackground(selectedOrder[0] == PdfSortOrder.AMOUNT ? selectedBg : unselectedBg);
            
            optSno.setTextColor(selectedOrder[0] == PdfSortOrder.SNO ? android.graphics.Color.WHITE : activity.getColor(R.color.text_primary));
            optDesc.setTextColor(selectedOrder[0] == PdfSortOrder.DESCRIPTION ? android.graphics.Color.WHITE : activity.getColor(R.color.text_primary));
            optDate.setTextColor(selectedOrder[0] == PdfSortOrder.DATE ? android.graphics.Color.WHITE : activity.getColor(R.color.text_primary));
            optAmount.setTextColor(selectedOrder[0] == PdfSortOrder.AMOUNT ? android.graphics.Color.WHITE : activity.getColor(R.color.text_primary));
        };
        
        optSno.setOnClickListener(v -> { selectedOrder[0] = PdfSortOrder.SNO; updateSelection.run(); });
        optDesc.setOnClickListener(v -> { selectedOrder[0] = PdfSortOrder.DESCRIPTION; updateSelection.run(); });
        optDate.setOnClickListener(v -> { selectedOrder[0] = PdfSortOrder.DATE; updateSelection.run(); });
        optAmount.setOnClickListener(v -> { selectedOrder[0] = PdfSortOrder.AMOUNT; updateSelection.run(); });
        
        updateSelection.run(); // Init

        android.view.View btnCancel = view.findViewById(R.id.btn_dialog_cancel);
        android.view.View btnExport = view.findViewById(R.id.btn_dialog_export);
        
        view.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.5f, 12f));

        btnCancel.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 8.0f));
        btnExport.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), ThemeManager.getPrimaryAccentColor(activity), 1.0f, 8.0f));

        ResponsiveUI.setupClickable(btnCancel, true, dialog::dismiss);
        ResponsiveUI.setupClickable(btnExport, true, () -> {
            dialog.dismiss();
            callback.onSortSelected(selectedOrder[0]);
        });

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    public static android.app.Dialog showProgressDialog(MainActivity activity) {
        android.app.Dialog dialog = new android.app.Dialog(activity);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        android.widget.LinearLayout layout = new android.widget.LinearLayout(activity);
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setPadding(60, 60, 60, 60);
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        layout.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 16.0f));

        android.widget.ProgressBar progressBar = new android.widget.ProgressBar(activity);
        progressBar.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(ThemeManager.getPrimaryAccentColor(activity)));

        android.widget.TextView tvMessage = new android.widget.TextView(activity);
        tvMessage.setText(activity.getString(R.string.msg_generating_pdf));
        tvMessage.setTextColor(activity.getColor(R.color.text_primary));
        tvMessage.setTextSize(16f);
        tvMessage.setPadding(40, 0, 0, 0);

        layout.addView(progressBar);
        layout.addView(tvMessage);

        dialog.setContentView(layout);
        dialog.show();

        return dialog;
    }
}
