import os

helper_path = r'c:\Users\Sameer Shaik\NoteCalc\app\src\main\java\com\example\notecalc\EditorHelper.java'
with open(helper_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Attachments Dialog
t1_start = '        if (activity.btnAttachFile != null) {'
t1_end = '                dialog.show();\n            });\n        }'
start_idx = content.find(t1_start)
end_idx = content.find(t1_end) + len(t1_end)
if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + '        EditorAttachmentsDialogHelper.setupAttachmentsDialog(activity);' + content[end_idx:]
else:
    print('Failed t1')

# 2. Category Helper
t2_start = '        if (activity.editCategoryField != null) {'
t2_end = '            activity.editCategoryField.setOnClickListener(v -> activity.editCategoryField.showDropDown());\n        }'
start_idx = content.find(t2_start)
end_idx = content.find(t2_end) + len(t2_end)
if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + '        EditorCategoryHelper.setupCategoryDropdown(activity);' + content[end_idx:]
else:
    print('Failed t2')

# 3. Theme Helper
t3_start = '        editTitle.setBackground(ResponsiveUI.createRoundedBg('
t3_end = '                0,\n                6.0f\n        ));'
start_idx = content.find(t3_start)
end_idx = content.rfind(t3_end) + len(t3_end) # Use rfind to get the last one (btnSave)
if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + '        if (account != null && account.isArchived()) {\n            btnSave.setVisibility(View.GONE);\n        }\n        EditorThemeHelper.applyEditorTheme(activity, formContainer, tableHeader, editTitle, editDesc, editAmount, btnDate, btnCancelEdit, btnAdd, btnSave);' + content[end_idx:]
else:
    print('Failed t3')

# 4. Save Helper
t4_start = '        ResponsiveUI.setupClickable(btnAdd, () -> {'
t4_end = '            DashboardHelper.showDashboard(activity);\n        });'
start_idx = content.find(t4_start)
end_idx = content.rfind(t4_end) + len(t4_end)
if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + '        EditorSaveHelper.setupSaveActions(activity, editTitle, editDesc, editAmount, btnAdd, btnSave);' + content[end_idx:]
else:
    print('Failed t4')

with open(helper_path, 'w', encoding='utf-8') as f:
    f.write(content)
print('Done!')
