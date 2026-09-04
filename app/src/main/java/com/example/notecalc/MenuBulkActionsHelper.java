package com.example.notecalc;

import android.view.View;
import android.widget.ImageView;
import java.util.List;
import java.util.ArrayList;

public class MenuBulkActionsHelper {

    @android.annotation.SuppressLint("InflateParams")
    public static void showMenu(MainActivity activity, View anchor) {
        View popupView = activity.getLayoutInflater().inflate(R.layout.layout_bulk_actions_menu, null);
        
        android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(
                popupView,
                (int) (220 * activity.getResources().getDisplayMetrics().density),
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setElevation(8.0f);
        
        View btnFilter = popupView.findViewById(R.id.btn_popup_filter);
        View btnExport = popupView.findViewById(R.id.btn_popup_export_pdf);
        View btnCut = popupView.findViewById(R.id.btn_popup_cut);
        View btnCopy = popupView.findViewById(R.id.btn_popup_copy);
        View btnDelete = popupView.findViewById(R.id.btn_popup_delete);
        android.widget.ImageView filterIcon = popupView.findViewById(R.id.img_popup_filter_icon);
        if (filterIcon != null && activity.recordsAdapter != null) {
            filterIcon.setColorFilter(null);
            if (activity.recordsAdapter.filterCategories.isEmpty()) {
                android.util.TypedValue typedValue = new android.util.TypedValue();
                activity.getTheme().resolveAttribute(R.attr.colorAccentPrimary, typedValue, true);
                filterIcon.setImageTintList(android.content.res.ColorStateList.valueOf(typedValue.data));
            } else {
                filterIcon.setImageTintList(android.content.res.ColorStateList.valueOf(ThemeManager.getSecondaryAccentColor(activity)));
            }
        }
        
        List<Record> selectedRecords = new ArrayList<>();
        for (Record r : StateHelper.getActiveRecords(activity)) if (r.isSelected()) selectedRecords.add(r);
        boolean hasSelection = !selectedRecords.isEmpty();
        
        if (btnExport != null) {
            btnExport.setAlpha(hasSelection ? 1.0f : 0.4f);
            btnExport.setEnabled(hasSelection);
        }
        btnCut.setAlpha(hasSelection ? 1.0f : 0.4f);
        btnCut.setEnabled(hasSelection);
        btnCopy.setAlpha(hasSelection ? 1.0f : 0.4f);
        btnCopy.setEnabled(hasSelection);
        btnDelete.setAlpha(hasSelection ? 1.0f : 0.4f);
        btnDelete.setEnabled(hasSelection);
        
        if (activity.currentEditingAccount != null && activity.currentEditingAccount.isArchived()) {
            btnCut.setVisibility(View.GONE);
            btnCopy.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            View sepCut = popupView.findViewById(R.id.sep_cut);
            View sepCopy = popupView.findViewById(R.id.sep_copy);
            View sepDelete = popupView.findViewById(R.id.sep_delete);
            if (sepCut != null) sepCut.setVisibility(View.GONE);
            if (sepCopy != null) sepCopy.setVisibility(View.GONE);
            if (sepDelete != null) sepDelete.setVisibility(View.GONE);
        }
        
        ResponsiveUI.setupClickable(btnFilter, false, () -> {
            popupWindow.dismiss();
            FilterHelper.showCategoryFilterDialog(activity, activity.currentEditingAccount, (ImageView) anchor);
        });
        
        if (hasSelection) {
            if (btnExport != null) {
                ResponsiveUI.setupClickable(btnExport, false, () -> {
                    popupWindow.dismiss();
                    DialogHelper.showPdfSortDialog(activity, order -> PdfHelper.generateAndOpenSelectedPdf(activity, selectedRecords, order));
                });
            }
            ResponsiveUI.setupClickable(btnCut, false, () -> {
                popupWindow.dismiss();
                TransferHelper.showTransferDialog(activity, selectedRecords, true);
            });
            ResponsiveUI.setupClickable(btnCopy, false, () -> {
                popupWindow.dismiss();
                TransferHelper.showTransferDialog(activity, selectedRecords, false);
            });
            ResponsiveUI.setupClickable(btnDelete, false, () -> {
                popupWindow.dismiss();
                DialogHelper.showDeleteMultipleConfirmationDialog(activity, selectedRecords);
            });
        }
        
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = popupView.getMeasuredHeight();
        
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        int anchorY = location[1];
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        
        if (anchorY + popupHeight > screenHeight - 150) {
            popupWindow.showAsDropDown(anchor, 0, -anchor.getHeight() - popupHeight);
        } else {
            popupWindow.showAsDropDown(anchor, 0, 0);
        }
    }
}
