import re

file_path = r'app\src\main\res\layout\item_record.xml'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

old_remarks = '''        <TextView
            android:id="@+id/text_record_remarks"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            tools:text="bought at DMart"
            android:textSize="11sp"
            android:textColor="@color/text_tertiary"
            android:textStyle="italic"
            android:visibility="gone"
            android:layout_marginTop="2dp" />
    </LinearLayout>'''

new_remarks_and_category = '''        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginTop="2dp"
            android:gravity="center_vertical">
            
            <TextView
                android:id="@+id/text_record_category"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                tools:text="Food"
                android:textSize="10sp"
                android:textColor="@color/text_on_accent"
                android:background="?attr/colorAccentPrimary"
                android:paddingStart="4dp"
                android:paddingEnd="4dp"
                android:paddingTop="1dp"
                android:paddingBottom="1dp"
                android:layout_marginEnd="4dp"
                android:visibility="gone" />

            <TextView
                android:id="@+id/text_record_remarks"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                tools:text="bought at DMart"
                android:textSize="11sp"
                android:textColor="@color/text_tertiary"
                android:textStyle="italic"
                android:visibility="gone" />
        </LinearLayout>
    </LinearLayout>'''

content = content.replace(old_remarks, new_remarks_and_category)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("item_record patched")
