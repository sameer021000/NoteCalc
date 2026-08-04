import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix the ClassCastException in ArrayAdapter
crash_search = '''            android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, catList) {
                @androidx.annotation.NonNull
                @Override
                public android.view.View getView(int position, @androidx.annotation.Nullable android.view.View convertView, @androidx.annotation.NonNull android.view.ViewGroup parent) {
                    android.view.View coreView = super.getView(position, convertView, parent);'''

crash_replace = '''            android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, catList) {
                @androidx.annotation.NonNull
                @Override
                public android.view.View getView(int position, @androidx.annotation.Nullable android.view.View convertView, @androidx.annotation.NonNull android.view.ViewGroup parent) {
                    // Pass null to avoid ClassCastException since we wrap the view in a LinearLayout
                    android.view.View coreView = super.getView(position, null, parent);'''

content = content.replace(crash_search, crash_replace)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Crash fix applied")
