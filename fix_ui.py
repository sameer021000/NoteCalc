import re

# 1. Fix layout_editor.xml Header Width
file_path_layout = r'app\src\main\res\layout\layout_editor.xml'
with open(file_path_layout, 'r', encoding='utf-8') as f:
    content_layout = f.read()

header_search = '''        <View
            android:layout_width="0dp"
            android:layout_height="1dp"
            android:layout_weight="1" />

        <EditText
            android:id="@+id/edit_account_title"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:hint="@string/hint_list_title"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="@color/text_primary"
            android:textColorHint="@color/text_tertiary"
            android:inputType="textCapSentences"
            android:singleLine="true"
            android:padding="8dp"
            android:background="@android:color/transparent"
            tools:ignore="Autofill,TextFields" />
            
        <View
            android:layout_width="0dp"
            android:layout_height="1dp"
            android:layout_weight="1" />'''

header_replace = '''        <EditText
            android:id="@+id/edit_account_title"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:hint="@string/hint_list_title"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="@color/text_primary"
            android:textColorHint="@color/text_tertiary"
            android:inputType="textCapSentences"
            android:singleLine="true"
            android:padding="8dp"
            android:background="@android:color/transparent"
            tools:ignore="Autofill,TextFields" />'''

content_layout = content_layout.replace(header_search, header_replace)

with open(file_path_layout, 'w', encoding='utf-8') as f:
    f.write(content_layout)
print("layout_editor.xml patched")

# 2. Fix MainActivity.java Date prefix and autocomplete colors
file_path_main = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path_main, 'r', encoding='utf-8') as f:
    content_main = f.read()

# Replace getString(R.string.date_prefix, selectedRecordDate) with selectedRecordDate
content_main = content_main.replace('getString(R.string.date_prefix, selectedRecordDate)', 'selectedRecordDate')

# Replace the ArrayAdapter in MainActivity to return text with proper color, or we can just set the popup background.
# I'll set the popup background to a colored drawable, or since we have ResponsiveUI we can use createRoundedBg.
adapter_search = '''            android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, catList);
            editCategoryField.setAdapter(catAdapter);
            
            // Fix text colors for autocomplete drop down
            editCategoryField.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    editCategoryField.showDropDown();
                }
            });'''
adapter_replace = '''            android.widget.ArrayAdapter<String> catAdapter = new android.widget.ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, catList) {
                @androidx.annotation.NonNull
                @Override
                public android.view.View getView(int position, @androidx.annotation.Nullable android.view.View convertView, @androidx.annotation.NonNull android.view.ViewGroup parent) {
                    android.view.View view = super.getView(position, convertView, parent);
                    if (view instanceof TextView) {
                        ((TextView) view).setTextColor(ThemeManager.getSecondaryAccentColor(MainActivity.this));
                    }
                    view.setBackgroundColor(ThemeManager.getBgSecondaryColor(MainActivity.this));
                    return view;
                }
            };
            editCategoryField.setAdapter(catAdapter);
            
            // Fix text colors for autocomplete drop down
            editCategoryField.setDropDownBackgroundDrawable(new android.graphics.drawable.ColorDrawable(ThemeManager.getBgSecondaryColor(this)));
            
            editCategoryField.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    editCategoryField.showDropDown();
                }
            });
            editCategoryField.setOnClickListener(v -> editCategoryField.showDropDown());'''

content_main = content_main.replace(adapter_search, adapter_replace)

with open(file_path_main, 'w', encoding='utf-8') as f:
    f.write(content_main)
print("MainActivity.java patched")
