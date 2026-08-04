import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

adapter_search = '''                        int hPad = (int)(16 * getResources().getDisplayMetrics().density);
                        int vPad = (int)(8 * getResources().getDisplayMetrics().density);
                        view.setPadding(hPad, vPad, hPad, vPad);'''

adapter_replace = '''                        int hPad = (int)(12 * getResources().getDisplayMetrics().density);
                        int vPad = (int)(4 * getResources().getDisplayMetrics().density);
                        view.setPadding(hPad, vPad, hPad, vPad);
                        view.setMinimumHeight(0);
                        android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
                        if (params != null) {
                            params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
                            view.setLayoutParams(params);
                        }'''

content = content.replace(adapter_search, adapter_replace)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Fix padding applied")
