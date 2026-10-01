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
        android.view.View attachmentSyncDetails = view.findViewById(com.example.notecalc.R.id.ll_attachment_sync_details);
        TextView tvLocalImageCount = view.findViewById(com.example.notecalc.R.id.tv_local_image_count);
        TextView tvLastAttachmentSync = view.findViewById(com.example.notecalc.R.id.tv_last_attachment_sync);
        android.view.View attachmentProgressContainer = view.findViewById(com.example.notecalc.R.id.ll_attachment_progress_container);
        android.widget.ProgressBar pbAttachmentSync = view.findViewById(com.example.notecalc.R.id.pb_attachment_sync);
        TextView tvAttachmentSyncProgress = view.findViewById(com.example.notecalc.R.id.tv_attachment_sync_progress);
        TextView btnSyncAttachmentsNow = view.findViewById(com.example.notecalc.R.id.btn_sync_attachments_now);
        android.view.View vSeparator = view.findViewById(com.example.notecalc.R.id.v_separator);
        TextView btnClose = view.findViewById(com.example.notecalc.R.id.btn_close);
        
        btnConnect.setBackground(ResponsiveUI.createRippleRoundedBg(activity, ThemeManager.getPrimaryAccentColor(activity), 0, 0f, 12f));
        btnSyncNow.setBackground(ResponsiveUI.createRippleRoundedBg(activity, android.graphics.Color.TRANSPARENT, ThemeManager.getPrimaryAccentColor(activity), 1.5f, 12f));
        btnSyncAttachmentsNow.setBackground(ResponsiveUI.createRippleRoundedBg(activity, android.graphics.Color.TRANSPARENT, ThemeManager.getPrimaryAccentColor(activity), 1.5f, 12f));
        btnDisconnect.setBackground(ResponsiveUI.createRippleRoundedBg(activity, android.graphics.Color.TRANSPARENT, android.graphics.Color.parseColor("#F44336"), 1.5f, 12f));
        
        pbAttachmentSync.setProgressTintList(android.content.res.ColorStateList.valueOf(ThemeManager.getPrimaryAccentColor(activity)));
        pbAttachmentSync.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(ThemeManager.getPrimaryAccentColor(activity)));
        
        Runnable refreshCloudSyncUI = () -> {
            boolean isConnected = com.example.notecalc.sync.models.SyncConfig.isSyncEnabled(activity);
            if (isConnected) {
                tvStatus.setText(activity.getString(com.example.notecalc.R.string.connected_to_cloud));
                tvStatus.setTextColor(android.graphics.Color.parseColor("#16A34A"));
                btnConnect.setVisibility(android.view.View.GONE);
                syncImagesContainer.setVisibility(android.view.View.VISIBLE);
                btnSyncNow.setVisibility(android.view.View.VISIBLE);
                btnDisconnect.setVisibility(android.view.View.VISIBLE);
                vSeparator.setVisibility(android.view.View.VISIBLE);
                btnSyncAttachmentsNow.setVisibility(android.view.View.VISIBLE);
                
                long lastSync = com.example.notecalc.sync.models.SyncConfig.getLastSyncTimestamp(activity);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm:ss a", java.util.Locale.getDefault());
                if (lastSync > 0) {
                    tvLastSync.setText(activity.getString(com.example.notecalc.R.string.last_sync_time, sdf.format(new java.util.Date(lastSync))));
                } else {
                    tvLastSync.setText(activity.getString(com.example.notecalc.R.string.last_sync_never));
                }
                
                long lastAttachSync = com.example.notecalc.sync.models.SyncConfig.getLastAttachmentSyncTimestamp(activity);
                if (lastAttachSync > 0) {
                    tvLastAttachmentSync.setText(activity.getString(com.example.notecalc.R.string.last_sync_time, sdf.format(new java.util.Date(lastAttachSync))));
                } else {
                    tvLastAttachmentSync.setText(activity.getString(com.example.notecalc.R.string.last_sync_never));
                }
                
                java.io.File attachDir = new java.io.File(activity.getFilesDir(), "attachments");
                int imgCount = 0;
                if (attachDir.exists()) {
                    java.io.File[] files = attachDir.listFiles();
                    if (files != null) {
                        java.util.regex.Pattern duplicatePattern = java.util.regex.Pattern.compile("^.*(?:(?:\\s*\\(\\d+\\))+\\.[a-zA-Z0-9]+|\\.[a-zA-Z0-9]+(?:\\s*\\(\\d+\\))+)$");
                        for (java.io.File f : files) {
                            if (f.isFile()) {
                                if (duplicatePattern.matcher(f.getName()).matches()) {
                                    @SuppressWarnings("unused")
                                    boolean ignored = f.delete();
                                } else {
                                    imgCount++;
                                }
                            }
                        }
                    }
                }
                tvLocalImageCount.setText(activity.getString(com.example.notecalc.R.string.total_local_images_count, imgCount));
                
                attachmentSyncDetails.setVisibility(android.view.View.VISIBLE);
                
                String uriStr = com.example.notecalc.sync.models.SyncConfig.getSafUriString(activity);
                if (uriStr != null) {
                    androidx.documentfile.provider.DocumentFile df = androidx.documentfile.provider.DocumentFile.fromTreeUri(activity, android.net.Uri.parse(uriStr));
                    if (df != null && df.getName() != null) {
                        String readablePath = df.getName();
                        try {
                            android.net.Uri parsedUri = android.net.Uri.parse(uriStr);
                            String authority = parsedUri.getAuthority();
                            String decodedPath = android.net.Uri.decode(uriStr);
                            
                            if (authority != null && authority.contains("com.google.android.apps.docs")) {
                                readablePath = "Google Drive / " + df.getName();
                            } else {
                                int treeIdx = decodedPath.indexOf("/tree/");
                                if (treeIdx != -1) {
                                    String sub = decodedPath.substring(treeIdx + 6);
                                    String[] parts = sub.split(":");
                                    if (parts.length == 2) {
                                        String root = parts[0].equals("primary") ? "Internal Storage" : (parts[0].matches("[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}") ? "SD Card" : parts[0]);
                                        readablePath = root + " / " + parts[1].replace("/", " / ");
                                    } else if (parts.length == 1) {
                                        readablePath = parts[0].equals("primary") ? "Internal Storage" : (parts[0].matches("[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}") ? "SD Card" : parts[0]);
                                    }
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
                attachmentSyncDetails.setVisibility(android.view.View.GONE);
                btnSyncNow.setVisibility(android.view.View.GONE);
                btnDisconnect.setVisibility(android.view.View.GONE);
                vSeparator.setVisibility(android.view.View.GONE);
                btnSyncAttachmentsNow.setVisibility(android.view.View.GONE);
            }
        };
        refreshCloudSyncUI.run();
        
        androidx.work.WorkManager.getInstance(activity).getWorkInfosForUniqueWorkLiveData("AttachmentSyncJob").observe(activity, workInfos -> {
            if (workInfos != null && !workInfos.isEmpty()) {
                androidx.work.WorkInfo workInfo = workInfos.get(0);
                if (workInfo.getState() == androidx.work.WorkInfo.State.RUNNING || workInfo.getState() == androidx.work.WorkInfo.State.ENQUEUED) {
                    attachmentProgressContainer.setVisibility(android.view.View.VISIBLE);
                    btnSyncAttachmentsNow.setEnabled(false);
                    btnSyncAttachmentsNow.setAlpha(0.5f);
                    int progress = workInfo.getProgress().getInt(com.example.notecalc.sync.jobs.AttachmentSyncWorker.PROGRESS_KEY, 0);
                    pbAttachmentSync.setProgress(progress);
                    tvAttachmentSyncProgress.setText(activity.getString(com.example.notecalc.R.string.attachment_sync_progress_format, progress));
                } else if (workInfo.getState().isFinished() && btnSyncAttachmentsNow.getAlpha() == 0.5f) {
                    pbAttachmentSync.setProgress(100);
                    tvAttachmentSyncProgress.setText(activity.getString(com.example.notecalc.R.string.attachment_sync_progress_format, 100));
                    btnSyncAttachmentsNow.setEnabled(true);
                    btnSyncAttachmentsNow.setAlpha(1.0f);
                    android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                    handler.postDelayed(() -> {
                        attachmentProgressContainer.setVisibility(android.view.View.GONE);
                        refreshCloudSyncUI.run();
                    }, 1000);
                }
            }
        });
        
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
        
        ResponsiveUI.setupClickable(btnDisconnect, true, () -> showDisconnectConfirmDialog(activity, tvFolderPath.getText().toString(), () -> {
            com.example.notecalc.sync.models.SyncConfig.setSyncEnabled(activity, false);
            com.example.notecalc.sync.models.SyncConfig.setSafUriString(activity, null);
            com.example.notecalc.sync.models.SyncConfig.setLastSyncTimestamp(activity, 0);
            com.example.notecalc.sync.models.SyncConfig.setSyncImagesEnabled(activity, false);
            refreshCloudSyncUI.run();
            if (activity.settingsRefreshDashboardCloudUI != null) {
                activity.settingsRefreshDashboardCloudUI.run();
            }
        }));
        
        ResponsiveUI.setupClickable(btnSyncNow, true, () -> {
            android.widget.Toast.makeText(activity, activity.getString(com.example.notecalc.R.string.syncing_dots), android.widget.Toast.LENGTH_SHORT).show();
            com.example.notecalc.sync.core.SyncManager.recordSave(activity);
        });
        
        ResponsiveUI.setupClickable(btnSyncAttachmentsNow, true, () -> {
            attachmentProgressContainer.setVisibility(android.view.View.VISIBLE);
            pbAttachmentSync.setProgress(0);
            tvAttachmentSyncProgress.setText("0%");
            btnSyncAttachmentsNow.setEnabled(false);
            btnSyncAttachmentsNow.setAlpha(0.5f);
            
            androidx.work.OneTimeWorkRequest syncRequest = new androidx.work.OneTimeWorkRequest.Builder(com.example.notecalc.sync.jobs.AttachmentSyncWorker.class).build();
            androidx.work.WorkManager.getInstance(activity).enqueueUniqueWork("AttachmentSyncJob", androidx.work.ExistingWorkPolicy.REPLACE, syncRequest);
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

    private static void showDisconnectConfirmDialog(MainActivity activity, String currentPath, Runnable onConfirm) {
        android.view.View view = activity.getLayoutInflater().inflate(com.example.notecalc.R.layout.layout_dialog_cloud_sync_disconnect_confirm, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(ResponsiveUI.createRoundedBg(activity, ThemeManager.getBgPrimaryColor(activity), ThemeManager.getBorderColor(activity), 1f, 24f));
        }
        
        TextView tvPath = view.findViewById(com.example.notecalc.R.id.tv_disconnect_path);
        TextView btnCancel = view.findViewById(com.example.notecalc.R.id.btn_disconnect_cancel);
        TextView btnConfirm = view.findViewById(com.example.notecalc.R.id.btn_disconnect_confirm);
        
        if (currentPath != null && !currentPath.isEmpty()) {
            tvPath.setText(activity.getString(com.example.notecalc.R.string.disconnect_confirm_path, currentPath.replace("Folder: ", "")));
        } else {
            tvPath.setVisibility(android.view.View.GONE);
        }
        
        ResponsiveUI.setupClickable(btnCancel, true, dialog::dismiss);
        
        ResponsiveUI.setupClickable(btnConfirm, true, () -> {
            dialog.dismiss();
            if (onConfirm != null) onConfirm.run();
        });
        
        dialog.show();
    }
}
