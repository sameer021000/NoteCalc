package com.example.notecalc.tools.trash;

import com.example.notecalc.MainActivity;
import com.example.notecalc.accounts.models.Account;
import com.example.notecalc.records.models.Record;
import com.example.notecalc.storage.core.StorageHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TrashCleanupEngine {

    // 24 hours in milliseconds
    private static final long EXPIRATION_TIME_MS = 24 * 60 * 60 * 1000L;

    public static void cleanup(MainActivity activity) {
        if (activity.appStorage == null || activity.appStorage.trashAccounts == null) return;

        long currentTime = System.currentTimeMillis();
        boolean hasChanges = false;

        Iterator<Account> accountIterator = activity.appStorage.trashAccounts.iterator();
        
        while (accountIterator.hasNext()) {
            Account trashAccount = accountIterator.next();
            boolean accountChanged = false;

            // Cleanup Expenses
            if (trashAccount.getRecords() != null) {
                Iterator<Record> recordIterator = trashAccount.getRecords().iterator();
                while (recordIterator.hasNext()) {
                    Record r = recordIterator.next();
                    if (r.getDeletedTimestamp() > 0 && (currentTime - r.getDeletedTimestamp() > EXPIRATION_TIME_MS)) {
                        recordIterator.remove();
                        accountChanged = true;
                        hasChanges = true;
                    }
                }
            }

            // Cleanup Budgets
            if (trashAccount.getBudgetRecords() != null) {
                Iterator<Record> budgetIterator = trashAccount.getBudgetRecords().iterator();
                while (budgetIterator.hasNext()) {
                    Record r = budgetIterator.next();
                    if (r.getDeletedTimestamp() > 0 && (currentTime - r.getDeletedTimestamp() > EXPIRATION_TIME_MS)) {
                        budgetIterator.remove();
                        accountChanged = true;
                        hasChanges = true;
                    }
                }
            }

            // Remove the Trash Account entirely if it became empty
            boolean hasExpenses = trashAccount.getRecords() != null && !trashAccount.getRecords().isEmpty();
            boolean hasBudgets = trashAccount.getBudgetRecords() != null && !trashAccount.getBudgetRecords().isEmpty();
            
            if (!hasExpenses && !hasBudgets) {
                accountIterator.remove();
                hasChanges = true;
            } else if (accountChanged) {
                trashAccount.updateLastModified();
            }
        }

        if (hasChanges) {
            StorageHelper.saveAppStorage(activity, activity.appStorage);
        }
    }
}
