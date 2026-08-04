import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix PDF 3 dots
pdf_search = '''                while (truncRemarks.length() > 1 && cellMutedPaint.measureText(truncRemarks) > colDesc - 8f) {
                    truncRemarks = truncRemarks.substring(0, truncRemarks.length() - 1);
                }
                if (!truncRemarks.equals(recRemarks)) truncRemarks += "\u2026";'''
pdf_replace = '''                while (truncRemarks.length() > 1 && cellMutedPaint.measureText(truncRemarks) > colDesc - 8f) {
                    truncRemarks = truncRemarks.substring(0, truncRemarks.length() - 1);
                }
                if (!truncRemarks.equals(combinedNotes)) truncRemarks += "\u2026";'''
content = content.replace(pdf_search, pdf_replace)

# Fix Category populate when editing
edit_search = '''        editDescField.setText(record.getDescription());
        editAmountField.setText(String.format(Locale.getDefault(), "%.2f", record.getAmount()));
        editRemarksField.setText(record.getRemarks());
        btnRecordDateField.setText(selectedRecordDate);'''
edit_replace = '''        editDescField.setText(record.getDescription());
        editAmountField.setText(String.format(Locale.getDefault(), "%.2f", record.getAmount()));
        editRemarksField.setText(record.getRemarks());
        btnRecordDateField.setText(selectedRecordDate);
        if (editCategoryField != null) editCategoryField.setText(record.getCategory() == null ? "" : record.getCategory());'''
content = content.replace(edit_search, edit_replace)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Java Edge cases fixed")

# Now layout XML
layout_path = r'app\src\main\res\layout\layout_editor.xml'
with open(layout_path, 'r', encoding='utf-8') as f:
    layout_content = f.read()

remarks_search = '''            <!-- Row for optional Remarks -->
            <EditText
                android:id="@+id/edit_record_remarks"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Remarks (e.g. bought at DMart - optional)"
                android:textSize="14sp"
                android:textColor="@color/text_primary"
                android:textColorHint="@color/text_tertiary"
                android:inputType="textCapSentences"
                android:singleLine="true"
                android:padding="10dp"
                android:layout_marginBottom="8dp"
                android:background="?attr/colorBgPrimary"
                tools:ignore="Autofill,TextFields" />'''

remarks_replace = '''            <!-- Row for optional Remarks and Cancel Button -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:layout_marginBottom="8dp">

                <EditText
                    android:id="@+id/edit_record_remarks"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:hint="Remarks (e.g. bought at DMart - optional)"
                    android:textSize="14sp"
                    android:textColor="@color/text_primary"
                    android:textColorHint="@color/text_tertiary"
                    android:inputType="textCapSentences"
                    android:singleLine="true"
                    android:padding="10dp"
                    android:background="?attr/colorBgPrimary"
                    tools:ignore="Autofill,TextFields" />

                <TextView
                    android:id="@+id/btn_cancel_edit_record"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/btn_cancel_edit_record"
                    android:textColor="@color/text_primary"
                    android:textSize="14sp"
                    android:gravity="center"
                    android:paddingLeft="16dp"
                    android:paddingRight="16dp"
                    android:paddingTop="10dp"
                    android:paddingBottom="10dp"
                    android:layout_marginStart="8dp"
                    android:clickable="true"
                    android:focusable="true"
                    android:visibility="gone"
                    android:background="?attr/colorBgPrimary" />
            </LinearLayout>'''

# Also remove the old Cancel button
cancel_search = '''                <TextView
                    android:id="@+id/btn_cancel_edit_record"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/btn_cancel_edit_record"
                    android:textColor="@color/text_primary"
                    android:textSize="14sp"
                    android:gravity="center"
                    android:paddingLeft="24dp"
                    android:paddingRight="24dp"
                    android:paddingTop="10dp"
                    android:paddingBottom="10dp"
                    android:layout_marginEnd="8dp"
                    android:clickable="true"
                    android:focusable="true"
                    android:visibility="gone"
                    android:background="?attr/colorBgPrimary" />'''

layout_content = layout_content.replace(remarks_search, remarks_replace)
layout_content = layout_content.replace(cancel_search, "")

with open(layout_path, 'w', encoding='utf-8') as f:
    f.write(layout_content)
print("Layout fixed")
