import re

file_path = r'app\src\main\java\com\example\notecalc\ncagent\parser\RecordSplitter.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

import_search = '''import java.util.regex.Pattern;'''
import_replace = '''import java.util.regex.Pattern;
import java.util.regex.Matcher;'''
content = content.replace(import_search, import_replace)

new_logic = '''
    // Regex for explicitly marked currency
    private static final Pattern CURRENCY_PATTERN = Pattern.compile(
            "(?:(?:rs\\\\.?|rupees?|inr|?|\\\\$)\\\\s*(\\\\d+(?:\\\\.\\\\d{1,2})?))|" +
            "(?:(\\\\d+(?:\\\\.\\\\d{1,2})?)\\\\s*(?:rs\\\\.?|rupees?|inr|?))",
            Pattern.CASE_INSENSITIVE
    );

    // Regex for a standalone number (fallback)
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\\\b(\\\\d+(?:\\\\.\\\\d{1,2})?)\\\\b");

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
'''

# Replace everything from public List<String> split to the end of the class.
content = re.sub(r'public List<String> split\(String expenseLine\).*', new_logic + '\n}', content, flags=re.DOTALL)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("RecordSplitter logic updated")
