import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix btnModeExpenses and btnModeBudget
content = re.sub(
    r'getColor\((isBudgetMode \? R\.color\.bg_secondary : ThemeManager\.getPrimaryAccentColor\(MainActivity\.this\))\)',
    r'isBudgetMode ? ThemeManager.getBgSecondaryColor(MainActivity.this) : ThemeManager.getPrimaryAccentColor(MainActivity.this)',
    content
)
content = re.sub(
    r'getColor\((isBudgetMode \? ThemeManager\.getPrimaryAccentColor\(MainActivity\.this\) : R\.color\.bg_secondary)\)',
    r'isBudgetMode ? ThemeManager.getPrimaryAccentColor(MainActivity.this) : ThemeManager.getBgSecondaryColor(MainActivity.this)',
    content
)

# Fix pinned icons (around line 1471 and 1581)
content = re.sub(
    r'getColor\(account\.isPinned\(\) \? ThemeManager\.getSecondaryAccentColor\(MainActivity\.this\) : R\.color\.text_tertiary\)',
    r'account.isPinned() ? ThemeManager.getSecondaryAccentColor(MainActivity.this) : getColor(R.color.text_tertiary)',
    content
)
content = re.sub(
    r'getColor\(account\.isPinned\(\) \? ThemeManager\.getPrimaryAccentColor\(MainActivity\.this\) : R\.color\.text_tertiary\)',
    r'account.isPinned() ? ThemeManager.getPrimaryAccentColor(MainActivity.this) : getColor(R.color.text_tertiary)',
    content
)

# Fix headers (around line 2303 and 2310)
content = re.sub(
    r'getColor\(active \? ThemeManager\.getPrimaryAccentColor\(MainActivity\.this\) : R\.color\.text_primary\)',
    r'active ? ThemeManager.getPrimaryAccentColor(MainActivity.this) : getColor(R.color.text_primary)',
    content
)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Ternary fixes applied!")
