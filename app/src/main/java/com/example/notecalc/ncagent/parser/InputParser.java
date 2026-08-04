package com.example.notecalc.ncagent.parser;

import com.example.notecalc.ncagent.RecordCandidate;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

public class InputParser {
    private static final Pattern INLINE_DATE_PATTERN = Pattern.compile(
            "\\b(today|yesterday|tomorrow|monday|tuesday|wednesday|thursday|friday|saturday|sunday|" +
            "last\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)|" +
            "next\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)|" +
            "\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}|" +
            "\\d{4}-\\d{2}-\\d{2}|" +
            "\\d{1,2}\\s+(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*|" +
            "(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\s+\\d{1,2})\\b",
            Pattern.CASE_INSENSITIVE
    );


    private final TextNormalizer textNormalizer = new TextNormalizer();
    private final LineProcessor lineProcessor = new LineProcessor();
    private final RecordSplitter recordSplitter = new RecordSplitter();
    private final ContextManager contextManager = new ContextManager();
    private final FieldExtractor fieldExtractor = new FieldExtractor();
    private final CandidateBuilder candidateBuilder = new CandidateBuilder();

    public List<RecordCandidate> parse(String rawInput) {
        List<RecordCandidate> candidates = new ArrayList<>();
        contextManager.clear();

        String normalized = textNormalizer.normalize(rawInput);
        List<ClassifiedLine> lines = lineProcessor.process(normalized);

        for (ClassifiedLine line : lines) {
            String text = line.getRawText();
            
            if (line.getType() == ClassifiedLine.LineType.CONTEXT_DATE) {
                // Future expansion: normalize "Yesterday" to "25-07-2026"
                // For now we just pass it along
                contextManager.setDateContext(text);
                continue;
            }
            
            if (line.getType() == ClassifiedLine.LineType.CONTEXT_CATEGORY) {
                // e.g. "Category: Food" -> "Food"
                String cat = text.replaceAll("(?i)^category\\s*(?:is|:)\\s*", "").trim();
                contextManager.setCategoryContext(cat);
                continue;
            }
            
            if (line.getType() == ClassifiedLine.LineType.CONTEXT_REMARKS) {
                // Remarks do NOT propagate, so we don't store them in ContextManager.
                // But wait! If the line is purely a remark, which record does it attach to?
                // The spec says "Remarks never propagate. Remarks belong only to the record where they are explicitly provided."
                // Since our pipeline splits segments, maybe a remark line is an isolated thing that we attach to the PREVIOUS record?
                // Or maybe remarks are provided inline like "Tea 20 Remark: Good"?
                // Let's attach standalone remark lines to the *last* candidate produced.
                if (!candidates.isEmpty()) {
                    String rm = text.replaceAll("(?i)^(?:remarks?|note)\\s*:\\s*", "").trim();
                    candidates.get(candidates.size() - 1).setRemarks(rm);
                }
                continue;
            }
            
            if (line.getType() == ClassifiedLine.LineType.EXPENSE) {
                // We might have inline context in an expense line (e.g. "Tea 20 Category: Food")
                // For a robust implementation, we would extract them here.
                String inlineDate = null;
                String inlineCategory = null;
                String inlineRemarks = null;
                
                Matcher dateMatcher = INLINE_DATE_PATTERN.matcher(text);
                if (dateMatcher.find()) {
                    inlineDate = dateMatcher.group(1);
                    text = new StringBuilder(text).replace(dateMatcher.start(), dateMatcher.end(), "").toString().trim();
                }
                
                // Very basic inline remark extraction
                int remarkIdx = text.toLowerCase().indexOf("remark:");
                if (remarkIdx == -1) remarkIdx = text.toLowerCase().indexOf("note:");
                if (remarkIdx != -1) {
                    inlineRemarks = text.substring(remarkIdx).replaceAll("(?i)^(?:remarks?|note)\\s*:\\s*", "").trim();
                    text = text.substring(0, remarkIdx).trim();
                }
                
                int catIdx = text.toLowerCase().indexOf("category:");
                if (catIdx != -1) {
                    inlineCategory = text.substring(catIdx).replaceAll("(?i)^category\\s*:\\s*", "").trim();
                    text = text.substring(0, catIdx).trim();
                }

                List<String> segments = recordSplitter.split(text);
                for (String segment : segments) {
                    FieldExtractor.ExtractedFields fields = fieldExtractor.extract(segment, contextManager, inlineDate, inlineCategory, inlineRemarks);
                    
                    // Clear inline context after first segment so it doesn't duplicate to multiple items on same line
                    inlineDate = null;
                    inlineCategory = null;
                    inlineRemarks = null;

                    RecordCandidate candidate = candidateBuilder.build(fields);
                    candidates.add(candidate);
                }
            }
        }
        
        return candidates;
    }
}
