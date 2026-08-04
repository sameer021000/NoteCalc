import re

file_path = r'app\src\main\java\com\example\notecalc\Record.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Add category property
content = content.replace('private boolean selected = false;', 'private boolean selected = false;\n    private String category = "";')

# Add getter and setter
getter_setter = '''
    public String getCategory() {
        return category == null ? "" : category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
'''
content = content.replace('    public boolean isSelected() {', getter_setter + '\n    public boolean isSelected() {')

# Add to toJSONObject
content = content.replace('obj.put("remarks", getRemarks());', 'obj.put("remarks", getRemarks());\n        obj.put("category", getCategory());')

# Add to fromJSONObject
content = content.replace('String remarks = obj.optString("remarks", "");', 'String remarks = obj.optString("remarks", "");\n        String category = obj.optString("category", "");')
content = content.replace('r.setRemarks(remarks);', 'r.setRemarks(remarks);\n        r.setCategory(category);')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Record patched")
