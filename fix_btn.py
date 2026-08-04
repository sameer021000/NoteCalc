import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

replacement = '''ImageView btnBack = editorView.findViewById(R.id.btn_back);
        ImageView btnAnalytics = editorView.findViewById(R.id.btn_analytics);
        if (btnAnalytics != null) {
            setupClickable(btnAnalytics, true, () -> showAnalytics(currentEditingAccount));
        }'''

content = content.replace('ImageView btnBack = editorView.findViewById(R.id.btn_back);', replacement)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Button injected!")
