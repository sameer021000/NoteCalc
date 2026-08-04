import re

file_path = r'app\src\main\res\layout\layout_editor.xml'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Replace the text_editor_mode with edit_account_title in header
old_header_title = '''        <TextView
            android:id="@+id/text_editor_mode"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            tools:text="New Expense List"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="@color/text_primary" />'''

new_header_title = '''        <EditText
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
content = content.replace(old_header_title, new_header_title)

# Remove old edit_account_title section entirely
old_title_section = '''    <!-- Account/List Title Input Section -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:layout_marginBottom="16dp">

        <EditText
            android:id="@+id/edit_account_title"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="@string/hint_list_title"
            android:textSize="18sp"
            android:textStyle="bold"
            android:textColor="@color/text_primary"
            android:textColorHint="@color/text_tertiary"
            android:inputType="textCapSentences"
            android:singleLine="true"
            android:padding="12dp"
            android:background="?attr/colorBgSecondary"
            tools:ignore="Autofill,TextFields" />

        <TextView
            android:id="@+id/text_title_error"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/error_title_exists"
            android:textSize="12sp"
            android:textColor="@color/error_red"
            android:layout_marginTop="4dp"
            android:layout_marginStart="4dp"
            android:visibility="gone" />
    </LinearLayout>'''
content = content.replace(old_title_section, '''    <TextView
        android:id="@+id/text_title_error"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/error_title_exists"
        android:textSize="12sp"
        android:textColor="@color/error_red"
        android:layout_marginBottom="8dp"
        android:visibility="gone" />''')

# Modify Date row to include Category. The user wants Date and Category on the same row.
# Let's locate: <!-- Row for Date Selector and Add Button -->
old_date_row = '''            <!-- Row for Date Selector and Add Button -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical">

                <TextView
                    android:id="@+id/btn_record_date"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    tools:text="Date: 17-06-2026"
                    android:textSize="14sp"
                    android:textColor="@color/text_primary"
                    android:padding="10dp"
                    android:gravity="center"
                    android:layout_marginEnd="8dp"
                    android:clickable="true"
                    android:focusable="true"
                    android:background="?attr/colorBgPrimary" />

                <TextView
                    android:id="@+id/btn_cancel_edit_record"'''

new_date_row = '''            <!-- Row for Date Selector, Category, and Add Button -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical">

                <TextView
                    android:id="@+id/btn_record_date"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    tools:text="17-06-2026"
                    android:textSize="14sp"
                    android:textColor="@color/text_primary"
                    android:padding="10dp"
                    android:gravity="center"
                    android:layout_marginEnd="8dp"
                    android:clickable="true"
                    android:focusable="true"
                    android:background="?attr/colorBgPrimary" />
                    
                <AutoCompleteTextView
                    android:id="@+id/edit_record_category"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:hint="Category"
                    android:textSize="14sp"
                    android:textColor="@color/text_primary"
                    android:textColorHint="@color/text_tertiary"
                    android:inputType="textCapWords"
                    android:singleLine="true"
                    android:padding="10dp"
                    android:layout_marginEnd="8dp"
                    android:background="?attr/colorBgPrimary"
                    android:completionThreshold="1"
                    tools:ignore="Autofill,TextFields" />

                <TextView
                    android:id="@+id/btn_cancel_edit_record"'''
content = content.replace(old_date_row, new_date_row)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("layout_editor patched")
