package com.example.notecalc;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

public class AboutCurrentVersionHelper {

    public static void showDialog(MainActivity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.layout_about_current_version, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }

        View rootLayout = (View) dialogView.getParent();
        if (rootLayout == null) {
            rootLayout = dialogView; // Fallback
        }
        
        // Apply responsive rounded background matching the app's styling
        rootLayout.setBackground(ResponsiveUI.createRoundedBg(
                activity, 
                ThemeManager.getBgPrimaryColor(activity), 
                ThemeManager.getBorderColor(activity), 
                1.0f, 
                16f
        ));

        TextView btnDismiss = dialogView.findViewById(R.id.btn_about_version_dismiss);
        btnDismiss.setBackground(ResponsiveUI.createRoundedBg(
                activity,
                ThemeManager.getSecondaryAccentColor(activity),
                android.graphics.Color.TRANSPARENT,
                0f,
                12f
        ));
        activity.setupClickable(btnDismiss, true, dialog::dismiss);

        dialog.show();
    }
}
