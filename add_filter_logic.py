import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update RecordsAdapter class with filterCategories and logic
adapter_var_search = '''        private List<Record> displayRecords = new ArrayList<>();
        private String currentRecordSearchQuery = "";'''
adapter_var_replace = '''        private List<Record> displayRecords = new ArrayList<>();
        private String currentRecordSearchQuery = "";
        public java.util.Set<String> filterCategories = new java.util.HashSet<>();

        public void setFilterCategories(java.util.Set<String> cats) {
            filterCategories.clear();
            if (cats != null) filterCategories.addAll(cats);
            refreshDisplay();
        }'''
content = content.replace(adapter_var_search, adapter_var_replace)

adapter_filter_search = '''                // Text search filter
                if (!q.isEmpty() && !r.getDescription().toLowerCase(Locale.getDefault()).contains(q)) {
                    continue;
                }'''
adapter_filter_replace = '''                // Category filter
                if (!filterCategories.isEmpty()) {
                    if (!filterCategories.contains(r.getCategory())) continue;
                }
                // Text search filter
                if (!q.isEmpty() && !r.getDescription().toLowerCase(Locale.getDefault()).contains(q)) {
                    continue;
                }'''
content = content.replace(adapter_filter_search, adapter_filter_replace)

# 2. Add btnFilterCategory handling in openEditor
binding_search = '''        ImageView btnAnalytics = editorView.findViewById(R.id.btn_analytics);
        if (btnAnalytics != null) {
            setupClickable(btnAnalytics, true, () -> showAnalytics(currentEditingAccount));
        }'''
binding_replace = '''        ImageView btnAnalytics = editorView.findViewById(R.id.btn_analytics);
        if (btnAnalytics != null) {
            setupClickable(btnAnalytics, true, () -> showAnalytics(currentEditingAccount));
        }
        
        ImageView btnFilterCategory = editorView.findViewById(R.id.btn_filter_category);
        if (btnFilterCategory != null) {
            setupClickable(btnFilterCategory, true, () -> showCategoryFilterDialog(currentEditingAccount, btnFilterCategory));
        }'''
content = content.replace(binding_search, binding_replace)

# 3. Add showCategoryFilterDialog method to MainActivity
new_method = '''
    private void showCategoryFilterDialog(Account account, ImageView btnFilterIcon) {
        java.util.Set<String> uniqueCats = new java.util.HashSet<>();
        for (Record r : account.getRecords()) {
            if (r.getCategory() != null && !r.getCategory().isEmpty()) {
                uniqueCats.add(r.getCategory());
            }
        }
        if (uniqueCats.isEmpty()) {
            Toast.makeText(this, "No categories available to filter.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> catList = new ArrayList<>(uniqueCats);
        java.util.Collections.sort(catList);
        
        String[] catArray = catList.toArray(new String[0]);
        boolean[] checkedItems = new boolean[catArray.length];
        
        for (int i = 0; i < catArray.length; i++) {
            if (recordsAdapter != null && recordsAdapter.filterCategories.contains(catArray[i])) {
                checkedItems[i] = true;
            }
        }

        new android.app.AlertDialog.Builder(this)
            .setTitle("Filter by Category")
            .setMultiChoiceItems(catArray, checkedItems, (dialog, which, isChecked) -> {
                checkedItems[which] = isChecked;
            })
            .setPositiveButton("Apply", (dialog, which) -> {
                java.util.Set<String> selected = new java.util.HashSet<>();
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) selected.add(catArray[i]);
                }
                if (recordsAdapter != null) {
                    recordsAdapter.setFilterCategories(selected);
                }
                if (selected.isEmpty()) {
                    btnFilterIcon.setColorFilter(ThemeManager.getSecondaryAccentColor(this));
                } else {
                    btnFilterIcon.setColorFilter(ThemeManager.getPrimaryAccentColor(this));
                }
            })
            .setNegativeButton("Clear All", (dialog, which) -> {
                if (recordsAdapter != null) {
                    recordsAdapter.setFilterCategories(new java.util.HashSet<>());
                }
                btnFilterIcon.setColorFilter(ThemeManager.getSecondaryAccentColor(this));
            })
            .show();
    }
'''
# inject method right before showDashboard
content = content.replace('private void showDashboard() {', new_method + '\n    private void showDashboard() {')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Filter logic added")
