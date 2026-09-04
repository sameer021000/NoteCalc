package com.example.notecalc;

import android.view.View;
import android.widget.TextView;

public class MenuAccountHelper {

    @android.annotation.SuppressLint("InflateParams")
    public static void showPopupMenu(MainActivity activity, View anchor, Account account) {
        View popupView = activity.getLayoutInflater().inflate(R.layout.layout_popup_menu, null);
        
        android.widget.PopupWindow popupWindow = new android.widget.PopupWindow(
                popupView,
                (int) (180 * activity.getResources().getDisplayMetrics().density),
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setElevation(8.0f);
        
        View btnDownload = popupView.findViewById(R.id.btn_popup_download);
        View btnDelete = popupView.findViewById(R.id.btn_popup_delete);
        
        ResponsiveUI.setupClickable(btnDownload, false, () -> {
            popupWindow.dismiss();
            PdfDialogHelper.showPdfSortDialog(activity, order -> PdfHelper.generateAndOpenPdf(activity, account, order));
        });
        
        View btnArchive = popupView.findViewById(R.id.btn_popup_archive);
        TextView textArchive = popupView.findViewById(R.id.text_popup_archive);
        if (textArchive != null) {
            textArchive.setText(account.isArchived() ? "Un-Archive" : "Archive");
        }
        
        ResponsiveUI.setupClickable(btnArchive, false, () -> {
            popupWindow.dismiss();
            account.setArchived(!account.isArchived());
            StorageHelper.saveAppStorage(activity, activity.appStorage);
            DashboardHelper.refreshDashboardList(activity);
        });

        ResponsiveUI.setupClickable(btnDelete, false, () -> {
            popupWindow.dismiss();
            AccountDialogHelper.showDeleteAccountConfirmationDialog(activity, account);
        });
        
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = popupView.getMeasuredHeight();
        
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        int anchorY = location[1];
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        
        if (anchorY + popupHeight > screenHeight - 150) {
            popupWindow.showAsDropDown(anchor, anchor.getWidth() / 2, -anchor.getHeight() - popupHeight);
        } else {
            popupWindow.showAsDropDown(anchor, anchor.getWidth() / 2, -anchor.getHeight() / 2);
        }
    }
}
