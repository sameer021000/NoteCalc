import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Remove textMode initialization
content = content.replace('TextView textMode = editorView.findViewById(R.id.text_editor_mode);', '')

# Remove textMode.setText references
content = re.sub(r'textMode\.setText\([^)]+\);', '', content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("textMode removed!")
