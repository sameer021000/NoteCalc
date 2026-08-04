import re

file_path = r'app\src\main\java\com\example\notecalc\ThemeManager.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

if "getBgPrimaryColor" not in content:
    getters = '''
    public static int getBgPrimaryColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorBgPrimary, typedValue, true);
        return typedValue.data;
    }
    public static int getBgSecondaryColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorBgSecondary, typedValue, true);
        return typedValue.data;
    }
    public static int getBgTertiaryColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorBgTertiary, typedValue, true);
        return typedValue.data;
    }
    public static int getBorderColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorBorder, typedValue, true);
        return typedValue.data;
    }
}
'''
    content = content.replace("}", getters, 1) # replaces the last closing brace

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('getColor(R.color.bg_primary)', 'ThemeManager.getBgPrimaryColor(MainActivity.this)')
content = content.replace('getColor(R.color.bg_secondary)', 'ThemeManager.getBgSecondaryColor(MainActivity.this)')
content = content.replace('getColor(R.color.bg_tertiary)', 'ThemeManager.getBgTertiaryColor(MainActivity.this)')
content = content.replace('getColor(R.color.border_color)', 'ThemeManager.getBorderColor(MainActivity.this)')
content = content.replace('R.color.accent_dark', 'ThemeManager.getPrimaryAccentColor(MainActivity.this)')
content = content.replace('getColor(ThemeManager.getPrimaryAccentColor(MainActivity.this))', 'ThemeManager.getPrimaryAccentColor(MainActivity.this)')

content = content.replace('R.color.text_secondary', 'ThemeManager.getSecondaryAccentColor(MainActivity.this)')
content = content.replace('getColor(ThemeManager.getSecondaryAccentColor(MainActivity.this))', 'ThemeManager.getSecondaryAccentColor(MainActivity.this)')

# btnModeExpenses.setBackgroundColor(...) had R.color.bg_secondary inside ternary.
content = content.replace('getColor(isBudgetMode ? ThemeManager.getBgSecondaryColor(MainActivity.this) : ThemeManager.getPrimaryAccentColor(MainActivity.this))', 'isBudgetMode ? ThemeManager.getBgSecondaryColor(MainActivity.this) : ThemeManager.getPrimaryAccentColor(MainActivity.this)')
content = content.replace('getColor(isBudgetMode ? ThemeManager.getPrimaryAccentColor(MainActivity.this) : ThemeManager.getBgSecondaryColor(MainActivity.this))', 'isBudgetMode ? ThemeManager.getPrimaryAccentColor(MainActivity.this) : ThemeManager.getBgSecondaryColor(MainActivity.this)')

content = content.replace('getColor(getDashboardSortColumn() == 0 ? ThemeManager.getSecondaryAccentColor(MainActivity.this) : R.color.text_tertiary)', 'getDashboardSortColumn() == 0 ? ThemeManager.getSecondaryAccentColor(MainActivity.this) : getColor(R.color.text_tertiary)')
content = content.replace('getColor(getDashboardSortColumn() == 1 ? ThemeManager.getSecondaryAccentColor(MainActivity.this) : R.color.text_tertiary)', 'getDashboardSortColumn() == 1 ? ThemeManager.getSecondaryAccentColor(MainActivity.this) : getColor(R.color.text_tertiary)')
content = content.replace('getColor(getDashboardSortColumn() == 2 ? ThemeManager.getSecondaryAccentColor(MainActivity.this) : R.color.text_tertiary)', 'getDashboardSortColumn() == 2 ? ThemeManager.getSecondaryAccentColor(MainActivity.this) : getColor(R.color.text_tertiary)')

# Fix PDF generation colors
content = content.replace('Color.parseColor("#0B0F19")', 'ThemeManager.getBgPrimaryColor(MainActivity.this)')
content = content.replace('Color.parseColor("#161E2E")', 'ThemeManager.getBgSecondaryColor(MainActivity.this)')
content = content.replace('Color.parseColor("#243046")', 'ThemeManager.getBorderColor(MainActivity.this)')
content = content.replace('Color.parseColor("#38BDF8")', 'ThemeManager.getSecondaryAccentColor(MainActivity.this)')
content = content.replace('Color.parseColor("#E2E8F0")', 'getColor(R.color.text_primary)')
content = content.replace('Color.parseColor("#94A3B8")', 'getColor(R.color.text_tertiary)')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Java files updated!")
