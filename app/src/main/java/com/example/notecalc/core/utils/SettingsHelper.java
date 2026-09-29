package com.example.notecalc.core.utils;
import com.example.notecalc.core.ui.*;
import com.example.notecalc.dashboard.*;
import com.example.notecalc.pdf.*;
import com.example.notecalc.*;
import android.view.View;

public class SettingsHelper {
    private View settingsView;
    private final MainActivity activity;

    public SettingsHelper(MainActivity activity) {
        this.activity = activity;
    }

private void initSettings() {
        settingsView = activity.getLayoutInflater().inflate(R.layout.layout_settings, activity.mainContainer, false);

        settingsView.findViewById(R.id.btn_settings_back).setOnClickListener(v -> closeSettings());

        // Style the main cards
        View cardAppearance = settingsView.findViewById(R.id.card_appearance);
        View cardData = settingsView.findViewById(R.id.card_data_backup);
        View cardPrint = settingsView.findViewById(R.id.card_print_share);

        cardAppearance.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 16f));
        cardData.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 16f));
        cardPrint.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 16f));
        
        View cardCloudSyncEntry = settingsView.findViewById(R.id.card_cloud_sync_entry);
        if (cardCloudSyncEntry != null) {
            cardCloudSyncEntry.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 16f));
            android.widget.TextView btnOpenCloud = settingsView.findViewById(R.id.btn_open_cloud_sync_settings);
            android.widget.TextView tvCloudStatus = settingsView.findViewById(R.id.tv_settings_cloud_status);
            
            btnOpenCloud.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), 0, 0f, 12f));
            ResponsiveUI.setupClickable(btnOpenCloud, true, () -> com.example.notecalc.sync.ui.CloudSyncDialogHelper.showSettingsDialog(activity));
            
            Runnable refreshCloudSyncDashboardUI = getRefreshCloudSyncDashboardUIRunnable(activity, tvCloudStatus, btnOpenCloud);
            refreshCloudSyncDashboardUI.run();
            activity.settingsRefreshDashboardCloudUI = refreshCloudSyncDashboardUI;
        }

        // Setup theme buttons instead of RadioGroup
        android.widget.TextView btnSystem = settingsView.findViewById(R.id.btn_theme_system);
        android.widget.TextView btnLight = settingsView.findViewById(R.id.btn_theme_light);
        android.widget.TextView btnDark = settingsView.findViewById(R.id.btn_theme_dark);

        Runnable updateThemeButtons = () -> {
            int currentMode = ThemeManager.getDarkMode(activity);
            btnSystem.setBackground(ResponsiveUI.createRippleRoundedBg(activity, currentMode == ThemeManager.MODE_SYSTEM ? ThemeManager.getPrimaryAccentColor(activity) : android.graphics.Color.TRANSPARENT, ThemeManager.getBorderColor(activity), 1f, 8f));
            btnLight.setBackground(ResponsiveUI.createRippleRoundedBg(activity, currentMode == ThemeManager.MODE_LIGHT ? ThemeManager.getPrimaryAccentColor(activity) : android.graphics.Color.TRANSPARENT, ThemeManager.getBorderColor(activity), 1f, 8f));
            btnDark.setBackground(ResponsiveUI.createRippleRoundedBg(activity, currentMode == ThemeManager.MODE_DARK ? ThemeManager.getPrimaryAccentColor(activity) : android.graphics.Color.TRANSPARENT, ThemeManager.getBorderColor(activity), 1f, 8f));

            btnSystem.setTextColor(currentMode == ThemeManager.MODE_SYSTEM ? activity.getColor(R.color.text_on_accent) : activity.getColor(R.color.text_tertiary));
            btnLight.setTextColor(currentMode == ThemeManager.MODE_LIGHT ? activity.getColor(R.color.text_on_accent) : activity.getColor(R.color.text_tertiary));
            btnDark.setTextColor(currentMode == ThemeManager.MODE_DARK ? activity.getColor(R.color.text_on_accent) : activity.getColor(R.color.text_tertiary));
        };
        updateThemeButtons.run();

        ResponsiveUI.setupClickable(btnSystem, true, () -> { ThemeManager.setDarkMode(activity, ThemeManager.MODE_SYSTEM); updateThemeButtons.run(); });
        ResponsiveUI.setupClickable(btnLight, true, () -> { ThemeManager.setDarkMode(activity, ThemeManager.MODE_LIGHT); updateThemeButtons.run(); });
        ResponsiveUI.setupClickable(btnDark, true, () -> { ThemeManager.setDarkMode(activity, ThemeManager.MODE_DARK); updateThemeButtons.run(); });

        android.widget.LinearLayout llColors = settingsView.findViewById(R.id.ll_accent_colors);
        String[] colors = {ThemeManager.ACCENT_BLUE, ThemeManager.ACCENT_GREEN, ThemeManager.ACCENT_PURPLE, ThemeManager.ACCENT_YELLOW, ThemeManager.ACCENT_ORANGE, ThemeManager.ACCENT_PINK};
        String[] hexes = {"#0284C7", "#16A34A", "#9333EA", "#CA8A04", "#EA580C", "#DB2777"};
        String active = ThemeManager.getAccentColorName(activity);

        for (int i=0; i<colors.length; i++) {
            final String cName = colors[i];
            android.view.View circle = new android.view.View(activity);
            android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(100, 100);
            lp.setMargins(16, 16, 16, 16);
            circle.setLayoutParams(lp);

            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            gd.setCornerRadius(16f);
            gd.setColor(android.graphics.Color.parseColor(hexes[i]));
            if (cName.equals(active)) {
                gd.setStroke(8, ThemeManager.getSecondaryAccentColor(activity));
            }
            circle.setBackground(gd);
            ResponsiveUI.setupClickable(circle, true, () -> {
                ThemeManager.setAccentColor(activity, cName);
                activity.recreate();
            });
            llColors.addView(circle);
        }

        // Setup settings buttons with new design
        android.widget.TextView btnExportJson = settingsView.findViewById(R.id.btn_export_json);
        btnExportJson.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), 0, 0f, 12f));
        ResponsiveUI.setupClickable(btnExportJson, true, () -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(android.content.Intent.EXTRA_TITLE, "NoteCalc_Backup.json");
            activity.exportJsonLauncher.launch(intent);
        });

        android.widget.TextView btnImportJson = settingsView.findViewById(R.id.btn_import_json);
        btnImportJson.setBackground(ResponsiveUI.createRippleRoundedBg(activity, android.graphics.Color.TRANSPARENT, ThemeManager.getPrimaryAccentColor(activity), 1.5f, 12f));
        ResponsiveUI.setupClickable(btnImportJson, true, () -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            activity.importJsonLauncher.launch(intent);
        });

        android.widget.TextView btnExportPdf = settingsView.findViewById(R.id.btn_export_pdf_all);
        btnExportPdf.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getSecondaryAccentColor(activity), 0, 0f, 12f));
        ResponsiveUI.setupClickable(btnExportPdf, true, () -> PdfHelper.generateAndOpenAllPdf(activity));

        // Format version container
        android.view.View versionContainer = settingsView.findViewById(R.id.version_container);
        if (versionContainer != null) {
            versionContainer.setBackground(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgSecondaryColor(activity), ThemeManager.getBorderColor(activity), 1.0f, 24f));
            ResponsiveUI.setupClickable(versionContainer, true, () -> AboutCurrentVersionHelper.showDialog(activity));
        }
    }

