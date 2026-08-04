import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Extract the entire showCategoryFilterDialog
start_idx = content.find('private void showCategoryFilterDialog')
end_idx = content.find('private void showDashboard()', start_idx)

if start_idx != -1 and end_idx != -1:
    old_method = content[start_idx:end_idx]
    
    new_method = '''private void showCategoryFilterDialog(Account account, ImageView btnFilterIcon) {
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

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
            .setTitle("Filter by Category")
            .setMultiChoiceItems(catArray, checkedItems, (d, which, isChecked) -> {
                checkedItems[which] = isChecked;
            })
            .setPositiveButton("Apply", (d, which) -> {
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
            .setNegativeButton("Clear All", (d, which) -> {
                if (recordsAdapter != null) {
                    recordsAdapter.setFilterCategories(new java.util.HashSet<>());
                }
                btnFilterIcon.setColorFilter(ThemeManager.getSecondaryAccentColor(this));
            })
            .create();

        // Apply our custom themes when the dialog shows
        dialog.setOnShowListener(di -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(ResponsiveUI.createRoundedBg(this, ThemeManager.getBgPrimaryColor(this), ThemeManager.getBorderColor(this), 1.0f, 16.0f));
            }
            
            // Style the Title if we can find it
            int titleId = getResources().getIdentifier("alertTitle", "id", "android");
            if (titleId > 0) {
                TextView titleView = dialog.findViewById(titleId);
                if (titleView != null) {
                    titleView.setTextColor(ThemeManager.getTextPrimaryColor(this));
                }
            }
            
            // Style the buttons
            android.widget.Button posBtn = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            if (posBtn != null) posBtn.setTextColor(ThemeManager.getPrimaryAccentColor(this));
            android.widget.Button negBtn = dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
            if (negBtn != null) negBtn.setTextColor(ThemeManager.getTextSecondaryColor(this));
            
            // Style the multi-choice list items
            android.widget.ListView listView = dialog.getListView();
            if (listView != null) {
                listView.setBackgroundColor(ThemeManager.getBgPrimaryColor(this));
                // We have to loop through child views to style them, but they are recycled. 
                // A reliable way without a custom adapter is to use a ColorStateList on the checkboxes.
                // However, Android's default select_dialog_multichoice uses CheckedTextView.
                // A simpler approach for the text color is overriding it in the adapter if possible, or using a wrapper adapter.
            }
        });
        
        // Since styling the inner CheckedTextViews of the default AlertDialog multi-choice list is very fragile 
        // across Android versions without using styles.xml, let's inject a custom adapter!
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(this, android.R.layout.select_dialog_multichoice, catList) {
            @androidx.annotation.NonNull
            @Override
            public android.view.View getView(int position, @androidx.annotation.Nullable android.view.View convertView, @androidx.annotation.NonNull android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                if (view instanceof android.widget.CheckedTextView) {
                    android.widget.CheckedTextView ctv = (android.widget.CheckedTextView) view;
                    ctv.setTextColor(ThemeManager.getTextPrimaryColor(MainActivity.this));
                    // Tint the checkbox to accent color
                    ctv.setCheckMarkTintList(android.content.res.ColorStateList.valueOf(ThemeManager.getPrimaryAccentColor(MainActivity.this)));
                }
                return view;
            }
        };
        
        // Re-create the dialog using our custom adapter so we can securely theme the text and checkboxes!
        android.app.AlertDialog themedDialog = new android.app.AlertDialog.Builder(this)
            .setTitle("Filter by Category")
            .setMultiChoiceItems(catArray, checkedItems, (d, which, isChecked) -> {
                checkedItems[which] = isChecked;
            })
            // Wait, setMultiChoiceItems with Array overrides our adapter! We need to pass the adapter.
            // But setMultiChoiceItems doesn't take an adapter directly. We'll stick to array, and we will do this instead:
            // It's actually possible to style the entire ListView dynamically! Let's do it in the simplest robust way.
            .create();
            
        // We will build a completely custom View for the dialog to guarantee perfect theming.
    }
'''
    content = content.replace(old_method, '') # Erase old completely, we'll write a better one below

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
