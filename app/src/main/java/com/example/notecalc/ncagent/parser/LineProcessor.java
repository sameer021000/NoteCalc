package com.example.notecalc.ncagent.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class LineProcessor {
    
    // Pattern to match supported dates roughly to classify a line as purely a date context line if it only contains a date
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "^(today|yesterday|tomorrow|monday|tuesday|wednesday|thursday|friday|saturday|sunday|" +
            "last\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)|" +
            "next\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)|" +
            "\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}|" +
            "\\d{4}-\\d{2}-\\d{2}|" +
            "\\d{1,2}\\s+(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*|" +
            "(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\s+\\d{1,2})$",
            Pattern.CASE_INSENSITIVE
    );

    public List<ClassifiedLine> process(String normalizedText) {
        List<ClassifiedLine> results = new ArrayList<>();
        if (normalizedText == null || normalizedText.isEmpty()) return results;
        
        String[] lines = normalizedText.split("\n");
        for (String line : lines) {
            String lower = line.toLowerCase(Locale.US).trim();
            if (lower.startsWith("category:") || lower.startsWith("category is")) {
                results.add(new ClassifiedLine(line, ClassifiedLine.LineType.CONTEXT_CATEGORY));
            } else if (lower.startsWith("remark:") || lower.startsWith("remarks:") || lower.startsWith("note:")) {
                results.add(new ClassifiedLine(line, ClassifiedLine.LineType.CONTEXT_REMARKS));
            } else if (DATE_PATTERN.matcher(lower).matches()) {
                results.add(new ClassifiedLine(line, ClassifiedLine.LineType.CONTEXT_DATE));
            } else {
                results.add(new ClassifiedLine(line, ClassifiedLine.LineType.EXPENSE));
            }
        }
        
        return results;
    }
}
