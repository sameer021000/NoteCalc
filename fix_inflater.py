import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix 1: showDashboard inflater
search_dashboard = '                View dashboardView = inflater.inflate(R.layout.layout_dashboard, mainContainer, false);'
replace_dashboard = '                LayoutInflater inflater = getLayoutInflater();\n                View dashboardView = inflater.inflate(R.layout.layout_dashboard, mainContainer, false);'
content = content.replace(search_dashboard, replace_dashboard)

# Fix 2: openEditor inflater
search_editor = '                View editorView = inflater.inflate(R.layout.layout_editor, mainContainer, false);'
replace_editor = '                LayoutInflater inflater = getLayoutInflater();\n                View editorView = inflater.inflate(R.layout.layout_editor, mainContainer, false);'
content = content.replace(search_editor, replace_editor)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Inflater restored")
