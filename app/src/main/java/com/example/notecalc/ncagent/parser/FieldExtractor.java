package com.example.notecalc.ncagent.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldExtractor {

    // Regex for explicitly marked currency: e.g. Rs 300, 300rs, ₹300, INR 300, $300, 300 INR
    private static final Pattern CURRENCY_PATTERN = Pattern.compile(
            "(?:(?:rs\\.?|rupees?|inr|₹|\\$)\\s*(\\d+(?:\\.\\d{1,2})?))|" +
            "(?:(\\d+(?:\\.\\d{1,2})?)\\s*(?:rs\\.?|rupees?|inr|₹))",
            Pattern.CASE_INSENSITIVE
    );

    // Regex for a standalone number (fallback)
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b(\\d+(?:\\.\\d{1,2})?)\\b");

    public ExtractedFields extract(String segment, ContextManager contextManager, String inlineDate, String inlineCategory, String inlineRemarks) {
        ExtractedFields fields = new ExtractedFields();
        fields.date = inlineDate != null ? inlineDate : contextManager.getCurrentDateContext();
        fields.category = inlineCategory != null ? inlineCategory : contextManager.getCurrentCategoryContext();
        fields.remarks = inlineRemarks; // Remarks never propagate

        // Extract Amount and Description
        String description = segment;
        Double amount = null;

        Matcher currencyMatcher = CURRENCY_PATTERN.matcher(segment);
        if (currencyMatcher.find()) {
            String valStr = currencyMatcher.group(1) != null ? currencyMatcher.group(1) : currencyMatcher.group(2);
            try {
                amount = Double.parseDouble(valStr);
                // Remove the matched amount chunk from description
                description = new StringBuilder(segment).replace(currencyMatcher.start(), currencyMatcher.end(), "").toString();
            } catch (Exception ignored) {}
        } else {
            // No currency symbol. Find all numbers. Pick the last one as the amount.
            Matcher numberMatcher = NUMBER_PATTERN.matcher(segment);
            int lastStart = -1;
            int lastEnd = -1;
            String lastVal = null;
            while (numberMatcher.find()) {
                lastVal = numberMatcher.group(1);
                lastStart = numberMatcher.start();
                lastEnd = numberMatcher.end();
            }
            if (lastVal != null) {
                try {
                    amount = Double.parseDouble(lastVal);
                    description = new StringBuilder(segment).replace(lastStart, lastEnd, "").toString();
                } catch (Exception ignored) {}
            }
        }

        // Clean up description (remove leftover 'for', '-', etc.)
        description = description.replaceAll("^(for|-|:)", "").replaceAll("(for|-|:)$", "").replaceAll("\\s+", " ").trim();
        
        // Spec rule: If description is absent but remarks exist, use remarks as description.
        // Spec rule: If both are absent, Action Executor later assigns 'Expense'.
        if (description.isEmpty() && fields.remarks != null && !fields.remarks.isEmpty()) {
            description = fields.remarks;
            fields.remarks = null; // Moved to description
        }

        fields.description = description;
        fields.amount = amount;

        return fields;
    }

    public static class ExtractedFields {
        public String description;
        public Double amount;
        public String date;
        public String category;
        public String remarks;
    }
}
