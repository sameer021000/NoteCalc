package com.example.notecalc.tools.transfer;

import com.example.notecalc.accounts.models.Account;
import com.example.notecalc.records.models.Record;
import com.example.notecalc.records.RecordUtils;
import java.util.List;

public class ListMutationEngine {

    public static void copyRecords(List<Record> source, Account targetAccount, boolean isBudgetMode) {
        List<Record> targetList = isBudgetMode ? targetAccount.getBudgetRecords() : targetAccount.getRecords();
        int maxIndex = -1;
        for (Record rec : targetList) {
            if (rec.getOriginalIndex() > maxIndex) {
                maxIndex = rec.getOriginalIndex();
            }
        }

        for (Record r : source) {
            Record copy = new Record(r.getDescription(), r.getAmount(), r.getDate());
            copy.setRemarks(r.getRemarks());
            copy.setCategory(r.getCategory());
            copy.setTimestampMillis(r.getTimestampMillis());
            if (r.getAttachments() != null) {
                copy.getAttachments().addAll(r.getAttachments());
            }
            maxIndex++;
            copy.setOriginalIndex(maxIndex);

            targetList.add(copy);
        }

        // Update target account budget flag
        if (isBudgetMode) {
            targetAccount.setHasBudget(!targetAccount.getBudgetRecords().isEmpty());
        }
    }

    public static void cutRecords(List<Record> source, Account sourceAccount, Account targetAccount, boolean isBudgetMode) {
        copyRecords(source, targetAccount, isBudgetMode);
        deleteRecords(source, sourceAccount, isBudgetMode);
    }

    public static void deleteRecords(List<Record> source, Account sourceAccount, boolean isBudgetMode) {
        if (sourceAccount == null) return;
        List<Record> sourceList = isBudgetMode ? sourceAccount.getBudgetRecords() : sourceAccount.getRecords();
        sourceList.removeAll(source);
        RecordUtils.resequentializeRecords(sourceList);
        
        // Update source account budget flag
        if (isBudgetMode) {
            sourceAccount.setHasBudget(!sourceAccount.getBudgetRecords().isEmpty());
        }
    }
}
