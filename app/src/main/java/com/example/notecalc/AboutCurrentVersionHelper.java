package com.example.notecalc;

import android.content.Context;
import android.graphics.Color;
import androidx.core.text.HtmlCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

public class AboutCurrentVersionHelper {

    public static void showDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.layout_about_current_version, null);
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
                context, 
                ThemeManager.getBgPrimaryColor(context), 
                ThemeManager.getBorderColor(context), 
                1.0f, 
                16f
        ));

        // Parse HTML formatting for the content
        TextView tvContent = dialogView.findViewById(R.id.tv_about_version_content);
        tvContent.setText(HtmlCompat.fromHtml(context.getString(R.string.about_version_content), HtmlCompat.FROM_HTML_MODE_COMPACT));

        TextView btnDismiss = dialogView.findViewById(R.id.btn_about_version_dismiss);
        btnDismiss.setBackground(ResponsiveUI.createRoundedBg(
                context,
                ThemeManager.getSecondaryAccentColor(context),
                android.graphics.Color.TRANSPARENT,
                0f,
                12f
        ));
        btnDismiss.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
