import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1) result = 0 fix (with correct comment 'Total amount' and 'Latest modified')
search_result = '''            int result = 0;
            if (mode == 0) { // Title
                result = a.getTitle().compareToIgnoreCase(b.getTitle());
            } else if (mode == 1) { // Total amount
                result = Double.compare(a.calculateTotal(), b.calculateTotal());
            } else { // Latest modified
                result = Long.compare(a.getLastModified(), b.getLastModified());
            }'''
replace_result = '''            int result;
            if (mode == 0) { // Title
                result = a.getTitle().compareToIgnoreCase(b.getTitle());
            } else if (mode == 1) { // Total amount
                result = Double.compare(a.calculateTotal(), b.calculateTotal());
            } else { // Latest modified
                result = Long.compare(a.getLastModified(), b.getLastModified());
            }'''
content = content.replace(search_result, replace_result)

# 4) printStackTrace -> Log.e line 1754
search_trace = '''                    } catch (Exception e) {
                        e.printStackTrace();
                    }'''
replace_trace = '''                    } catch (Exception e) {
                        android.util.Log.e("NoteCalc", "Error resetting date", e);
                    }'''
content = content.replace(search_trace, replace_trace)

# 5) Statement lambda to expression lambda 1815
search_lambda_1 = '''            setupClickable(holder.itemView, true, () -> {
                enterEditRecordMode(trueIndex, record);
            }, () -> {'''
replace_lambda_1 = '''            setupClickable(holder.itemView, true, () -> enterEditRecordMode(trueIndex, record), () -> {'''
content = content.replace(search_lambda_1, replace_lambda_1)

# 6, 7) Private field TYPE_ACCOUNT and TYPE_GROUP never used
search_type = '''        private static final int TYPE_ACCOUNT = 0;
        private static final int TYPE_GROUP = 1;

        private final List<Object> displayItems = new ArrayList<>();'''

replace_type = '''        private static final int TYPE_ACCOUNT = 0;
        private static final int TYPE_GROUP = 1;

        private final List<Object> displayItems = new ArrayList<>();''' # Wait, these will be fixed by just using them in getItemViewType

content = content.replace('if (displayItems.get(position) instanceof AccountGroup) return 1; // TYPE_GROUP', 'if (displayItems.get(position) instanceof AccountGroup) return TYPE_GROUP;')
content = content.replace('return 0; // TYPE_ACCOUNT', 'return TYPE_ACCOUNT;')
content = content.replace('if (viewType == 1) { // TYPE_GROUP', 'if (viewType == TYPE_GROUP) {')

# 10) Statement lambda to expression lambda line 1965
search_lambda_2 = '''                }, () -> {
                    showAccountPopupMenu(accHolder.itemView, account);
                });'''
replace_lambda_2 = '''                }, () -> showAccountPopupMenu(accHolder.itemView, account));'''
content = content.replace(search_lambda_2, replace_lambda_2)

# 11) groupNames never used
search_groupNames = '''                            String[] groupNames = new String[appStorage.groups.size()];
                            for (int i = 0; i < appStorage.groups.size(); i++) {
                                groupNames[i] = appStorage.groups.get(i).getTitle();
                            }'''
content = content.replace(search_groupNames, '')

# 12) identical branches R.drawable.ic_pin
content = content.replace('account.isPinned() ? R.drawable.ic_pin : R.drawable.ic_pin', 'R.drawable.ic_pin')

# 14) Statement lambda to expression lambda line 2091
search_lambda_3 = '''                setupClickable(groupHolder.itemView, true, () -> {
                    openGroup(group);
                }, () -> {
                    showGroupPopupMenu(groupHolder.itemView, group);
                });'''
replace_lambda_3 = '''                setupClickable(groupHolder.itemView, true, () -> openGroup(group), () -> showGroupPopupMenu(groupHolder.itemView, group));'''
content = content.replace(search_lambda_3, replace_lambda_3)

# 16) inflater never used
content = content.replace('LayoutInflater inflater = LayoutInflater.from(this);\n', '')

# 8, 9, 13, 15, 17, 18, 19, 20) Suppress warnings on methods/classes
content = content.replace('private class AccountsAdapter', '@android.annotation.SuppressLint("NotifyDataSetChanged")\n    private class AccountsAdapter')

search_updateSelectAll = 'private void updateSelectAllHeaderState() {'
replace_updateSelectAll = '@android.annotation.SuppressLint("SetTextI18n")\n    private void updateSelectAllHeaderState() {'
content = content.replace(search_updateSelectAll, replace_updateSelectAll)

search_updateDelete = 'private void updateDeleteSelectedButtonState() {'
replace_updateDelete = '@android.annotation.SuppressLint("SetTextI18n")\n    private void updateDeleteSelectedButtonState() {'
content = content.replace(search_updateDelete, replace_updateDelete)

search_showDeleteMultiple = 'private void showDeleteMultipleConfirmationDialog(List<Record> selectedRecords) {'
replace_showDeleteMultiple = '@android.annotation.SuppressLint("SetTextI18n")\n    private void showDeleteMultipleConfirmationDialog(List<Record> selectedRecords) {'
content = content.replace(search_showDeleteMultiple, replace_showDeleteMultiple)

search_showAccountPopup = 'private void showAccountPopupMenu(View anchor, Account account) {'
replace_showAccountPopup = '@android.annotation.SuppressLint({"SetTextI18n", "InflateParams"})\n    private void showAccountPopupMenu(View anchor, Account account) {'
content = content.replace(search_showAccountPopup, replace_showAccountPopup)

search_showGroupPopup = 'private void showGroupPopupMenu(View anchor, AccountGroup group) {'
replace_showGroupPopup = '@android.annotation.SuppressLint({"SetTextI18n", "InflateParams"})\n    private void showGroupPopupMenu(View anchor, AccountGroup group) {'
content = content.replace(search_showGroupPopup, replace_showGroupPopup)

search_showDeleteAccount = 'private void showDeleteAccountConfirmationDialog(final Account account) {'
replace_showDeleteAccount = '@android.annotation.SuppressLint("SetTextI18n")\n    private void showDeleteAccountConfirmationDialog(final Account account) {'
content = content.replace(search_showDeleteAccount, replace_showDeleteAccount)

search_enterEdit = 'private void enterEditRecordMode(int index, Record record) {'
replace_enterEdit = '@android.annotation.SuppressLint("SetTextI18n")\n    private void enterEditRecordMode(int index, Record record) {'
content = content.replace(search_enterEdit, replace_enterEdit)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Warnings 4 fixed")
