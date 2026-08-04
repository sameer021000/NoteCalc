package com.example.notecalc.ncagent;

import com.example.notecalc.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecordMatchingEngine {

    public List<Record> match(RecordCandidate candidate, List<Record> databaseRecords) {
        List<Record> matches = new ArrayList<>();
        
        for (Record record : databaseRecords) {
            if (isMatch(candidate, record)) {
                matches.add(record);
            }
        }
        
        return matches;
    }

    private boolean isMatch(RecordCandidate candidate, Record record) {
        // Priority 1: Description
        if (candidate.getDescription() != null && !candidate.getDescription().isEmpty()) {
            if (!candidate.getDescription().equalsIgnoreCase(record.getDescription())) {
                return false;
            }
        }
        
        // Priority 2: Amount
        if (candidate.getAmount() != null) {
            // Use an epsilon for double comparison to avoid floating point issues
            if (Math.abs(candidate.getAmount() - record.getAmount()) > 0.001) {
                return false;
            }
        }
        
        // Priority 3: Date
        if (candidate.getDate() != null && !candidate.getDate().isEmpty()) {
            // In a real app we'd normalize the date string before comparing
            // Assuming candidate.getDate() is already normalized to dd-MM-yyyy by ActionExecutor/Parser
            if (!candidate.getDate().equals(record.getDate())) {
                return false;
            }
        }
        
        // Priority 4: Category
        if (candidate.getCategory() != null && !candidate.getCategory().isEmpty()) {
            if (!candidate.getCategory().equalsIgnoreCase(record.getCategory())) {
                return false;
            }
        }
        
        // Priority 5: Remarks
        if (candidate.getRemarks() != null && !candidate.getRemarks().isEmpty()) {
            if (!candidate.getRemarks().equalsIgnoreCase(record.getRemarks())) {
                return false;
            }
        }
        
        // If the candidate provided no fields at all, it shouldn't match anything
        boolean hasAnyField = (candidate.getDescription() != null && !candidate.getDescription().isEmpty()) ||
                              (candidate.getAmount() != null) ||
                              (candidate.getDate() != null && !candidate.getDate().isEmpty()) ||
                              (candidate.getCategory() != null && !candidate.getCategory().isEmpty()) ||
                              (candidate.getRemarks() != null && !candidate.getRemarks().isEmpty());
                              
        return hasAnyField;
    }
}
