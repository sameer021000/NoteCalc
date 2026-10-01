package com.example.notecalc.sync.core;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingWorkPolicy;
import com.example.notecalc.sync.jobs.SyncWorker;
import com.example.notecalc.sync.models.SyncConfig;

public class SyncManager {
    public static void recordSave(Context context) {
        if (SyncConfig.isSyncEnabled(context)) {
            Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
                
            OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();
                
            WorkManager.getInstance(context).enqueueUniqueWork("NoteCalcCloudSync", ExistingWorkPolicy.REPLACE, syncRequest);
            
            if (SyncConfig.isSyncImagesEnabled(context)) {
                OneTimeWorkRequest attachmentRequest = new OneTimeWorkRequest.Builder(com.example.notecalc.sync.jobs.AttachmentSyncWorker.class)
                    .setConstraints(constraints)
                    .build();
                WorkManager.getInstance(context).enqueueUniqueWork("AttachmentSyncJob", ExistingWorkPolicy.REPLACE, attachmentRequest);
            }
        }
    }

    public static void triggerManualJsonSync(Context context) {
        if (SyncConfig.isSyncEnabled(context)) {
            Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
                
            OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();
                
            WorkManager.getInstance(context).enqueueUniqueWork("NoteCalcCloudSync", ExistingWorkPolicy.REPLACE, syncRequest);
        }
    }
}
