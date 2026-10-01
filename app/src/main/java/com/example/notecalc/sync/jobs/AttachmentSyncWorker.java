package com.example.notecalc.sync.jobs;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.Data;
import androidx.documentfile.provider.DocumentFile;

import com.example.notecalc.sync.core.SafHelper;
import com.example.notecalc.sync.models.SyncConfig;
import org.json.JSONObject;

public class AttachmentSyncWorker extends Worker {

    public static final String PROGRESS_KEY = "PROGRESS";

    public AttachmentSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        if (!SyncConfig.isSyncEnabled(context)) {
            return Result.success();
        }

        String safUri = SyncConfig.getSafUriString(context);
        if (safUri == null || safUri.isEmpty()) {
            return Result.failure();
        }

        try {
            DocumentFile cloudFolder = SafHelper.getRootFolder(context, safUri);
            if (cloudFolder == null) {
                return Result.failure();
            }

            setProgressAsync(new Data.Builder().putInt(PROGRESS_KEY, 0).build());
            
            syncAttachmentsWithManifest(context, cloudFolder);

            SyncConfig.setLastAttachmentSyncTimestamp(context, System.currentTimeMillis());
            setProgressAsync(new Data.Builder().putInt(PROGRESS_KEY, 100).build());

            return Result.success();
        } catch (Exception e) {
            android.util.Log.e("AttachmentSyncWorker", "Error during attachment sync", e);
            return Result.retry();
        }
    }

    private void syncAttachmentsWithManifest(Context context, DocumentFile cloudFolder) {
        java.io.File localAttachDir = new java.io.File(context.getFilesDir(), "attachments");
        if (!localAttachDir.exists()) {
            if (!localAttachDir.mkdirs()) {
                return;
            }
        }

        // Phase 1: Local Duplicate Cleanup (The Pollution Fix)
        java.io.File[] rawFiles = localAttachDir.listFiles();
        if (rawFiles != null) {
            java.util.regex.Pattern duplicatePattern = java.util.regex.Pattern.compile("^.*(?:(?:\\s*\\(\\d+\\))+\\.[a-zA-Z0-9]+|\\.[a-zA-Z0-9]+(?:\\s*\\(\\d+\\))+)$");
            for (java.io.File rf : rawFiles) {
                if (rf.isFile() && duplicatePattern.matcher(rf.getName()).matches()) {
                    rf.delete();
                }
            }
        }

        DocumentFile remoteAttachDir = SafHelper.getOrCreateDirectory(cloudFolder, "attachments");
        if (remoteAttachDir == null) return;

        // Load or create cloud manifest
        JSONObject manifest = new JSONObject();
        DocumentFile manifestFile = remoteAttachDir.findFile("attachments_manifest.json");
        if (manifestFile != null && manifestFile.exists()) {
            try {
                String manifestStr = SafHelper.readTextFile(context, manifestFile);
                if (manifestStr != null && !manifestStr.isEmpty()) {
                    manifest = new JSONObject(manifestStr);
                }
            } catch (Exception e) {
                android.util.Log.e("AttachmentSyncWorker", "Error reading manifest", e);
            }
        }

        java.io.File[] localFiles = localAttachDir.listFiles();
        if (localFiles == null) localFiles = new java.io.File[0];

        int totalFiles = localFiles.length;
        if (totalFiles == 0) return;

        boolean manifestChanged = false;

        for (int i = 0; i < totalFiles; i++) {
            java.io.File lf = localFiles[i];
            if (!lf.isFile()) continue;

            String fileName = lf.getName();
            long localSize = lf.length();
            long localMod = lf.lastModified();

            boolean needsUpload = true;
            try {
                if (manifest.has(fileName)) {
                    JSONObject fileInfo = manifest.getJSONObject(fileName);
                    long cloudSize = fileInfo.optLong("size", -1);
                    long cloudMod = fileInfo.optLong("modified", -1);
                    if (cloudSize == localSize && cloudMod == localMod) {
                        needsUpload = false; // Delta sync match
                    }
                }
            } catch (Exception e) { }

            if (needsUpload) {
                String mimeType = fileName.toLowerCase().endsWith(".pdf") ? "application/pdf" : "image/*";
                // Delete existing duplicate if we are re-uploading
                DocumentFile existing = remoteAttachDir.findFile(fileName);
                if (existing != null) {
                    existing.delete();
                }
                SafHelper.copyFileToSaf(context, lf, remoteAttachDir, mimeType);

                // Update manifest
                try {
                    JSONObject fileInfo = new JSONObject();
                    fileInfo.put("size", localSize);
                    fileInfo.put("modified", localMod);
                    manifest.put(fileName, fileInfo);
                    manifestChanged = true;
                } catch (Exception e) { }
            }

            int progress = (int) (((i + 1) / (float) totalFiles) * 95); // Reserve 5% for finalizing
            setProgressAsync(new Data.Builder().putInt(PROGRESS_KEY, progress).build());
        }

        if (manifestChanged) {
            SafHelper.writeTextFile(context, remoteAttachDir, "attachments_manifest.json", manifest.toString());
        }
    }
}
