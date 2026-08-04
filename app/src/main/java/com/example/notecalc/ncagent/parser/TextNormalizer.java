package com.example.notecalc.ncagent.parser;

public class TextNormalizer {
    public String normalize(String rawInput) {
        if (rawInput == null) return "";
        // Replace CRLF with LF
        String text = rawInput.replace("\r\n", "\n");
        // Collapse multiple spaces into one, but preserve newlines
        // We can do this by splitting by newline, then replacing multiple spaces in each line
        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].replaceAll("[ \t]+", " ").trim();
            if (!line.isEmpty()) {
                sb.append(line);
                if (i < lines.length - 1) {
                    sb.append("\n");
                }
            }
        }
        return sb.toString().trim();
    }
}
