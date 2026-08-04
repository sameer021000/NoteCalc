import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

search_text = '''        btnCancelEdit.setBackground(ResponsiveUI.createRoundedBg(
                this,
                ThemeManager.getBgPrimaryColor(MainActivity.this),
                ThemeManager.getBorderColor(MainActivity.this),
                1.0f,
                4.0f
        ));'''

replace_text = '''        btnCancelEdit.setBackground(ResponsiveUI.createRoundedBg(
                this,
                getColor(R.color.error_red),
                getColor(R.color.error_red),
                0f,
                4.0f
        ));
        btnCancelEdit.setTextColor(getColor(R.color.text_on_accent));
        btnCancelEdit.setTypeface(null, android.graphics.Typeface.BOLD);'''

content = content.replace(search_text, replace_text)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Cancel btn fixed")
