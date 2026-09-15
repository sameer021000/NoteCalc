package com.example.notecalc.tools.trash;

import com.example.notecalc.MainActivity;
import com.example.notecalc.accounts.models.Account;
import com.example.notecalc.accounts.models.AccountGroup;
import com.example.notecalc.core.utils.StateHelper;
import com.example.notecalc.records.models.Record;
import com.example.notecalc.records.RecordUtils;
import com.example.notecalc.storage.core.StorageHelper;

import java.util.ArrayList;
import java.util.List;

public class TrashActionEngine {

    public static void restoreRecords(MainActivity activity, Account trashAccount, List<Record> recordsToRestore, boolean isBudgetMode) {
        if (recordsToRestore == null || recordsToRestore.isEmpty()) return;

        // 1. Find or create the target live account based on the Trash Account's title
        Account targetAccount = findOrCreateLiveAccount(activity, trashAccount.getTitle());

        // 2. Move records from Trash to Live
        for (Record r : recordsToRestore) {
            r.setSelected(false);
            r.setDeletedTimestamp(0); // Reset deleted timestamp
            r.setOriginalIndex(Integer.MAX_VALUE); // Push to bottom
            
            if (isBudgetMode) {
                trashAccount.getBudgetRecords().remove(r);
                if (targetAccount.getBudgetRecords() == null) {
                    targetAccount.setHasBudget(true);
                }
                targetAccount.getBudgetRecords().add(r);
                targetAccount.setHasBudget(true);
            } else {
                trashAccount.getRecords().remove(r);
                targetAccount.getRecords().add(r);
            }
        }

        // 3. Clean up the target account indices
        if (isBudgetMode) {
            RecordUtils.resequentializeRecords(targetAccount.getBudgetRecords());
        } else {
            RecordUtils.resequentializeRecords(targetAccount.getRecords());
        }
        targetAccount.updateLastModified();

        // 4. Update the active tempRecords so UI reflects the change immediately if we are in Trash List
        if (activity.currentEditingAccount == trashAccount) {
            StateHelper.getActiveRecords(activity).removeAll(recordsToRestore);
            RecordUtils.resequentializeRecords(StateHelper.getActiveRecords(activity));
        }

        // 5. Clean up the Trash Account if empty
        cleanupTrashAccountIfEmpty(activity, trashAccount);

        // 6. Save storage
        StorageHelper.saveAppStorage(activity, activity.appStorage);
    }

    public static void deleteRecordsPermanently(MainActivity activity, Account trashAccount, List<Record> recordsToDelete, boolean isBudgetMode) {
        if (recordsToDelete == null || recordsToDelete.isEmpty()) return;

        // 1. Remove from Trash Account
        if (isBudgetMode) {
            trashAccount.getBudgetRecords().removeAll(recordsToDelete);
        } else {
            trashAccount.getRecords().removeAll(recordsToDelete);
        }

        // 2. Update the active tempRecords so UI reflects the change immediately
        if (activity.currentEditingAccount == trashAccount) {
            StateHelper.getActiveRecords(activity).removeAll(recordsToDelete);
            RecordUtils.resequentializeRecords(StateHelper.getActiveRecords(activity));
        }

        // 3. Clean up the Trash Account if empty
        cleanupTrashAccountIfEmpty(activity, trashAccount);

        // 4. Save storage
        StorageHelper.saveAppStorage(activity, activity.appStorage);
    }

    private static Account findOrCreateLiveAccount(MainActivity activity, String title) {
        // Search standalone accounts
        for (Account acc : activity.appStorage.standaloneAccounts) {
            if (acc.getTitle().equalsIgnoreCase(title)) {
                return acc;
            }
        }
        // Search grouped accounts
        for (AccountGroup group : activity.appStorage.groups) {
            for (Account acc : group.getAccounts()) {
                if (acc.getTitle().equalsIgnoreCase(title)) {
                    return acc;
                }
            }
        }
        
        // If not found, create a new standalone account
        Account newAccount = new Account(title);
        activity.appStorage.standaloneAccounts.add(newAccount);
        return newAccount;
    }

    private static void cleanupTrashAccountIfEmpty(MainActivity activity, Account trashAccount) {
        boolean hasExpenses = trashAccount.getRecords() != null && !trashAccount.getRecords().isEmpty();
        boolean hasBudgets = trashAccount.getBudgetRecords() != null && !trashAccount.getBudgetRecords().isEmpty();
        
        if (!hasExpenses && !hasBudgets) {
            activity.appStorage.trashAccounts.remove(trashAccount);
        }
    }
}
