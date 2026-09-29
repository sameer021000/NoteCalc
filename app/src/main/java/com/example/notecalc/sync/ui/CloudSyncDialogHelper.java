package com.example.notecalc.sync.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;
import com.example.notecalc.MainActivity;
import com.example.notecalc.core.ui.ResponsiveUI;
import com.example.notecalc.core.ui.ThemeManager;
import com.example.notecalc.sync.models.SyncConfig;

public class CloudSyncDialogHelper {

    private static final String PREF_ONBOARDING_SHOWN = "cloud_sync_onboarding_shown";

    public static void checkAndShowOnboarding(MainActivity activity) {
        android.content.SharedPreferences prefs = activity.getSharedPreferences("sync_prefs", android.content.Context.MODE_PRIVATE);
        if (!prefs.getBoolean(PREF_ONBOARDING_SHOWN, false)) {
            showOnboardingDialog(activity);
            prefs.edit().putBoolean(PREF_ONBOARDING_SHOWN, true).apply();
        }
    }

    public static void showOnboardingDialog(MainActivity activity) {
        android.view.View view = activity.getLayoutInflater().inflate(com.example.notecalc.R.layout.layout_dialog_cloud_sync_onboarding, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1f, 24f));
        }
        
        TextView btnCancel = view.findViewById(com.example.notecalc.R.id.btn_cancel);
        TextView btnSetup = view.findViewById(com.example.notecalc.R.id.btn_setup);
        
        btnSetup.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), 0, 0f, 8f));
        
        ResponsiveUI.setupClickable(btnCancel, true, dialog::dismiss);
        ResponsiveUI.setupClickable(btnSetup, true, () -> {
            dialog.dismiss();
            launchSafPicker(activity);
        });
        
        dialog.show();
    }

    public static void showSettingsDialog(MainActivity activity) {
        android.view.View view = activity.getLayoutInflater().inflate(com.example.notecalc.R.layout.layout_dialog_cloud_sync_settings, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1f, 24f));
        }
        
        TextView tvStatus = view.findViewById(com.example.notecalc.R.id.tv_cloud_sync_status);
        TextView tvLastSync = view.findViewById(com.example.notecalc.R.id.tv_last_sync_timestamp);
        TextView tvFolderPath = view.findViewById(com.example.notecalc.R.id.tv_cloud_folder_path);
        TextView btnConnect = view.findViewById(com.example.notecalc.R.id.btn_connect_cloud);
        TextView btnSyncNow = view.findViewById(com.example.notecalc.R.id.btn_sync_now);
        TextView btnDisconnect = view.findViewById(com.example.notecalc.R.id.btn_disconnect_cloud);
        android.view.View syncImagesContainer = view.findViewById(com.example.notecalc.R.id.ll_sync_images_container);
        TextView btnClose = view.findViewById(com.example.notecalc.R.id.btn_close);
        
        btnConnect.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), 0, 0f, 12f));
        btnSyncNow.setBackground(ResponsiveUI.createRippleRoundedBg(activity, android.graphics.Color.TRANSPARENT, ThemeManager.getPrimaryAccentColor(activity), 1.5f, 12f));
        btnDisconnect.setBackground(ResponsiveUI.createRippleRoundedBg(activity, android.graphics.Color.TRANSPARENT, android.graphics.Color.parseColor("#F44336"), 1.5f, 12f));
        
        Runnable refreshCloudSyncUI = () -> {
            boolean isConnected = com.example.notecalc.sync.models.SyncConfig.isSyncEnabled(activity);
            if (isConnected) {
                tvStatus.setText(activity.getString(com.example.notecalc.R.string.connected_to_cloud));
                tvStatus.setTextColor(android.graphics.Color.parseColor("#16A34A"));
                btnConnect.setVisibility(android.view.View.GONE);
                syncImagesContainer.setVisibility(android.view.View.VISIBLE);
                btnSyncNow.setVisibility(android.view.View.VISIBLE);
                btnDisconnect.setVisibility(android.view.View.VISIBLE);
                
                long lastSync = com.example.notecalc.sync.models.SyncConfig.getLastSyncTimestamp(activity);
                if (lastSync > 0) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault());
                    tvLastSync.setText(activity.getString(com.example.notecalc.R.string.last_sync_time, sdf.format(new java.util.Date(lastSync))));
                } else {
                    tvLastSync.setText(activity.getString(com.example.notecalc.R.string.last_sync_never));
                }
                
                String uriStr = com.example.notecalc.sync.models.SyncConfig.getSafUriString(activity);
                if (uriStr != null) {
                    androidx.documentfile.provider.DocumentFile df = androidx.documentfile.provider.DocumentFile.fromTreeUri(activity, android.net.Uri.parse(uriStr));
                    if (df != null && df.getName() != null) {
                        String readablePath = df.getName();
                        try {
                            String decodedPath = android.net.Uri.decode(uriStr);
                            int treeIdx = decodedPath.indexOf("/tree/");
                            if (treeIdx != -1) {
                                String sub = decodedPath.substring(treeIdx + 6);
                                String[] parts = sub.split(":");
                                if (parts.length == 2) {
                                    String root = parts[0].equals("primary") ? "Internal Storage" : "SD Card";
                                    readablePath = root + " / " + parts[1].replace("/", " / ");
                                } else if (parts.length == 1) {
                                    readablePath = parts[0].equals("primary") ? "Internal Storage" : "SD Card";
                                }
                            }
                        } catch (Exception e) {
                            // Fallback to df.getName()
                        }
                        tvFolderPath.setText(activity.getString(com.example.notecalc.R.string.cloud_folder_path, readablePath));
                        tvFolderPath.setVisibility(android.view.View.VISIBLE);
                    } else {
                        tvFolderPath.setVisibility(android.view.View.GONE);
                    }
                } else {
                    tvFolderPath.setVisibility(android.view.View.GONE);
                }
                
            } else {
                tvStatus.setText(activity.getString(com.example.notecalc.R.string.not_connected));
                tvStatus.setTextColor(activity.getColor(com.example.notecalc.R.color.text_primary));
                tvLastSync.setText(activity.getString(com.example.notecalc.R.string.backup_data_securely));
                tvFolderPath.setVisibility(android.view.View.GONE);
                btnConnect.setVisibility(android.view.View.VISIBLE);
                syncImagesContainer.setVisibility(android.view.View.GONE);
                btnSyncNow.setVisibility(android.view.View.GONE);
                btnDisconnect.setVisibility(android.view.View.GONE);
            }
        };
        refreshCloudSyncUI.run();
        
        TextView btnSyncImgOn = view.findViewById(com.example.notecalc.R.id.btn_sync_img_on);
        TextView btnSyncImgOff = view.findViewById(com.example.notecalc.R.id.btn_sync_img_off);
        
        Runnable updateSyncImagesToggleUI = () -> {
            boolean isSyncImg = com.example.notecalc.sync.models.SyncConfig.isSyncImagesEnabled(activity);
            btnSyncImgOn.setBackground(ResponsiveUI.createRippleRoundedBg(activity, isSyncImg ? ThemeManager.getPrimaryAccentColor(activity) : android.graphics.Color.TRANSPARENT, ThemeManager.getBorderColor(activity), 1f, 8f));
            btnSyncImgOff.setBackground(ResponsiveUI.createRippleRoundedBg(activity, !isSyncImg ? ThemeManager.getPrimaryAccentColor(activity) : android.graphics.Color.TRANSPARENT, ThemeManager.getBorderColor(activity), 1f, 8f));
            
            btnSyncImgOn.setTextColor(isSyncImg ? activity.getColor(com.example.notecalc.R.color.text_on_accent) : activity.getColor(com.example.notecalc.R.color.text_tertiary));
            btnSyncImgOff.setTextColor(!isSyncImg ? activity.getColor(com.example.notecalc.R.color.text_on_accent) : activity.getColor(com.example.notecalc.R.color.text_tertiary));
        };
        updateSyncImagesToggleUI.run();
        
        ResponsiveUI.setupClickable(btnSyncImgOn, true, () -> {
            com.example.notecalc.sync.models.SyncConfig.setSyncImagesEnabled(activity, true);
            updateSyncImagesToggleUI.run();
        });
        
        ResponsiveUI.setupClickable(btnSyncImgOff, true, () -> {
            com.example.notecalc.sync.models.SyncConfig.setSyncImagesEnabled(activity, false);
            updateSyncImagesToggleUI.run();
        });
        
        ResponsiveUI.setupClickable(btnConnect, true, () -> {
            dialog.dismiss();
            showOnboardingDialog(activity);
        });
        
        ResponsiveUI.setupClickable(btnDisconnect, true, () -> {
            com.example.notecalc.sync.models.SyncConfig.setSyncEnabled(activity, false);
            com.example.notecalc.sync.models.SyncConfig.setSafUriString(activity, null);
            com.example.notecalc.sync.models.SyncConfig.setLastSyncTimestamp(activity, 0);
            com.example.notecalc.sync.models.SyncConfig.setSyncImagesEnabled(activity, false);
            refreshCloudSyncUI.run();
            if (activity.settingsRefreshDashboardCloudUI != null) {
                activity.settingsRefreshDashboardCloudUI.run();
            }
        });
        
        ResponsiveUI.setupClickable(btnSyncNow, true, () -> {
            android.widget.Toast.makeText(activity, activity.getString(com.example.notecalc.R.string.syncing_dots), android.widget.Toast.LENGTH_SHORT).show();
            com.example.notecalc.sync.core.SyncManager.recordSave(activity);
        });
        
        ResponsiveUI.setupClickable(btnClose, true, dialog::dismiss);
        
        // Let main activity know how to refresh this dialog if SAF finishes while it's open
        activity.settingsRefreshCloudUI = refreshCloudSyncUI;
        
        dialog.setOnDismissListener(d -> {
            if (activity.settingsRefreshDashboardCloudUI != null) {
                activity.settingsRefreshDashboardCloudUI.run();
            }
        });
        
        dialog.show();
    }

    private static void launchSafPicker(MainActivity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        activity.startActivityForResult(intent, MainActivity.REQUEST_CODE_SAF_FOLDER);
    }

    public static void handleSafResult(MainActivity activity, Intent data) {
        Uri uri = data.getData();
        if (uri == null) return;
        
        activity.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        
        String uriString = uri.toString();
        
        if (uriString.contains("com.android.externalstorage")) {
            showLocalFolderWarning(activity, uriString);
        } else {
            finalizeSyncSetup(activity, uriString);
        }
    }

    private static void showLocalFolderWarning(MainActivity activity, String uriString) {
        android.view.View view = activity.getLayoutInflater().inflate(com.example.notecalc.R.layout.layout_dialog_cloud_sync_local_warning, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1f, 24f));
        }
        
        TextView btnContinueAnyway = view.findViewById(com.example.notecalc.R.id.btn_continue_anyway);
        TextView btnChooseCloud = view.findViewById(com.example.notecalc.R.id.btn_choose_cloud);
        
        btnChooseCloud.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), 0, 0f, 8f));
        
        ResponsiveUI.setupClickable(btnContinueAnyway, true, () -> {
            dialog.dismiss();
            finalizeSyncSetup(activity, uriString);
        });
        
        ResponsiveUI.setupClickable(btnChooseCloud, true, () -> {
            dialog.dismiss();
            launchSafPicker(activity);
        });
        
        dialog.show();
    }

    private static void finalizeSyncSetup(MainActivity activity, String uriString) {
        SyncConfig.setSafUriString(activity, uriString);
        SyncConfig.setSyncEnabled(activity, true);
        
        android.widget.Toast.makeText(activity, activity.getString(com.example.notecalc.R.string.folder_linked_syncing), android.widget.Toast.LENGTH_LONG).show();
        com.example.notecalc.sync.core.SyncManager.recordSave(activity);
        
        if (activity.settingsRefreshCloudUI != null) {
            activity.settingsRefreshCloudUI.run();
        }
        if (activity.settingsRefreshDashboardCloudUI != null) {
            activity.settingsRefreshDashboardCloudUI.run();
        }
    }
}
