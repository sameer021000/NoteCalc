package com.example.notecalc.sync.jobs;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.documentfile.provider.DocumentFile;

import com.example.notecalc.storage.core.AppStorage;
import com.example.notecalc.storage.core.StorageHelper;
import com.example.notecalc.storage.mappers.AppStorageJsonMapper;
import com.example.notecalc.sync.core.MutationEngine;
import com.example.notecalc.sync.core.SafHelper;
import com.example.notecalc.sync.models.SyncConfig;

import org.json.JSONObject;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        // 1. Check if sync is globally enabled
        if (!SyncConfig.isSyncEnabled(context)) {
            return Result.success();
        }

        // 2. Ensure we have a valid SAF folder URI configured
        String safUri = SyncConfig.getSafUriString(context);
        if (safUri == null || safUri.isEmpty()) {
            return Result.failure();
        }

        try {
            DocumentFile cloudFolder = SafHelper.getRootFolder(context, safUri);
            if (cloudFolder == null) {
                return Result.failure();
            }

            // Load local storage
            AppStorage localStorage = StorageHelper.loadAppStorage(context);

            // Read cloud accounts.json
            DocumentFile cloudFile = cloudFolder.findFile("accounts.json");
            if (cloudFile == null || !cloudFile.exists()) {
                // Cloud file doesn't exist yet, this is the very first sync from this device.
                // Just push local state to cloud.
                String localJson = AppStorageJsonMapper.toJSONObject(localStorage).toString();
                SafHelper.writeTextFile(context, cloudFolder, "accounts.json", localJson);
                SyncConfig.setLastSyncTimestamp(context, System.currentTimeMillis());
                return Result.success();
            }

            // Cloud file exists, pull it down
            String cloudJsonString = SafHelper.readTextFile(context, cloudFile);
            if (cloudJsonString == null || cloudJsonString.isEmpty()) {
                return Result.retry();
            }

            JSONObject cloudJson = new JSONObject(cloudJsonString);
            AppStorage cloudStorage = AppStorageJsonMapper.fromJSONObject(cloudJson);

            // Re-load local storage to ensure we don't overwrite user edits made during the network IO above
            AppStorage latestLocal = StorageHelper.loadAppStorage(context);

            // Run the deep merge algorithm against the most up-to-date local data
            boolean needsPush = MutationEngine.mergeStorage(context, latestLocal, cloudStorage);

            // Save the merged result locally (silently to avoid infinite sync loops)
            StorageHelper.saveAppStorageSilently(context, latestLocal);

            if (needsPush) {
                // Push merged result back to cloud
                String mergedJsonString = AppStorageJsonMapper.toJSONObject(latestLocal).toString();
                SafHelper.writeTextFile(context, cloudFolder, "accounts.json", mergedJsonString);
            }

            // Record successful sync timestamp
            SyncConfig.setLastSyncTimestamp(context, System.currentTimeMillis());
            return Result.success();
        } catch (Exception e) {
            android.util.Log.e("SyncWorker", "Error during background sync execution", e);
            return Result.retry();
        }
    }
}
