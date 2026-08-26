package com.example.notecalc.ncagent;

import com.example.notecalc.Record;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;

public class ActionExecutor {
    private String normalizeDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date());
        }
        String lower = dateStr.toLowerCase(Locale.US).trim();
        Calendar cal = Calendar.getInstance();
        
        if (lower.equals("yesterday")) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
            return new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(cal.getTime());
        } else if (lower.equals("tomorrow")) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            return new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(cal.getTime());
        } else if (lower.equals("today")) {
            return new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(cal.getTime());
        }

        java.util.Map<String, Integer> days = new java.util.HashMap<>();
        days.put("sunday", Calendar.SUNDAY);
        days.put("monday", Calendar.MONDAY);
        days.put("tuesday", Calendar.TUESDAY);
        days.put("wednesday", Calendar.WEDNESDAY);
        days.put("thursday", Calendar.THURSDAY);
        days.put("friday", Calendar.FRIDAY);
        days.put("saturday", Calendar.SATURDAY);

        String[] parts = lower.split("\\s+");
        String dayName = parts[parts.length - 1];
        
        if (days.containsKey(dayName)) {
            int targetDay = days.get(dayName);
            int currentDay = cal.get(Calendar.DAY_OF_WEEK);
            int diff = targetDay - currentDay;
            
            if (lower.startsWith("last ")) {
                if (diff >= 0) diff -= 7;
            } else if (lower.startsWith("next ")) {
                if (diff <= 0) diff += 7;
            } else {
                if (diff >= 0) diff -= 7; 
            }
            
            // If it's exactly the same day (e.g. today is Monday and user says "Monday"), diff is 0, so diff-=7 makes it last Monday.
            
            cal.add(Calendar.DAY_OF_YEAR, diff);
            return new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(cal.getTime());
        }

        return dateStr;
    }


    public NCAction execute(NCAgentIntent intent, RecordCandidate candidate, List<Record> databaseRecords, RecordMatchingEngine matcher) {
        NCAction action = new NCAction(intent);

        if (intent == NCAgentIntent.AMBIGUOUS) {
            action.setValid(false);
            action.setErrorMessage("Multiple action intents detected (e.g. Add and Delete). Please submit one action at a time.");
            return action;
        }

        // Common validation
        if (intent == NCAgentIntent.ADD || intent == NCAgentIntent.UPDATE) {
            if (candidate.getAmount() == null) {
                action.setValid(false);
                action.setErrorMessage("Amount is mandatory.");
                return action;
            }
        }

        if (intent == NCAgentIntent.ADD) {
            String desc = (candidate.getDescription() == null || candidate.getDescription().isEmpty()) ? "Expense" : candidate.getDescription();
            String date = normalizeDate(candidate.getDate());
            double amt = candidate.getAmount() != null ? candidate.getAmount() : 0.0;
            
            Record newRecord = new Record(desc, amt, date);
            newRecord.setCategory(candidate.getCategory() == null ? "" : candidate.getCategory());
            newRecord.setRemarks(candidate.getRemarks() == null ? "" : candidate.getRemarks());
            
            action.setValidatedRecord(newRecord);
            
        } else if (intent == NCAgentIntent.UPDATE || intent == NCAgentIntent.DELETE) {
            List<Record> matches = matcher.match(candidate, databaseRecords);
            
            if (matches.isEmpty()) {
                action.setValid(false);
                action.setErrorMessage("No matching record found to " + intent.name().toLowerCase() + ".");
            } else if (matches.size() == 1) {
                action.setTargetRecord(matches.get(0));
                
                if (intent == NCAgentIntent.UPDATE) {
                    // Copy old
                    Record updatedRecord = new Record(matches.get(0).getDescription(), matches.get(0).getAmount(), matches.get(0).getDate());
                    updatedRecord.setCategory(matches.get(0).getCategory());
                    updatedRecord.setRemarks(matches.get(0).getRemarks());
                    updatedRecord.setOriginalIndex(matches.get(0).getOriginalIndex());
                    updatedRecord.setTimestampMillis(System.currentTimeMillis());
                    if (matches.get(0).getAttachments() != null) {
                        updatedRecord.getAttachments().addAll(matches.get(0).getAttachments());
                    }
                    
                    // Apply new
                    if (candidate.getDescription() != null && !candidate.getDescription().isEmpty()) updatedRecord.setDescription(candidate.getDescription());
                    if (candidate.getAmount() != null) updatedRecord.setAmount(candidate.getAmount());
                    if (candidate.getDate() != null && !candidate.getDate().isEmpty()) updatedRecord.setDate(normalizeDate(candidate.getDate()));
                    if (candidate.getCategory() != null && !candidate.getCategory().isEmpty()) updatedRecord.setCategory(candidate.getCategory());
                    if (candidate.getRemarks() != null && !candidate.getRemarks().isEmpty()) updatedRecord.setRemarks(candidate.getRemarks());
                    
                    action.setValidatedRecord(updatedRecord);
                }
            } else {
                action.setNeedsDisambiguation(true);
                action.setDisambiguationCandidates(matches);
            }
        }

        return action;
    }
}
