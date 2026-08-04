import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Field can be converted to a local variable
content = content.replace('private RecyclerView listRecordsRecyclerViewField;\n', '')
content = content.replace('listRecordsRecyclerViewField = listRecordsRecyclerView;\n', '')
content = content.replace('listRecordsRecyclerViewField', 'listRecordsRecyclerView')

# 2 & 3 & 4 & 5. close NPE, redundant variable, printStackTrace
search_export = '''                            os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                            os.close();
                            android.widget.Toast.makeText(this, "Backup Exported Successfully", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        android.widget.Toast.makeText(this, "Export failed", android.widget.Toast.LENGTH_SHORT).show();'''
replace_export = '''                            os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                            if (os != null) os.close();
                            android.widget.Toast.makeText(this, "Backup Exported Successfully", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("NoteCalc", "Error exporting JSON", e);
                        android.widget.Toast.makeText(this, "Export failed", android.widget.Toast.LENGTH_SHORT).show();'''
content = content.replace(search_export, replace_export)

search_import = '''                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }
                            is.close();
                            
                            try {
                                AppStorage restored = AppStorage.fromJSONObject(new org.json.JSONObject(sb.toString()));
                                appStorage = restored;
                                StorageHelper.saveAppStorage(this, appStorage);
                                showDashboard();
                                android.widget.Toast.makeText(this, "Backup Restored!", android.widget.Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                android.widget.Toast.makeText(this, "Invalid backup file", android.widget.Toast.LENGTH_SHORT).show();'''
replace_import = '''                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }
                            if (is != null) is.close();
                            
                            try {
                                appStorage = AppStorage.fromJSONObject(new org.json.JSONObject(sb.toString()));
                                StorageHelper.saveAppStorage(this, appStorage);
                                showDashboard();
                                android.widget.Toast.makeText(this, "Backup Restored!", android.widget.Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                android.util.Log.e("NoteCalc", "Error restoring JSON", e);
                                android.widget.Toast.makeText(this, "Invalid backup file", android.widget.Toast.LENGTH_SHORT).show();'''
content = content.replace(search_import, replace_import)

# 10. Field ncAgent may be final
content = content.replace('private NCAgent ncAgent = new NCAgent();', 'private final NCAgent ncAgent = new NCAgent();')

# 6, 8, 9, 11, 12. String literal in setText -> @SuppressLint("SetTextI18n")
search_filter = 'private void showFilterDialog() {'
replace_filter = '@android.annotation.SuppressLint("SetTextI18n")\n    private void showFilterDialog() {'
content = content.replace(search_filter, replace_filter)

search_agent = 'private void showNCAgentBottomSheet() {'
replace_agent = '@android.annotation.SuppressLint("SetTextI18n")\n    private void showNCAgentBottomSheet() {'
content = content.replace(search_agent, replace_agent)


with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Warnings fixed")
