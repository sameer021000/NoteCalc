import re

file_path = r'app\src\main\res\layout\layout_editor.xml'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

replacement = '''        <TextView
            android:id="@+id/text_editor_mode"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            tools:text="New Expense List"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="@color/text_primary" />
            
        <View
            android:layout_width="0dp"
            android:layout_height="1dp"
            android:layout_weight="1" />

        <ImageView
            android:id="@+id/btn_analytics"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:padding="8dp"
            android:src="@android:drawable/ic_menu_sort_by_size" 
            android:tint="?attr/colorAccentSecondary"
            android:clickable="true"
            android:focusable="true"
            android:contentDescription="Analytics" />'''

content = content.replace('''        <TextView
            android:id="@+id/text_editor_mode"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            tools:text="New Expense List"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="@color/text_primary" />''', replacement)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Added Analytics button to layout_editor.xml")
