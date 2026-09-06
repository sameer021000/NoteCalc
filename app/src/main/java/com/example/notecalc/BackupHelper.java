package com.example.notecalc;

import static android.app.Activity.RESULT_OK;

public class BackupHelper {

    public static void handleExportResult(MainActivity activity, androidx.activity.result.ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            android.net.Uri uri = result.getData().getData();
            if (uri != null) {
                try {
                    java.io.OutputStream os = activity.getContentResolver().openOutputStream(uri);
                    if (os != null) {
                        String json = AppStorageJsonMapper.toJSONObject(activity.appStorage).toString(4);
                        os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        os.close();
                        android.widget.Toast.makeText(activity, "Backup Exported Successfully", android.widget.Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    android.util.Log.e("NoteCalc", "Error exporting JSON", e);
                    android.widget.Toast.makeText(activity, "Export failed", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static void handleImportResult(MainActivity activity, androidx.activity.result.ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            android.net.Uri uri = result.getData().getData();
            if (uri != null) {
                new androidx.appcompat.app.AlertDialog.Builder(activity, R.style.CustomDialogTheme)
                    .setTitle(activity.getString(R.string.auto_restore_backup_38))
                    .setMessage(activity.getString(R.string.auto_are_you_sure_this_wi_39))
                    .setPositiveButton("Overwrite", (d, w) -> {
                        try {
                            java.io.InputStream is = activity.getContentResolver().openInputStream(uri);
                            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) sb.append(line);
                            if (is != null) is.close();
                            
                            activity.appStorage = AppStorageJsonMapper.fromJSONObject(new org.json.JSONObject(sb.toString()));
                            StorageHelper.saveAppStorage(activity, activity.appStorage);
                            DashboardHelper.showDashboard(activity);
                            android.widget.Toast.makeText(activity, "Backup Restored!", android.widget.Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            android.util.Log.e("NoteCalc", "Error restoring JSON", e);
                            android.widget.Toast.makeText(activity, "Invalid backup file", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        }
    }
}
