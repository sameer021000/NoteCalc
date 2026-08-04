import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

adapter_search = '''            android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, catList) {
                @androidx.annotation.NonNull
                @Override
                public android.view.View getView(int position, @androidx.annotation.Nullable android.view.View convertView, @androidx.annotation.NonNull android.view.ViewGroup parent) {
                    android.view.View view = super.getView(position, convertView, parent);
                    if (view instanceof TextView) {
                        ((TextView) view).setTextColor(ThemeManager.getSecondaryAccentColor(MainActivity.this));
                        int hPad = (int)(12 * getResources().getDisplayMetrics().density);
                        int vPad = (int)(4 * getResources().getDisplayMetrics().density);
                        view.setPadding(hPad, vPad, hPad, vPad);
                        view.setMinimumHeight(0);
                        android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
                        if (params != null) {
                            params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
                            view.setLayoutParams(params);
                        }
                    }
                    view.setBackgroundColor(ThemeManager.getBgSecondaryColor(MainActivity.this));
                    return view;
                }
            };
            editCategoryField.setAdapter(catAdapter);
            
            // Fix text colors for autocomplete drop down
            editCategoryField.setDropDownBackgroundDrawable(new android.graphics.drawable.ColorDrawable(ThemeManager.getBgSecondaryColor(this)));'''

adapter_replace = '''            android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, catList) {
                @androidx.annotation.NonNull
                @Override
                public android.view.View getView(int position, @androidx.annotation.Nullable android.view.View convertView, @androidx.annotation.NonNull android.view.ViewGroup parent) {
                    android.view.View coreView = super.getView(position, convertView, parent);
                    
                    LinearLayout container = new LinearLayout(MainActivity.this);
                    container.setOrientation(LinearLayout.VERTICAL);
                    
                    if (coreView instanceof TextView) {
                        ((TextView) coreView).setTextColor(ThemeManager.getSecondaryAccentColor(MainActivity.this));
                        int hPad = (int)(12 * getResources().getDisplayMetrics().density);
                        int vPad = (int)(8 * getResources().getDisplayMetrics().density); // Slightly larger padding since we have lines
                        coreView.setPadding(hPad, vPad, hPad, vPad);
                        coreView.setMinimumHeight(0);
                        android.view.ViewGroup.LayoutParams params = coreView.getLayoutParams();
                        if (params != null) {
                            params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
                            coreView.setLayoutParams(params);
                        }
                    }
                    
                    // The text view
                    container.addView(coreView);
                    
                    // The thin line separator (only if not the last item, though it's easier to just add it to all)
                    if (position < getCount() - 1) {
                        View divider = new View(MainActivity.this);
                        divider.setBackgroundColor(ThemeManager.getBorderColor(MainActivity.this));
                        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1); // 1px thin line
                        container.addView(divider, divParams);
                    }
                    
                    container.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                    return container;
                }
            };
            editCategoryField.setAdapter(catAdapter);
            
            // Set rounded corners for autocomplete drop down box
            editCategoryField.setDropDownBackgroundDrawable(ResponsiveUI.createRoundedBg(
                    this,
                    ThemeManager.getBgSecondaryColor(this),
                    ThemeManager.getBorderColor(this),
                    1.0f,
                    8.0f // nice curve
            ));'''

content = content.replace(adapter_search, adapter_replace)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Dropdown UI modifications applied")
