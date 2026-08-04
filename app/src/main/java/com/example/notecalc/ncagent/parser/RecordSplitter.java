package com.example.notecalc.ncagent.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class RecordSplitter {

    // Split on comma, semicolon, or the word 'and' (case insensitive) if it's used as a separator between distinct items.
    // To be safe, we split by these common natural language separators.
    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("\\s*(?:[,;]|\\band\\b|\\&)\\s*", Pattern.CASE_INSENSITIVE);

    // Regex for explicitly marked currency
    private static final Pattern CURRENCY_PATTERN = Pattern.compile(
            "(?:(?:rs\\.?|rupees?|inr|₹|\\$)\\s*(\\d+(?:\\.\\d{1,2})?))|" +
            "(?:(\\d+(?:\\.\\d{1,2})?)\\s*(?:rs\\.?|rupees?|inr|₹))",
            Pattern.CASE_INSENSITIVE
    );

    // Regex for a standalone number (fallback)
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b(\\d+(?:\\.\\d{1,2})?)\\b");

    private boolean hasAmount(String text) {
        if (text == null || text.isEmpty()) return false;
        Matcher currencyMatcher = CURRENCY_PATTERN.matcher(text);
        if (currencyMatcher.find()) return true;
        Matcher numberMatcher = NUMBER_PATTERN.matcher(text);
        return numberMatcher.find();
    }

    public List<String> split(String expenseLine) {
        List<String> segments = new ArrayList<>();
        if (expenseLine == null || expenseLine.trim().isEmpty()) return segments;
        
        String[] parts = SEPARATOR_PATTERN.split(expenseLine);
        StringBuilder currentSegment = new StringBuilder();
        
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            
            if (currentSegment.length() > 0) {
                if (hasAmount(currentSegment.toString()) && hasAmount(trimmed)) {
                    segments.add(currentSegment.toString().trim());
                    currentSegment = new StringBuilder(trimmed);
                } else {
                    currentSegment.append(", ").append(trimmed);
                }
            } else {
                currentSegment.append(trimmed);
            }
        }
        
        if (currentSegment.length() > 0) {
            segments.add(currentSegment.toString().trim());
        }
        
        if (segments.size() == 1) {
            segments.clear();
            segments.add(expenseLine.trim()); // preserve exact original if no split occurred
        }
        
        return segments;
    }
}