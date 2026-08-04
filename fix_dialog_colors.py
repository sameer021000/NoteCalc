import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('ThemeManager.getTextPrimaryColor(this)', 'getColor(R.color.text_primary)')
content = content.replace('ThemeManager.getTextPrimaryColor(MainActivity.this)', 'getColor(R.color.text_primary)')
content = content.replace('ThemeManager.getTextSecondaryColor(this)', 'getColor(R.color.text_tertiary)')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Color fix applied")
