package com.example.notecalc.ncagent;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntentDetectionEngine {
    
    private static final List<String> ADD_TRIGGERS = Arrays.asList(
            "add", "added", "adding", "spend", "spent", "pay", "paid",
            "purchase", "purchased", "buy", "bought", "enter", "entered"
    );
    
    private static final List<String> UPDATE_TRIGGERS = Arrays.asList(
            "update", "updated", "updating", "change", "changed", "changing",
            "modify", "modified", "modifying", "edit", "edited", "editing",
            "replace", "replaced", "replacing", "correct", "corrected", "correcting"
    );
    
    private static final List<String> DELETE_TRIGGERS = Arrays.asList(
            "delete", "deleted", "deleting", "remove", "removed", "removing",
            "erase", "erased", "erasing", "discard", "discarded", "discarding"
    );

    public NCAgentIntent detectIntent(String rawInput) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return NCAgentIntent.ADD;
        }

        String lowerInput = rawInput.toLowerCase(Locale.US);
        
        // We look for whole words to avoid partial matches (e.g., "adding" inside "padding")
        boolean hasAdd = containsTrigger(lowerInput, ADD_TRIGGERS);
        boolean hasUpdate = containsTrigger(lowerInput, UPDATE_TRIGGERS);
        boolean hasDelete = containsTrigger(lowerInput, DELETE_TRIGGERS);
        
        int intentCount = 0;
        if (hasAdd) intentCount++;
        if (hasUpdate) intentCount++;
        if (hasDelete) intentCount++;
        
        if (intentCount > 1) {
            return NCAgentIntent.AMBIGUOUS;
        } else if (hasUpdate) {
            return NCAgentIntent.UPDATE;
        } else if (hasDelete) {
            return NCAgentIntent.DELETE;
        } else {
            // Default is Add if no triggers or only Add triggers are found.
            return NCAgentIntent.ADD;
        }
    }

    private boolean containsTrigger(String input, List<String> triggers) {
        for (String trigger : triggers) {
            // Check if the trigger exists as a distinct word in the text
            String regex = "\\b" + Pattern.quote(trigger) + "\\b";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }
}
