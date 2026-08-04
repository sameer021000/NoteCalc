import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

adapter_search = '''        private List<Record> displayRecords = new ArrayList<>();'''

adapter_replace = '''        private List<Record> displayRecords = new ArrayList<>();
        public java.util.Set<String> filterCategories = new java.util.HashSet<>();

        public void setFilterCategories(java.util.Set<String> cats) {
            filterCategories.clear();
            if (cats != null) filterCategories.addAll(cats);
            refreshDisplay();
        }'''

content = content.replace(adapter_search, adapter_replace)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Adapter vars fixed")
