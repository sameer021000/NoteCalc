import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1, 2, 3, 4: Supress in openEditor
search_open_editor = 'private void openEditor(Account account) {'
replace_open_editor = '@android.annotation.SuppressLint({"SetTextI18n", "ClickableViewAccessibility", "NotifyDataSetChanged"})\n    private void openEditor(Account account) {'
if replace_open_editor not in content:
    content = content.replace(search_open_editor, replace_open_editor)

# 7, 8, 9, 10: Supress in updateHeaderLabels
search_update_headers = 'private void updateHeaderLabels() {'
replace_update_headers = '@android.annotation.SuppressLint("SetTextI18n")\n    private void updateHeaderLabels() {'
if replace_update_headers not in content:
    content = content.replace(search_update_headers, replace_update_headers)

# 11, 12, 14: Supress in RecordsAdapter
search_records_adapter = 'private class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RecordViewHolder> {'
replace_records_adapter = '@android.annotation.SuppressLint("NotifyDataSetChanged")\n    private class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RecordViewHolder> {'
if replace_records_adapter not in content:
    content = content.replace(search_records_adapter, replace_records_adapter)

# 12: final displayRecords
search_display_records = 'List<Record> displayRecords = new ArrayList<>();'
replace_display_records = 'final List<Record> displayRecords = new ArrayList<>();'
content = content.replace(search_display_records, replace_display_records)

# 5, 6: applySorting Collections.sort and <>
search_sort_1 = 'java.util.Collections.sort(records, new java.util.Comparator<Record>() {'
replace_sort_1 = 'java.util.Collections.sort(records, new java.util.Comparator<>() {'
content = content.replace(search_sort_1, replace_sort_1)
search_sort_2 = 'java.util.Collections.sort(displayRecords, new java.util.Comparator<Record>() {'
replace_sort_2 = 'java.util.Collections.sort(displayRecords, new java.util.Comparator<>() {'
content = content.replace(search_sort_2, replace_sort_2)

# 13: printStackTrace inside RecordsAdapter
search_trace = '''                    } catch (ParseException e) {
                        e.printStackTrace();
                    }'''
replace_trace = '''                    } catch (ParseException e) {
                        android.util.Log.e("NoteCalc", "Date parse error", e);
                    }'''
content = content.replace(search_trace, replace_trace)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Warnings 2 fixed")
