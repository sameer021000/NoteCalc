import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add editCategoryField to instance variables
if 'private AutoCompleteTextView editCategoryField;' not in content:
    content = content.replace('private EditText editRemarksField;', 'private EditText editRemarksField;\n    private android.widget.AutoCompleteTextView editCategoryField;')

# 2. Add AutoCompleteTextView to editor view binding in openEditor
openEditor_binding = '''        TextView btnAdd = editorView.findViewById(R.id.btn_add_record);
        editCategoryField = editorView.findViewById(R.id.edit_record_category);
        if (editCategoryField != null) {
            java.util.Set<String> catSet = new java.util.HashSet<>();
            for (AccountGroup group : appStorage.groups) {
                for (Account acc : group.getAccounts()) {
                    for (Record r : acc.getRecords()) if (!r.getCategory().isEmpty()) catSet.add(r.getCategory());
                }
            }
            for (Account acc : appStorage.standaloneAccounts) {
                for (Record r : acc.getRecords()) if (!r.getCategory().isEmpty()) catSet.add(r.getCategory());
            }
            java.util.List<String> catList = new java.util.ArrayList<>(catSet);
            java.util.Collections.sort(catList);
            android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, catList);
            editCategoryField.setAdapter(catAdapter);
            
            // Fix text colors for autocomplete drop down
            editCategoryField.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    editCategoryField.showDropDown();
                }
            });
        }
'''
if 'editCategoryField = editorView.findViewById(R.id.edit_record_category);' not in content:
    content = content.replace('TextView btnAdd = editorView.findViewById(R.id.btn_add_record);', openEditor_binding)

# 3. Add to btnAdd logic
add_logic_search = '''            String desc = editDesc.getText().toString().trim();
            String amountStr = editAmount.getText().toString().trim();
            String remarks = editRemarksField.getText().toString().trim();'''
add_logic_replace = '''            String desc = editDesc.getText().toString().trim();
            String amountStr = editAmount.getText().toString().trim();
            String remarks = editRemarksField.getText().toString().trim();
            String category = editCategoryField != null ? editCategoryField.getText().toString().trim() : "";'''
if 'String category = editCategoryField' not in content:
    content = content.replace(add_logic_search, add_logic_replace)

update_mode_search = '''                record.setAmount(amount);
                record.setDate(selectedRecordDate);
                record.setRemarks(remarks);'''
update_mode_replace = '''                record.setAmount(amount);
                record.setDate(selectedRecordDate);
                record.setRemarks(remarks);
                record.setCategory(category);'''
if 'record.setCategory(category);' not in content:
    content = content.replace(update_mode_search, update_mode_replace)

add_mode_search = '''                Record newRecord = new Record(desc, amount, selectedRecordDate);
                newRecord.setRemarks(remarks);'''
add_mode_replace = '''                Record newRecord = new Record(desc, amount, selectedRecordDate);
                newRecord.setRemarks(remarks);
                newRecord.setCategory(category);'''
if 'newRecord.setCategory(category);' not in content:
    content = content.replace(add_mode_search, add_mode_replace)

ui_reset_search = '''                editDesc.setText("");
                editAmount.setText("");
                editRemarksField.setText("");'''
ui_reset_replace = '''                editDesc.setText("");
                editAmount.setText("");
                editRemarksField.setText("");
                if (editCategoryField != null) editCategoryField.setText("");'''
if 'if (editCategoryField != null) editCategoryField.setText("");' not in content:
    content = content.replace(ui_reset_search, ui_reset_replace)

# 4. Bind in RecordViewHolder
vh_fields = '''        class RecordViewHolder extends RecyclerView.ViewHolder {
            TextView tvSno;
            TextView tvDesc;
            TextView tvDate;
            TextView tvAmount;
            TextView tvRemarks;
            TextView tvCategory;'''
if 'TextView tvCategory;' not in content:
    content = content.replace('''        class RecordViewHolder extends RecyclerView.ViewHolder {
            TextView tvSno;
            TextView tvDesc;
            TextView tvDate;
            TextView tvAmount;
            TextView tvRemarks;''', vh_fields)

vh_init = '''                tvDate = itemView.findViewById(R.id.text_record_date);
                tvAmount = itemView.findViewById(R.id.text_record_amount);
                tvRemarks = itemView.findViewById(R.id.text_record_remarks);
                tvCategory = itemView.findViewById(R.id.text_record_category);'''
if 'tvCategory = itemView.findViewById(R.id.text_record_category);' not in content:
    content = content.replace('''                tvDate = itemView.findViewById(R.id.text_record_date);
                tvAmount = itemView.findViewById(R.id.text_record_amount);
                tvRemarks = itemView.findViewById(R.id.text_record_remarks);''', vh_init)

# 5. Bind in onBindViewHolder
bind_remarks = '''            // Bind remarks (show only if non-empty)
            String remarks = record.getRemarks();
            if (holder.tvRemarks != null) {
                if (remarks != null && !remarks.isEmpty()) {
                    holder.tvRemarks.setText(remarks);
                    holder.tvRemarks.setVisibility(View.VISIBLE);
                } else {
                    holder.tvRemarks.setVisibility(View.GONE);
                }
            }
            
            // Bind category
            String category = record.getCategory();
            if (holder.tvCategory != null) {
                if (category != null && !category.isEmpty()) {
                    holder.tvCategory.setText(category);
                    holder.tvCategory.setVisibility(View.VISIBLE);
                } else {
                    holder.tvCategory.setVisibility(View.GONE);
                }
            }'''
if 'String category = record.getCategory();' not in content:
    content = content.replace('''            // Bind remarks (show only if non-empty)
            String remarks = record.getRemarks();
            if (holder.tvRemarks != null) {
                if (remarks != null && !remarks.isEmpty()) {
                    holder.tvRemarks.setText(remarks);
                    holder.tvRemarks.setVisibility(View.VISIBLE);
                } else {
                    holder.tvRemarks.setVisibility(View.GONE);
                }
            }''', bind_remarks)

# 6. enterEditRecordMode should set the category
enter_edit_search = '''        editDesc.setText(record.getDescription());
        editAmount.setText(String.format(Locale.getDefault(), "%.2f", record.getAmount()));
        editRemarksField.setText(record.getRemarks());'''
enter_edit_replace = '''        editDesc.setText(record.getDescription());
        editAmount.setText(String.format(Locale.getDefault(), "%.2f", record.getAmount()));
        editRemarksField.setText(record.getRemarks());
        if (editCategoryField != null) editCategoryField.setText(record.getCategory());'''
if 'if (editCategoryField != null) editCategoryField.setText(record.getCategory());' not in content:
    content = content.replace(enter_edit_search, enter_edit_replace)

# 7. cancelEditRecordMode should clear the category
cancel_edit_search = '''        editDesc.setText("");
        editAmount.setText("");
        editRemarksField.setText("");'''
cancel_edit_replace = '''        editDesc.setText("");
        editAmount.setText("");
        editRemarksField.setText("");
        if (editCategoryField != null) editCategoryField.setText("");'''
# Only do this if not already there from ui_reset (which actually matches too, but we can do a global replace carefully)
content = content.replace(cancel_edit_search, cancel_edit_replace)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("MainActivity patched!")
