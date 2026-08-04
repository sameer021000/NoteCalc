import re

file_path = r'app\src\main\res\layout\layout_editor.xml'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

fab_xml = '''        <ImageView
            android:id="@+id/btn_nc_agent"
            android:layout_width="56dp"
            android:layout_height="56dp"
            android:layout_gravity="bottom|end"
            android:layout_margin="16dp"
            android:padding="16dp"
            android:src="@android:drawable/ic_menu_edit"
            android:background="?attr/colorAccentPrimary"
            android:elevation="6dp"
            android:clickable="true"
            android:focusable="true"
            android:contentDescription="NC Agent" />
'''

# Find the end of the empty state layout to insert the FAB before the FrameLayout closes
empty_state_end_search = '''        </LinearLayout>
    </FrameLayout>'''

empty_state_end_replace = '''        </LinearLayout>
''' + fab_xml + '''    </FrameLayout>'''

content = content.replace(empty_state_end_search, empty_state_end_replace)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("FAB added to layout")