public void openSettings() {
        if (settingsView == null) initSettings();
        activity.mainContainer.removeAllViews();
        activity.mainContainer.addView(settingsView);
    }

private void closeSettings() {
        activity.mainContainer.removeAllViews();
        DashboardHelper.showDashboard(activity);
    }
    private static Runnable getRefreshCloudSyncDashboardUIRunnable(com.example.notecalc.MainActivity activity, android.widget.TextView tvCloudStatus, android.widget.TextView btnOpenCloud) {
        return () -> {
            if (tvCloudStatus == null) return;
            boolean isConnected = com.example.notecalc.sync.models.SyncConfig.isSyncEnabled(activity);
            if (isConnected) {
                tvCloudStatus.setVisibility(View.VISIBLE);
                long lastSync = com.example.notecalc.sync.models.SyncConfig.getLastSyncTimestamp(activity);
                String timeStr = activity.getString(R.string.cloud_never_synced);
                if (lastSync > 0) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault());
                    timeStr = sdf.format(new java.util.Date(lastSync));
                }
                tvCloudStatus.setText(activity.getString(R.string.cloud_connected_last_sync, timeStr));
                btnOpenCloud.setText(activity.getString(R.string.manage_cloud_sync_settings));
            } else {
                tvCloudStatus.setVisibility(View.GONE);
                btnOpenCloud.setText(activity.getString(R.string.cloud_setup_sync));
            }
        };
    }
}