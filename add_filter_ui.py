import re

file_path = r'app\src\main\res\layout\layout_editor.xml'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

search_text = '''        <EditText
            android:id="@+id/edit_records_search"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"'''

replace_text = '''        <ImageView
            android:id="@+id/btn_filter_category"
            android:layout_width="36dp"
            android:layout_height="36dp"
            android:padding="6dp"
            android:src="@android:drawable/ic_menu_sort_by_size" 
            android:tint="?attr/colorAccentSecondary"
            android:clickable="true"
            android:focusable="true"
            android:layout_marginEnd="8dp"
            android:contentDescription="Filter Categories" />

        <EditText
            android:id="@+id/edit_records_search"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"'''

content = content.replace(search_text, replace_text)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Filter UI added")
