package com.example.notecalc.ncagent.parser;

public class ClassifiedLine {
    public enum LineType {
        CONTEXT_CATEGORY,
        CONTEXT_REMARKS,
        CONTEXT_DATE,
        EXPENSE
    }
    
    private final String rawText;
    private final LineType type;
    
    public ClassifiedLine(String rawText, LineType type) {
        this.rawText = rawText;
        this.type = type;
    }
    
    public String getRawText() { return rawText; }
    public LineType getType() { return type; }
}
