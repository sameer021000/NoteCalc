package com.example.notecalc;

import android.view.View;
import com.google.android.material.snackbar.Snackbar;

public class SnackbarHelper {
    public static void showUndoSnackbar(MainActivity activity, String message, final Runnable onUndo, final Runnable onCommit) {
        if (activity.currentSnackbar != null) {
            activity.currentSnackbar.dismiss();
            activity.currentSnackbar = null;
        }
        
        View targetView = activity.findViewById(android.R.id.content);
        if (targetView == null) targetView = activity.mainContainer;
        
        Snackbar snackbar = Snackbar.make(targetView, message, Snackbar.LENGTH_LONG);
        snackbar.setDuration(5000); // 5 seconds
        
        snackbar.setAction("UNDO", v -> {
            if (onUndo != null) onUndo.run();
        });
        
        snackbar.setActionTextColor(activity.getColor(R.color.error_red)); 
        snackbar.setTextColor(activity.getColor(R.color.text_primary));
        snackbar.setBackgroundTint(ThemeManager.getBgTertiaryColor(activity));
        
        View sbView = snackbar.getView();
        sbView.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgTertiaryColor(activity), 0, 0, 8f));
        
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (activity.currentSnackbar == transientBottomBar) {
                    activity.currentSnackbar = null;
                }
                if (event != DISMISS_EVENT_ACTION) {
                    if (onCommit != null) onCommit.run();
                }
            }
        });
        
        activity.currentSnackbar = snackbar;
        snackbar.show();
    }
}
