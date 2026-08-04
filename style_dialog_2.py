import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

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
        
        boolean[] checkedItems = new boolean[catList.size()];
        for (int i = 0; i < catList.size(); i++) {
            if (recordsAdapter != null && recordsAdapter.filterCategories.contains(catList.get(i))) {
                checkedItems[i] = true;
            }
        }

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(20 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);
        
        TextView title = new TextView(this);
        title.setText("Filter by Category");
        title.setTextSize(20);
        title.setTextColor(ThemeManager.getTextPrimaryColor(this));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, pad);
        root.addView(title);
        
        android.widget.ListView listView = new android.widget.ListView(this);
        listView.setDividerHeight(0);
        
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, catList) {
            @androidx.annotation.NonNull
            @Override
            public android.view.View getView(int position, @androidx.annotation.Nullable android.view.View convertView, @androidx.annotation.NonNull android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                if (view instanceof android.widget.CheckedTextView) {
                    android.widget.CheckedTextView ctv = (android.widget.CheckedTextView) view;
                    ctv.setTextColor(ThemeManager.getTextPrimaryColor(MainActivity.this));
                    ctv.setCheckMarkTintList(android.content.res.ColorStateList.valueOf(ThemeManager.getPrimaryAccentColor(MainActivity.this)));
                    int ipads = (int)(12 * getResources().getDisplayMetrics().density);
                    ctv.setPadding(ipads, ipads, ipads, ipads);
                }
                view.setBackgroundColor(ThemeManager.getBgPrimaryColor(MainActivity.this));
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setChoiceMode(android.widget.ListView.CHOICE_MODE_MULTIPLE);
        for (int i = 0; i < checkedItems.length; i++) {
            listView.setItemChecked(i, checkedItems[i]);
        }
        
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        root.addView(listView, listParams);
        
        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.setGravity(android.view.Gravity.END);
        btnLayout.setPadding(0, pad, 0, 0);
        
        android.widget.Button btnClear = new android.widget.Button(this);
        btnClear.setText("CLEAR ALL");
        btnClear.setTextColor(ThemeManager.getTextSecondaryColor(this));
        btnClear.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        
        android.widget.Button btnApply = new android.widget.Button(this);
        btnApply.setText("APPLY");
        btnApply.setTextColor(ThemeManager.getPrimaryAccentColor(this));
        btnApply.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        
        btnLayout.addView(btnClear);
        btnLayout.addView(btnApply);
        root.addView(btnLayout);
        
        dialog.setContentView(root);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(this), ThemeManager.getBorderColor(this), 1.0f, 16.0f));
            // Set max height if needed
            dialog.getWindow().setLayout((int)(300 * getResources().getDisplayMetrics().density), android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        
        btnApply.setOnClickListener(v -> {
            java.util.Set<String> selected = new java.util.HashSet<>();
            android.util.SparseBooleanArray checked = listView.getCheckedItemPositions();
            for (int i = 0; i < catList.size(); i++) {
                if (checked.get(i)) selected.add(catList.get(i));
            }
            if (recordsAdapter != null) {
                recordsAdapter.setFilterCategories(selected);
            }
            if (selected.isEmpty()) {
                btnFilterIcon.setColorFilter(ThemeManager.getSecondaryAccentColor(this));
            } else {
                btnFilterIcon.setColorFilter(ThemeManager.getPrimaryAccentColor(this));
            }
            dialog.dismiss();
        });
        
        btnClear.setOnClickListener(v -> {
            if (recordsAdapter != null) {
                recordsAdapter.setFilterCategories(new java.util.HashSet<>());
            }
            btnFilterIcon.setColorFilter(ThemeManager.getSecondaryAccentColor(this));
            dialog.dismiss();
        });
        
        dialog.show();
    }
'''

content = content.replace('private void showDashboard() {', new_method + '\n    private void showDashboard() {')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Beautiful custom dialog added")
