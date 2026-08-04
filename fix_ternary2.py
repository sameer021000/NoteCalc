import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('getColor(active ? ThemeManager.getPrimaryAccentColor(MainActivity.this) : ThemeManager.getSecondaryAccentColor(MainActivity.this))', 'active ? ThemeManager.getPrimaryAccentColor(MainActivity.this) : ThemeManager.getSecondaryAccentColor(MainActivity.this)')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Ternary fixes 2 applied!")
