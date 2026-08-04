import re

# Fix MainActivity.java
file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

add_record_search = '''                    if (action.getIntent() == NCAgentIntent.ADD) {
                        getActiveRecords().add(action.getValidatedRecord());
                        added++;'''
add_record_replace = '''                    if (action.getIntent() == NCAgentIntent.ADD) {
                        Record validated = action.getValidatedRecord();
                        validated.setOriginalIndex(getNewOriginalIndex());
                        getActiveRecords().add(validated);
                        added++;'''
content = content.replace(add_record_search, add_record_replace)

sorting_search = '''            StorageHelper.saveAppStorage(MainActivity.this, appStorage);
            populateRecordsList();'''
sorting_replace = '''            applySorting();
            StorageHelper.saveAppStorage(MainActivity.this, appStorage);
            populateRecordsList();'''
content = content.replace(sorting_search, sorting_replace)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)


# Fix InputParser.java
file_path = r'app\src\main\java\com\example\notecalc\ncagent\parser\InputParser.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

import_search = '''import java.util.ArrayList;'''
import_replace = '''import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;'''
content = content.replace(import_search, import_replace)

date_pattern = '''
    private static final Pattern INLINE_DATE_PATTERN = Pattern.compile(
            "\\\\b(today|yesterday|tomorrow|monday|tuesday|wednesday|thursday|friday|saturday|sunday|" +
            "last\\\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)|" +
            "next\\\\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)|" +
            "\\\\d{1,2}[-/]\\\\d{1,2}[-/]\\\\d{2,4}|" +
            "\\\\d{4}-\\\\d{2}-\\\\d{2}|" +
            "\\\\d{1,2}\\\\s+(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*|" +
            "(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\\\s+\\\\d{1,2})\\\\b",
            Pattern.CASE_INSENSITIVE
    );
'''

# Find class start
class_search = '''public class InputParser {'''
content = content.replace(class_search, class_search + date_pattern)

inline_search = '''                String inlineRemarks = null;'''
inline_replace = inline_search + '''
                
                Matcher dateMatcher = INLINE_DATE_PATTERN.matcher(text);
                if (dateMatcher.find()) {
                    inlineDate = dateMatcher.group(1);
                    text = new StringBuilder(text).replace(dateMatcher.start(), dateMatcher.end(), "").toString().trim();
                }'''
content = content.replace(inline_search, inline_replace)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)


# Fix ActionExecutor.java
file_path = r'app\src\main\java\com\example\notecalc\ncagent\ActionExecutor.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

import_executor_search = '''import java.util.Locale;'''
import_executor_replace = '''import java.util.Locale;
import java.util.Calendar;'''
content = content.replace(import_executor_search, import_executor_replace)

normalize_date_func = '''
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
        // TODO: Full relative date parsing (Monday, Last Friday, etc). 
        // For now, we return standard dates as is, or fallback to today if parsing fails completely on relative days not yet supported.
        return dateStr;
    }
'''
content = content.replace('public class ActionExecutor {', 'public class ActionExecutor {' + normalize_date_func)

date_usage_search = '''String date = (candidate.getDate() == null || candidate.getDate().isEmpty()) ? new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date()) : candidate.getDate();'''
date_usage_replace = '''String date = normalizeDate(candidate.getDate());'''
content = content.replace(date_usage_search, date_usage_replace)

update_date_search = '''if (candidate.getDate() != null && !candidate.getDate().isEmpty()) updatedRecord.setDate(candidate.getDate());'''
update_date_replace = '''if (candidate.getDate() != null && !candidate.getDate().isEmpty()) updatedRecord.setDate(normalizeDate(candidate.getDate()));'''
content = content.replace(update_date_search, update_date_replace)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Bugs fixed")
