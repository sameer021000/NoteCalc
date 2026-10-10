package com.example.notecalc.core.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.example.notecalc.R;

public class CustomDialogBuilder {
    private final Context context;
    private final View dialogView;
    private final AlertDialog dialog;
    
    public CustomDialogBuilder(Context context, int layoutResId) {
        this.context = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        this.dialogView = LayoutInflater.from(context).inflate(layoutResId, null);
        builder.setView(dialogView);
        
        this.dialog = builder.create();
        if (this.dialog.getWindow() != null) {
            this.dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        View dialogRoot = dialogView.findViewById(R.id.dialog_root);
        if (dialogRoot != null) {
            dialogRoot.setBackground(ResponsiveUI.createRoundedBg(
                    context,
                    ThemeManager.getBgSecondaryColor(context),
                    ThemeManager.getBorderColor(context),
                    1.5f,
                    12f
            ));
        }
        
        ResponsiveUI.applyResponsiveness(dialogView);
    }
    
    public View getView() {
        return dialogView;
    }
    
    public AlertDialog getDialog() {
        return dialog;
    }
    
    public void setCancelButton(int buttonId, Runnable onClick) {
        View btn = dialogView.findViewById(buttonId);
        if (btn != null) {
            ResponsiveUI.setupClickable(btn, true, () -> {
                dialog.dismiss();
                if (onClick != null) onClick.run();
            });
        }
    }
    
    public void setConfirmButton(int buttonId, Runnable onClick) {
        View btn = dialogView.findViewById(buttonId);
        if (btn != null) {
            ResponsiveUI.setupClickable(btn, true, () -> {
                dialog.dismiss();
                if (onClick != null) onClick.run();
            });
        }
    }
    
    public void show() {
        dialog.show();
    }
}
