import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1, 2) applySorting Collections.sort to List.sort
search_sort_1 = 'java.util.Collections.sort(getActiveRecords(), new java.util.Comparator<Record>() {'
replace_sort_1 = 'getActiveRecords().sort(new java.util.Comparator<>() {'
content = content.replace(search_sort_1, replace_sort_1)

# 3, 4, 6) displayItems final + AccountsAdapter suppressions
search_displayItems = 'private List<Object> displayItems = new ArrayList<>();'
replace_displayItems = 'private final List<Object> displayItems = new ArrayList<>();'
content = content.replace(search_displayItems, replace_displayItems)

search_adapter = '@android.annotation.SuppressLint("NotifyDataSetChanged")\n    private class AccountsAdapter'
replace_adapter = '@android.annotation.SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})\n    private class AccountsAdapter'
content = content.replace(search_adapter, replace_adapter)

# 5) Statement lambda to expression lambda line 2085
search_lambda_1 = '''                setupClickable(grpHolder.btnDeleteGroup, false, () -> {
                    showDeleteGroupConfirmation(group);
                });'''
replace_lambda_1 = '''                setupClickable(grpHolder.btnDeleteGroup, false, () -> showDeleteGroupConfirmation(group));'''
content = content.replace(search_lambda_1, replace_lambda_1)

# 7, 8) Suppress SetTextI18n on cancelEditRecordMode
search_cancel = 'private void cancelEditRecordMode() {'
replace_cancel = '@android.annotation.SuppressLint("SetTextI18n")\n    private void cancelEditRecordMode() {'
content = content.replace(search_cancel, replace_cancel)

# 9, 10) Remove unused methods getRippleDrawable and createRoundedRippleBg
# Actually, wait, getRippleDrawable was:
# private Drawable getRippleDrawable(Drawable content, int rippleColor) {
# ... }
# Let's just use a regex or string replacement.
content = re.sub(r'(?s)\s*/\*\*\n\s*\* Wraps a background drawable in a RippleDrawable.*?\n    }', '', content)
content = re.sub(r'(?s)\s*/\*\*\n\s*\* Creates a rounded background wrapped in a RippleDrawable.*?\n    }', '', content)

# 11, 12, 13) handler final, longPressRunnable final and lambda
search_runnable = '''            private android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            private Runnable longPressRunnable = new Runnable() {
                @Override
                public void run() {'''
replace_runnable = '''            private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            private final Runnable longPressRunnable = () -> {'''
content = content.replace(search_runnable, replace_runnable)

# 14, 15) parse() null check line 2728, 2739
search_parse_from = 'try { cal.setTime(new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(tempFrom[0])); } catch (Exception ignored) {}'
replace_parse_from = '''try {
                    java.util.Date parsed = new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(tempFrom[0]);
                    if (parsed != null) cal.setTime(parsed);
                } catch (Exception ignored) {}'''
content = content.replace(search_parse_from, replace_parse_from)

search_parse_to = 'try { cal.setTime(new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(tempTo[0])); } catch (Exception ignored) {}'
replace_parse_to = '''try {
                    java.util.Date parsed = new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(tempTo[0]);
                    if (parsed != null) cal.setTime(parsed);
                } catch (Exception ignored) {}'''
content = content.replace(search_parse_to, replace_parse_to)

# 16, 17) showUndoSnackbar SameParameterValue
search_undo = 'private void showUndoSnackbar(String message, final Runnable onUndo, final Runnable onCommit) {'
replace_undo = '@SuppressWarnings("SameParameterValue")\n    private void showUndoSnackbar(String message, final Runnable onUndo, final Runnable onCommit) {'
content = content.replace(search_undo, replace_undo)

# 18, 19, 20) .size() > 0 to !isEmpty() and pdfDir null check
content = content.replace('account.getRecords().size() > 0', '!account.getRecords().isEmpty()')

search_pdf_dir = '''            java.io.File pdfDir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
            if (!pdfDir.exists()) pdfDir.mkdirs();'''
replace_pdf_dir = '''            java.io.File pdfDir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
            if (pdfDir == null) return;
            if (!pdfDir.exists()) pdfDir.mkdirs();'''
content = content.replace(search_pdf_dir, replace_pdf_dir)

# Just checking to make sure line 2975 doesn't crash too
search_pdf_dir_2 = '''            java.io.File pdfDir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
            if (!pdfDir.exists()) pdfDir.mkdirs();
            java.io.File file = new java.io.File(pdfDir,'''
replace_pdf_dir_2 = '''            java.io.File pdfDir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
            if (pdfDir == null) return;
            if (!pdfDir.exists()) pdfDir.mkdirs();
            java.io.File file = new java.io.File(pdfDir,'''
content = content.replace(search_pdf_dir_2, replace_pdf_dir_2)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Warnings 5 fixed")
