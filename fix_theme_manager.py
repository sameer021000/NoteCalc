import re

file_path = r'app\src\main\java\com\example\notecalc\ThemeManager.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# First, I will revert the bad insertion.
bad_getters = '''
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
content = content.replace(bad_getters, "}")

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
    content = content[:-1].rstrip() + "\n" + getters

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("ThemeManager fixed!")
