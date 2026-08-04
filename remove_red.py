import re

# 1. Update ThemeManager
file_path = r'app\src\main\java\com\example\notecalc\ThemeManager.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = re.sub(r'public static final String ACCENT_RED = "Red";\n\s*', '', content)
content = re.sub(r'case ACCENT_RED: themeId = R\.style\.Theme_NoteCalc_Red; break;\n\s*', '', content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

# 2. Update MainActivity initSettings
file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('ThemeManager.ACCENT_PURPLE, ThemeManager.ACCENT_RED, ThemeManager.ACCENT_YELLOW', 'ThemeManager.ACCENT_PURPLE, ThemeManager.ACCENT_YELLOW')
content = content.replace('\"#9333EA\", \"#DC2626\", \"#CA8A04\"', '\"#9333EA\", \"#CA8A04\"')

# 3. Fix rowOddPaint
content = content.replace('Color.parseColor("#1C2538")', 'ThemeManager.getBgTertiaryColor(MainActivity.this)')
# Wait, also fix totalBgPaint
content = content.replace('Color.parseColor("#0284C7")', 'ThemeManager.getPrimaryAccentColor(MainActivity.this)')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Red theme removed and paints fixed!")
