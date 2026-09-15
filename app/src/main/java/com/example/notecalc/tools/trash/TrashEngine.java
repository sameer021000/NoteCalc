package com.example.notecalc.tools.trash;

import com.example.notecalc.MainActivity;
import com.example.notecalc.accounts.models.Account;
import com.example.notecalc.records.models.Record;
import com.example.notecalc.storage.core.StorageHelper;

import java.util.List;

public class TrashEngine {

    public static void moveToTrash(MainActivity activity, Account sourceAccount, List<Record> deletedRecords, boolean isBudgetMode) {
        if (deletedRecords == null || deletedRecords.isEmpty() || sourceAccount == null) return;
        
        long now = System.currentTimeMillis();
        
        // Find or create Trash Account
        Account trashAccount = null;
        for (Account acc : activity.appStorage.trashAccounts) {
            if (acc.getTitle().equals(sourceAccount.getTitle())) {
                trashAccount = acc;
                break;
            }
        }
        
        if (trashAccount == null) {
            trashAccount = new Account(sourceAccount.getTitle());
            activity.appStorage.trashAccounts.add(trashAccount);
        }
        
        if (trashAccount.getBudgetRecords() == null) {
            trashAccount.setBudgetRecords(new java.util.ArrayList<>());
        }
        
        for (Record r : deletedRecords) {
            Record copy = new Record(r.getDescription(), r.getAmount(), r.getDate());
            copy.setRemarks(r.getRemarks());
            copy.setCategory(r.getCategory());
            copy.setTimestampMillis(r.getTimestampMillis());
            copy.setDeletedTimestamp(now);
            if (r.getAttachments() != null) {
                copy.getAttachments().addAll(r.getAttachments());
            }
            
            if (isBudgetMode) {
                trashAccount.getBudgetRecords().add(copy);
            } else {
                trashAccount.getRecords().add(copy);
            }
        }
        
        StorageHelper.saveAppStorage(activity, activity.appStorage);
    }
}
