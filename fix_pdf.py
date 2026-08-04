import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

search_text = '''            // Check if remarks exist; add extra height if so
            String recRemarks = rec.getRemarks();
            boolean hasRemarks = recRemarks != null && !recRemarks.isEmpty();
            float actualRowHeight = hasRemarks ? rowHeight + 14f : rowHeight;'''

replace_text = '''            // Check if remarks or category exist; add extra height if so
            String recRemarks = rec.getRemarks();
            String cat = rec.getCategory();
            String combinedNotes = "";
            if (cat != null && !cat.isEmpty()) combinedNotes += "[" + cat + "] ";
            if (recRemarks != null && !recRemarks.isEmpty()) combinedNotes += recRemarks;
            
            boolean hasRemarks = !combinedNotes.isEmpty();
            float actualRowHeight = hasRemarks ? rowHeight + 14f : rowHeight;'''

content = content.replace(search_text, replace_text)

search_text2 = '''            // Draw remarks below description if present
            if (hasRemarks) {
                String truncRemarks = recRemarks;
                while (truncRemarks.length() > 1 && cellMutedPaint.measureText(truncRemarks) > colDesc - 8f) {
                    truncRemarks = truncRemarks.substring(0, truncRemarks.length() - 1);
                }'''

replace_text2 = '''            // Draw remarks/category below description if present
            if (hasRemarks) {
                String truncRemarks = combinedNotes;
                while (truncRemarks.length() > 1 && cellMutedPaint.measureText(truncRemarks) > colDesc - 8f) {
                    truncRemarks = truncRemarks.substring(0, truncRemarks.length() - 1);
                }'''

content = content.replace(search_text2, replace_text2)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("PDF category fix applied")
