import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('!truncRemarks.equals(recRemarks)', '!truncRemarks.equals(combinedNotes)')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("PDF dots fixed in Java")
