import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add background for editCategoryField
bg_code = '''        editRemarksField.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgPrimaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                4.0f
        ));
        
        if (editCategoryField != null) {
            editCategoryField.setBackground(ResponsiveUI.createRoundedBg(
                    this,
                    ThemeManager.getBgPrimaryColor(MainActivity.this),
                    ThemeManager.getBorderColor(MainActivity.this),
                    1.0f,
                    4.0f
            ));
        }'''
content = content.replace('''        editRemarksField.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgPrimaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                4.0f
        ));''', bg_code)

# 2. Add padding adjustment to dropdown adapter
adapter_search = '''                    if (view instanceof TextView) {
                        ((TextView) view).setTextColor(ThemeManager.getSecondaryAccentColor(MainActivity.this));
                    }
                    view.setBackgroundColor(ThemeManager.getBgSecondaryColor(MainActivity.this));'''
adapter_replace = '''                    if (view instanceof TextView) {
                        ((TextView) view).setTextColor(ThemeManager.getSecondaryAccentColor(MainActivity.this));
                        int hPad = (int)(16 * getResources().getDisplayMetrics().density);
                        int vPad = (int)(8 * getResources().getDisplayMetrics().density);
                        view.setPadding(hPad, vPad, hPad, vPad);
                    }
                    view.setBackgroundColor(ThemeManager.getBgSecondaryColor(MainActivity.this));'''
content = content.replace(adapter_search, adapter_replace)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Fix applied")
